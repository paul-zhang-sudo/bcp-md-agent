drop table if exists md_agent_datasource;

/*==============================================================*/
/* Table: md_agent_datasource                                   */
/*==============================================================*/
create table md_agent_datasource
(
  id                   integer not null primary key autoincrement, --主键
  name                 varchar(20) not null , -- '名称',
  remark               varchar(1000) default NULL , -- '备注',
  tenantId             integer not null , -- '租户',
  type                 varchar(10) not null , -- '类型',
  classify             varchar(10) not null , -- '分类',
  nodeId               varchar(200) , -- '节点',
  configValue          varchar(2000) not null , -- '配置数据',
  delFlag              int not null , -- '是否删除',
  createBy             int not null , -- '创建人',
  createTime           datetime not null , -- '创建时间',
  lastUpdateBy         int not null , -- '最后更新人',
  lastUpdateTime       datetime not null , -- '最后更新时间',
  enable               int not null  -- '是否有效'
);



drop table if exists md_agent_config;

/*==============================================================*/
/* Table: md_agent_config                                       */
/*==============================================================*/
create table md_agent_config
(
  id                   integer not null primary key autoincrement, --主键
  name                 varchar(20) not null , -- '名称',
  remark               varchar(1000) default NULL , -- '备注',
  tenantId             int not null , -- '租户',
  status               varchar(10) not null , -- '状态',
  nodeId               varchar(200) , -- '节点',
  configValue          varchar(2000) not null , -- '配置数据',
  delFlag              int not null , -- '是否删除',
  createBy             int not null , -- '创建人',
  createTime           datetime not null , -- '创建时间',
  lastUpdateBy         int not null , -- '最后更新人',
  lastUpdateTime       datetime not null , -- '最后更新时间',
  enable               int not null  -- '是否有效'
);

drop table if exists md_agent_job;

/*==============================================================*/
/* Table: md_agent_job                                          */
/*==============================================================*/
create table md_agent_job
(
  id                   integer not null primary key autoincrement, --主键
  configId             int not null , -- '配置id',
  name                 varchar(100) not null , -- '名称',
  cron                 varchar(50) not null , -- 'cron表达式',
  delFlag              int not null , -- '是否删除',
  createBy             int not null , -- '创建人',
  createTime           datetime not null , -- '创建时间',
  lastUpdateBy         int not null , -- '最后更新人',
  lastUpdateTime       datetime not null , -- '最后更新时间',
  enable               int not null  -- '是否有效'
);

drop table if exists md_agent_api_proxy;

/*==============================================================*/
/* Table: md_agent_api_proxy                                    */
/*==============================================================*/
create table md_agent_api_proxy
(
  id                   integer not null primary key autoincrement, --主键
  configId             int not null , -- '配置id',
  name                 varchar(100) not null , -- '名称',
  delFlag              int not null , -- '是否删除',
  createBy             int not null , -- '创建人',
  createTime           datetime not null , -- '创建时间',
  lastUpdateBy         int not null , -- '最后更新人',
  lastUpdateTime       datetime not null , -- '最后更新时间',
  enable               int not null  -- '是否有效'
);


drop table if exists md_agent_job_param;

/*==============================================================*/
/* Table: md_agent_job_param                                    */
/*==============================================================*/
create table md_agent_job_param
(
  id                   integer not null primary key autoincrement, --主键
  lastRunTime          datetime not null , -- '最后运行时间',
  lastRunFlag          varchar(20) not null , -- '最后运行标志',
  jobId                int not null , -- '任务id',
  delFlag              int not null , -- '是否删除',
  createBy             int not null , -- '创建人',
  createTime           datetime not null , -- '创建时间',
  lastUpdateBy         int not null , -- '最后更新人',
  lastUpdateTime       datetime not null , -- '最后更新时间',
  enable               int not null  -- '是否有效'
);

