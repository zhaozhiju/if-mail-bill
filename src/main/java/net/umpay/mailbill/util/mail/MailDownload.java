package net.umpay.mailbill.util.mail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.umpay.mailbill.util.constants.Constants;
import net.umpay.mailbill.util.exception.ErrorCodeContants;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;

/**
 * 邮件下载
 */
public class MailDownload {
	private static Logger log = LoggerFactory.getLogger(MailDownload.class);
	/**
	 * 将账单邮件下载到本地
	 * 
	 * @param path 根目录路径
	 * @param content 文件内容
	 * @param path_dir 动态路径 根据用户email动态生成
	 * 
	 */
	@SuppressWarnings("unused")
	public static void write(String path,String content,String path_dir) {  
		String s = new String();  
	        
	    BufferedReader input = null ;
	    BufferedWriter output = null ;
	    try {  
	        File f = new File(path);
	        File f2 = new File(Constants.FILE_PATH+path_dir);
	        f2.mkdirs();	//没有目录会自动创建
	        log.info(path);
	            
	        if (f.exists()) {  
	        	log.info("文件存在"); 
	        }
	        else {  
	        	log.info("文件不存在，正在创建...");  
	            if (f.createNewFile()) {  
	                log.info("文件创建成功！");  
	            } 
	            else {  
	            	log.info("文件创建失败！");  
	            }  
	        }  
	        input = new BufferedReader(new InputStreamReader(new FileInputStream(f), Constants.CHARSET));
	        while ((s = input.readLine()) != null) {  
	            s = "";
	        }  
	        output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), Constants.CHARSET));
	        if(content.contains("charset")){
	        	content = content.replaceFirst("(?<=charset=)[\\w-]+", Constants.CHARSET);
         	}else {
         		content = "<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset="+Constants.CHARSET+"\"/></head>"+content;
         	}
	        output.write(content);
	    } catch (Exception e) {  
	        log.error(e.getMessage(),e);
	    } finally {
	    	if(input!=null) {
	    		try {
	    			input.close();
	    		} catch (Exception e) {
	    			log.error(e.getMessage(),e);
	            }
	        }
	        	  
	        if(output!=null) {
	        	try {
	        		output.close();
	            } catch (Exception e) {
	            	log.error(e.getMessage(),e);
	            }
	        }
	    }  
	}
	
	/**
	 * 读取文件（账单邮件）
	 * 
	 * @param file 文件路径
	 */
	 public static StringBuffer read(String file) {  
	        String s = null;  
	        StringBuffer sb = new StringBuffer();
	        BufferedReader input = null;
	        File f = new File(file);  
	        if (f.exists()) {  
	            try {  
	            	input = new BufferedReader(new InputStreamReader(new FileInputStream(f),Constants.CHARSET));  
	                while ((s = input.readLine()) != null) {  
	                    sb.append(s);  
	                } 
	            } catch (Exception e) {  
	               log.error(e.getMessage(),e);
	            } finally {
        	    	if(input!=null) {
        	    		try {
        	    			input.close();
        	    		} catch (Exception e) {
        	    			log.error(e.getMessage(),e);
        	            }
        	        }
        	    }
	        } else {  
	            log.info("文件不存在!");  
	        }
	        return sb;
	}
	
	 /** 
	 * 通过DFS 方式获取文件 
	 * 
	 * @param groupName 
	 * @param remoteFilename 
	 * @return StringBuffer
	 * @throws IOException 
	 */ 
	
	public static StringBuffer getRemoteFile(String groupName,String remoteFilename) { 
		 StringBuffer sb = new StringBuffer();
		 String s = null;
		 
		 try {
			 byte[] downLoadFile = new FastFDSUtils().downLoadFile(groupName, remoteFilename);
			 s = new String(downLoadFile, Constants.CHARSET);
			 sb.append(s);  
			} catch (Exception e) {
				MailBillExceptionUtil.getWithLog(ErrorCodeContants.DFS_READ_FILE_FAILED_CODE, ErrorCodeContants.DFS_READ_FILE_FAILED.getMsg(), log);
			}
		 return sb; 
	 }
}
