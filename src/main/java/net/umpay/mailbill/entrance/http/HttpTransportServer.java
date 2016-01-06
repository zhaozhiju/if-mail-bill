package net.umpay.mailbill.entrance.http;

import net.umpay.mailbill.entrance.websocket.MyWebSocketHandler;
import net.umpay.mailbill.util.constants.Constants;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpTransportServer extends AbstractLifeCycle implements
		TransportReceiver {
	/**
	 * Logger for this class
	 */
	private static Logger logger = LoggerFactory.getLogger(HttpTransportServer.class);

	private String name = "defaultHttpServer";
	private Server server;
	private int port = Integer.valueOf(Constants.TRANSPORT_HTTP_PORT);
	private String pro_name = Constants.TRANSPORT_HTTP_PROJECT_NAME;

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPort(int port) {
		if (isRunning()) {
			logger.error("can not set port after server initialized");
			return;
		}
		this.port = port;
	}

	public void doStart() throws Exception {
		this.setName("defaultHttpServer");
		server = new Server(port);
		final String webPath = "src/main/webapp";
		//项目
		WebAppContext appContext = new WebAppContext(webPath, "/" + pro_name);
		//websocket
		MyWebSocketHandler myWebSocketHandler = new MyWebSocketHandler();
		myWebSocketHandler.setHandler(appContext);
		server.setHandler(myWebSocketHandler);
		
		QueuedThreadPool pool = new QueuedThreadPool();
		pool.setMinThreads(Integer.valueOf(Constants.TRANSPORT_HTTP_THREADS_MIN));
		pool.setMaxThreads(Integer.valueOf(Constants.TRANSPORT_HTTP_THREADS_MAX));
		pool.setMaxQueued(Integer.valueOf(Constants.TRANSPORT_HTTP_QUEUE_LIMIT));
		pool.setName("http-server");
		server.setThreadPool(pool);
		
		server.start();
		server.join();
		logger.info("The jetty service is started.");
	}

	public void doStop() throws Exception {
		server.stop();
		logger.info("The jetty service has stopped.");
	}
}
