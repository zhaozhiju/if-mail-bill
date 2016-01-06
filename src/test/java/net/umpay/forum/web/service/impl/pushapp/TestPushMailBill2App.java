package net.umpay.forum.web.service.impl.pushapp;

import net.umpay.forum.web.service.impl.base.Context;
import net.umpay.mailbill.api.pushapp.IPushMailBill2App;
import net.umpay.mailbill.service.impl.pushapp.PushMailBill2App;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TestPushMailBill2App extends Context {

	/**
	 * logger for this class
	 */
	private static Logger log = LoggerFactory
			.getLogger(TestPushMailBill2App.class);

	@Autowired
	private IPushMailBill2App pushMailBill2App;

	@Test
	public void testPushMailBillEntrance() {
		pushMailBill2App = (PushMailBill2App) this.apc
				.getBean("pushMailBill2App");
		/*try {
			String mailUrl = "";
			Long accountId = 123456789l;
			String phoneId = "";
			String csVersion = "";
			String socketKey = "";

			pushMailBill2App.pushMailBillEntrance(mailUrl, accountId, phoneId,
					csVersion, socketKey);
		} catch (MailBillException e) {
			log.error(e.getMessage());
		}*/

		log.info("---------推送成功---------");
	}
}
