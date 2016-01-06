package net.umpay.mailbill.service.impl.mailhandle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.umpay.mailbill.api.mailhandle.ISearchSingleBill;
import net.umpay.mailbill.util.constants.Constants;
import net.umpay.mailbill.util.exception.ErrorCodeContants;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * 获取下载文件中缺失第二次下载的邮件账单
 * 		如：有*.html，没有*_second.html
 * 返回单份的*.html文件路径的列表
 * @author admin
 *
 */
@Deprecated
public class SearchOnceBillImpl implements ISearchSingleBill {
	private static Logger log = LoggerFactory.getLogger(SearchOnceBillImpl.class);
	//存放指定目录下的所有文件及文件夹
	List<String> listFiles = new ArrayList<String>();
	
	@Override
	public List<String> searchSingleFliesPath() {
		//log.info("method:{} \tservice:{}", new Object[]{"searchSingleFliesPath", this.getClass()});
		File filePath = new File(Constants.FILE_PATH);//在Linux指定目录//opt//data//pic//mail_bill//下，开始读取文件
		List<String> filesPath = new ArrayList<String>();//存放所有以.html结尾的文件
		List<String> singleFilesPath = new ArrayList<String>();//存放缺失_second.html的文件路径
		
		try {
			//开始递归循环找出文件目录
			showAllFiles(filePath);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		//开始存放以.html结尾的文件
		int size = listFiles.size();
		for (int i = 0; i < size; i++) {
			if (listFiles.get(i).endsWith(".html")){
				filesPath.add(listFiles.get(i));
			}
		}
		//过滤出缺失_second.html的文件
		int size2 = filesPath.size();
		for (int j = 0; j < size2; j++) {
			//如果仅有一封邮件账单，则到此结束
			if (size2 <= 1){
				singleFilesPath.add(filesPath.get(j));
				break;
			}
			if (filesPath.get(j).endsWith(".html")){
				if (j+1 >= size2){
					singleFilesPath.add(filesPath.get(j));
					break;
				}
				if (filesPath.get(j+1).endsWith("_second.html")){
					j += 1;
					continue;
				} else {
					singleFilesPath.add(filesPath.get(j));
				}
			}
		}
		return singleFilesPath;
	}
	
	/**
	 * 递归遍历指定目录下的文件夹及文件
	 * @param dir 指定目录
	 * @throws MailBillException 指定目录下可能为空
	 */
	public void showAllFiles(File dir) throws MailBillException{
		  //log.info("method:{} \tservice:{}", new Object[]{"showAllFiles", this.getClass()});
		  File[] fs = dir.listFiles();
		  int length = fs.length;
		  for(int i = 0; i < length; i++){
			  listFiles.add(fs[i].getAbsolutePath());
			   if(fs[i].isDirectory()){
				    try{
				     showAllFiles(fs[i]);
				    }catch(Exception e){
				    	throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.NULL_CODE , e.getMessage(), log);
				    }
			   }
		  }
	}
}