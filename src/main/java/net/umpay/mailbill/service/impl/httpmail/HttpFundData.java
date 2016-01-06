package net.umpay.mailbill.service.impl.httpmail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;

import net.umpay.mailbill.api.httpmail.IHttpFundData;
import net.umpay.mailbill.util.constants.MailBillTypeConstants;
import net.umpay.mailbill.util.date.CurrentTime;
import net.umpay.mailbill.util.exception.ErrorCodeContants;
import net.umpay.mailbill.util.exception.MailBillException;
import net.umpay.mailbill.util.exception.MailBillExceptionUtil;
import net.umpay.mailbill.util.mail.httpmail.HttpClientHelper;
import net.umpay.mailbill.util.mail.httpmail.HttpResult;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 抓取基金平台数据
 * 
 * @author zhaozj
 * @date 2015-9-21 上午11:51:16
 * @version 0.0.1
 */
@Service
public class HttpFundData implements IHttpFundData {

	/**
	 * logger
	 */
	private static Logger log = LoggerFactory.getLogger(HttpFundData.class);
	// @Autowired
	// private ThreadPoolTaskExecutor taskExecutor;

	// 存储文件路径
	private static final String FILE_PATH = "D://fund//";

	// 登录相关的链接
	private static final String SESSION_INIT = "http://fund.eastmoney.com/";
	// === 开放式基金净值
	// --全部
	private static final String GET_URL_JIJIN_ALL = "http://fund.eastmoney.com/Data/Fund_JJJZ_Data.aspx?t={0}&lx={1}&letter=&gsid=&text=&sort=zdf,desc&page=1,9999&dt={2}&atfc=&onlySale=0";
	// --债券
	private static final String GET_URL_JIJIN_ZQ = "http://fund.eastmoney.com/Data/Fund_JJJZ_Data.aspx?t={0}&lx={1}&letter=&gsid=&text=&sort=zdf,desc&page=1,9999&dt={2}&atfc=&onlySale=0";
	// --股票
	private static final String GET_URL_JIJIN_GQ = "http://fund.eastmoney.com/Data/Fund_JJJZ_Data.aspx?t={0}&lx={1}&letter=&gsid=&text=&sort=zdf,desc&page=1,9999&dt={2}&atfc=&onlySale=0";
	// --混合型
	private static final String GET_URL_JIJIN_HH = "http://fund.eastmoney.com/Data/Fund_JJJZ_Data.aspx?t={0}&lx={1}&letter=&gsid=&text=&sort=zdf,desc&page=1,9999&dt={2}&atfc=&onlySale=0";
	// --指数型
	private static final String GET_URL_JIJIN_ZS = "http://fund.eastmoney.com/Data/Fund_JJJZ_Data.aspx?t={0}&lx={1}&letter=&gsid=&text=&sort=zdf,desc&page=1,9999&dt={2}&atfc=&onlySale=0";
	// --QDII
	private static final String GET_URL_JIJIN_QDII = "http://fund.eastmoney.com/Data/Fund_JJJZ_Data.aspx?t={0}&lx={1}&letter=&gsid=&text=&sort=zdf,desc&page=1,9999&dt={2}&atfc=&onlySale=0";
	// --LOF
	private static final String GET_URL_JIJIN_LOF = "http://fund.eastmoney.com/Data/Fund_JJJZ_Data.aspx?t={0}&lx={1}&letter=&gsid=&text=&sort=zdf,desc&page=1,9999&dt={2}&atfc=&onlySale=0";
	// --ETF链接
	private static final String GET_URL_JIJIN_ETF = "http://fund.eastmoney.com/Data/Fund_JJJZ_Data.aspx?t=t={0}&lx={1}&letter=&gsid=&text=&sort=zdf,desc&page=1,9999&dt={2}&atfc=&onlySale=0";
	// ===理财型基金收益
	private static final String GET_URL_JIJIN_SY = "http://fund.eastmoney.com/data/Fund_JJJZ_Data.aspx?t={0}&lx={1}&letter=&gsid=0&text=&sort=dwjz,desc&page=1,10000000&dt={2}&atfc=&cycle=&onlySale=1&v={0}";
	// ===分级基金净值
	private static final String GET_URL_JIJIN_FJ = "http://fund.eastmoney.com/Data/Fund_JJJZ_Data.aspx?t={0}&lx={1}&letter=&gsid=0&text=&sort=zdf,desc&page=1,10000000&dt={2}&atfc=";
	// ====货币性基金收益
	private static final String GET_URL_JIJIN_HB = "http://fund.eastmoney.com/data/Fund_JJJZ_Data.aspx?t={0}&page=1,1000000&js=reData&sort=mui,desc";

	/**
	 * 基金平台多类型数据并发抓取
	 * 
	 * @throws MailBillException
	 * @throws InterruptedException
	 */
	public void getPlatFormOfData() throws MailBillException,
			InterruptedException {
		// long beginTime = System.currentTimeMillis();
		String[] queryUrlArr = { GET_URL_JIJIN_ALL, GET_URL_JIJIN_ZQ,
				GET_URL_JIJIN_GQ, GET_URL_JIJIN_HH, GET_URL_JIJIN_ZS,
				GET_URL_JIJIN_QDII, GET_URL_JIJIN_LOF, GET_URL_JIJIN_ETF,
				GET_URL_JIJIN_SY, GET_URL_JIJIN_FJ, GET_URL_JIJIN_HB };
		int[] fundTypeArr = { MailBillTypeConstants.GET_URL_JIJIN_ALL_TYPE,
				MailBillTypeConstants.GET_URL_JIJIN_ZQ_TYPE,
				MailBillTypeConstants.GET_URL_JIJIN_GQ_TYPE,
				MailBillTypeConstants.GET_URL_JIJIN_HH_TYPE,
				MailBillTypeConstants.GET_URL_JIJIN_ZS_TYPE,
				MailBillTypeConstants.GET_URL_JIJIN_QDII_TYPE,
				MailBillTypeConstants.GET_URL_JIJIN_LOF_TYPE,
				MailBillTypeConstants.GET_URL_JIJIN_ETF_TYPE,
				MailBillTypeConstants.GET_URL_JIJIN_SY_TYPE,
				MailBillTypeConstants.GET_URL_JIJIN_FJ_TYPE,
				MailBillTypeConstants.GET_URL_JIJIN_HB_TYPE };

		if (null != queryUrlArr) {
			int length = queryUrlArr.length;
			// CountDownLatch downLatch = new CountDownLatch(length);
			for (int i = 0; i < length; i++) {
				// taskExecutor.execute(new FundThreadRunAble(SESSION_INIT,
				// queryUrlArr[i], fundTypeArr[i], downLatch, new HttpFundData()
				// ,length) );
				final String url = queryUrlArr[i];
				final int type = fundTypeArr[i];
				// 每个类型单独起线程
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							new HttpFundData().httpScan(SESSION_INIT, url, type);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}, i + "").start();
			}
			// downLatch.await();
		}
		// long endTime = System.currentTimeMillis();
		// System.out.println("耗时=" + (endTime - beginTime));

	}

	/**
	 * 开放式基金净值数据 {@link http
	 * ://fund.eastmoney.com/fund.html#os_0;isall_0;ft_;pt_1}
	 * 
	 * @param initUrl
	 * @param queryUrl
	 * @throws MailBillException
	 */
	@Override
	public void httpScan(String initUrl, String queryUrl, int fundType)
			throws MailBillException {

		HttpClientHelper hc = new HttpClientHelper(true);
		HttpResult lr = hc.get(initUrl);// 目的是得到 csrfToken 类似

		int type = fundType;
		int t = 1;
		int lx = 1;
		if (type < 100) {
			t = type / 10;
			lx = type % 10;
		} else {
			t = type / 100;
			lx = type % 100;
		}

		lr = hc.get(
				MessageFormat.format(queryUrl,
						new Object[] { t, lx, System.currentTimeMillis() }),
				setFundGetMailInfo());
		String jsonStr = lr.getHtml();
		// System.out.println("jsonStr=" + jsonStr);
		if (StringUtils.isNotBlank(jsonStr) && jsonStr.indexOf("=") != -1) {
			String[] strArr = jsonStr.split("=");
			// System.out.println(strArr);
			String newStr = strArr[1];
			switch (fundType) {
			case MailBillTypeConstants.GET_URL_JIJIN_ALL_TYPE:
			case MailBillTypeConstants.GET_URL_JIJIN_ZQ_TYPE:
			case MailBillTypeConstants.GET_URL_JIJIN_GQ_TYPE:
			case MailBillTypeConstants.GET_URL_JIJIN_HH_TYPE:
			case MailBillTypeConstants.GET_URL_JIJIN_ZS_TYPE:
			case MailBillTypeConstants.GET_URL_JIJIN_QDII_TYPE:
			case MailBillTypeConstants.GET_URL_JIJIN_LOF_TYPE:
			case MailBillTypeConstants.GET_URL_JIJIN_ETF_TYPE:
				this.handleJz(newStr, fundType);
				System.out.println("---this.handleJz(newStr, " + fundType
						+ ");---");
				break;
			case MailBillTypeConstants.GET_URL_JIJIN_SY_TYPE:
				this.handleSy(newStr, "SY");
				System.out.println("---this.handleSy(newStr, " + fundType
						+ ");---");
				break;
			case MailBillTypeConstants.GET_URL_JIJIN_FJ_TYPE:
				this.handleFj(newStr, "FJ");
				System.out.println("---this.handleFj(newStr, " + fundType
						+ ");---");
				break;
			case MailBillTypeConstants.GET_URL_JIJIN_HB_TYPE:
				this.handleHb(newStr, "HB");
				System.out.println("---this.handleHb(newStr, " + fundType
						+ ");---");
				break;
			default: {
				// TODO
			}
			}
		}

	}

	// ---------- private function ----------

	/*
	 * 处理基金净值
	 */
	private void handleJz(String jsonStr, int fundType)
			throws MailBillException {
		String jzType = "";
		switch (fundType) {
		case MailBillTypeConstants.GET_URL_JIJIN_ALL_TYPE:
			jzType = "ALL";
			break;
		case MailBillTypeConstants.GET_URL_JIJIN_ZQ_TYPE:
			jzType = "ZQ";
			break;
		case MailBillTypeConstants.GET_URL_JIJIN_GQ_TYPE:
			jzType = "GQ";
			break;
		case MailBillTypeConstants.GET_URL_JIJIN_HH_TYPE:
			jzType = "HH";
			break;
		case MailBillTypeConstants.GET_URL_JIJIN_ZS_TYPE:
			jzType = "ZS";
			break;
		case MailBillTypeConstants.GET_URL_JIJIN_QDII_TYPE:
			jzType = "QDII";
			break;
		case MailBillTypeConstants.GET_URL_JIJIN_LOF_TYPE:
			jzType = "LOF";
			break;
		case MailBillTypeConstants.GET_URL_JIJIN_ETF_TYPE:
			jzType = "ETF";
			break;
		}

		StringBuilder sbFileName = new StringBuilder();
		sbFileName.append("foundJz_");
		sbFileName.append(jzType);
		sbFileName.append("_");
		sbFileName.append(CurrentTime.getFullDay());
		sbFileName.append(".txt");
		// if file exists, then clear content
		File file = new File(FILE_PATH + sbFileName.toString());
		if (file.exists()) {
			this.dataToFile(FILE_PATH, sbFileName.toString(), "", false, false);
		}

		try {
			JSONObject jsonObject = new JSONObject(jsonStr);
			String array = jsonObject.getString("datas");
			// System.out.println(array);
			JSONArray jsonArr = new JSONArray(array);
			if (null != jsonArr) {
				int length = jsonArr.length();
				for (int i = 0; i < length; i++) {
					String lineObj = jsonArr.getString(i);
					// System.out.println("line" + i + "=" + lineObj);
					this.dataToFile(FILE_PATH, sbFileName.toString(), "line_"
							+ i + "=" + lineObj, true, true);
					JSONArray perObj = new JSONArray(lineObj);
					int per_length = perObj.length();
					for (int k = 0; k < per_length; k++) {
						// System.out.print("colum" + k + "=" +
						// perObj.getString(k) +",");
						this.dataToFile(FILE_PATH, sbFileName.toString(),
								"colum_" + k + "=" + perObj.getString(k) + ",",
								false, true);
					}
					this.dataToFile(FILE_PATH, sbFileName.toString(), "", true,
							true);
					// System.out.println("");
				}
			}

			String showday = jsonObject.getString("showday");
			this.dataToFile(FILE_PATH, sbFileName.toString(), showday, true,
					true);
			// System.out.println("showday==" + showday);
		} catch (JSONException e) {
			throw MailBillExceptionUtil.getWithLog(e,
					ErrorCodeContants.JSON_FORMAT_EXCEPTION_CODE,
					ErrorCodeContants.JSON_FORMAT_EXCEPTION.getMsg(), log);
		}
	}

	/*
	 * 理财型基金收益
	 */
	private void handleSy(String jsonStr, String fundType)
			throws MailBillException {
		StringBuilder sbFileName = new StringBuilder();
		sbFileName.append("foundSy_");
		sbFileName.append(fundType + "_");
		sbFileName.append(CurrentTime.getFullDay());
		sbFileName.append(".txt");
		// if file exists, then clear content
		File file = new File(FILE_PATH + sbFileName.toString());
		if (file.exists()) {
			this.dataToFile(FILE_PATH, sbFileName.toString(), "", false, false);
		}

		try {
			JSONObject jsonObject = new JSONObject(jsonStr);
			String array = jsonObject.getString("datas");
			// System.out.println(array);
			JSONArray jsonArr = new JSONArray(array);
			if (null != jsonArr) {
				int length = jsonArr.length();
				for (int i = 0; i < length; i++) {
					String lineObj = jsonArr.getString(i);
					// System.out.println("line" + i + "=" + lineObj);
					this.dataToFile(FILE_PATH, sbFileName.toString(), "line_"
							+ i + "=" + lineObj, true, true);
					JSONArray perObj = new JSONArray(lineObj);
					int per_length = perObj.length();
					for (int k = 0; k < per_length; k++) {
						// System.out.print("colum" + k + "=" +
						// perObj.getString(k) +",");
						this.dataToFile(FILE_PATH, sbFileName.toString(),
								"colum_" + k + "=" + perObj.getString(k) + ",",
								false, true);
					}
					this.dataToFile(FILE_PATH, sbFileName.toString(), "", true,
							true);
					// System.out.println("");
				}
			}

			String showday = jsonObject.getString("showday");
			this.dataToFile(FILE_PATH, sbFileName.toString(), showday, true,
					true);
			// System.out.println("showday==" + showday);
		} catch (JSONException e) {
			throw MailBillExceptionUtil.getWithLog(e,
					ErrorCodeContants.JSON_FORMAT_EXCEPTION_CODE,
					ErrorCodeContants.JSON_FORMAT_EXCEPTION.getMsg(), log);
		}
	}

	/*
	 * 分级基金净值
	 */
	private void handleFj(String jsonStr, String fundType)
			throws MailBillException {
		StringBuilder sbFileName = new StringBuilder();
		sbFileName.append("foundFj_");
		sbFileName.append(fundType + "_");
		sbFileName.append(CurrentTime.getFullDay());
		sbFileName.append(".txt");
		// if file exists, then clear content
		File file = new File(FILE_PATH + sbFileName.toString());
		if (file.exists()) {
			this.dataToFile(FILE_PATH, sbFileName.toString(), "", false, false);
		}

		try {
			JSONObject jsonObject = new JSONObject(jsonStr);
			String array = jsonObject.getString("datas");
			// System.out.println(array);
			JSONArray jsonArr = new JSONArray(array);
			if (null != jsonArr) {
				int length = jsonArr.length();
				for (int i = 0; i < length; i++) {
					String lineObj = jsonArr.getString(i);
					// System.out.println("line" + i + "=" + lineObj);
					this.dataToFile(FILE_PATH, sbFileName.toString(), "line_"
							+ i + "=" + lineObj, true, true);
					JSONArray perObj = new JSONArray(lineObj);
					int per_length = perObj.length();
					for (int k = 0; k < per_length; k++) {
						// System.out.print("colum" + k + "=" +
						// perObj.getString(k) +",");
						this.dataToFile(FILE_PATH, sbFileName.toString(),
								"colum_" + k + "=" + perObj.getString(k) + ",",
								false, true);
					}
					this.dataToFile(FILE_PATH, sbFileName.toString(), "", true,
							true);
					// System.out.println("");
				}
			}

			String showday = jsonObject.getString("showday");
			this.dataToFile(FILE_PATH, sbFileName.toString(), showday, true,
					true);
			// System.out.println("showday==" + showday);
		} catch (JSONException e) {
			throw MailBillExceptionUtil.getWithLog(e,
					ErrorCodeContants.JSON_FORMAT_EXCEPTION_CODE,
					ErrorCodeContants.JSON_FORMAT_EXCEPTION.getMsg(), log);
		}
	}

	/*
	 * 货币性基金收益
	 */
	private void handleHb(String jsonStr, String fundType)
			throws MailBillException {
		StringBuilder sbFileName = new StringBuilder();
		sbFileName.append("foundHb_");
		sbFileName.append(fundType + "_");
		sbFileName.append(CurrentTime.getFullDay());
		sbFileName.append(".txt");
		// if file exists, then clear content
		File file = new File(FILE_PATH + sbFileName.toString());
		if (file.exists()) {
			this.dataToFile(FILE_PATH, sbFileName.toString(), "", false, false);
		}

		try {
			JSONObject jsonObject = new JSONObject(jsonStr);
			String array = jsonObject.getString("datas");
			// System.out.println(array);
			JSONArray jsonArr = new JSONArray(array);
			if (null != jsonArr) {
				int length = jsonArr.length();
				for (int i = 0; i < length; i++) {
					String lineObj = jsonArr.getString(i);
					// System.out.println("line" + i + "=" + lineObj);
					this.dataToFile(FILE_PATH, sbFileName.toString(), "line_"
							+ i + "=" + lineObj, true, true);
					JSONArray perObj = new JSONArray(lineObj);
					int per_length = perObj.length();
					for (int k = 0; k < per_length; k++) {
						// System.out.print("colum" + k + "=" +
						// perObj.getString(k) +",");
						this.dataToFile(FILE_PATH, sbFileName.toString(),
								"colum_" + k + "=" + perObj.getString(k) + ",",
								false, true);
					}
					this.dataToFile(FILE_PATH, sbFileName.toString(), "", true,
							true);
					// System.out.println("");
				}
			}

			String showday = jsonObject.getString("showday");
			this.dataToFile(FILE_PATH, sbFileName.toString(), showday, true,
					true);
			// System.out.println("showday==" + showday);
		} catch (JSONException e) {
			throw MailBillExceptionUtil.getWithLog(e,
					ErrorCodeContants.JSON_FORMAT_EXCEPTION_CODE,
					ErrorCodeContants.JSON_FORMAT_EXCEPTION.getMsg(), log);
		}
	}

	/**
	 * 设置基金head信息
	 */
	private static Header[] setFundGetMailInfo() {
		Header[] result = {
				new BasicHeader("Host", "fund.eastmoney.com"),
				new BasicHeader("User-Agent",
						"Mozilla/5.0 (Windows NT 6.2; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0"),
				new BasicHeader("Accept", "*/*"),
				new BasicHeader("Accept-Language",
						"zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3"),
				new BasicHeader("Accept-Encoding", "gzip, deflate"),
				new BasicHeader("Referer",
						"http://fund.eastmoney.com/fund.html"),
				new BasicHeader("Connection", "keep-alive") };

		return result;
	}

	/**
	 * 将数据存储到文件
	 * 
	 * @param filePath
	 *            文件路径
	 * @param fileName
	 *            文件名称
	 * @param dataStr
	 *            待存储串
	 * @param ifRn
	 *            是否换行 true 换行; false 不换行;
	 * @param isOverWrite
	 *            是否追加 true追加; false 重写;
	 */
	private void dataToFile(String filePath, String fileName, String dataStr,
			boolean ifRn, boolean isOverWrite) {
		File file = null;
		FileWriter fileWritter = null;
		BufferedWriter bufferWritter = null;
		try {
			file = new File(filePath + fileName);
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			// true = append file
			fileWritter = new FileWriter(file.getAbsoluteFile(), isOverWrite);
			bufferWritter = new BufferedWriter(fileWritter);
			bufferWritter.write(dataStr);
			if (ifRn) {
				bufferWritter.write("\r");
			}
			bufferWritter.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != bufferWritter)
					bufferWritter.close();
				if (null != fileWritter)
					fileWritter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 测试main方法
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		HttpFundData fundService = new HttpFundData();
		fundService.getPlatFormOfData();
	}
}
