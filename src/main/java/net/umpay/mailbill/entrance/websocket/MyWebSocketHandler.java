package net.umpay.mailbill.entrance.websocket;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyWebSocketHandler extends WebSocketHandler
{
	private static Logger log = LoggerFactory.getLogger(MyWebSocketHandler.class);
	
	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol)
	{
		log.info("websocket :　{} doWebSocketConnect : protocol={}", new Object[]{this.getClass(), protocol});
		log.info("websocket :　{} doWebSocketConnect : remote={}", new Object[]{this.getClass(), request.getRemoteAddr()});
		return new MyWebSocket();
	}
}
