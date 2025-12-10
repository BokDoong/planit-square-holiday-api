package com.company.holiday.holiday_service.api.domain;

import com.company.holiday.holiday_service.global.error.ErrorCode;
import com.company.holiday.holiday_service.global.error.exception.InvalidValueException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static com.company.holiday.holiday_service.api.domain.HolidayYearRangeCalculator.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "holiday",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_holiday_country_date_local_name",
                        columnNames = {"country_id", "date", "local_name"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_holiday_country_date",
                        columnList = "country_id, date"
                )
        }
)
@EntityListeners(AuditingEntityListener.class)
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "local_name", length = 200, nullable = false)
    private String localName;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "is_global", nullable = false)
    private boolean global;

    @Column(name = "is_fixed", nullable = false)
    private boolean fixed;

    @Column(name = "launch_year")
    private Integer launchYear;

    @Column(name = "types_raw", length = 200)
    private String typesRaw;

    @Column(name = "counties_raw", length = 200)
    private String countiesRaw;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Holiday(
            Country country,
            LocalDate date, String localName, String name,
            boolean global, boolean fixed,
            Integer launchYear, String typesRaw, String countiesRaw
    ) {
        this.country = country;
        this.date = date;
        this.localName = localName;
        this.name = name;
        this.global = global;
        this.fixed = fixed;
        this.launchYear = launchYear;
        this.typesRaw = typesRaw;
        this.countiesRaw = countiesRaw;
    }

    public static Holiday of(
            Country country,
            LocalDate date, String localName, String name,
            boolean global, boolean fixed,
            Integer launchYear, String typesRaw, String countiesRaw
    ) {
        return Holiday.builder()
                .country(country)
                .date(date)
                .localName(localName)
                .name(name)
                .global(global)
                .fixed(fixed)
                .launchYear(launchYear)
                .typesRaw(typesRaw)
                .countiesRaw(countiesRaw)
                .build();
    }

    public List<HolidayType> getTypes() {
        if (typesRaw == null || typesRaw.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(typesRaw.split(","))
                .map(HolidayType::of)
                .toList();
    }

    public List<String> getCounties() {
        if (countiesRaw == null || countiesRaw.isEmpty()) {
            return List.of();
        }
        return List.of(countiesRaw.split(","));
    }

    public static boolean verifyYearInRecentFiveYears(int year) {

        if (year < lastFiveYears().fromYear() || year > lastFiveYears().toYear()) {
            throw new InvalidValueException(ErrorCode.YEAR_OUT_OF_RANGE, "year=" + year + " (허용 범위: 2021~2025)");
        }
        return true;
    }

}
