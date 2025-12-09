package com.company.holiday.holiday_service.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HolidayType {

    PUBLIC("Public"),
    BANK("Bank"),
    SCHOOL("School"),
    AUTHORITIES("Authorities"),
    OPTIONAL("Optional"),
    OBSERVANCE("Observance");

    private final String value;

    public static HolidayType of(String value) {
        for (HolidayType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown holiday type: " + value);
    }

}
