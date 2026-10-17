package com.assessment.employees.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DayNightPolicyTest {
    private static final LocalTime DAYTIME_START = LocalTime.of(6, 0);

    @Test
    void shouldUseLegacyWindowBeforeLaw2466Of2025() {
        LocalDate shiftDate = LocalDate.of(2025, 12, 24);

        assertEquals(DAYTIME_START, DayNightPolicy.daytimeStart());
        assertEquals(LocalTime.of(21, 0), DayNightPolicy.daytimeEnd(shiftDate));
        assertEquals(LocalTime.of(21, 0), DayNightPolicy.nighttimeStart(shiftDate));
    }

    @Test
    void shouldUseExtendedNightWindowFromLaw2466Of2025() {
        LocalDate shiftDate = LocalDate.of(2025, 12, 25);

        assertEquals(DAYTIME_START, DayNightPolicy.daytimeStart());
        assertEquals(LocalTime.of(19, 0), DayNightPolicy.daytimeEnd(shiftDate));
        assertEquals(LocalTime.of(19, 0), DayNightPolicy.nighttimeStart(shiftDate));
    }
}
