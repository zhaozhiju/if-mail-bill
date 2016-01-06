package net.umpay.mailbill.service.impl.mailhandle;

import java.util.List;

import net.umpay.mailbill.api.mailhandle.ImailJudge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** 
 * 判断过滤到的邮件是否为账单邮件
 *
 */
@Service
public class mailJudgeImpl implements ImailJudge {
	
	private static Logger log = LoggerFactory.getLogger(mailJudgeImpl.class);
	/**
	 *  判断过滤到的邮件是否为账单邮件
	 *  （内容中包含“信用额度、最低还款、还款日、交易日期、卡号末4位”任意一个关键字即认为是账单邮件）
	 *  @param parse 初步过滤后的邮件内容
	 *  @param subject 邮件主题
	 *  @return boolean
	 */
	@Override
	public boolean Judge(List<String> parse, String subject) {
		log.info("action:{} \tparse:{} \tsubject:{}", new Object[]{"Judge(List<String> , String )", new Object[]{parse, subject}});
		int count_parse = 0;
		int size = parse.size();
		for(int j = 0 ; j < size ; j ++){
			count_parse++;
			if(parse.get(j).contains("最低还款") || parse.get(j).contains("还款日") || parse.get(j).contains("交易日期")|| parse.get(j).contains("可用额度") || parse.get(j).contains("卡号末4位")){
				//log.info("method:{} \tservice:{} \tbill_subject:{}", new Object[]{"Judge", this.getClass(), subject});
				return true;
			}
		}
		
		if(count_parse == size){
			//log.info("method:{} \t service:{} \tnot_bill_subject:{}", new Object[]{"Judge", this.getClass(), subject});
		}
		return false;
	}
	
	
}