package org.example.marlo_site_helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.marlo_site_helpdesk.models.Priority;

public class TicketCreateDto {

    @NotBlank(message = "Введите название заявки")
    @Size(max = 200, message = "Максимум 200 символов")
    private String title;

    @NotBlank(message = "Введите описание проблемы")
    @Size(max = 5000, message = "Максимум 5000 символов")
    private String description;

    @NotNull(message = "Выберите категорию")
    private Long categoryId;

    @NotNull(message = "Выберите приоритет")
    private Priority priority;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}