package com.demo.service.base.impl;

import com.demo.service.base.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 *
 */
@Service
public abstract class BaseServiceImpl<T> implements BaseService<T> {

    //利用spring4.0新特性，泛型注入
    //必须用@Autowired而不是@Resource
    @Autowired
    private Mapper<T> mapper;

    public List<T> selectAll() {
        return mapper.selectAll();
    }

    public T selectByPrimaryKey(Object o) {
        return mapper.selectByPrimaryKey(o);
    }

    public int selectCount(T t) {
        return mapper.selectCount(t);
    }

    public List<T> select(T t) {
        return mapper.select(t);
    }

    public T selectOne(T t) {
        return mapper.selectOne(t);
    }

    @Transactional
    public int insert(T t) {
        return mapper.insert(t);
    }

    @Transactional
    public int insertSelective(T t) {
        return mapper.insertSelective(t);
    }

    @Transactional
    public int updateByPrimaryKey(T t) {
        return mapper.updateByPrimaryKey(t);
    }

    @Transactional
    public int updateByPrimaryKeySelective(T t) {
        return mapper.updateByPrimaryKeySelective(t);
    }

    @Transactional
    public int deleteByPrimaryKey(Object o) {
        return mapper.deleteByPrimaryKey(o);
    }

    @Transactional
    public int delete(T t) {
        return mapper.delete(t);
    }






}
