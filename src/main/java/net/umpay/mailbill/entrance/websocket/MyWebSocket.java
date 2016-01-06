package net.umpay.mailbill.entrance.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.umpay.mailbill.util.exception.ErrorCodeContants;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;
import net.umpay.mailbill.util.security.RandomUtil;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * web-socket 服务与客户端建立双向通道传输信息
 * 
 * @version 1.0.0
 */
@Service
public class MyWebSocket implements WebSocket.OnTextMessage, WebSocket.OnBinaryMessage {
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(MyWebSocket.class);

	//连接的建立
	private Connection connection;
	//作为标示某个唯一连接的key
	protected String key;
	// 缓存现有的长连接, key与Connection相对应
	public static Map<String, Connection> connectionMap = new HashMap<String, Connection>();
	// 缓存验证码
	public static Map<String, String> vcodeMap = new HashMap<String, String>();

	/**
	 * client端返回的数据信息
	 * 
	 * @param message
	 */
	@Override
	public void onMessage(String message) {
		log.info("onmessage:{}", new Object[]{message});
		if (StringUtils.isNotBlank(message) && message.contains("RD1`") || StringUtils.isNotBlank(message) && message.contains("RH1`") || StringUtils.isNotBlank(message) && message.contains("RJ1`") || StringUtils.isNotBlank(message) && message.contains("RI1`")){
			vcodeMap.put(key, message);
		}
	}
	
	/**
	 * 创建长连接
	 * 
	 * @param connection
	 */
	@Override
	public void onOpen(Connection connection) {
		log.info("onopen connection:{}", new Object[]{connection});
		this.connection = connection;
		key = RandomUtil.getUniqueBigDecimal(20).toString();
		connectionMap.put(key, connection);
		vcodeMap.put(key, null);
		this.sendClientKey(key);
	}

	/**
	 * 关闭长连接
	 */
	@Override
	public void onClose(int closeCode, String message) {
		log.info("onclose:{} \tmessage:{}", new Object[]{closeCode, message});
		if (connectionMap.containsKey(key)) {
			connectionMap.remove(key);
		} else {
			// do nothing
			//MailBillExceptionUtil.getWithLog(ErrorCodeContants.CONNECTION_NOT_KEY_CODE, ErrorCodeContants.CONNECTION_NOT_KEY.getMsg(), log);
		}
	}

	/**
	 * 根据key获取相应的长连接，发送数据信息
	 * 
	 * @param msg	发送的数据信息
	 * @param key	长连接对应的key
	 */
	
	public void send(String msg, String key) {
		try {
			if (null != connectionMap && !connectionMap.isEmpty()
					&& StringUtils.isNotBlank(key)) {
				log.info("push msg:{} to client", new Object[]{msg});
				if (connectionMap.containsKey(key)) {
					connectionMap.get(key).sendMessage(msg);
				} else {
					MailBillExceptionUtil.getWithLog(ErrorCodeContants.CONNECTION_NOT_KEY_CODE, ErrorCodeContants.CONNECTION_NOT_KEY.getMsg(), log);
				}
			}
		} catch (IOException e) {
			MailBillExceptionUtil.getWithLog(ErrorCodeContants.WEBSOCKET_IO_EXCEPTION_CODE, ErrorCodeContants.WEBSOCKET_IO_EXCEPTION.getMsg(), log);
		}
	}

	/**
	 * 服务端自生成key发往客户端,以此key维持相应的Connection
	 * 
	 * @param key	服务端自生成的key
	 */
	
	public void sendClientKey(String key) {
		try {
			log.info("push key:{} to client", new Object[]{key});
			connection.sendMessage("L1`" + key);
		} catch (IOException e) {
			MailBillExceptionUtil.getWithLog(ErrorCodeContants.WEBSOCKET_IO_EXCEPTION_CODE, ErrorCodeContants.WEBSOCKET_IO_EXCEPTION.getMsg(), log);
		}
	}

	@Override
	public void onMessage(byte[] arg0, int arg1, int arg2) {

	}

}
