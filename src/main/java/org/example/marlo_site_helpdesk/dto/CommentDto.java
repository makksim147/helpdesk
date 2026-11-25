package org.example.marlo_site_helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentDto {

    @NotBlank(message = "Введите текст комментария")
    @Size(max = 3000, message = "Максимум 3000 символов")
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}