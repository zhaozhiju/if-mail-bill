package net.umpay.mailbill.util.constants;

public class MailBillTypeConstants {

	/**		日月类型的====================begin======================*/
	public static final int BILL_TYPE_MONTH = 1; // 月账单
	public static final int BILL_TYPE_DAY = 2; // 日账单
	/**		关键字类型的====================end========================*/
	
	/**		是否成功类型的====================begin======================*/
	public static final int SUCCESS = 1; // 成功
	public static final int FAILED = 2; // 失败
	/**		是否成功类型的====================end========================*/
	
	/**		存入支出类型的(相对银行来说的。默认为支出)====================begin======================*/
	public static final int BILL_PAY = 0; // 支出
	public static final int BILL_INCOME = 1; // 收入
	/**		存入支出类型的====================end========================*/
	
	/**		是否类型的====================begin======================*/
	public static final int BILL_TRUE = 1; // 是账单
	public static final int BILL_FALSE = 0; // 非账单
	/**		关键字类型的====================end========================*/
	
	
	/**		主副卡类型的====================begin======================*/
	public static final int BANK_MAIN_CARD = 1; // 主卡
	public static final int BANK_VICE_CARD = 2; // 副卡
	/**		关键字类型的====================end========================*/
	/**		币种====================begin======================*/
	public static final String RMB_CURRENCY_TYPE = "CNY";
	public static final String USD_CURRENCY_TYPE = "USD";
	/**		币种====================end======================*/
	/** 银行种类=====================begin======================*/
	public static final int BANK_TYPE_ICBC = 4233; // 工商银行
	public static final int BANK_TYPE_CMB = 4317; // 招商银行
	public static final int BANK_TYPE_CCB = 4311; // 建行银行
	public static final int BANK_TYPE_SPD = 4373; // 浦发银行
	public static final int BANK_TYPE_CHINA = 4330; // 中国银行
	public static final int BANK_TYPE_CEB = 4326; // 光大银行
	public static final int BANK_TYPE_CGB = 4375; // 广发银行
	public static final int BANK_TYPE_CIB = 4360; // 兴业银行
	public static final int BANK_TYPE_CITIC = 4368; // 中信银行
	public static final int BANK_TYPE_CMSB = 4319; // 民生银行
	public static final int BANK_TYPE_PINGAN = 4370; // 平安银行
	public static final int BANK_TYPE_HUAXIA = 4362; // 华夏银行
	public static final int BANK_TYPE_BOCOMM = 4313; // 交行银行
	public static final int BANK_TYPE_AGRICULTURAL = 4284; // 农业银行
	public static final int BANK_TYPE_BEIJING = 4381; // 北京银行
	/** 银行种类=====================end========================*/
	
	/** 银行账单类型VO层===============begin=====================*/
	public static final int BILL_TYPE_BOCOMMVISA = 1; // 交行VISA卡
	public static final int BILL_TYPE_NEW_CMB = 2; // 招商新版账单
	public static final int BILL_TYPE_DAY_CMB = 3; // 招商日账单
	public static final int BILL_TYPE_MONTHLY_CMB = 4; // 招商商务卡账单
	public static final int BILL_TYPE_OLD_CMB = 5; // 招商旧卡账单
	public static final int BILL_TYPE_CCB = 6; // 建行账单
	public static final int BILL_TYPE_SPD = 7; // 浦发账单
	public static final int BILL_TYPE_CHINA = 8; // 中国账单
	public static final int BILL_TYPE_CEB = 9; // 光大账单
	public static final int BILL_TYPE_CGB = 10; // 广发账单
	public static final int BILL_TYPE_CIB = 11; // 兴业账单
	public static final int BILL_TYPE_CITIC = 12; // 中信账单
	public static final int BILL_TYPE_CMSB = 13; // 民生账单
	public static final int BILL_TYPE_ICBC = 14; // 工商账单
	public static final int BILL_TYPE_PINGAN = 15; // 平安账单
	public static final int BILL_TYPE_HUAXIA = 16; // 华夏账单
	public static final int BILL_TYPE_BOCOMM_UNIONPAY = 17; // 银联标准的交通银行卡账单
	public static final int BILL_TYPE_CMB = 18; // 招商新发现的账单
	public static final int BILL_TYPE_AGRICULTURAL = 19; // 农业银行账单
	public static final int BILL_TYPE_BEIJING = 20; // 北京银行账单
	public static final int BILL_TYPE_BOCOMM_MASTERCARD = 21; // 交通银行万事达卡账单
	/** 银行账单类型VO层===============end=====================*/
	
	/** 邮箱厂商 类型===============begin=====================*/
	public static final String HTTP_SCAN_126_TYPE = "126.com"; // 126邮箱
	public static final String HTTP_SCAN_163_TYPE = "163.com"; // 163邮箱
	public static final String HTTP_SCAN_YEAH_TYPE = "yeah.net"; // yeah邮箱
	public static final String HTTP_SCAN_139_TYPE = "139.com"; // 139邮箱
	public static final String HTTP_SCAN_QQ_TYPE = "qq.com"; // QQ邮箱
	/** 邮箱厂商 类型================end======================*/
	
	/***************** 推送手机的数据类型 begin*******************/ 
	public static final String DATA_TYPE_TO_APP1 = "1"; // 1 卡列表
	public static final String DATA_TYPE_TO_APP2 = "2"; // 2 账期列表
	public static final String DATA_TYPE_TO_APP3 = "3"; // 3 月账单详情
	public static final String DATA_TYPE_TO_APP4 = "4"; // 4 日账单详情 
	public static final String DATA_TYPE_TO_APP5 = "5"; // 5 账号绑定邮箱列表
	public static final String DATA_TYPE_TO_APP6 = "6"; // 6 解绑邮箱成功/失败
	/***************** 推送手机的数据类型 end*******************/
	
	/***************** 基金平台数据类型 begin*******************/
	public static final int GET_URL_JIJIN_ALL_TYPE = 11; // 全部 - 开放式基金净值
	public static final int GET_URL_JIJIN_ZQ_TYPE = 113; // 债券  - 开放式基金净值
	public static final int GET_URL_JIJIN_GQ_TYPE = 12; // 股票 - 开放式基金净值
	public static final int GET_URL_JIJIN_HH_TYPE = 13; // 混合型 - 开放式基金净值
	public static final int GET_URL_JIJIN_ZS_TYPE = 15; // 指数型 - 开放式基金净值
	public static final int GET_URL_JIJIN_QDII_TYPE = 16; // QDII - 开放式基金净值
	public static final int GET_URL_JIJIN_LOF_TYPE = 18; // LOF - 开放式基金净值
	public static final int GET_URL_JIJIN_ETF_TYPE = 111; // ETF链接 - 开放式基金净值
	public static final int GET_URL_JIJIN_SY_TYPE = 51; // 理财型基金收益
	public static final int GET_URL_JIJIN_FJ_TYPE = 19; // 分级基金净值
	public static final int GET_URL_JIJIN_HB_TYPE = 71; // 货币性基金收益
	/***************** 基金平台数据类型  end*******************/
}
