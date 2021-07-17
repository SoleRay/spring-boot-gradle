package com.demo.bean.page;

/**
 * Created by Arthur on 2016/12/30 0030.
 */
public class Pageable {

    private long pageNum;

    private int pageSize;

    public Pageable(long pageNum, int pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public long getStart(){
        return (pageNum-1)*pageSize;
    }

    public long getTotalPages(long totalCount){

        if(totalCount==0){
            return 1;
        }

        if(totalCount%pageSize==0){
            return totalCount/pageSize;
        }

        return totalCount/pageSize+1;
    }

    private int genPageSize(long totalPages, long totalCount) {
        if(totalPages==1){
            return (int) totalCount;
        }

        if(pageNum<totalPages){
            return pageSize;
        }

        return (int) (totalCount - (pageNum-1)*pageSize);

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

    public PageInfo genPageInfo(long totalCount){
        long totalPages = getTotalPages(totalCount);
        int currentSize = genPageSize(totalPages,totalCount);

        return new PageInfo(pageNum,currentSize,totalCount,totalPages);
    }




}
