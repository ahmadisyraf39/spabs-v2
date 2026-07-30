package com.ahmadisyraf39.spabs_v2.progress.entity.enums;

public enum ModuleProgressStatus {
    NOT_STARTED(0),
    STARTED(25),
    IN_PROGRESS(50),
    ALMOST_COMPLETE(75),
    COMPLETED(100);

    private final int percentage;

    ModuleProgressStatus(int percentage) {
        this.percentage = percentage;
    }

    public int getPercentage() {
        return percentage;
    }
}
