package com.demo.dto.response.demo;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class DemoRespDTO {

    private long id;

    @JsonFormat(pattern="yyyy-MM-dd",timezone="GMT+8")
    private LocalDate createDate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDate getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDate createDate) {
        this.createDate = createDate;
    }
}
