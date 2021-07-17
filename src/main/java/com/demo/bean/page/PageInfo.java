package com.demo.bean.page;

/**
 * Created by Arthur on 2016/12/30 0030.
 */
public class PageInfo {

    private long pageNum;

    private int pageSize;

    private long totalNum;

    private long totalPages;

    public PageInfo(){

    }

    public PageInfo(long pageNum, int pageSize, long totalNum, long totalPages) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalNum = totalNum;
        this.totalPages = totalPages;
    }

    public long getPageNum() {
        return pageNum;
    }

    public void setPageNum(long pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(long totalNum) {
        this.totalNum = totalNum;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(long totalPages) {
        this.totalPages = totalPages;
    }
}
