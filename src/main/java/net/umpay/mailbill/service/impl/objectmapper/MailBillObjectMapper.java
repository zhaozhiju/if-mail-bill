package net.umpay.mailbill.service.impl.objectmapper;

import net.umpay.mailbill.api.objectmapper.IMailBillObjectMapper;
import net.umpay.mailbill.util.exception.ErrorCodeContants;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 对象转换为json
 * 
 * @version 1.0.0
 */
@Service
public class MailBillObjectMapper implements IMailBillObjectMapper {

	/**
	 * logger for this class
	 */
	private static Logger log = LoggerFactory.getLogger(MailBillObjectMapper.class);

	@Autowired
	private ObjectMapper objectMapperJson;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.umpay.forum.web.api.objectmapper.IForumObjectMapper#writeValueAsString(java.lang.Object)
	 */
	@Override
	public String writeValueAsString(Object object) throws MailBillException {
		String returnJson = null;
		try {
			returnJson = objectMapperJson.writeValueAsString(object);
		} catch (Exception e) {
			throw MailBillExceptionUtil.getWithLog(e,
					ErrorCodeContants.OBJECT_TO_JSON_EXCEPTION_CODE,
					ErrorCodeContants.OBJECT_TO_JSON_EXCEPTION.getMsg(), log);
		}

		return returnJson;
	}

}
