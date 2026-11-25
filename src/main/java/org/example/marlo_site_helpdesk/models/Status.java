package org.example.marlo_site_helpdesk.models;

public enum Status {

    NEW("Новая"),
    ACCEPTED("Принята"),
    IN_PROGRESS("В работе"),
    WAITING("Ожидает ответа"),
    DONE("Выполнена"),
    CLOSED("Закрыта");

    private final String label;

    Status(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}