package net.umpay.mailbill.util.mail;

import net.umpay.mailbill.util.constants.MailBillTypeConstants;


/**
 * 简单工厂
 * 
 * @author admin
 *
 */
public class BankBillTypesFactory {

	/**
	 * 工厂模式
	 * 		--简单工厂
	 * @param subject --邮件主题
	 * @return	--返回一个银行的实例 ， 若不在这些范围内，则返回-1
	 */
	public int getMailBillTypes(String subject){
		
		if (subject.contains("招商银行") || subject.contains("商务") || subject.contains("消费明细") || subject.contains("每日信用管家") ){
			return MailBillTypeConstants.BANK_TYPE_CMB;
		}
		
		if (subject.contains("建设银行")){
			return MailBillTypeConstants.BANK_TYPE_CCB;
		}
		
		if (subject.contains("华夏信用卡")){
			return MailBillTypeConstants.BANK_TYPE_HUAXIA;
		}
		
		if (subject.contains("平安信用卡")){
			return MailBillTypeConstants.BANK_TYPE_PINGAN;
		}
		
		if (subject.contains("光大银行")){
			return MailBillTypeConstants.BANK_TYPE_CEB;
		}
		
		if (subject.contains("中国银行")){
			return MailBillTypeConstants.BANK_TYPE_CHINA;
		}
		
		if (subject.contains("中信银行")){
			return MailBillTypeConstants.BANK_TYPE_CITIC;
		}
		
		if (subject.contains("交通银行")){
			return MailBillTypeConstants.BANK_TYPE_BOCOMM;
		}
		
		if (subject.contains("民生信用卡")){
			return MailBillTypeConstants.BANK_TYPE_CMSB;
		}
		
		if (subject.contains("广发卡")){
			return MailBillTypeConstants.BANK_TYPE_CGB;
		}
		
		if (subject.contains("兴业银行")){
			return MailBillTypeConstants.BANK_TYPE_CIB;
		}
		
		if (subject.contains("工商银行")){
			return MailBillTypeConstants.BANK_TYPE_ICBC;
		}
		
		if (subject.contains("浦发银行")){
			return MailBillTypeConstants.BANK_TYPE_SPD;
		}
		
		if (subject.contains("农业银行")){
			return MailBillTypeConstants.BANK_TYPE_AGRICULTURAL;
		}
		
		if (subject.contains("北京银行")){
			return MailBillTypeConstants.BANK_TYPE_BEIJING;
		}
		
		return -1;
	}
	
}
