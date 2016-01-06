package net.umpay.mailbill.util.mail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.umpay.mailbill.util.exception.ErrorCodeContants;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;

import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.FileInfo;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.StorageServer;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.csource.fastdfs.UploadStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/****
 * FastFDSUtils 帮助文档
 * 
 * @author 孙伟鹏
 * 
 */

public class FastFDSUtils {
	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(FastFDSUtils.class);
	static {
		try {	
			ClientGlobal.init("./conf/fdfs_client.conf");
		} catch (Exception e) {
			MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.DFS_CONF_EXCEPTION_CODE, ErrorCodeContants.DFS_CONF_EXCEPTION.getMsg(), log);
		}
	}

	/***
	 * 通过文件路径上传图片
	 * 
	 * @param filePath
	 * @return	groupName + path
	 */
	public String uploadFileByPath(String filePath) throws Exception {
		File file = new File(filePath);
		if (!file.exists()) {
			return null;
		}
		// 文件后缀名
		String suffix = getSuffix(file.getName());
		return uploadFile1(getBytesByFile(file), suffix, null);
	}

	/***
	 * 通过文件路径上传图片
	 * 
	 * @param filePath
	 * @return	String[0] : groupName | String[1] : path
	 * @throws MailBillException 
	 */
	public String[] uploadFile(String filePath) throws MailBillException  {
		File file = new File(filePath);
		if (!file.exists()) {
			return null;
		}
		// 文件后缀名
		String suffix = getSuffix(file.getName());
		try {
			return uploadFile(getBytesByFile(file), suffix, null);
		} catch (MailBillException e) {
			throw MailBillExceptionUtil.getWithLog(e, e.getErrorCode(), e.getMessage(), log);
		}
	}

	/***
	 * 通过固定组上传图片
	 * 
	 * @param groupName
	 * @param filePath
	 * @return  groupName + path
	 * @throws MailBillException 
	 * @throws Exception
	 */
	public String uploadFileByPath(String groupName, String filePath) throws MailBillException, Exception
			{
		File file = new File(filePath);
		if (!file.exists()) {
			return null;
		}
		// 文件后缀名
		String suffix = getSuffix(file.getName());
		return uploadFile1(groupName, getBytesByFile(file), suffix, null);
	}

	/***
	 * 通过固定组上传图片
	 * 
	 * @param groupName
	 * @param filePath
	 * @return groupName + path
	 * @throws Exception
	 */
	public String uploadFileByPath(String groupName, String filePath,
			NameValuePair[] metaList) throws Exception {
		File file = new File(filePath);
		if (!file.exists()) {
			return null;
		}
		// 文件后缀名
		String suffix = getSuffix(file.getName());
		return uploadFile1(groupName, getBytesByFile(file), suffix, metaList);
	}

	/***
	 * 通过固定组上传图片
	 * 
	 * @param groupName
	 * @param fileName
	 * @param is
	 * @param metaList
	 * @return groupName + path
	 * @throws Exception
	 */
	public String uploadFileByInputStream(String groupName,
			String fileName, InputStream is, NameValuePair[] metaList) throws Exception
			{
		// 文件后缀名
		String suffix = getSuffix(fileName);
		return uploadFile1(groupName, is, suffix, metaList);
	}

	/***
	 * 通过固定组上传图片
	 * 
	 * @param groupName
	 * @param fileName
	 * @param is
	 * @return groupName + path
	 * @throws Exception
	 */
	public String uploadFileByInputStream(String groupName,
			String fileName, InputStream is) throws Exception {
		// 文件后缀名
		String suffix = getSuffix(fileName);
		return uploadFile1(groupName, is, suffix, null);
	}

	/***
	 * 通过固定组上传图片
	 * 
	 * @param fileName
	 * @param is
	 * @return groupName + path
	 * @throws Exception
	 */
	public String uploadFileByInputStream(String fileName, InputStream is)
			throws Exception {
		// 文件后缀名
		String suffix = getSuffix(fileName);
		return uploadFile1(null, is, suffix, null);
	}

	/**
	 * 下载保存到本地
	 * 
	 * @param groupName
	 *            组名
	 * @param remoteFilename
	 *            文件名
	 * @param path
	 *            保存路径
	 * @throws Exception
	 */
	public void saveDownLoadByPath(String groupName,
			String remoteFilename, String path) throws Exception {
		TrackerClient trackerClient = new TrackerClient();
		TrackerServer trackerServer = trackerClient.getConnection();
		StorageServer storageServer = null;
		StorageClient1 client = new StorageClient1(trackerServer, storageServer);
		// byte[] b = client.download_file(groupName, remoteFilename);
		File file = new File(path);
		FileOutputStream fos = new FileOutputStream(file);
		byte[] file_buff = client.download_file(groupName, remoteFilename);
		if (file_buff != null) {
			fos.write(file_buff);
			fos.flush();
		}
		fos.close();
		closeTrackerClient(trackerServer);
	}

	/***
	 * 删除文件
	 * 
	 * @param groupName
	 *            组名
	 * @param remoteFilename
	 *            文件名
	 * @return int
	 * @throws Exception
	 */
	public int deleteFile(String groupName, String remoteFilename)
			throws Exception {
		TrackerClient trackerClient = new TrackerClient();
		TrackerServer trackerServer = trackerClient.getConnection();
		StorageServer storageServer = null;
		StorageClient1 client = new StorageClient1(trackerServer, storageServer);
		int i = client.delete_file(groupName, remoteFilename);
		closeTrackerClient(trackerServer);
		return i;
	}

	/***
	 * 获取文件信息
	 * 
	 * @param groupName
	 *            组名
	 * @param remoteFilename
	 *            文件名
	 * @return FileInfo
	 * @throws Exception
	 */
	public FileInfo getFileInfo(String groupName, String remoteFilename)
			throws Exception {
		TrackerClient trackerClient = new TrackerClient();
		TrackerServer trackerServer = trackerClient.getConnection();
		StorageServer storageServer = null;
		StorageClient client = new StorageClient(trackerServer, storageServer);
		FileInfo fileInfo = client.get_file_info(groupName, remoteFilename);
		closeTrackerClient(trackerServer);
		return fileInfo;
	}

	/***
	 * 获取StorageClient
	 * 
	 * @return StorageClient
	 * @throws Exception
	 */
	// private static StorageClient getStorageClient() throws Exception {
	// TrackerClient trackerClient = new TrackerClient();
	// FastFDSUtils util = new FastFDSUtils();
	// util.trackerServer = trackerClient.getConnection();
	// StorageServer storageServer = null;
	// //FastFDSUtils.trackerServer = trackerServer;
	// return new StorageClient(util.trackerServer, storageServer);
	// }

	/***
	 * 获取StorageClient1
	 * 
	 * @return StorageClient1
	 * @throws Exception
	 */
	// private static StorageClient1 getStorageClient1() throws Exception {
	// TrackerClient trackerClient = new TrackerClient();
	// FastFDSUtils util = new FastFDSUtils();
	// util.trackerServer = trackerClient.getConnection();
	// StorageServer storageServer = null;
	// // FastFDSUtils.trackerServer = trackerServer;
	// return new StorageClient1(util.trackerServer, storageServer);
	// }

	/***
	 * File 转为 byte[]
	 * 
	 * @param file
	 * @return byte[] 
	 * @throws MailBillException 
	 */
	private static byte[] getBytesByFile(File file) throws MailBillException {
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			byte[] file_buff = null;
			if (fis != null) {
				int len = fis.available();
				file_buff = new byte[len];
				fis.read(file_buff);
			}
			return file_buff;
		} catch (FileNotFoundException e) {
			throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.File_Not_Found_EXCEPTION_CODE, ErrorCodeContants.File_Not_Found_EXCEPTION.getMsg(), log);
		} catch (IOException e) {
			throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.IO_EXCEPTION_CODE, ErrorCodeContants.IO_EXCEPTION.getMsg(), log);
		}
	}

	/***
	 * 获取文件后缀名
	 * 
	 * @param fileName
	 * @return groupName + path
	 */
	private static String getSuffix(String fileName) {
		return fileName.substring(fileName.lastIndexOf(".") + 1);
	}

	/**
	 * 处理返回路径结果信息
	 * 
	 * @param result 
	 * @return	groupName + path
	 */
	@SuppressWarnings("unused")
	private static String getResultToString(String[] result) {
		return (result == null) ? null : File.separator + result[0]
				+ File.separator + result[1];
	}

	/***
	 * 上传文件 随机获取group
	 * 
	 * @param fileBuff
	 * @param suffix
	 *            后缀名
	 * @return String[0] : groupName | String[1] : path
	 * @throws MailBillException 
	 * @throws Exception
	 */
	public String[] uploadFile(byte[] fileBuff, String suffix,
			NameValuePair[] metaList) throws MailBillException  {
		return uploadFile("", fileBuff, suffix, metaList);
	}

	/***
	 * 上传文件 随机获取group
	 * 
	 * @param fileBuff
	 * @param suffix
	 *            后缀名
	 * @return groupName + path
	 * @throws Exception
	 */
	public String uploadFile1(byte[] fileBuff, String suffix,
			NameValuePair[] metaList) throws Exception {
		return uploadFile1("", fileBuff, suffix, metaList);
	}

	/***
	 * 通过固定组名 上传文件
	 * 
	 * @param groupName
	 * @param fileBuff
	 * @param suffix
	 * @param metaList
	 * @return groupName+path
	 * @throws IOException 
	 * @throws MyException 
	 */
	public String uploadFile1(String groupName, byte[] fileBuff,
			String suffix, NameValuePair[] metaList) throws IOException, MyException {
		TrackerClient trackerClient = new TrackerClient();
		TrackerServer trackerServer = trackerClient.getConnection();
		StorageServer storageServer = null;
		StorageClient1 client = new StorageClient1(trackerServer, storageServer);
		String str = client.upload_file1(groupName, fileBuff, suffix, metaList);
		closeTrackerClient(trackerServer);
		return str;
	}

	/***
	 * 通过inputStream 上传文件
	 * 
	 * @param groupName
	 * @param is
	 * @param suffix
	 * @param metaList
	 * @return groupName+path
	 * @throws Exception
	 */
	public String uploadFile1(String groupName, InputStream is,
			String suffix, NameValuePair[] metaList) throws Exception {
		TrackerClient trackerClient = new TrackerClient();
		TrackerServer trackerServer = trackerClient.getConnection();
		StorageServer storageServer = null;
		StorageClient1 client = new StorageClient1(trackerServer, storageServer);
		int length = is.available();
		String str = client.upload_file1(groupName, length, new UploadStream(
				is, length), suffix, metaList);
		closeTrackerClient(trackerServer);
		return str;
	}

	/***
	 * 通过inputStream 上传文件
	 * 
	 * @param groupName
	 * @param is
	 * @param suffix
	 * @param metaList
	 * @return String[0] : groupName | String[1] : path
	 * @throws MailBillException 
	 * @throws Exception
	 */
	public String[] uploadFile(String groupName, InputStream is,
			String suffix, NameValuePair[] metaList) throws MailBillException {
		TrackerClient trackerClient = new TrackerClient();
		TrackerServer trackerServer;
		try {
			trackerServer = trackerClient.getConnection();
			StorageServer storageServer = null;
			StorageClient client = new StorageClient(trackerServer, storageServer);
			int length = is.available();
			String[] str = client.upload_file(groupName, length, new UploadStream(
					is, length), suffix, metaList);
			closeTrackerClient(trackerServer);
			return str;
		} catch (IOException e) {
			throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.IO_EXCEPTION_CODE, ErrorCodeContants.IO_EXCEPTION.getMsg(), log);
		} catch (MyException e) {
			throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.DFS_UNKNOWN_ERROR_CODE, ErrorCodeContants.DFS_UNKNOWN_ERROR.getMsg(), log);
		}
	}

	/***
	 * 通过固定组名 上传文件
	 * 
	 * @param groupName
	 * @param fileBuff
	 * @param suffix
	 * @param metaList
	 * @return String[0] : groupName | String[1] : path
	 * @throws MailBillException 
	 * @throws Exception
	 */
	public String[] uploadFile(String groupName, byte[] fileBuff,
			String suffix, NameValuePair[] metaList) throws MailBillException  {
		TrackerClient trackerClient = new TrackerClient();
		TrackerServer trackerServer;
		try {
			trackerServer = trackerClient.getConnection();
			StorageServer storageServer = null;
			StorageClient client = new StorageClient(trackerServer, storageServer);
			String[] str = client
					.upload_file(groupName, fileBuff, suffix, metaList);
			closeTrackerClient(trackerServer);
			return str;
		} catch (IOException e) {
			throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.IO_EXCEPTION_CODE, ErrorCodeContants.IO_EXCEPTION.getMsg(), log);
		} catch (MyException e) {
			throw MailBillExceptionUtil.getWithLog(e, ErrorCodeContants.DFS_UNKNOWN_ERROR_CODE, ErrorCodeContants.DFS_UNKNOWN_ERROR.getMsg(), log);
		}
	}

	/***
	 * 下载文件
	 * 
	 * @param groupName
	 * @param remoteFilename
	 * @return byte[]
	 * @throws Exception
	 */
	public byte[] downLoadFile(String groupName, String remoteFilename)
			throws Exception {
		TrackerClient trackerClient = new TrackerClient();
		TrackerServer trackerServer = trackerClient.getConnection();
		StorageServer storageServer = null;
		StorageClient1 client = new StorageClient1(trackerServer, storageServer);
		byte[] b = client.download_file(groupName, remoteFilename);
		closeTrackerClient(trackerServer);
		return b;
	}

	public void closeTrackerClient(TrackerServer trackerServer) throws IOException
			{
		if (trackerServer != null) {
			trackerServer.close();
		}
	}
	/**
	* 将一个字符串转化为输入流
	*/
	public InputStream getStringStream(String sInputString){
		if (sInputString != null && !sInputString.trim().equals("")){
			try{
				ByteArrayInputStream tInputStringStream = new ByteArrayInputStream(sInputString.getBytes());
				return tInputStringStream;
			}catch (Exception ex){
				ex.printStackTrace();
			}
		}
		return null;
	}

}
