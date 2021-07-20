##### 1.关于一级缓存

mybaits一级缓存是session级别的缓存，当sqlSession开启以后，在这个sqlSession的生命周期内，对于相同sql的执行，只执行一次。当查询过一次以后，结果集将缓存在localCache中：

```java
public abstract class BaseExecutor implements Executor {
  //...其它属性
  protected PerpetualCache localCache;
  
  //...其它方法
  
  @Override
  public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) throws SQLException {
    //....其它操作
    
    if (queryStack == 0 && ms.isFlushCacheRequired()) {
      clearLocalCache();
    }
    List<E> list;
    try {
      queryStack++;
      list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;//这里就是一级缓存
      if (list != null) {
        handleLocallyCachedOutputParameters(ms, key, parameter, boundSql);
      } else {
        list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);//没有就去数据库里查
      }
    } finally {
      queryStack--;
    }
    if (queryStack == 0) {
      for (DeferredLoad deferredLoad : deferredLoads) {
        deferredLoad.load();
      }
      // issue #601
      deferredLoads.clear();
      if (configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
        // issue #482
        clearLocalCache();
      }
    }
    return list;
  }
}
```

一级缓存是默认开启的。

##### 2.关于二级缓存

二级缓存默认不开启，需要在xxxMapper.xml或者xxxDao.xml加上<cache>标签才可以。

什么是mybatis的二级缓存，他用在什么场景下？
我们先看这样一个场景，我们在实际开发业务的过程中，必然需要用到缓存，比如redis。很多情况下，就是有些热点数据，需要在数据库中存一份，在redis中存一份。每次查的时候，先从redis中查，查不到再数据库，从数据库查到数据以后，再更新redis。当我们要往更新数据时，需要先删除redis里的数据，然后更新数据库。这样下次查的时候，就会重复查询的流程从而更新redis。

这个场景是不是很熟悉，mybatis的二级缓存就完全可以做到这一点！这样说的话，应该可以清楚，什么是mybatis的二级缓存了

##### 3.关于二级缓存的缺陷

多表操作无法使用二级缓存。如果可以，建议用自己的方式实现缓存控制。

##### 4.Mybaits如何保证二级缓存和数据库的数据一致性？

Mybatis在更新的时候，是先更新数据库，然后再删除缓存的。这时候，可能存在的唯一情况是，如果缓存没有删除成功会怎样？
如果不使用外部存储，默认情况下，mybatis的二级缓存保存在内存中，使用的是HashMap。而对HashMap的clear操作，不可能引发异常。如果在clear之前，系统停止运行，那么内存的二级缓存也不复存在。所以不大可能出在缓存不一致的问题。

但如果使用了外部存储，则可能会存在缓存不一致的情况。

```java
 private class SqlSessionInterceptor implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      SqlSession sqlSession = getSqlSession(
          SqlSessionTemplate.this.sqlSessionFactory,
          SqlSessionTemplate.this.executorType,
          SqlSessionTemplate.this.exceptionTranslator);
      try {
        Object result = method.invoke(sqlSession, args);//先更新
        if (!isSqlSessionTransactional(sqlSession, SqlSessionTemplate.this.sqlSessionFactory)) {
          // force commit even on non-dirty sessions because some databases require
          // a commit/rollback before calling close()
          sqlSession.commit(true);//后删除缓存
        }
        return result;
      } catch (Throwable t) {
        ....
      } finally {
        ....
      }
    }
  }
```



##### 5.关于缓存的几个概念

###### 5.1 缓存穿透

缓存穿透是指缓存和数据库中都没有的数据，而用户不断发起请求，如发起为id为“-1”的数据或id为特别大不存在的数据。这时的用户很可能是攻击者，攻击会导致数据库压力过大。

解决方案：
布隆过滤器

###### 5.2 缓存击穿

缓存击穿是指缓存中没有但数据库中有的数据（一般是缓存时间到期），这时由于并发用户特别多，同时读缓存没读到数据，又同时去数据库去取数据，引起数据库压力瞬间增大，造成过大压力

解决方案：
设置热点数据永远不过期。
加互斥锁，互斥锁

对于一些设置了过期时间的key，如果这些key可能会在某些时间点被超高并发地访问，是一种非常“热点”的数据。这个时候，需要考虑一个问题：缓存被“击穿”的问题，这个和缓存雪崩的区别在于这里针对某一key缓存，前者则是很多key。

缓存在某个时间点过期的时候，恰好在这个时间点对这个Key有大量的并发请求过来，这些请求发现缓存过期一般都会从后端DB加载数据并回设到缓存，这个时候大并发的请求可能会瞬间把后端DB压垮。

###### 5.3 缓存雪崩

缓存雪崩是指缓存中数据大批量到过期时间，而查询数据量巨大，引起数据库压力过大甚至down机。
***和缓存击穿不同的是， 缓存击穿指并发查同一条数据，缓存雪崩是不同数据都过期了，很多数据都查不到从而查数据库。***
***缓存雪崩是指在我们设置缓存时采用了相同的过期时间，导致缓存在某一时刻同时失效，请求全部转发到DB，DB瞬时压力过重雪崩。***

解决方案：
缓存数据的过期时间设置随机，防止同一时间大量数据过期现象发生。
如果缓存数据库是分布式部署，将热点数据均匀分布在不同搞得缓存数据库中。
设置热点数据永远不过期。

缓存失效时的雪崩效应对底层系统的冲击非常可怕。大多数系统设计者考虑用加锁或者队列的方式保证缓存的单线 程（进程）写，从而避免失效时大量的并发请求落到底层存储系统上。这里分享一个简单方案就时讲缓存失效时间分散开，比如我们可以在原有的失效时间基础上增加一个随机值，比如1-5分钟随机，这样每一个缓存的过期时间的重复率就会降低，就很难引发集体失效的事件。

###### 5.4 缓存与数据库不一致的问题

缓存与数据库最常见的一个问题是：当更新数据库时，如何更新缓存？

通常的策略是执行以下两步操作：

1. 更新数据库
2. 删除缓存

只要这两个步骤能够原子性地完成，那么后续再有读请求时，发现缓存不存在，就会去数据库读，然后更新缓存。如果存在并发，只要加锁保证缓存只被更新一次即可。

然而这里就有一个问题，是先更新数据库，还是先删除缓存？

方式一：先更新数据库，再删除缓存
假如数据库更新完毕，程序发生异常导致缓存没有删除，或者删除失败，或者redis突然宕机，导致没有删除成功。此时就导致数据库和缓存不一致。

方式二：先删除缓存，再更新数据库
假如缓存正常删除，此时开启事务更新数据库，但事务尚未结束，又有读请求过来了。此时发现缓存没了，就去数据库读，但更新还未结束，所以读到的是旧数据，然后旧数据更新了缓存，然后数据库事务更新完毕。此时，又发生数据库和缓存不一致了。

解决方案
更新数据时，将更新操作放入到一个队列中。读取数据的时候，如果发现数据不在缓存中，那么将重新读取数据+更新缓存的操作也发送到这个队列中。

这样的话，操作串行化执行，就不会出现不一致的问题。
这里有一个**优化点**，一个队列中，其实**多个更新缓存请求串在一起是没意义的**，因此可以做过滤，如果发现队列中已经有一个更新缓存的请求了，那么就不用再放个更新请求操作进去了，直接等待前面的更新操作请求完成即可

该解决方案，最大的风险点在于说，**可能数据更新很频繁**，导致队列中积压了大量更新操作在里面，然后**读请求会发生大量的超时**，最后导致大量的请求直接走数据库

