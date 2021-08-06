drop table if exists fw_tenant;
create table fw_tenant --'租户管理'
(
  id                   integer not null primary key autoincrement,  --  '主键id'
  name                 varchar(100) not null, --  '租户名称'
  code                 varchar(20) UNIQUE not null, --  '编码'
  remark               varchar(500), --  '备注'
  delFlag                int not null, --  '是否删除'
  createBy             integer not null, --  '创建人'
  createTime           datetime not null, --  '创建时间'
  lastUpdateBy         integer not null, --  '最后更新人'
  lastUpdateTime       datetime not null, --  '最后更新时间'
  enable               tinyint not null --  '是否有效'
);

drop table if exists fw_api_auth;
create table fw_api_auth
(
  id                   integer not null ,-- '流水主键'
  name                 varchar not null ,-- '名称'
  key                  varchar not null ,-- 'key',
  secret               varchar not null ,-- '密钥',
  remark               varchar(500) default NULL ,-- '备注',
  delFlag              int not null ,-- '是否删除',
  tenantId             int not null ,-- '租户id',
  createBy             integer not null ,-- '创建人',
  createTime           datetime not null ,-- '创建时间',
  lastUpdateBy         integer not null ,-- '最后更新人',
  lastUpdateTime       datetime not null ,-- '最后更新时间',
  enable               tinyint not null ,-- '是否启用',
  primary key (id)
);

drop table if exists fw_prop;
create table fw_prop --属性表
(
  id                   integer not null primary key autoincrement , -- '主键id'
  name                 varchar(50) not null , -- '名称'
  code                 varchar(50) unique not null , -- '编码'
  remark               varchar(500) default NULL , -- '备注'
  delFlag                int not null , -- '是否删除'
  tenantId             integer not null , -- '租户id'
  createBy             integer not null , -- '创建人'
  createTime           datetime not null , -- '创建时间'
  lastUpdateBy         integer not null , -- '最后更新人'
  lastUpdateTime       datetime not null , -- '最后更新时间'
  enable               tinyint not null   -- '是否有效'
);

drop table if exists fw_proplist;
create table fw_proplist --属性行表
(
  id                   integer not null primary key autoincrement,
  propId               integer not null,
  propKey              varchar(50) not null,
  propValue            varchar(2000) not null,
  orderNo              int(4) not null,
  remark               varchar(1000) default NULL,
  delFlag                int not null,
  tenantId             integer not null,
  createBy             integer not null,
  createTime           datetime not null,
  lastUpdateBy         integer not null,
  lastUpdateTime       datetime not null,
  enable               tinyint not null
);


drop table if exists md_bcp_commodity;
create table md_bcp_commodity --系统
(
  id                   integer not null primary key autoincrement , -- '流水主键'
  name                 varchar not null , -- '名称'
  remark               varchar(500) default NULL , -- '备注'
  delFlag                int not null , -- '是否删除'
  tenantId             int not null , -- '租户id'
  createBy             integer not null , -- '创建人'
  createTime           datetime not null , -- '创建时间'
  lastUpdateBy         integer not null , -- '最后更新人'
  lastUpdateTime       datetime not null , -- '最后更新时间'
  enable               tinyint not null -- '是否启用'
);


drop table if exists md_bcp_producer;
create table md_bcp_producer   -- '厂商'
(
  id                   integer not null primary key autoincrement , -- '流水主键'
  name                 varchar not null , -- '名称'
  remark               varchar(500) default NULL , -- '备注'
  delFlag                int not null , -- '是否删除'
  tenantId             int not null , -- '租户id'
  createBy             integer not null , -- '创建人'
  createTime           datetime not null , -- '创建时间'
  lastUpdateBy         integer not null , -- '最后更新人'
  lastUpdateTime       datetime not null , -- '最后更新时间'
  enable               tinyint not null -- '是否启用'
);

drop table if exists md_bcp_host;
create table md_bcp_host --域名
(
  id                   integer not null primary key autoincrement , -- '主键id'
  name                 varchar(50) not null , -- '名称'
  host                 varchar not null , -- '域名'
  remark               varchar(500) default NULL , -- '备注'
  delFlag                int not null , -- '是否删除'
  tenantId             int not null , -- '租户id'
  createBy             integer not null , -- '创建人'
  createTime           datetime not null , -- '创建时间'
  lastUpdateBy         integer not null , -- '最后更新人'
  lastUpdateTime       datetime not null , -- '最后更新时间'
  enable               tinyint not null  -- '是否有效'
);


drop table if exists md_bcp_apistore;
create table md_bcp_api_store --api仓库
(
  id                   integer not null primary key autoincrement , -- '主键id'
  host                 varchar , -- '域名'
  name                 varchar(50) not null , -- 'api名称'
  method               varchar , -- '调用方式'
  producerId          integer not null , -- '厂商'
  commodityId          integer , -- '系统'
  url                  varchar(200) , -- 'api地址'
  reqHeader            varchar(1000), -- '请求头'
  reqParamType         varchar(10) not null , -- '请求参数类型'
  reqParam             varchar(1000) , -- '请求参数'
  reqParamValue        varchar(1000) , -- '请求参数值'
  respParamType        varchar(10)  not null , -- '响应参数类型'
  respParam            varchar(1000) not null , -- '响应参数'
  status               varchar(1)  not null , -- '状态(1:已发布2:未发布)'
  code                 varchar(50) not null , -- 'api编码'
  remark               varchar(500) default NULL , -- '备注'
  codeField            varchar not null, -- '返回码字段标识'
  dataField            varchar not null, -- '返回数据的字段标识'
  authType             varchar(20), --认证方式
  authConfig           varchar(50), --认证配置信息，对应属性配置表的code
  version              varchar(20), -- '版本'
  delFlag                int not null , -- '是否删除'
  tenantId             integer not null , -- '租户id'
  createBy             integer not null , -- '创建人'
  createTime           datetime not null , -- '创建时间'
  lastUpdateBy         integer not null , -- '最后更新人'
  lastUpdateTime       datetime not null , -- '最后更新时间'
  enable               tinyint not null -- '是否有效'
);

drop table if exists md_bcp_api_rule;
create table md_bcp_api_rule --调用规则
(
  id                   integer not null primary key autoincrement , -- '主键id'
  name                 varchar(50) not null , -- '名称'
  code                 varchar(50) unique not null , -- '编码'
  batchFlag            int not null default 0, -- '是否批量'
  directFlag           int not null default 0, -- '是否直传'
  sourceApiStoreId     integer , -- '源端接口'
  targetApiStoreId     integer , -- '目标接口'
  sourceMapping        varchar(1000) , -- '源端参数映射'
  targetMapping        varchar(1000) , -- '目标端参数映射'
  filterRule           varchar(1000) , -- '源端过滤规则'
  remark               varchar(500) default NULL , -- '备注'
  delFlag              int not null default 0 , -- '是否删除'
  tenantId             integer not null , -- '租户id'
  createBy             integer not null , -- '创建人'
  createTime           datetime not null , -- '创建时间'
  lastUpdateBy         integer not null , -- '最后更新人'
  lastUpdateTime       datetime not null , -- '最后更新时间'
  enable               tinyint not null -- '是否有效'
);


drop table if exists md_bcp_case;
create table md_bcp_case --标准业务场景
(
  id                   integer not null primary key autoincrement , -- '主键id'
  name                 varchar(50) not null , -- '名称'
  code                 varchar(50) unique not null , -- '编码'
  standardParam        varchar(1000), -- '标准参数格式'
  standardParamObj     varchar(4000), -- '标准参数对象'
  remark               varchar(500) default NULL , -- '备注'
  tag                  varchar not null , -- '标签'
  version              varchar not null , -- '版本'
  delFlag                int not null , -- '是否删除'
  tenantId             integer not null , -- '租户id'
  createBy             integer not null , -- '创建人'
  createTime           datetime not null , -- '创建时间'
  lastUpdateBy         integer not null , -- '最后更新人'
  lastUpdateTime       datetime not null , -- '最后更新时间'
  enable               tinyint not null   -- '是否有效'
);

drop table if exists md_bcp_user_case;
create table md_bcp_user_case  -- ‘客户场景’
(
  id                   integer not null primary key autoincrement , -- '主键id'
  name                 varchar(50) not null , -- '名称'
  code                 varchar(50) not null unique , -- '编码'
  caseId               integer not null , -- '标准场景'
  apiRuleId            integer not null , -- '规则id'
  remark               varchar(1000) default NULL , -- '备注'
  delFlag                int not null , -- '是否删除'
  tenantId             integer not null , -- '租户id'
  createBy             integer not null , -- '创建人'
  createTime           datetime not null , -- '创建时间'
  lastUpdateBy         integer not null , -- '最后更新人'
  lastUpdateTime       datetime not null , -- '最后更新时间'
  enable               tinyint not null   -- '是否有效'
);


drop table if exists md_bcp_task;
create table md_bcp_task --计划任务
(
  id                   integer not null primary key autoincrement , -- '主键id'
  name                 varchar(50) not null , -- '名称'
  code                 varchar(50) not null unique , -- '编码'
  cron                 varchar not null , -- 'cron表达式'
  type                 varchar , -- '类型' 1:拉通，2:自定义
  execService          varchar , -- '执行的service类'
  userCaseId           integer , -- '客户场景'
  result               varchar , -- '运行结果'
  remark               varchar(500) default NULL , -- '备注'
  delFlag                int not null , -- '是否删除'
  tenantId             integer not null , -- '租户id'
  createBy             integer not null , -- '创建人'
  createTime           datetime not null , -- '创建时间'
  lastUpdateBy         integer not null , -- '最后更新人'
  lastUpdateTime       datetime not null , -- '最后更新时间'
  enable               tinyint not null   -- '是否有效'
);

drop table if exists md_bcp_api;
create table md_bcp_api -- api管理
(
  id                   integer not null primary key autoincrement , -- '主键id'
  name                 varchar(50) not null , -- '名称'
  code                 varchar(50) not null unique , -- '编码'
  path                 varchar not null , -- '路径'
  userCaseId           integer not null , -- '客户场景'
  remark               varchar(500) default NULL , -- '备注'
  tenantId             integer not null , -- '租户id'
  delFlag                int not null , -- '是否删除'
  createBy             integer not null , -- '创建人'
  createTime           datetime not null , -- '创建时间'
  lastUpdateBy         integer not null , -- '最后更新人'
  lastUpdateTime       datetime not null , -- '最后更新时间'
  enable               tinyint not null   -- '是否有效'
);

drop table if exists md_bcp_api_proxy;
create table md_bcp_api_proxy --api代理表
(
  id                   integer not null primary key autoincrement , -- '主键id'
  name                 varchar(50) not null , -- '名称'
  path                 varchar(50) unique not null , -- '编码'
  protocol             varchar(50) not null, -- '协议'
  target               varchar(50) not null, -- '代理对象'
  apiStoreId           integer not null , -- 'api仓库id'
  loginFlag            int not null default 0, -- 是否需要登录
  remark               varchar(500) default NULL , -- '备注'
  delFlag              int not null , -- '是否删除'
  tenantId             integer not null , -- '租户id'
  createBy             integer not null , -- '创建人'
  createTime           datetime not null , -- '创建时间'
  lastUpdateBy         integer not null , -- '最后更新人'
  lastUpdateTime       datetime not null , -- '最后更新时间'
  enable               tinyint not null   -- '是否有效'
);

drop table if exists fw_user;
create table fw_user --用户表
(
  id                   integer not null primary key autoincrement, --主键id'
  name                 varchar(50) default NULL , -- '姓名'
  uName                varchar(20) unique not null , -- '登录名'
  password             varchar(100) not null , -- '密码'
  type                 varchar(20) not null default 'normal' , -- '类型(admin:管理员normal:普通用户)'
  status               varchar(10) default NULL , -- '状态:正常:normal、锁定:locked、冻结:freezed'
  phoneNo              varchar(20) default NULL , -- '手机号码'
  email                varchar(100) default NULL , -- '邮箱地址'
  sex                  varchar(1) not null , -- '性别'
  remark               varchar(1000) default NULL , -- '备注'
  amount1              decimal(16,2) default NULL , -- '金额1'
  amount2              decimal(16,2) default NULL , -- '金额2'
  amount3              decimal(16,2) default NULL , -- '金额3'
  amount4              decimal(16,2) default NULL , -- '金额4'
  attr1                varchar(500) default NULL , -- '扩展字段1'
  attr2                varchar(500) default NULL , -- '扩展字段2'
  attr3                varchar(500) default NULL , -- '扩展字段3'
  attr4                varchar(500) default NULL , -- '扩展字段4'
  attr5                varchar(500) default NULL , -- '扩展字段5'
  attr6                varchar(500) default NULL , -- '扩展字段6'
  orgId                integer not null , -- '所属组织'
  delFlag                int not null , -- '是否删除'
  tenantId             integer not null , -- '租户'
  createBy             integer not null , -- '创建人'
  createTime           datetime not null , -- '创建时间'
  lastUpdateBy         integer not null , -- '最后更新人'
  lastUpdateTime       datetime not null , -- '最后更新时间'
  enable               tinyint not null -- '是否有效'
);