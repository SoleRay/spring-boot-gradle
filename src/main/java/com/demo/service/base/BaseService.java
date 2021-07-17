package com.demo.service.base;

import java.util.List;

/**
 * Created by Administrator on 2017-1-3.
 */
public interface BaseService<T>  {

    public int deleteByPrimaryKey(Object o);

    public int delete(T t);

    public int insert(T t);

    public int insertSelective(T t);

    public List<T> selectAll();

    public T selectByPrimaryKey(Object o);

    public int selectCount(T t);

    public List<T> select(T t);

    public T selectOne(T t);

    public int updateByPrimaryKey(T t);

    public int updateByPrimaryKeySelective(T t);
}
