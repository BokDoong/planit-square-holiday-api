package com.company.holiday.holiday_service.clients.nager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NagerAvailableCountryResponse(

        @JsonProperty("countryCode")
        String countryCode,

        @JsonProperty("name")
        String name

) { }
