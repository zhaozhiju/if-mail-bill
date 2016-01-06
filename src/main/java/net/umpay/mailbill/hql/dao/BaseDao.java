package net.umpay.mailbill.hql.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import net.umpay.mailbill.hql.orm.Page;
import net.umpay.mailbill.hql.orm.PropertyFilter;
import net.umpay.mailbill.hql.orm.PropertyFilter.MatchType;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Criterion;

public interface BaseDao<T,PK extends Serializable>
{
	//以下是SimpleHibernateDao里所有的方法声明
	
	/**
	 * 保存新增或修改的对象.
	 */
	void save(final T entity);
	
	/**
	 * 删除对象.
	 * 
	 * @param entity 对象必须是session中的对象或含id属性的transient对象.
	 */
	void delete(final T entity);
	
	/**
	 * 按id删除对象.
	 */
	void delete(final PK id);
	
	/**
	 * 按id获取对象.
	 */
	T get(final PK id);
	
	/**
	 *	获取全部对象.
	 */
	List<T> getAll();
	
	/**
	 *	获取全部对象,支持排序.
	 */
	List<T> getAll(String orderBy, boolean isAsc);
	
	/**
	 * 按属性查找对象列表,匹配方式为相等.
	 */
	List<T> findBy(final String propertyName, final Object value);
	
	/**
	 * 按属性查找唯一对象,匹配方式为相等.
	 */
	T findUniqueBy(final String propertyName, final Object value);
	
	/**
	 * 按id列表获取对象.
	 */
	List<T> findByIds(List<PK> ids);
	
	/**
	 * 按HQL查询对象列表.
	 * 
	 * @param values 数量可变的参数,按顺序绑定.
	 */
	<X> List<X> find(final String hql, final Object... values);
	
	/**
	 * 按HQL查询对象列表.
	 * 
	 * @param values 命名参数,按名称绑定.
	 */
	<X> List<X> find(final String hql, final Map<String, ?> values);
	
	/**
	 * 按SQL查询对象列表.
	 * 
	 * @param values 数量可变的参数,按顺序绑定.
	 */
	<X> List<X> findSQL(final String sql, final Object... values);
	
	/**
	 * 按SQL查询对象列表.
	 * 
	 * @param values 命名参数,按名称绑定.
	 */
	<X> List<X> findSQL(final String sql, final Map<String, ?> values);
	
	/**
	 * 按HQL查询唯一对象.
	 * 
	 * @param values 数量可变的参数,按顺序绑定.
	 */
	<X> X findUnique(final String hql, final Object... values);
	
	/**
	 * 按HQL查询唯一对象.
	 * 
	 * @param values 命名参数,按名称绑定.
	 */
	<X> X findUnique(final String hql, final Map<String, ?> values);
	
	/**
	 * 按SQL查询唯一对象.
	 * 
	 * @param values 数量可变的参数,按顺序绑定.
	 */
	<X> X findUniqueSql(final String sql, final Object... values);
	
	/**
	 * 按SQL查询唯一对象.
	 * 
	 * @param values 命名参数,按名称绑定.
	 */
	<X> X findUniqueSql(final String sql, final Map<String, ?> values);
	
	/**
	 * 执行HQL进行批量修改/删除操作.
	 */
	int batchExecute(final String hql, final Object... values);
	
	/**
	 * 执行HQL进行批量修改/删除操作.
	 * @return 更新记录数.
	 */
	int batchExecute(final String hql, final Map<String, ?> values);
	
	/**
	 * 执行SQL进行批量修改/删除操作.
	 */
	int batchSQLExecute(final String sql, final Object... values);
	
	/**
	 * 执行SQL进行批量修改/删除操作.
	 * @return 更新记录数.
	 */
	int batchSQLExecute(final String sql, final Map<String, ?> values);
	
	/**
	 * 根据查询HQL与参数列表创建Query对象.
	 * 
	 * 本类封装的find()函数全部默认返回对象类型为T,当不为T时使用本函数.
	 * 
	 * @param values 数量可变的参数,按顺序绑定.
	 */
	Query createQuery(final String queryString, final Object... values);
	
	/**
	 * 根据查询SQL与参数列表创建SQLQuery对象.
	 * 
	 * 本类封装的find()函数全部默认返回对象类型为T,当不为T时使用本函数.
	 * 
	 * @param values 数量可变的参数,按顺序绑定.
	 */
	SQLQuery createSQLQuery(final String queryString, final Object... values);
	
	/**
	 * 根据查询HQL与参数列表创建Query对象.
	 * 
	 * @param values 命名参数,按名称绑定.
	 */
	Query createQuery(final String queryString, final Map<String, ?> values);
	
	/**
	 * 根据查询SQL与参数列表创建SQLQuery对象.
	 * 
	 * @param values 命名参数,按名称绑定.
	 */
	SQLQuery createSQLQuery(final String queryString, final Map<String, ?> values);
	
	/**
	 * 按Criteria查询对象列表.
	 * 
	 * @param criterions 数量可变的Criterion.
	 */
	List<T> find(final Criterion... criterions);
	
	/**
         * 按Criteria查询对象列表.
         * 
         * @param criterions 数量可变的Criterion.
         */
        List<Serializable> findSerial(final Criterion... criterions);
	
	/**
	 * 按Criteria查询对象列表.
	 * 
	 * @param criteria
	 */
	List<T> find(final Criteria criteria);
	
	/**
	 * 按Criteria查询唯一对象.
	 * 
	 * @param criterions 数量可变的Criterion.
	 */
	T findUnique(final Criterion... criterions);
	
	/**
	 * 按Criteria查询唯一对象.同时启用查询缓存
	 * 
	 * @param criterions 数量可变的Criterion.
	 */
	T findUniqueByCache(final Criterion... criterions);
	
	/**
	 * 根据Criterion条件创建Criteria.
	 * 
	 * 本类封装的find()函数全部默认返回对象类型为T,当不为T时使用本函数.
	 * 
	 * @param criterions 数量可变的Criterion.
	 */
	Criteria createCriteria(final Criterion... criterions);
	
	/**
	 * 初始化对象.
	 * 使用load()方法得到的仅是对象Proxy, 在传到View层前需要进行初始化.
	 * 只初始化entity的直接属性,但不会初始化延迟加载的关联集合和属性.
	 * 如需初始化关联属性,可实现新的函数,执行:
	 * Hibernate.initialize(user.getRoles())，初始化User的直接属性和关联集合.
	 * Hibernate.initialize(user.getDescription())，初始化User的直接属性和延迟加载的Description属性.
	 */
	void initEntity(T entity);
	
	/**
	 * @see #initEntity(Object)
	 */
	void initEntity(List<T> entityList);
	
	/**
	 * Flush当前Session.
	 */
	void flush();
	/**
	 * 为Query添加distinct transformer.
	 */
	Query distinct(Query query);
	
	/**
	 * 为Criteria添加distinct transformer.
	 */
	Criteria distinct(Criteria criteria);
	
	/**
	 * 取得对象的主键名.
	 */
	String getIdName();
	
	
	
	//以下是HibernateDao里所有的方法声明
	
	//-- 分页查询函数 --//
	/**
	 * 分页获取全部对象.
	 */
	Page<T> getAll(final Page<T> page);
	
	/**
	 * 按HQL分页查询.
	 * 
	 * @param page 分页参数.不支持其中的orderBy参数.
	 * @param hql hql语句.
	 * @param values 数量可变的查询参数,按顺序绑定.
	 * 
	 * @return 分页查询结果, 附带结果列表及所有查询时的参数.
	 */
	Page<T> findPage(final Page<T> page, final String hql, final Object... values);
	
	/**
	 * 按sqlquery分页查询
	 */
	Page<T> findSQLPage(final Page<T> page, final String sql, final Object... values);
	
	/**
	 * 按HQL分页查询.
	 * 
	 * @param page 分页参数.
	 * @param hql hql语句.
	 * @param values 命名参数,按名称绑定.
	 * 
	 * @return 分页查询结果, 附带结果列表及所有查询时的参数.
	 */
	Page<T> findPage(final Page<T> page, final String hql, final Map<String, ?> values);
	
	/**
	 * 按sqlquery分页查询
	 */
	Page<T> findSQLPage(final Page<T> page, final String sql, final Map<String, ?> values);
	
	/**
	 * 按Criteria分页查询.
	 * 
	 * @param page 分页参数.
	 * @param criterions 数量可变的Criterion.
	 * 
	 * @return 分页查询结果.附带结果列表及所有查询时的参数.
	 */
	Page<T> findPage(final Page<T> page, final Criterion... criterions);
	
	/**
	 * 按Criteria分页查询.
	 * 
	 * @param page 分页参数.
	 * @param criteria
	 * 
	 * @return 分页查询结果.附带结果列表及所有查询时的参数.
	 */
	Page<T> findPage(final Page<T> page, final Criteria criteria);
	
	//-- 属性过滤条件(PropertyFilter)查询函数 --//
	/**
	 * 按属性查找对象列表,支持多种匹配方式.
	 * 
	 * @param matchType 匹配方式,目前支持的取值见PropertyFilter的MatcheType enum.
	 */
	List<T> findBy(final String propertyName, final Object value, final MatchType matchType);
	
	/**
	 * 按属性过滤条件列表查找对象列表.
	 */
	List<T> find(List<PropertyFilter> filters);
	
	/**
	 * 按属性过滤条件列表分页查找对象.
	 */
	Page<T> findPage(final Page<T> page, final List<PropertyFilter> filters);
	
	/**
	 * 判断对象的属性值在数据库内是否唯一.
	 * 
	 * 在修改对象的情景下,如果属性新修改的值(value)等于属性原来的值(orgValue)则不作比较.
	 */
	boolean isPropertyUnique(final String propertyName, final Object newValue, final Object oldValue);
	
	public long countSqlResult(final String sql, final Map<String, ?> values);
}
