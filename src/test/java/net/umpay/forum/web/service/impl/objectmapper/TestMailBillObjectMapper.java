package net.umpay.forum.web.service.impl.objectmapper;

import java.util.HashMap;
import java.util.Map;

import net.umpay.forum.web.service.impl.base.Context;
import net.umpay.mailbill.api.objectmapper.IMailBillObjectMapper;
import net.umpay.mailbill.service.impl.objectmapper.MailBillObjectMapper;
import net.umpay.mailbill.util.exception.MailBillException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 对象转json串
 */
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class TestMailBillObjectMapper extends Context {

	/**
	 * logger for this class
	 */
	private static Logger log = LoggerFactory
			.getLogger(TestMailBillObjectMapper.class);

	@Autowired
	private IMailBillObjectMapper mailBillObjectMapper;

	@Test
	public void testWriteValueAsString() throws MailBillException {
		mailBillObjectMapper = (MailBillObjectMapper) this.apc
				.getBean("mailBillObjectMapper");
		Map<String, String> mapInfo = new HashMap<String, String>();
		mapInfo.put("key1", "value1");
		mapInfo.put("key2", "value2");
		mapInfo.put("key3", "value3");
		String jsonStr = mailBillObjectMapper.writeValueAsString(mapInfo);
		log.info("jsonStr:{}", new Object[] { jsonStr });
	}
}
