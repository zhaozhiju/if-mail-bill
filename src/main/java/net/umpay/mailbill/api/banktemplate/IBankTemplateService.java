package net.umpay.mailbill.api.banktemplate;

import java.util.List;

import net.umpay.mailbill.util.exception.MailBillException;

/**
 * 银行解析接口
 * @author zhaozj
 */
public interface IBankTemplateService {

	/**
	 * 解析银行邮件账单
	 * @param parse	html过滤掉所有的标签后的String
	 * @param senderUrl		发件人地址
	 * @param oldHTML		原始的拼接地址信息
	 * @param newHtml		截取后拼接的地址信息
	 * @param id			更新时的id
	 * @param accountId		用户账号
	 * @param scVersion		服务端数据版本号
	 * @return	账单标示
	 * Created on 2014/05/13
	 * @throws MailBillException
	 */
	public String bankTemplateParse(List<String> parse, String senderUrl, String[] oldHTML, 
			String[] newHtml, Long[] id, Long accountId, Long scVersion) throws MailBillException;
	
	/**
	 * 获取银行类型
	 * {@link net.umpay.mailbill.util.constants.MailBillTypeConstants}
	 * @return int
	 */
	public int getBankType();
}
