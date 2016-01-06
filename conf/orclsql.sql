## zzj 2015/01/06
drop table T_MAIL_BALANCE_DETAIL cascade constraints;
-- Create table
create table T_MAIL_BALANCE_DETAIL
(
  ID               NUMBER(20) not null,
  BALANCE          NUMBER(10,2),
  NEW_CHARGES      NUMBER(10,2),
  PAYMENT          NUMBER(10,2),
  ADJUSTMENT       NUMBER(10,2),
  INTEREST         NUMBER(10,2),
  NEW_BALANCE      NUMBER(10,2),
  BILL_CYCLE_PK_ID NUMBER(20),
  DETAIL           NVARCHAR2(50),
  CURRENCY_TYPE    NVARCHAR2(10)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
-- Add comments to the table 
comment on table T_MAIL_BALANCE_DETAIL
  is '本期应还总额信息详情';
-- Add comments to the columns 
comment on column T_MAIL_BALANCE_DETAIL.ID
  is '主键id';
comment on column T_MAIL_BALANCE_DETAIL.BALANCE
  is '上期账单金额';
comment on column T_MAIL_BALANCE_DETAIL.NEW_CHARGES
  is '本期账单金额';
comment on column T_MAIL_BALANCE_DETAIL.PAYMENT
  is '上期还款金额';
comment on column T_MAIL_BALANCE_DETAIL.ADJUSTMENT
  is '本期调整金额';
comment on column T_MAIL_BALANCE_DETAIL.INTEREST
  is '循环利息';
comment on column T_MAIL_BALANCE_DETAIL.NEW_BALANCE
  is '本期应缴余额';
comment on column T_MAIL_BALANCE_DETAIL.BILL_CYCLE_PK_ID
  is '账单周期表主键id';
comment on column T_MAIL_BALANCE_DETAIL.DETAIL
  is '描述信息';
comment on column T_MAIL_BALANCE_DETAIL.CURRENCY_TYPE
  is '币种';
-- Create/Recreate primary, unique and foreign key constraints 
alter table T_MAIL_BALANCE_DETAIL
  add constraint PK_T_MAIL_BALANCE_DETAIL primary key (ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

drop table T_MAIL_BILL_BANK_DAY_DETAIL cascade constraints;
-- Create table
create table T_MAIL_BILL_BANK_DAY_DETAIL
(
  ID                 NUMBER(20) not null,
  IS_MASTER          INTEGER,
  CARD_END_OF_FOUR   NVARCHAR2(4),
  MERCHANDISE_DATE   DATE,
  MERCHANDISE_TIME   DATE,
  CURRENCY_TYPE      NVARCHAR2(10),
  MERCHANDISE_AMOUNT NUMBER(10,2),
  MERCHANDISE_DETAIL NVARCHAR2(100),
  DETAIL             NVARCHAR2(100),
  BILL_CYCLE_PK_ID   NUMBER(20),
  VICE_CARD          NVARCHAR2(4),
  INCOME_OR_PAY      INTEGER default 0
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
-- Add comments to the table 
comment on table T_MAIL_BILL_BANK_DAY_DETAIL
  is '邮件账单日账单明细';
-- Add comments to the columns 
comment on column T_MAIL_BILL_BANK_DAY_DETAIL.ID
  is '主键id';
comment on column T_MAIL_BILL_BANK_DAY_DETAIL.IS_MASTER
  is '主/附属卡（1：主卡；0：附属卡）';
comment on column T_MAIL_BILL_BANK_DAY_DETAIL.CARD_END_OF_FOUR
  is '卡号末四位';
comment on column T_MAIL_BILL_BANK_DAY_DETAIL.MERCHANDISE_DATE
  is '交易日期';
comment on column T_MAIL_BILL_BANK_DAY_DETAIL.MERCHANDISE_TIME
  is '交易时间';
comment on column T_MAIL_BILL_BANK_DAY_DETAIL.CURRENCY_TYPE
  is '币种';
comment on column T_MAIL_BILL_BANK_DAY_DETAIL.MERCHANDISE_AMOUNT
  is '交易金额';
comment on column T_MAIL_BILL_BANK_DAY_DETAIL.MERCHANDISE_DETAIL
  is '交易摘要';
comment on column T_MAIL_BILL_BANK_DAY_DETAIL.DETAIL
  is '信息描述';
comment on column T_MAIL_BILL_BANK_DAY_DETAIL.BILL_CYCLE_PK_ID
  is '账单周期表主键';
comment on column T_MAIL_BILL_BANK_DAY_DETAIL.VICE_CARD
  is '附属卡号（有附属卡必填）';
comment on column T_MAIL_BILL_BANK_DAY_DETAIL.INCOME_OR_PAY
  is '是否为收入，1收入0支出（默认为0）';
-- Create/Recreate primary, unique and foreign key constraints 
alter table T_MAIL_BILL_BANK_DAY_DETAIL
  add constraint PK_T_MAIL_BILL_BANK_DAY_DETAIL primary key (ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

drop table T_MAIL_BILL_BANK_MONTH_DETAIL cascade constraints;
-- Create table
create table T_MAIL_BILL_BANK_MONTH_DETAIL
(
  ID                    NUMBER(20) not null,
  IS_MASTER             INTEGER,
  CARD_END_OF_FOUR      NVARCHAR2(4),
  MERCHANDISE_DATE      DATE,
  POST_DATE             DATE,
  MERCHANDISE_DETAIL    NVARCHAR2(100),
  CURRENCY_TYPE         NVARCHAR2(10),
  AMOUNT                NUMBER(10,2),
  MERCHANDISE_AREA      NVARCHAR2(50),
  ORIGINAL_TRANS_AMOUNT NUMBER(10,2),
  DETAIL                NVARCHAR2(100),
  BILL_CYCLE_PK_ID      NUMBER(20) not null,
  INCOME_OR_PAY         INTEGER default 0
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
-- Add comments to the table 
comment on table T_MAIL_BILL_BANK_MONTH_DETAIL
  is '邮件账单月账单明细';
-- Add comments to the columns 
comment on column T_MAIL_BILL_BANK_MONTH_DETAIL.ID
  is '主键id';
comment on column T_MAIL_BILL_BANK_MONTH_DETAIL.IS_MASTER
  is '主/附属卡（1：主卡；0：附属卡）';
comment on column T_MAIL_BILL_BANK_MONTH_DETAIL.CARD_END_OF_FOUR
  is '卡号末四位';
comment on column T_MAIL_BILL_BANK_MONTH_DETAIL.MERCHANDISE_DATE
  is '交易日（明细）';
comment on column T_MAIL_BILL_BANK_MONTH_DETAIL.POST_DATE
  is '记账日（明细）';
comment on column T_MAIL_BILL_BANK_MONTH_DETAIL.MERCHANDISE_DETAIL
  is '交易摘要（明细）';
comment on column T_MAIL_BILL_BANK_MONTH_DETAIL.CURRENCY_TYPE
  is '币种';
comment on column T_MAIL_BILL_BANK_MONTH_DETAIL.AMOUNT
  is '交易金额（明细）';
comment on column T_MAIL_BILL_BANK_MONTH_DETAIL.MERCHANDISE_AREA
  is '交易地点（明细）';
comment on column T_MAIL_BILL_BANK_MONTH_DETAIL.ORIGINAL_TRANS_AMOUNT
  is '交易地金额（明细）';
comment on column T_MAIL_BILL_BANK_MONTH_DETAIL.DETAIL
  is '信息描述';
comment on column T_MAIL_BILL_BANK_MONTH_DETAIL.BILL_CYCLE_PK_ID
  is '账单周期表主键';
comment on column T_MAIL_BILL_BANK_MONTH_DETAIL.INCOME_OR_PAY
  is '收入或者支出标示（1：收入，0：支出）';
-- Create/Recreate primary, unique and foreign key constraints 
alter table T_MAIL_BILL_BANK_MONTH_DETAIL
  add constraint PK_T_MAIL_BILL_BANK_MONTH_DETA primary key (ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

drop table T_MAIL_BILL_CYCLE_INFO cascade constraints;
-- Create table
create table T_MAIL_BILL_CYCLE_INFO
(
  ID                     NUMBER(20) not null,
  SENDER_URL             NVARCHAR2(50),
  BANK_ID                NUMBER(5),
  CARD_TYPE              NVARCHAR2(1),
  BILL_TYPE              INTEGER,
  BILL_CYCLE_BEGIN       DATE,
  BILL_CYCLE_END         DATE,
  BILL_DATE              NVARCHAR2(2),
  ACCOUNT_OF_DATE        NVARCHAR2(8),
  USA_CREDIT_LIMIT       NUMBER(10,2),
  NEW_RMB_BALANCE        NUMBER(10,2),
  NEW_USA_BALANCE        NUMBER(10,2),
  MIN_RMB_PAYMENT        NUMBER(10,2),
  MIN_USA_PAYMENT        NUMBER(10,2),
  PAST_DUE_AMOUNT        NUMBER(10,2),
  PAYMENT_DUE_DATE       DATE,
  CASH_USA_ADVANCE_LIMIT NUMBER(10,2),
  USA_INTEGRATION        NUMBER(10),
  OLD_HTML_URL           NVARCHAR2(300),
  NEW_HTML_URL           NVARCHAR2(300),
  INFO_SOURCE            NVARCHAR2(50),
  DETAIL                 NVARCHAR2(100),
  IS_BILL                INTEGER,
  IS_PUSH                INTEGER default 0 not null,
  RMB_CREDIT_LIMIT       NUMBER(10,2),
  RMB_INTEGRATION        NUMBER(10),
  CASH_RMB_ADVANCE_LIMIT NUMBER(10,2),
  BANK_BILL_TYPE         NVARCHAR2(2),
  USER_NAME              NVARCHAR2(20),
  USER_GENDER            NVARCHAR2(4),
  CARD_END_OF_FOUR       NVARCHAR2(4),
  NEW_HTML_DFS           NVARCHAR2(80),
  OLD_HTML_DFS           NVARCHAR2(80),
  RECEIVE_ADD_URL        NVARCHAR2(50),
  SENT_DATA              DATE,
  SUBJECT                NVARCHAR2(100),
  ACCOUNT_ID             NUMBER(20),
  SC_VERSION             NUMBER(20)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 8K
    minextents 1
    maxextents unlimited
  );
-- Add comments to the table 
comment on table T_MAIL_BILL_CYCLE_INFO
  is '邮件账单周期表';
-- Add comments to the columns 
comment on column T_MAIL_BILL_CYCLE_INFO.ID
  is '主键id';
comment on column T_MAIL_BILL_CYCLE_INFO.SENDER_URL
  is '用户发件人地址';
comment on column T_MAIL_BILL_CYCLE_INFO.BANK_ID
  is '银行类型（银行主键id）';
comment on column T_MAIL_BILL_CYCLE_INFO.CARD_TYPE
  is '银行卡品种（如：VISA。。。）';
comment on column T_MAIL_BILL_CYCLE_INFO.BILL_TYPE
  is '账单类型(1 月账单;  2 日账单;)';
comment on column T_MAIL_BILL_CYCLE_INFO.BILL_CYCLE_BEGIN
  is '账单周期开始日期';
comment on column T_MAIL_BILL_CYCLE_INFO.BILL_CYCLE_END
  is '账单周期结束日期';
comment on column T_MAIL_BILL_CYCLE_INFO.BILL_DATE
  is '账单日';
comment on column T_MAIL_BILL_CYCLE_INFO.ACCOUNT_OF_DATE
  is '月账单时为账期（yyyymm），日账单时为消费日期';
comment on column T_MAIL_BILL_CYCLE_INFO.USA_CREDIT_LIMIT
  is '美元信用额度';
comment on column T_MAIL_BILL_CYCLE_INFO.NEW_RMB_BALANCE
  is '本期应还人民币总额';
comment on column T_MAIL_BILL_CYCLE_INFO.NEW_USA_BALANCE
  is '本期应还美元总额';
comment on column T_MAIL_BILL_CYCLE_INFO.MIN_RMB_PAYMENT
  is '本期最低人民币还款额';
comment on column T_MAIL_BILL_CYCLE_INFO.MIN_USA_PAYMENT
  is '本期最低美元还款额';
comment on column T_MAIL_BILL_CYCLE_INFO.PAST_DUE_AMOUNT
  is '逾期还款额（目前只是浦发）';
comment on column T_MAIL_BILL_CYCLE_INFO.PAYMENT_DUE_DATE
  is '到期还款日';
comment on column T_MAIL_BILL_CYCLE_INFO.CASH_USA_ADVANCE_LIMIT
  is '预借美元现金额度';
comment on column T_MAIL_BILL_CYCLE_INFO.USA_INTEGRATION
  is '美元可用积分余额';
comment on column T_MAIL_BILL_CYCLE_INFO.OLD_HTML_URL
  is '原邮件HTML访问地址';
comment on column T_MAIL_BILL_CYCLE_INFO.NEW_HTML_URL
  is '截取拼接后HTML访问地址';
comment on column T_MAIL_BILL_CYCLE_INFO.INFO_SOURCE
  is '信息来源的邮箱地址';
comment on column T_MAIL_BILL_CYCLE_INFO.DETAIL
  is '信息描述';
comment on column T_MAIL_BILL_CYCLE_INFO.IS_BILL
  is '是否为账单（0：否；1：是）';
comment on column T_MAIL_BILL_CYCLE_INFO.IS_PUSH
  is '是否推送到app（0：否；1：是）默认是0';
comment on column T_MAIL_BILL_CYCLE_INFO.RMB_CREDIT_LIMIT
  is '人民币信用额度';
comment on column T_MAIL_BILL_CYCLE_INFO.RMB_INTEGRATION
  is '人民币可用积分余额';
comment on column T_MAIL_BILL_CYCLE_INFO.CASH_RMB_ADVANCE_LIMIT
  is '预借人民币现金额度';
comment on column T_MAIL_BILL_CYCLE_INFO.BANK_BILL_TYPE
  is '详细的账单类型（如：招行新版账单、招行旧版账单）';
comment on column T_MAIL_BILL_CYCLE_INFO.USER_NAME
  is '用户的姓名';
comment on column T_MAIL_BILL_CYCLE_INFO.USER_GENDER
  is '用户的称谓（先生、女士）';
comment on column T_MAIL_BILL_CYCLE_INFO.CARD_END_OF_FOUR
  is '卡号末四位（主卡）';
comment on column T_MAIL_BILL_CYCLE_INFO.NEW_HTML_DFS
  is 'DFS分布式文件管理上的截取拼接后邮件HTML存储地址';
comment on column T_MAIL_BILL_CYCLE_INFO.OLD_HTML_DFS
  is 'DFS分布式文件管理上的原始邮件HTML存储地址';
comment on column T_MAIL_BILL_CYCLE_INFO.RECEIVE_ADD_URL
  is '用户收件人地址';
comment on column T_MAIL_BILL_CYCLE_INFO.SENT_DATA
  is '用户发件时间';
comment on column T_MAIL_BILL_CYCLE_INFO.SUBJECT
  is '邮件主题';
comment on column T_MAIL_BILL_CYCLE_INFO.ACCOUNT_ID
  is '用户账号';
comment on column T_MAIL_BILL_CYCLE_INFO.SC_VERSION
  is '服务端数据版本号';
-- Create/Recreate primary, unique and foreign key constraints 
alter table T_MAIL_BILL_CYCLE_INFO
  add constraint PK_T_MAIL_BILL_CYCLE_INFO primary key (ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
alter table T_MAIL_BILL_CYCLE_INFO
  add constraint UNIQUE_CARD unique (INFO_SOURCE, CARD_END_OF_FOUR, BANK_BILL_TYPE, ACCOUNT_OF_DATE, USER_NAME)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

drop table T_MAIL_NOT_BILL_CYCLE_INFO cascade constraints;
-- Create table
create table T_MAIL_NOT_BILL_CYCLE_INFO
(
  ID              NUMBER(20),
  SENDER_URL      NVARCHAR2(50),
  OLD_HTML_URL    NVARCHAR2(300),
  NEW_HTML_URL    NVARCHAR2(300),
  INFO_SOURCE     NVARCHAR2(50),
  DETAIL          NVARCHAR2(100),
  IS_BILL         INTEGER,
  IS_PUSH         INTEGER default 0 not null,
  NEW_HTML_DFS    NVARCHAR2(80),
  OLD_HTML_DFS    NVARCHAR2(80),
  RECEIVE_ADD_URL NVARCHAR2(50),
  SENT_DATA       DATE,
  SUBJECT         NVARCHAR2(100),
  ACCOUNT_ID      NUMBER(20)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 8K
    minextents 1
    maxextents unlimited
  );
-- Add comments to the table 
comment on table T_MAIL_NOT_BILL_CYCLE_INFO
  is '非账单存储表内容';
-- Add comments to the columns 
comment on column T_MAIL_NOT_BILL_CYCLE_INFO.ID
  is '主键Id';
comment on column T_MAIL_NOT_BILL_CYCLE_INFO.SENDER_URL
  is '用户发件人地址';
comment on column T_MAIL_NOT_BILL_CYCLE_INFO.OLD_HTML_URL
  is '原邮件HTML访问地址';
comment on column T_MAIL_NOT_BILL_CYCLE_INFO.NEW_HTML_URL
  is '截取拼接后HTML访问地址';
comment on column T_MAIL_NOT_BILL_CYCLE_INFO.INFO_SOURCE
  is '信息来源的邮箱地址';
comment on column T_MAIL_NOT_BILL_CYCLE_INFO.DETAIL
  is '信息描述';
comment on column T_MAIL_NOT_BILL_CYCLE_INFO.IS_BILL
  is '是否为账单（0：否；1：是）';
comment on column T_MAIL_NOT_BILL_CYCLE_INFO.IS_PUSH
  is '是否推送到app（0：否；1：是）默认是0';
comment on column T_MAIL_NOT_BILL_CYCLE_INFO.NEW_HTML_DFS
  is 'DFS分布式文件管理上的截取拼接后邮件HTML存储地址';
comment on column T_MAIL_NOT_BILL_CYCLE_INFO.OLD_HTML_DFS
  is 'DFS分布式文件管理上的原始邮件HTML存储地址';
comment on column T_MAIL_NOT_BILL_CYCLE_INFO.RECEIVE_ADD_URL
  is '用户收件人地址';
comment on column T_MAIL_NOT_BILL_CYCLE_INFO.SENT_DATA
  is '用户发件时间';
comment on column T_MAIL_NOT_BILL_CYCLE_INFO.SUBJECT
  is '邮件主题';
comment on column T_MAIL_NOT_BILL_CYCLE_INFO.ACCOUNT_ID
  is '用户账号';

  

  
drop table T_MAIL_BILL_USER_INFO cascade constraints;
-- Create table
create table T_MAIL_BILL_USER_INFO
(
  ID             NUMBER(20) not null,
  EAMIL_URL      NVARCHAR2(50),
  PASSWORD       NVARCHAR2(100),
  ACCOUNT_ID     NUMBER(20),
  BINDING_DATE   DATE,
  DETAIL         NVARCHAR2(100),
  BINDING_STATUS NUMBER(1) default 1
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 8K
    minextents 1
    maxextents unlimited
  );
-- Add comments to the table 
comment on table T_MAIL_BILL_USER_INFO
  is '邮件账单用户信息';
-- Add comments to the columns 
comment on column T_MAIL_BILL_USER_INFO.ID
  is '主键id';
comment on column T_MAIL_BILL_USER_INFO.EAMIL_URL
  is '邮箱地址';
comment on column T_MAIL_BILL_USER_INFO.PASSWORD
  is '邮箱密码';
comment on column T_MAIL_BILL_USER_INFO.ACCOUNT_ID
  is '用户标示';
comment on column T_MAIL_BILL_USER_INFO.BINDING_DATE
  is '绑定时间';
comment on column T_MAIL_BILL_USER_INFO.DETAIL
  is '描述信息';
comment on column T_MAIL_BILL_USER_INFO.BINDING_STATUS
  is '邮箱绑定状态（1绑定，0解绑，默认1）';
-- Create/Recreate primary, unique and foreign key constraints 
alter table T_MAIL_BILL_USER_INFO
  add constraint PK_T_MAIL_BILL_USER_INFO primary key (ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

drop table T_MAIL_INTEGRATION_DETAIL cascade constraints;
-- Create table
create table T_MAIL_INTEGRATION_DETAIL
(
  ID                       NUMBER(20) not null,
  BALANCE_POINTS           INTEGER,
  ADDED_POINTS             INTEGER,
  REVISE_POINTS            INTEGER,
  AWARD_POINTS             INTEGER,
  EXCHANGE_POINTS          INTEGER,
  USE_POINTS               INTEGER,
  BILL_CYCLE_PK_ID         NUMBER(20),
  DETAIL                   NVARCHAR2(50),
  CURRENCY_TYPE            NVARCHAR2(10),
  TOURISM_POINTS           INTEGER,
  IMPENDING_FAILURE_POINTS NVARCHAR2(100)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
-- Add comments to the table 
comment on table T_MAIL_INTEGRATION_DETAIL
  is '本期积分汇总详情';
-- Add comments to the columns 
comment on column T_MAIL_INTEGRATION_DETAIL.ID
  is '主键id';
comment on column T_MAIL_INTEGRATION_DETAIL.BALANCE_POINTS
  is '上期积分余额';
comment on column T_MAIL_INTEGRATION_DETAIL.ADDED_POINTS
  is '本期新增积分';
comment on column T_MAIL_INTEGRATION_DETAIL.REVISE_POINTS
  is '本期调整积分';
comment on column T_MAIL_INTEGRATION_DETAIL.AWARD_POINTS
  is '本期奖励积分';
comment on column T_MAIL_INTEGRATION_DETAIL.EXCHANGE_POINTS
  is '本期兑换积分总数';
comment on column T_MAIL_INTEGRATION_DETAIL.USE_POINTS
  is '可用积分余额';
comment on column T_MAIL_INTEGRATION_DETAIL.BILL_CYCLE_PK_ID
  is '账单周期表主键';
comment on column T_MAIL_INTEGRATION_DETAIL.DETAIL
  is '描述信息';
comment on column T_MAIL_INTEGRATION_DETAIL.CURRENCY_TYPE
  is '币种';
comment on column T_MAIL_INTEGRATION_DETAIL.TOURISM_POINTS
  is '旅游积分';
comment on column T_MAIL_INTEGRATION_DETAIL.IMPENDING_FAILURE_POINTS
  is '即将失效积分';
-- Create/Recreate primary, unique and foreign key constraints 
alter table T_MAIL_INTEGRATION_DETAIL
  add constraint PK_T_MAIL_INTEGRATION_DETAIL primary key (ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
  
drop table T_MAIL_LEAD_INFO cascade constraints;
-- Create table
create table T_MAIL_LEAD_INFO
(
  ID            NUMBER(20) not null,
  EMAIL_URL     NVARCHAR2(50),
  POP3_IS_OPEN  INTEGER,
  IMAP4_IS_OPEN INTEGER,
  DETAIL        NVARCHAR2(100)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
-- Add comments to the table 
comment on table T_MAIL_LEAD_INFO
  is '邮箱引导统计信息(pop3/imap4的开启提示)';
-- Add comments to the columns 
comment on column T_MAIL_LEAD_INFO.ID
  is '主键id';
comment on column T_MAIL_LEAD_INFO.EMAIL_URL
  is '邮箱地址';
comment on column T_MAIL_LEAD_INFO.POP3_IS_OPEN
  is 'pop3协议是否需要开启（1：需要；0：不需要）';
comment on column T_MAIL_LEAD_INFO.IMAP4_IS_OPEN
  is 'IMAP4协议是否需要开启（1：需要；0：不需要）';
comment on column T_MAIL_LEAD_INFO.DETAIL
  is '描述信息';
-- Create/Recreate primary, unique and foreign key constraints 
alter table T_MAIL_LEAD_INFO
  add constraint PK_T_MAIL_LEAD_INFO primary key (ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

drop table T_MAIL_SYSTEM_NOT_SPPORT_INFO cascade constraints;
-- Create table
create table T_MAIL_SYSTEM_NOT_SPPORT_INFO
(
  ID             NUMBER(20) not null,
  EMAIL_SUFFIX   NVARCHAR2(15),
  BINDING_DATE   DATE,
  BINDING_NUMBER INTEGER,
  DETAIL         NVARCHAR2(50)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
-- Add comments to the table 
comment on table T_MAIL_SYSTEM_NOT_SPPORT_INFO
  is '系统未兼容的邮箱';
-- Add comments to the columns 
comment on column T_MAIL_SYSTEM_NOT_SPPORT_INFO.ID
  is '主键id';
comment on column T_MAIL_SYSTEM_NOT_SPPORT_INFO.EMAIL_SUFFIX
  is '注册邮箱的后缀';
comment on column T_MAIL_SYSTEM_NOT_SPPORT_INFO.BINDING_DATE
  is '最后绑定的日期';
comment on column T_MAIL_SYSTEM_NOT_SPPORT_INFO.BINDING_NUMBER
  is '绑定失败次数';
comment on column T_MAIL_SYSTEM_NOT_SPPORT_INFO.DETAIL
  is '描述信息';
-- Create/Recreate primary, unique and foreign key constraints 
alter table T_MAIL_SYSTEM_NOT_SPPORT_INFO
  add constraint PK_T_MAIL_SYSTEM_NOT_SPPORT_IN primary key (ID)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );

drop table T_MAIL_BILL_JOB cascade constraints;
-- Create table
create table T_MAIL_BILL_JOB
(
  ID               NUMBER not null,
  BANK_ID          NUMBER(5) not null,
  CARD_END_OF_FOUR NVARCHAR2(4) not null,
  BILL_DATE        NVARCHAR2(2),
  BILL_TYPE        NVARCHAR2(1) not null,
  USER_EMAIL       NVARCHAR2(50) not null,
  B1               NVARCHAR2(50),
  B2               NVARCHAR2(50),
  B3               NVARCHAR2(50)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64k
    next 8
    minextents 1
    maxextents unlimited
  );
-- Add comments to the table 
comment on table T_MAIL_BILL_JOB
  is '定时任务表';
-- Add comments to the columns 
comment on column T_MAIL_BILL_JOB.ID
  is '主键id';
comment on column T_MAIL_BILL_JOB.BANK_ID
  is '银行标示';
comment on column T_MAIL_BILL_JOB.CARD_END_OF_FOUR
  is '卡号末四位';
comment on column T_MAIL_BILL_JOB.BILL_DATE
  is '账单日';
comment on column T_MAIL_BILL_JOB.BILL_TYPE
  is '账单类型';
comment on column T_MAIL_BILL_JOB.USER_EMAIL
  is '用户Email地址';
comment on column T_MAIL_BILL_JOB.B1
  is '备用';
comment on column T_MAIL_BILL_JOB.B2
  is '备用';
comment on column T_MAIL_BILL_JOB.B3
  is '备用';


drop table T_MAIL_BILL_JOB_TEMP cascade constraints;
-- Create table
create table T_MAIL_BILL_JOB_TEMP
(
  ID               NUMBER not null,
  ACCOUNT_ID       NUMBER(20) not null,
  BANK_ID          NUMBER(5) not null,
  CARD_END_OF_FOUR NVARCHAR2(4) not null,
  USER_EMAIL       NVARCHAR2(50) not null,
  BILL_DATE        NVARCHAR2(2),
  BILL_TYPE        NVARCHAR2(1) not null,
  BILL_START_DATE  DATE not null,
  BILL_END_DATE    DATE,
  IS_VAILD         NUMBER(1) default 1 not null,
  B1               NVARCHAR2(50),
  B2               NVARCHAR2(50),
  B3               NVARCHAR2(50)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
-- Add comments to the table 
comment on table T_MAIL_BILL_JOB_TEMP
  is '任务的临时表';
-- Add comments to the columns 
comment on column T_MAIL_BILL_JOB_TEMP.ID
  is '主键id';
comment on column T_MAIL_BILL_JOB_TEMP.ACCOUNT_ID
  is '用户标示';
comment on column T_MAIL_BILL_JOB_TEMP.BANK_ID
  is '银行id';
comment on column T_MAIL_BILL_JOB_TEMP.CARD_END_OF_FOUR
  is '卡号末四位';
comment on column T_MAIL_BILL_JOB_TEMP.USER_EMAIL
  is '用户Email地址';
comment on column T_MAIL_BILL_JOB_TEMP.BILL_DATE
  is '账单日';
comment on column T_MAIL_BILL_JOB_TEMP.BILL_TYPE
  is '账单类型（1：月账单；2：日账单）';
comment on column T_MAIL_BILL_JOB_TEMP.BILL_START_DATE
  is '搜索账单开始时间';
comment on column T_MAIL_BILL_JOB_TEMP.BILL_END_DATE
  is '最近一次搜索账单时间';
comment on column T_MAIL_BILL_JOB_TEMP.IS_VAILD
  is '搜索账单是否有效(1：有效；2：无效)';
comment on column T_MAIL_BILL_JOB_TEMP.B1
  is '备用';
comment on column T_MAIL_BILL_JOB_TEMP.B2
  is '备用';
comment on column T_MAIL_BILL_JOB_TEMP.B3
  is '备用';

drop table T_MAIL_BILL_LOG cascade constraints;
-- Create table
create table T_MAIL_BILL_LOG
(
  ID               NUMBER(20) not null,
  EMAIL_URL        NVARCHAR2(50) not null,
  PHONE_ID         NVARCHAR2(100),
  LOGIN_TIME       DATE,
  ACCOUNT_ID       NUMBER(20),
  LOG_ON           NUMBER(1),
  FILTERS_NUMBER   NVARCHAR2(4),
  FORWARD_RESULTS  NVARCHAR2(1),
  DOWNLOAD_NUMBER  NVARCHAR2(4),
  ANALYSIS_NUMBER  NVARCHAR2(4),
  EXCEPTION_NUMBER NVARCHAR2(3),
  LOGOUT_TIME      DATE
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
-- Add comments to the columns 
comment on column T_MAIL_BILL_LOG.ID
  is '主键id';
comment on column T_MAIL_BILL_LOG.EMAIL_URL
  is '邮箱地址';
comment on column T_MAIL_BILL_LOG.PHONE_ID
  is '每个手机的唯一标示';
comment on column T_MAIL_BILL_LOG.LOGIN_TIME
  is '登陆邮箱开始时间';
comment on column T_MAIL_BILL_LOG.ACCOUNT_ID
  is '用户唯一标识';
comment on column T_MAIL_BILL_LOG.LOG_ON
  is '登陆邮箱失败或成功（1：成功；2失败）';
comment on column T_MAIL_BILL_LOG.FILTERS_NUMBER
  is '过滤到的疑似邮件账单数';
comment on column T_MAIL_BILL_LOG.FORWARD_RESULTS
  is '转发规则的添加是否成功（1：成功；2失败）';
comment on column T_MAIL_BILL_LOG.DOWNLOAD_NUMBER
  is '下载的邮件账单数目';
comment on column T_MAIL_BILL_LOG.ANALYSIS_NUMBER
  is '解析的邮件账单数目';
comment on column T_MAIL_BILL_LOG.EXCEPTION_NUMBER
  is '所抛出的异常数目';
comment on column T_MAIL_BILL_LOG.LOGOUT_TIME
  is '操作完成后最后推送数据时间';

drop table T_MAIL_FORWARD_YXB_MAIL cascade constraints;
-- Create table
create table T_MAIL_FORWARD_YXB_MAIL
(
  ID                NUMBER(20) not null,
  MAIL_NAME         NVARCHAR2(50) not null,
  YXB_MAIL_NAME     NVARCHAR2(50),
  YXB_MAIL_PASSWORD NVARCHAR2(50),
  ACCOUNT_ID        NUMBER(20),
  CREATE_TIME       DATE not null
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
-- Add comments to the table 
comment on table T_MAIL_FORWARD_YXB_MAIL
  is '自建邮箱服务的关系表';
-- Add comments to the columns 
comment on column T_MAIL_FORWARD_YXB_MAIL.ID
  is '主键id';
comment on column T_MAIL_FORWARD_YXB_MAIL.MAIL_NAME
  is '个人邮箱';
comment on column T_MAIL_FORWARD_YXB_MAIL.YXB_MAIL_NAME
  is '自建邮箱';
comment on column T_MAIL_FORWARD_YXB_MAIL.YXB_MAIL_PASSWORD
  is '自建邮箱密码';
comment on column T_MAIL_FORWARD_YXB_MAIL.ACCOUNT_ID
  is '用户id';
comment on column T_MAIL_FORWARD_YXB_MAIL.CREATE_TIME
  is '时间';
-- Create/Recreate primary, unique and foreign key constraints 
alter table T_MAIL_FORWARD_YXB_MAIL
  add constraint MAIL_NAME unique (MAIL_NAME)
  using index 
  tablespace USERS
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );

drop table T_MAIL_BILL_PUSH cascade constraints;
-- Create table
create table T_MAIL_BILL_PUSH
(
  ID                NUMBER(20) not null,
  ACCOUNT_ID        NUMBER(20),
  BILL_CYCLE_PK_IDS NVARCHAR2(500),
  B3                NVARCHAR2(100),
  CREATE_TIME       DATE not null,
  IS_PUSH           NVARCHAR2(1) default 0 not null
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
-- Add comments to the table 
comment on table T_MAIL_BILL_PUSH
  is '推送关系表';
-- Add comments to the columns 
comment on column T_MAIL_BILL_PUSH.ID
  is '主键Id';
comment on column T_MAIL_BILL_PUSH.ACCOUNT_ID
  is '用户唯一标示';
comment on column T_MAIL_BILL_PUSH.BILL_CYCLE_PK_IDS
  is '所推送的数据的Id';
comment on column T_MAIL_BILL_PUSH.B3
  is '备用';
comment on column T_MAIL_BILL_PUSH.CREATE_TIME
  is '数据入库时间';
comment on column T_MAIL_BILL_PUSH.IS_PUSH
  is '0:未推送；1：已推送（默认0）';

drop table T_MAIL_BILL_MAIN_VICE_CARD cascade constraints;
-- Create table
create table T_MAIL_BILL_MAIN_VICE_CARD
(
  ID                NUMBER(20) not null,
  BANK_ID           NUMBER(5),
  ACCOUNT_ID        NUMBER(20),
  INFO_SOURCE_EMAIL NVARCHAR2(100),
  MAIN_CARD_OF_FOUR NUMBER(4),
  VICE_CARD_OF_FOUR NVARCHAR2(50),
  CREATE_TIME       DATE not null,
  UPDATE_TIME       DATE
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
-- Add comments to the table 
comment on table T_MAIL_BILL_MAIN_VICE_CARD
  is '主副卡表关系';
-- Add comments to the columns 
comment on column T_MAIL_BILL_MAIN_VICE_CARD.ID
  is '主键id';
comment on column T_MAIL_BILL_MAIN_VICE_CARD.BANK_ID
  is '银行id';
comment on column T_MAIL_BILL_MAIN_VICE_CARD.ACCOUNT_ID
  is '用户账号';
comment on column T_MAIL_BILL_MAIN_VICE_CARD.INFO_SOURCE_EMAIL
  is '邮箱来源（用户登陆地址）';
comment on column T_MAIL_BILL_MAIN_VICE_CARD.MAIN_CARD_OF_FOUR
  is '主卡卡号';
comment on column T_MAIL_BILL_MAIN_VICE_CARD.VICE_CARD_OF_FOUR
  is '副卡卡号';
comment on column T_MAIL_BILL_MAIN_VICE_CARD.CREATE_TIME
  is '主副卡创建时间';
comment on column T_MAIL_BILL_MAIN_VICE_CARD.UPDATE_TIME
  is '关系更新时间';

drop table T_MAIL_BILL_CARD_INFO cascade constraints;
-- Create table
create table T_MAIL_BILL_CARD_INFO
(
  ID                     NUMBER(20) not null,
  BANK_ID                NUMBER(5),
  ACCOUNT_ID             NUMBER(20),
  INFO_SOURCE_EMAIL      NVARCHAR2(100),
  MAIN_CARD_OF_FOUR      NUMBER(4),
  BILL_DATE              NVARCHAR2(2),
  PAYMENT_DUE_DATE       DATE,
  RMB_CREDIT_LIMIT       NUMBER(10,2),
  USA_CREDIT_LIMIT       NUMBER(10,2),
  CARD_TYPE              NVARCHAR2(1),
  CARD_SPECIES           NVARCHAR2(1),
  USER_NAME              NVARCHAR2(20),
  USER_GENDER            NVARCHAR2(4),
  CASH_USA_ADVANCE_LIMIT NUMBER(10,2),
  CASH_RMB_ADVANCE_LIMIT NUMBER(10,2)
)
tablespace USERS
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 8K
    minextents 1
    maxextents unlimited
  );
-- Add comments to the columns 
comment on column T_MAIL_BILL_CARD_INFO.ID
  is '主键id';
comment on column T_MAIL_BILL_CARD_INFO.BANK_ID
  is '银行id';
comment on column T_MAIL_BILL_CARD_INFO.ACCOUNT_ID
  is '用户账号';
comment on column T_MAIL_BILL_CARD_INFO.INFO_SOURCE_EMAIL
  is '邮箱来源（用户登陆地址）';
comment on column T_MAIL_BILL_CARD_INFO.MAIN_CARD_OF_FOUR
  is '主卡卡号';
comment on column T_MAIL_BILL_CARD_INFO.BILL_DATE
  is '账单日';
comment on column T_MAIL_BILL_CARD_INFO.PAYMENT_DUE_DATE
  is '到期还款日';
comment on column T_MAIL_BILL_CARD_INFO.RMB_CREDIT_LIMIT
  is '人民币信用额度';
comment on column T_MAIL_BILL_CARD_INFO.USA_CREDIT_LIMIT
  is '美元信用额度';
comment on column T_MAIL_BILL_CARD_INFO.CARD_TYPE
  is '银行卡品牌（如：VISA。。。）';
comment on column T_MAIL_BILL_CARD_INFO.CARD_SPECIES
  is '卡种';
comment on column T_MAIL_BILL_CARD_INFO.USER_NAME
  is '用户的姓名';
comment on column T_MAIL_BILL_CARD_INFO.USER_GENDER
  is '用户的称谓（先生、女士）';
comment on column T_MAIL_BILL_CARD_INFO.CASH_USA_ADVANCE_LIMIT
  is '预借美元现金额度';
comment on column T_MAIL_BILL_CARD_INFO.CASH_RMB_ADVANCE_LIMIT
  is '预借人民币现金额度';




-- Create sequence
--删除
Drop Sequence Mail_Bill_Sequence;
--创建
create sequence MAIL_BILL_SEQUENCE
minvalue 1
maxvalue 999999999999999999999999999
start with 1
increment by 1
Cache 20
order;

--删除
Drop Sequence Seq_Cycle_Info_Sc_Version;
--创建
create sequence SEQ_CYCLE_INFO_SC_VERSION
Minvalue 1
maxvalue 99999999999999999999999999
start with 1
increment by 1
nocache
order;