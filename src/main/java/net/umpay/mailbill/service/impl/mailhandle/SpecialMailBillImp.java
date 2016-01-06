package net.umpay.mailbill.service.impl.mailhandle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.AndTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import net.umpay.mailbill.api.mailhandle.ISpecialMail;
import net.umpay.mailbill.util.constants.Constants;
import net.umpay.mailbill.util.exception.ErrorCodeContants;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;
import net.umpay.mailbill.util.string.ReadProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 过滤特殊关键字的的邮件
 * 
 */
@Service
public class SpecialMailBillImp implements ISpecialMail {
	private static Logger log = LoggerFactory.getLogger(SpecialMailBillImp.class);
	/**
	 * 设置过滤条件
	 * @param folder
	 * @return	Message[]
	 * @throws MailBillException 
	 */
	
	@Override
	public Message[] searchSpecialMailBill(Folder folder) throws MailBillException {
		//log.info("method:{} \tservice:{}", new Object[]{"searchSpecialMailBill", this.getClass()});
		SearchTerm orTerm = null ;
		SearchTerm orTermAll = null ;
		SearchTerm andTerm = null;
		List<SearchTerm> listSearchTerm = new ArrayList<SearchTerm>();
		//获取key值的列表
		Map<String, String> readPropKey = ReadProperty.readProperties(Constants.PROPERTY_READ_PATH);//key list
		//是否key对应的值为空
		int size2 = listSearchTerm.size();
		if (size2 == 0){
			return null;
		}
		for (String str : readPropKey.keySet()){
			if (str.equals("queryKey") || str.equals("bankMailAddress")){
				continue;
			}
			String value = readPropKey.get(str);//根据key拿到value
			//获取value非空的key 
			if (value != null && !value.equals("")){
				//匹配指定的13家银行
				int size = readPropKey.size();
				for (int j = 0; j < size; j++) {
					//支持某一家银行的特殊邮件搜索， 找到非空值的key过滤出来处理，  多家的支持以后我再想想
					if (str.equalsIgnoreCase("icbc")){
						String[] split = value.split(",");
						int length = split.length;
						for (int k = 0; k < length; k++) {
							if (k == 0){
								andTerm = new AndTerm( 
										new FromStringTerm("webmaster@icbc.com.cn"), 
										new SubjectTerm(split[k]));
								continue;
							}
							orTerm = new OrTerm(andTerm , new AndTerm(
									new FromStringTerm("webmaster@icbc.com.cn"), new SubjectTerm(split[k])));
						}
						listSearchTerm.add(orTerm);
					}
					if (str.equalsIgnoreCase("Huaxia")){
						String[] split = value.split(",");
						int length = split.length;
						for (int k = 0; k < length; k++) {
							if (k == 0){
								andTerm = new AndTerm( 
										new FromStringTerm("admin@creditcard.hxb.com.cn"), 
										new SubjectTerm(split[k]));
								continue;
							}
							orTerm = new OrTerm(andTerm , new AndTerm(
									new FromStringTerm("admin@creditcard.hxb.com.cn"), new SubjectTerm(split[k])));
						}
						listSearchTerm.add(orTerm);
					}
					if (str.equalsIgnoreCase("China")){
						String[] split = value.split(",");
						int length = split.length;
						for (int k = 0; k < length; k++) {
							if (k == 0){
								andTerm = new AndTerm( 
										new FromStringTerm("PersonalService@bank-of-china.com"), 
										new SubjectTerm(split[k]));
								continue;
							}
							orTerm = new OrTerm(andTerm , new AndTerm(
									new FromStringTerm("PersonalService@bank-of-china.com"), new SubjectTerm(split[k])));
						}
						listSearchTerm.add(orTerm);
					}
					if (str.equalsIgnoreCase("Citic")){
						String[] split = value.split(",");
						int length = split.length;
						for (int k = 0; k < length; k++) {
							if (k == 0){
								andTerm = new AndTerm( 
										new FromStringTerm("citiccard@citiccard.com"), 
										new SubjectTerm(split[k]));
								continue;
							}
							orTerm = new OrTerm(andTerm , new AndTerm(
									new FromStringTerm("citiccard@citiccard.com"), new SubjectTerm(split[k])));
						}
						listSearchTerm.add(orTerm);
					}
					if (str.equalsIgnoreCase("BoConmm")){
						String[] split = value.split(",");
						int length = split.length;
						for (int k = 0; k < length; k++) {
							if (k == 0){
								andTerm = new AndTerm( 
										new FromStringTerm("PCCC@bocomcc.com"), 
										new SubjectTerm(split[k]));
								continue;
							}
							orTerm = new OrTerm(andTerm , new AndTerm(
									new FromStringTerm("PCCC@bocomcc.com"), new SubjectTerm(split[k])));
						}
						listSearchTerm.add(orTerm);
					}
					if (str.equalsIgnoreCase("CMSB")){
						String[] split = value.split(",");
						int length = split.length;
						for (int k = 0; k < length; k++) {
							if (k == 0){
								andTerm = new AndTerm( 
										new FromStringTerm("cardservice@cmbc.com.cn"), 
										new SubjectTerm(split[k]));
								continue;
							}
							orTerm = new OrTerm(andTerm , new AndTerm(
									new FromStringTerm("cardservice@cmbc.com.cn"), new SubjectTerm(split[k])));
						}
						listSearchTerm.add(orTerm);
					}
					if (str.equalsIgnoreCase("Ceb")){
						String[] split = value.split(",");
						int length = split.length;
						for (int k = 0; k < length; k++) {
							if (k == 0){
								andTerm = new AndTerm( 
										new FromStringTerm("cebbank@cardcenter.cebbank.com"), 
										new SubjectTerm(split[k]));
								continue;
							}
							orTerm = new OrTerm(andTerm , new AndTerm(
									new FromStringTerm("cebbank@cardcenter.cebbank.com"), new SubjectTerm(split[k])));
						}
						listSearchTerm.add(orTerm);
					}
					if (str.equalsIgnoreCase("Cmb")){
						String[] split = value.split(",");
						int length = split.length;
						for (int k = 0; k < length; k++) {
							if (k == 0){
								andTerm = new AndTerm( 
										new FromStringTerm("ccsvc@message.cmbchina.com"), 
										new SubjectTerm(split[k]));
								continue;
							}
							orTerm = new OrTerm(andTerm , new AndTerm(
									new FromStringTerm("ccsvc@message.cmbchina.com"), new SubjectTerm(split[k])));
						}
						listSearchTerm.add(orTerm);
					}
					if (str.equalsIgnoreCase("Ccb")){
						String[] split = value.split(",");
						int length = split.length;
						for (int k = 0; k < length; k++) {
							if (k == 0){
								andTerm = new AndTerm( 
										new FromStringTerm("service@vip.ccb.com"), 
										new SubjectTerm(split[k]));
								continue;
							}
							orTerm = new OrTerm(andTerm , new AndTerm(
									new FromStringTerm("service@vip.ccb.com"), new SubjectTerm(split[k])));
						}
						listSearchTerm.add(orTerm);
					}
					if (str.equalsIgnoreCase("Spd")){
						String[] split = value.split(",");
						int length = split.length;
						for (int k = 0; k < length; k++) {
							if (k == 0){
								andTerm = new AndTerm( 
										new FromStringTerm("estmtservice@eb.spdbccc.com.cn"), 
										new SubjectTerm(split[k]));
								continue;
							}
							orTerm = new OrTerm(andTerm , new AndTerm(
									new FromStringTerm("estmtservice@eb.spdbccc.com.cn"), new SubjectTerm(split[k])));
						}
						listSearchTerm.add(orTerm);
					}
					if (str.equalsIgnoreCase("Cib")){
						String[] split = value.split(",");
						int length = split.length;
						for (int k = 0; k < length; k++) {
							if (k == 0){
								andTerm = new AndTerm( 
										new FromStringTerm("creditcard@message.cib.com.cn"), 
										new SubjectTerm(split[k]));
								continue;
							}
							orTerm = new OrTerm(andTerm , new AndTerm(
									new FromStringTerm("creditcard@message.cib.com.cn"), new SubjectTerm(split[k])));
						}
						listSearchTerm.add(orTerm);
					}
					if (str.equalsIgnoreCase("Cgb")){
						String[] split = value.split(",");
						int length = split.length;
						for (int k = 0; k < length; k++) {
							if (k == 0){
								andTerm = new AndTerm( 
										new FromStringTerm("creditcard@cgbchina.com.cn"), 
										new SubjectTerm(split[k]));
								continue;
							}
							orTerm = new OrTerm(andTerm , new AndTerm(
									new FromStringTerm("creditcard@cgbchina.com.cn"), new SubjectTerm(split[k])));
						}
						listSearchTerm.add(orTerm);
					}
					if (str.equalsIgnoreCase("PingAn")){
						String[] split = value.split(",");
						int length = split.length;
						for (int k = 0; k < length; k++) {
							if (k == 0){
								andTerm = new AndTerm( 
										new FromStringTerm("creditcard@service.pingan.com"), 
										new SubjectTerm(split[k]));
								continue;
							}
							orTerm = new OrTerm(andTerm , new AndTerm(
									new FromStringTerm("creditcard@service.pingan.com"), new SubjectTerm(split[k])));
						}
						listSearchTerm.add(orTerm);
					}
				}
				
				try {
					for (int i = 0; i < size2; i++) {
						if (size2 == 1){
		    				return folder.search(listSearchTerm.get(i));
		    			}
		    			if (size2 <= 2){
		    				orTermAll = new OrTerm( listSearchTerm.get(i), listSearchTerm.get(i+1));
							return folder.search(orTermAll);
		    			}
		    			if (i < 1){
		    				orTermAll = new OrTerm( listSearchTerm.get(i), listSearchTerm.get(i+1));
		    				continue;
		    			}
		    			orTermAll = new OrTerm(listSearchTerm.get(i), orTermAll);
		    		}
					return folder.search(orTermAll);
				} catch (MessagingException e) {
					throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.SEARCH_FAILED_CODE , e.getMessage(), log);
				}
			}
		}
		return null;
	}
}
