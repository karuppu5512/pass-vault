package com.passvault.enums;

import java.time.temporal.ChronoUnit;

public enum RotationUnit {
    MINUTES(ChronoUnit.MINUTES),
    HOURS(ChronoUnit.HOURS),
    DAYS(ChronoUnit.DAYS);

    private final ChronoUnit chronoUnit;

    RotationUnit(ChronoUnit chronoUnit) {
        this.chronoUnit = chronoUnit;
    }

    public ChronoUnit toChronoUnit() {
        return chronoUnit;
    }
}
