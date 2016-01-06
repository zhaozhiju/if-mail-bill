package net.umpay.mailbill.service.impl.mailhandle;

import java.util.List;

import net.umpay.mailbill.api.mailhandle.ISecondDownload;
import net.umpay.mailbill.util.constants.Constants;
import net.umpay.mailbill.util.mail.MailDownload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 将只经过第一次下载的HTML，进行第二次下载
 * 
 * @author admin
 *
 */
@Deprecated
public class SecondDownloadAgainImpl implements ISecondDownload {
	private static Logger log = LoggerFactory.getLogger(SecondDownloadAgainImpl.class);
	@Override
	public int downloadSecond(List<String> onceMailsList) {
		//log.info("method:{} \tservice:{}", new Object[]{"downloadSecond", this.getClass()});
		int count_success = 0;
		
		int size = onceMailsList.size();
		for(int i = 0; i < size; i ++){
//			E://opt//data//pic//mail_bill//y//c//ycj7126168@163.com//dongxinkd@139.com//*.html
			//获取文件名称
			int flag_fileNameStart = onceMailsList.get(i).lastIndexOf("\\");
			int flag_fileNameEnd = onceMailsList.get(i).lastIndexOf(".");
			String fileName = onceMailsList.get(i).substring(flag_fileNameStart+1,flag_fileNameEnd);
			
			//根据发件人判断否为银行原件
			//获得文件存放目录 E://opt//data//pic//mail_bill//y//c//ycj7126168@163.com//dongxinkd@139.com
			String path_sub = onceMailsList.get(i).substring(0, flag_fileNameStart);
			
			//获得发件人
			int flag_sender = path_sub.lastIndexOf("\\");
			String sender = path_sub.substring(flag_sender+1);//dongxinkd@139.com
			
			//获得后续动态目录
			int flag_lastPath = path_sub.indexOf(Constants.FILE_PATH) + Constants.FILE_PATH.length();
			String lastPath = path_sub.substring(flag_lastPath) + "\\";//   y//c//ycj7126168@163.com//dongxinkd@139.com
			
			
			String bankMailAddressAll = Constants.BANK_MAIL_ADDRESS_ALL;
			String[] bankMailAddress_array = bankMailAddressAll.split(",");
			int count = 0;
			int length = bankMailAddress_array.length;
			for(int j = 0; j < length; j++){
				if(sender.equals(bankMailAddress_array[j])){
					//若条件成立 则说明该账单为银行原件 不做截取处理进行二次下载
					StringBuffer content_original = MailDownload.read(onceMailsList.get(i));
					MailDownload.write(path_sub + "\\" + fileName + "_second" + ".html", content_original.toString(),lastPath);
					count_success++;
					break;
				}
				count++;
			}
			
			//条件成立说明邮件为转发件
			if(count == length){
				StringBuffer content_forward = MailDownload.read(onceMailsList.get(i));
				int indexOf = content_forward.indexOf("<div");
				//若转发邮件内没有DIV则非邮件账单
				if (-1 != indexOf){
					StringBuffer content_sub = new StringBuffer() ;
					content_sub.append("<html><head><title>"+ path_sub + "\\" + fileName + "_second" + ".html"+"</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/></head><body>");
					content_sub.append(content_forward.substring(indexOf)); 
					// 第二次下载
					MailDownload.write(path_sub + "\\" + fileName + "_second" + ".html", content_sub.toString(),lastPath);
					count_success++;
				}
				else{
					MailDownload.write(path_sub + "\\" + fileName + "_second" + ".html", content_forward.toString(),lastPath);
					count_success++;
				}
			}
		}
		log.info("Received need to download {} records, including several download success {}", new Object[]{size, count_success});
		return count_success;
	}

}
