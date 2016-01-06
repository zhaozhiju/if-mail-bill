package net.umpay.mailbill.service.impl.banktemplate;

import java.util.ArrayList;
import java.util.List;

import net.umpay.mailbill.api.banktemplate.IBankTemplateService;
import net.umpay.mailbill.api.banktemplate.IReadMail;
import net.umpay.mailbill.hql.model.BillCycleInfoEntity;
import net.umpay.mailbill.hql.model.NotBillCycleInfoEntity;
import net.umpay.mailbill.service.impl.resolve.BillCycleInfoServiceImpl;
import net.umpay.mailbill.service.impl.resolve.NotBillCycleInfoServiceImpl;
import net.umpay.mailbill.util.constants.Constants;
import net.umpay.mailbill.util.exception.ErrorCodeContants;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;
import net.umpay.mailbill.util.mail.BankBillTypesFactory;
import net.umpay.mailbill.util.mail.MailDownload;
import net.umpay.mailbill.util.string.ReadProperty;
import net.umpay.mailbill.util.string.TextExtract;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 根据给出的HTML地址进行账单的解析的实现类
 * 其中包括未解析的账单、解析出错的账单、解析内容错误需要重新解析的账单
 * 
 * @author admin
 *
 */
@Service
public class ReadMailHtml implements IReadMail{
	
	private static Logger log = LoggerFactory.getLogger(ReadMailHtml.class);
	@Autowired
	private BillCycleInfoServiceImpl billCycleInfoServiceImpl;
	@Autowired
	private NotBillCycleInfoServiceImpl cycleInfoServiceImpl;
	
	private List<IBankTemplateService> bankTemplateServicesList; // IBankTemplateService -- 接口
	
	public List<IBankTemplateService> getBankTemplateServicesList() {
		return bankTemplateServicesList;
	}
	public void setBankTemplateServicesList(
			List<IBankTemplateService> bankTemplateServicesList) {
		this.bankTemplateServicesList = bankTemplateServicesList;
	}
	@Override
	public boolean getReadMailAccount(String newHtml, String card, Long accountId, Long scVersion) throws MailBillException {
		//DFS下载解析的方法:
		BankBillTypesFactory bankBillTypesFactory = new BankBillTypesFactory();//简单工厂  
		String[] splitCard = card.split(";");
		List<BillCycleInfoEntity> entities = new ArrayList<BillCycleInfoEntity>();
		List<NotBillCycleInfoEntity> notEntities = new ArrayList<NotBillCycleInfoEntity>();
		Long[] cardId = new Long[splitCard.length];
		int n = 0;
		//获取DFS上文件的地址
		if (!StringUtils.isBlank(card)){
			for(String str : splitCard){
				BillCycleInfoEntity entity = billCycleInfoServiceImpl.getDFSURlName(newHtml, str);
				NotBillCycleInfoEntity dfsuRlName = cycleInfoServiceImpl.getDFSURlName(newHtml, str);
				if (entity != null){
					Long id = entity.getId();
					cardId[n] = id;
					entities.add(entity);
					n++;
				}else if(dfsuRlName != null){
					Long id = dfsuRlName.getId();
					cardId[n] = id;
					notEntities.add(dfsuRlName);
					n++;
				}
			}
		}
		if(StringUtils.isBlank(card)){
			log.info("the card number is empty");
		}
		// 如果没有查询到相关内容则重新下载解析
		if (entities.size() == 0 && notEntities.size() == 0){
			entities = billCycleInfoServiceImpl.getDFSURlByNew(newHtml);
			notEntities = cycleInfoServiceImpl.getDFSURlByNew(newHtml);
			if(entities.size() == 0 && notEntities.size() == 0){
				log.info("this data does not exist, please download again.");
				return false;
			}
		}
		
		for (BillCycleInfoEntity billCycleInfoEntity : entities){
			// 找出组名，路径名
			String dfsuRlName = billCycleInfoEntity.getNewHtmlDFS();
			dfsuRlName = dfsuRlName.substring(Constants.DOMAIN_NAME_PATH.length(), dfsuRlName.length());
			String[] splitDfs = dfsuRlName.split("/");
			String remoteFilename = dfsuRlName.substring(splitDfs[0].length()+1, dfsuRlName.length());
			// 读取文件内容
			StringBuffer read = MailDownload.getRemoteFile(splitDfs[0], remoteFilename);
			// 获取主题
			String subject = null;
			newHtml = newHtml.substring(Constants.DOMAIN_NAME_PATH.length()+1, newHtml.length());
			String[] split = newHtml.split("/"); // 分割地址路径
			for (String s : split){
				if (s.contains("_")){
					String[] split2 = s.split("_");
					subject = split2[0]; // 获取主题
					break;
				}
			}
			// 解析账单
			Document document = Jsoup.parse(read.toString()); // 格式化文本内容
			List<String> parse = new TextExtract().parse(document.toString()); // 从文本中截取内容
			// 获取需要保存的路径与名称
			String[] oldHTML = new String[2];
			String[] newHTML = new String[2];
			oldHTML[0] = billCycleInfoEntity.getOldHtmlUrl();
			oldHTML[1] = billCycleInfoEntity.getOldHtmlDFS();
			newHTML[0] = billCycleInfoEntity.getNewHtmlUrl();
			newHTML[1] = billCycleInfoEntity.getNewHtmlDFS();
			cardId[0] = billCycleInfoEntity.getId();
			String senderUrl = ReadProperty.getEmailUrl(newHTML[0],2);
			boolean subflag = StringUtils.isBlank(subject);
			int bankType = -1;
			if(!subflag){
				bankType = bankBillTypesFactory.getMailBillTypes(subject); // 获取银行实例
			}
			if (bankType != -1){
				 for (IBankTemplateService service : bankTemplateServicesList) {
					 try {
						  if (bankType == service.getBankType()) { // 首先预先知道银行类型
							  String bankTemplateParse;
							  bankTemplateParse = service.bankTemplateParse(parse, senderUrl, oldHTML, newHTML, cardId, accountId, scVersion);
							  log.info("bill:{} parse is complete", bankTemplateParse);
						  }
					 } catch (Exception e) {
						 if(StringUtils.isBlank(e.getMessage())){
							 throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.NULL_CODE, e.getMessage(), log);
						 }else if (e.getMessage().contains("Index:") || e.getMessage().contains("String index out of range:")){
							 throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.ARRAY_INDEXOUTOF_BOUNDS_EXCEPTION_CODE, e.getMessage(), log);
						 }else if (e.getMessage().contains("转换日期发生错误")){
							 throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.DATA_TYPE_CONVERSION_CODE, e.getMessage(), log);
						 }else if(e.getMessage().contains("Could not execute JDBC batch update")){
							 throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.ANALYSIS_DATA_EXCEPTION_CODE, e.getMessage(), log);
						 }else{
							 throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.ANALYSIS_DATA_EXCEPTION_CODE, e.getMessage(), log);
						 }
					 }
				 }
			}else {
				log.info("no bank template for this type of subject:{}", subject);
				return false;
			}
		}
		for (NotBillCycleInfoEntity notBillCycleInfoEntity : notEntities){
			// 找出组名，路径名
			String dfsuRlName = notBillCycleInfoEntity.getNewHtmlDFS();
			dfsuRlName = dfsuRlName.substring(Constants.DOMAIN_NAME_PATH.length(), dfsuRlName.length());
			String[] splitDfs = dfsuRlName.split("/");
			String remoteFilename = dfsuRlName.substring(splitDfs[0].length()+1, dfsuRlName.length());
			// 读取文件内容
			StringBuffer read = MailDownload.getRemoteFile(splitDfs[0], remoteFilename);
			// 获取主题
			String subject = null;
			newHtml = newHtml.substring(Constants.DOMAIN_NAME_PATH.length()+1, newHtml.length());
			String[] split = newHtml.split("/"); // 分割地址路径
			for (String s : split){
				if (s.contains("_")){
					String[] split2 = s.split("_");
					subject = split2[0]; // 获取主题
					break;
				}
			}
			// 解析账单
			Document document = Jsoup.parse(read.toString()); // 格式化文本内容
			List<String> parse = new TextExtract().parse(document.toString()); // 从文本中截取内容
			// 获取需要保存的路径与名称
			String[] oldHTML = new String[2];
			String[] newHTML = new String[2];
			oldHTML[0] = notBillCycleInfoEntity.getOldHtmlUrl();
			oldHTML[1] = notBillCycleInfoEntity.getOldHtmlDFS();
			newHTML[0] = notBillCycleInfoEntity.getNewHtmlUrl();
			newHTML[1] = notBillCycleInfoEntity.getNewHtmlDFS();
			cardId[0] = notBillCycleInfoEntity.getId();
			String senderUrl = ReadProperty.getEmailUrl(newHTML[0],2);
			boolean subflag = StringUtils.isBlank(subject);
			int bankType = -1;
			if(!subflag){
				bankType = bankBillTypesFactory.getMailBillTypes(subject); // 获取银行实例
			}
			if (bankType != -1){
				for (IBankTemplateService service : bankTemplateServicesList) {
					try {
						if (bankType == service.getBankType()) { // spaceKind 首先预先知道银行类型
							String bankTemplateParse;
							bankTemplateParse = service.bankTemplateParse(parse, senderUrl, oldHTML, newHTML, cardId, accountId, scVersion);
							log.info("bill:{} parse is complete", bankTemplateParse);
						}
					} catch (Exception e) {
						if(StringUtils.isBlank(e.getMessage())){
							throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.NULL_CODE, e.getMessage(), log);
						}else if (e.getMessage().contains("Index:") || e.getMessage().contains("String index out of range:")){
							throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.ARRAY_INDEXOUTOF_BOUNDS_EXCEPTION_CODE, e.getMessage(), log);
						}else if (e.getMessage().contains("转换日期发生错误")){
							throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.DATA_TYPE_CONVERSION_CODE, e.getMessage(), log);
						}else if(e.getMessage().contains("Could not execute JDBC batch update")){
							throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.ANALYSIS_DATA_EXCEPTION_CODE, e.getMessage(), log);
						}else{
							throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.ANALYSIS_DATA_EXCEPTION_CODE, e.getMessage(), log);
						}
					}
				}
			}else {
				log.info("no bank template for this type of subject:{}", subject);
				return false;
			}
		}
		return true;
}
}
