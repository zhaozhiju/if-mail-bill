package net.umpay.mailbill.api.mailhandle;

import javax.mail.Folder;
import javax.mail.Message;

import net.umpay.mailbill.util.exception.MailBillException;

public interface ISpecialMail {

	/**
	 * 过滤特殊关键字的的邮件
	 * 
	 * @param folder 某文件夹下
	 * @return   返回含有特殊关键字的邮件列表
	 * @throws MailBillException 
	 */
	public Message[] searchSpecialMailBill(Folder folder) throws MailBillException;
	
}
