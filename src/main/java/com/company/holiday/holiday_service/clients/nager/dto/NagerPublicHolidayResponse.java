package com.company.holiday.holiday_service.clients.nager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record NagerPublicHolidayResponse(

        @JsonProperty("date")
        LocalDate date,

        @JsonProperty("localName")
        String localName,

        @JsonProperty("name")
        String name,

        @JsonProperty("countryCode")
        String countryCode,

        @JsonProperty("fixed")
        boolean fixed,

        @JsonProperty("global")
        boolean global,

        @JsonProperty("counties")
        List<String> counties,

        @JsonProperty("launchYear")
        Integer launchYear,

        @JsonProperty("types")
        List<String> types

) { }
