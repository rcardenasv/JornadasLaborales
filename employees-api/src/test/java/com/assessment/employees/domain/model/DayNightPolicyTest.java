package com.assessment.employees.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DayNightPolicyTest {
    @Test
    void shouldUseLegacyWindowBeforeLaw2466Of2025() {
        LocalDate shiftDate = LocalDate.of(2025, 12, 24);

        assertEquals(LocalTime.of(6, 0), DayNightPolicy.daytimeStart(shiftDate));
        assertEquals(LocalTime.of(21, 0), DayNightPolicy.daytimeEnd(shiftDate));
        assertEquals(LocalTime.of(21, 0), DayNightPolicy.nighttimeStart(shiftDate));
    }

    @Test
    void shouldUseExtendedNightWindowFromLaw2466Of2025() {
        LocalDate shiftDate = LocalDate.of(2025, 12, 25);

        assertEquals(LocalTime.of(6, 0), DayNightPolicy.daytimeStart(shiftDate));
        assertEquals(LocalTime.of(19, 0), DayNightPolicy.daytimeEnd(shiftDate));
        assertEquals(LocalTime.of(19, 0), DayNightPolicy.nighttimeStart(shiftDate));
    }

    @Test
    void shouldKeepDaytimeStartStableAcrossVigencias() {
        assertEquals(DayNightPolicy.daytimeStart(LocalDate.of(2020, 1, 1)),
            DayNightPolicy.daytimeStart(LocalDate.of(2030, 1, 1)));
    }
}
