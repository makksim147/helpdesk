package org.example.marlo_site_helpdesk.dto;

import org.example.marlo_site_helpdesk.models.Priority;
import org.example.marlo_site_helpdesk.models.Status;

public class TicketFilterDto {

    private String search = "";
    private Status status;
    private Priority priority;
    private Long categoryId;
    private String sort = "desc";

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}