package com.company.holiday.holiday_service.api.domain;

import java.time.LocalDate;
import java.time.ZoneId;

public final class HolidayYearRangeCalculator {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private HolidayYearRangeCalculator() {}

    public record YearRange(int fromYear, int toYear) {}

    public static YearRange recentYears(int years) {
        int thisYear = LocalDate.now(KST).getYear();
        return new YearRange(thisYear - years + 1, thisYear);
    }

    public static YearRange lastFiveYears() {
        return recentYears(5);
    }

    public static YearRange lastTwoYears() {
        return recentYears(2);
    }

}
