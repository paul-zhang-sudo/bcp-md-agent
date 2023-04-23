package com.bsi.md.agent.datasource;

import com.bsi.framework.core.jdbc.FwJdbcTemplate;
import com.bsi.framework.core.service.FwService;
import com.bsi.framework.core.utils.CollectionUtils;
import com.bsi.framework.core.utils.FwJdbcDialectUtils;
import com.bsi.framework.core.vo.resp.PageResp;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.dialect.AbstractHelperDialect;
import com.github.pagehelper.parser.CountSqlParser;
import org.apache.ibatis.cache.CacheKey;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;
import java.util.Map;
import java.util.Properties;
/**
 * springjdbc工具类
 * @author fish
 */
public class AgJdbcTemplate extends FwService {
	
	protected AbstractHelperDialect autoDialect = null;
	private FwJdbcTemplate jdbcTemplate = null;

	
	public AgJdbcTemplate() {
		
	}

	public void setDataSource(String driverClassName,String url,String username,String password) {
		jdbcTemplate = new FwJdbcTemplate();
		//读取数据库中的默认第三方数据源
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName( driverClassName );
		dataSource.setUrl( url );
		dataSource.setUsername( username );
		dataSource.setPassword( password );
		jdbcTemplate.setDataSource( dataSource );


		//根据数据库连接信息解析出，数据库方言，用来分页
		Class dialectClass = FwJdbcDialectUtils.getDialectClass(  url );
		try {
			autoDialect = (AbstractHelperDialect) dialectClass.newInstance();
			autoDialect.setProperties(new Properties());
		} catch (Exception e) {
			throw new PageException("初始化 helper [" + dialectClass.toString() + "]时出错:" + e.getMessage(), e);
		} 
	}
	
	public FwJdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(FwJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public <T> T queryForObject(String sql, Object[] args, Class<T> c) throws DataAccessException {
		return jdbcTemplate.queryForObject(sql, args, new BeanPropertyRowMapper<T>(c));
	}

	public <T> Map<String, Object> queryForMap(String sql, Object[] args) throws DataAccessException {
		return jdbcTemplate.queryForMap(sql, args);
	}
	
	public <T> List<T> queryForList(String sql,Class<T> c, Object[] args) throws DataAccessException {
		return jdbcTemplate.query(sql, new BeanPropertyRowMapper<T>(c), args);
	}

	public List<Map<String, Object>> queryForList(String sql, Object[] args) throws DataAccessException {
		return jdbcTemplate.queryForList(sql,args);
	}
	
	public <T> PageResp<T> queryListPage(String sql,Class<T> c, Object[] args) throws DataAccessException {
		Page<T> p = PageHelper.getLocalPage();
		
		Page<T> rp = new Page<>(p.getPageNum(),p.getPageSize());
		CountSqlParser csp = new CountSqlParser();
		String countSql = csp.getSmartCountSql(sql);
		Integer count = jdbcTemplate.queryForObject(countSql, Integer.class, args);
		rp.setTotal(count);
		if( count<=0 ){
			rp.close();
			return new PageResp<T>();
		}
		String pagesql = autoDialect.getPageSql(sql, rp, new CacheKey(  ));
		List<T> resultList = queryForList(pagesql, c, args);
		if( CollectionUtils.isNotEmpty(resultList) ){
			rp.addAll(resultList);
		}
		return new PageResp<T>(rp);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> PageResp<T> queryListPage(String sql,Class<T> c, Object[] args,Integer currentPage,Integer pageSize,String limitOption) throws DataAccessException {
		PageResp res = new PageResp();
		CountSqlParser csp = new CountSqlParser();
		String countSql = csp.getSmartCountSql(sql);
		Integer count = jdbcTemplate.queryForObject(countSql, Integer.class, args);
		res.setTotalCount(Long.parseLong(count+""));
		if( count<=0 ){
			return new PageResp<T>();
		}
		int pageCount = (int) (count / pageSize);
		res.setPageSize(pageSize);
		res.setTotalPage(pageCount);
		res.setCurrentPage(currentPage);
		res.setCode(200);
		String limitSql=" select top "+pageSize+" o.* from (select row_number() over("+limitOption+") as rownumber,* from("+sql+") as mm) as o where rownumber>"+pageSize*(currentPage -1);
		List<T> resultList = queryForList(limitSql, c, args);
		res.setModel(resultList);
		return res;
	}

	/**
	 * 插入/更新
	 * @param sql
	 * @param args
	 * @return
	 */
	public int update(String sql,Object[] args){
		return jdbcTemplate.update(sql,args);
	}
}
