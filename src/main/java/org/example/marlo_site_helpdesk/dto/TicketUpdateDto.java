package org.example.marlo_site_helpdesk.dto;

import jakarta.validation.constraints.NotNull;
import org.example.marlo_site_helpdesk.models.Priority;
import org.example.marlo_site_helpdesk.models.Status;

public class TicketUpdateDto {

    @NotNull(message = "Выберите статус")
    private Status status;

    @NotNull(message = "Выберите приоритет")
    private Priority priority;

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
}