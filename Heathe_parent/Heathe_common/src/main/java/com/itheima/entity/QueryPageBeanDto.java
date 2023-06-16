package com.itheima.entity;
import java.io.Serializable;

/**
 * 封装查询条件
 */
public class QueryPageBeanDto implements Serializable{
    private Integer currentPage;//页码
    private Integer pageSize;//每页记录数
    private Integer queryString;//查询条件
    public Integer getCurrentPage() {
        return currentPage;
    }
    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }
    public Integer getPageSize() {
        return pageSize;
    }
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
    public Integer getQueryString() {
        return queryString;
    }
    public void setQueryString(Integer queryString) {
        this.queryString = queryString;
    }
}
