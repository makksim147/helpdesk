package org.example.marlo_site_helpdesk.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProfileDto {

    @NotBlank(message = "Введите имя")
    @Size(max = 100, message = "Имя слишком длинное")
    private String name;

    @NotBlank(message = "Введите почту")
    @Email(message = "Некорректный адрес почты")
    @Size(max = 254, message = "Почта слишком длинная")
    private String email;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}