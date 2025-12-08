package com.company.holiday.holiday_service.clients.nager;

import com.company.holiday.holiday_service.clients.nager.dto.NagerAvailableCountryResponse;
import com.company.holiday.holiday_service.clients.nager.dto.NagerPublicHolidayResponse;

import java.util.List;

public interface NagerClient {

    List<NagerAvailableCountryResponse> getAvailableCountries();

    List<NagerPublicHolidayResponse> getPublicHolidays(int year, String countryCode);

}
