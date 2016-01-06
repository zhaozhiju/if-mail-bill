package net.umpay.mailbill.entrance;

import net.umpay.mailbill.entrance.http.HttpTransportServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * 服务开启
 * 
 * @version 1.0.0
 */
public class StartServer {
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(StartServer.class);

	public static void main(String[] args) throws Exception {
		//PropertyConfigurator.configure("./conf/log4j.properties");
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(lc);
            lc.reset();
            log.debug("user.dir:"+System.getProperty("user.dir"));
            configurator.doConfigure(System.getProperty("user.dir") + "/conf/logback.xml");
        } catch (JoranException e) {
        	log.error(e.getMessage());
        }
		log.info("action:{}", new Object[] { "--- service start ---" });
		HttpTransportServer http = new HttpTransportServer();
		http.doStart();
	}
}
