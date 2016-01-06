package net.umpay.mailbill.util.date;

import java.text.SimpleDateFormat;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

public class DateAsStringObjectMapper extends ObjectMapper {
	@SuppressWarnings("deprecation")
	public DateAsStringObjectMapper() {
		super();
		this.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
		this.getSerializationConfig().setDateFormat(new SimpleDateFormat(("yyyy-MM-dd'T'HH:mm:ss.SZ")));//ISO 8601
	}
}

