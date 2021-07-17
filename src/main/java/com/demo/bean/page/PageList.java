package com.demo.bean.page;

import java.util.List;

/**
 * Created by Arthur on 2017/3/7 0007.
 */
public class PageList<T> {

    private List<T> content;

    private PageInfo pageInfo;

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }
}
