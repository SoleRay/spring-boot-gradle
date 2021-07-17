package com.demo.entity.demo;

import org.apache.ibatis.type.LocalDateTimeTypeHandler;
import org.apache.ibatis.type.LocalDateTypeHandler;
import tk.mybatis.mapper.annotation.ColumnType;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 不实现Serializable接口，无法缓存对象到redis
 */
@Table(name = "tb_demo")
public class Demo implements Serializable {

    @Id
    private Long id;

    @Column(name = "demo_key")
    private String demoKey;

    @Column(name = "demo_name")
    private String demoName;

    /**
     * 通用mybatis目前以这种方式支持LocalDate和LocalDateTime
     */
    @Column(name = "create_date")
    @ColumnType(typeHandler = LocalDateTypeHandler.class)
    private LocalDate createDate;


    @Column(name = "update_time")
    @ColumnType(typeHandler = LocalDateTimeTypeHandler.class)
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDemoKey() {
        return demoKey;
    }

    public void setDemoKey(String demoKey) {
        this.demoKey = demoKey;
    }

    public String getDemoName() {
        return demoName;
    }

    public void setDemoName(String demoName) {
        this.demoName = demoName;
    }

    public LocalDate getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDate createDate) {
        this.createDate = createDate;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
