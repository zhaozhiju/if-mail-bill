package net.umpay.mailbill.api.banktemplate;

import net.umpay.mailbill.util.exception.MailBillException;

public interface IReadMail {

	/**
	 * 根据给出的HTML地址进行解析邮件账单的内容
	 * 
	 * @param htmlhref		给出的原邮件地址例如：L/o/kkkkk@183.com/kkkk_dss_sss.html
	 * 						用户email地址第一个字符/用户email地址第二个字符/用户email地址/发件人/*.html
	 * @param card			卡号末四位
	 * @param accountId		用户账号
	 * @param scVersion		服务端数据版本号
	 * @return				成功或者失败
	 * @throws MailBillException
	 */
	public boolean getReadMailAccount(String htmlhref, String card, Long accountId, Long scVersion) throws MailBillException;
}
