package de.bushnaq.abdalla.projecthub.dto;

public enum TaskMode {
    MANUALLY_SCHEDULED(0),
    AUTO_SCHEDULED(1);

    private final int type;

    TaskMode(int type) {
        this.type = type;
    }

    public int getValue() {
        return type;
    }
}
