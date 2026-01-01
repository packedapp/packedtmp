package app.packed.concurrent.cron.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests for CronExpression validation and matching logic.
 */
class CronExpressionFieldTest {

    private LocalDateTime at(int y, int m, int d, int h, int min) {
        return LocalDateTime.of(y, m, d, h, min, 0);
    }

    private LocalDateTime at(int y, int m, int d, int h, int min, int sec) {
        return LocalDateTime.of(y, m, d, h, min, sec);
    }

    @Nested
    @DisplayName("Strict Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should throw exception for out-of-range values")
        void testRangeValidation() {
            // Seconds 60 is invalid
            assertThatThrownBy(() -> CronExpression.of("60 * * * * ?"))
                    .isInstanceOf(RuntimeException.class);

            // Hour 24 is invalid
            assertThatThrownBy(() -> CronExpression.of("0 0 24 * * ?"))
                    .isInstanceOf(RuntimeException.class);

            // Day 32 is invalid
            assertThatThrownBy(() -> CronExpression.of("0 0 0 32 * ?"))
                    .isInstanceOf(RuntimeException.class);

            // Month 13 is invalid
            assertThatThrownBy(() -> CronExpression.of("0 0 0 1 13 ?"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should throw exception for invalid characters")
        void testInvalidCharacters() {
            assertThatThrownBy(() -> CronExpression.of("0 0 0 A * ?"))
                    .isInstanceOf(RuntimeException.class);

            assertThatThrownBy(() -> CronExpression.of("0 0 0 1 FOO ?"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should throw exception for invalid step or range logic")
        void testLogicValidation() {
            // Step of 0
            assertThatThrownBy(() -> CronExpression.of("*/0 * * * * ?"))
                    .isInstanceOf(RuntimeException.class);

            // Range start > end
            assertThatThrownBy(() -> CronExpression.of("0 0 0 10-5 * ?"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Day of Month (DOM) Logic")
    class DayOfMonthTests {

        @Test
        @DisplayName("Should handle 'L' (Last Day) correctly for different months")
        void testLastDayOfMonth() {
            CronExpression cron = CronExpression.of("0 0 0 L * ?");

            assertThat(cron.isValid(at(2024, 1, 31, 0, 0))).isTrue();  // Jan
            assertThat(cron.isValid(at(2024, 2, 29, 0, 0))).isTrue();  // Feb (Leap)
            assertThat(cron.isValid(at(2023, 2, 28, 0, 0))).isTrue();  // Feb (Non-leap)
            assertThat(cron.isValid(at(2024, 4, 30, 0, 0))).isTrue();  // April
            assertThat(cron.isValid(at(2024, 4, 29, 0, 0))).isFalse();
        }

        @Test
        @DisplayName("Should handle 'W' (Nearest Weekday) logic")
        void testNearestWeekday() {
            // 20W: If 20th is Sat, fire on Fri 19th. If 20th is Sun, fire on Mon 21st.
            CronExpression cron = CronExpression.of("0 0 0 20W * ?");

            // Jan 20, 2024 is Saturday. Nearest weekday is Friday the 19th.
            assertThat(cron.isValid(at(2024, 1, 19, 0, 0))).isTrue();
            assertThat(cron.isValid(at(2024, 1, 20, 0, 0))).isFalse();

            // Jan 21, 2024 is Sunday. Nearest weekday is Monday the 22nd.
            CronExpression cron2 = CronExpression.of("0 0 0 21W * ?");
            assertThat(cron2.isValid(at(2024, 1, 22, 0, 0))).isTrue();
        }
    }

    @Nested
    @DisplayName("Day of Week (DOW) Logic")
    class DayOfWeekTests {

        @ParameterizedTest
        @CsvSource({ "MON,1", "TUE,2", "WED,3", "THU,4", "FRI,5", "SAT,6", "SUN,7" })
        @DisplayName("Should map names and numbers to same DOW")
        void testDowMapping(String name, int num) {
            CronExpression cronName = CronExpression.of("0 0 0 ? * " + name);
            CronExpression cronNum = CronExpression.of("0 0 0 ? * " + num);

            // Jan 1, 2024 is a Monday
            LocalDateTime start = at(2024, 1, 1, 0, 0);
            for (int i = 0; i < 7; i++) {
                LocalDateTime target = start.plusDays(i);
                assertThat(cronName.isValid(target)).isEqualTo(cronNum.isValid(target));
            }
        }

        @Test
        @DisplayName("Should handle 'L' suffix (Last occurrence of DOW)")
        void testLastDow() {
            // Last Friday of Jan 2024 is the 26th
            CronExpression cron = CronExpression.of("0 0 0 ? * FRIL");
            assertThat(cron.isValid(at(2024, 1, 26, 0, 0))).isTrue();
            assertThat(cron.isValid(at(2024, 1, 19, 0, 0))).isFalse();
        }

        @Test
        @DisplayName("Should handle '#' (Nth occurrence of DOW)")
        void testNthDow() {
            // 2nd Monday of Jan 2024 is the 8th
            CronExpression cron = CronExpression.of("0 0 0 ? * MON#2");
            assertThat(cron.isValid(at(2024, 1, 8, 0, 0))).isTrue();
            assertThat(cron.isValid(at(2024, 1, 1, 0, 0))).isFalse(); // 1st Mon
            assertThat(cron.isValid(at(2024, 1, 15, 0, 0))).isFalse(); // 3rd Mon
        }
    }

    @Nested
    @DisplayName("Time and Date Field Logic")
    class FieldLogicTests {

        @Test
        @DisplayName("Should handle steps (/) and ranges (-)")
        void testStepsAndRanges() {
            // Every 15 seconds, between minute 0 and 30
            CronExpression cron = CronExpression.of("*/15 0-30 * * * ?");

            assertThat(cron.isValid(at(2024, 1, 1, 12, 10, 0))).isTrue();
            assertThat(cron.isValid(at(2024, 1, 1, 12, 10, 15))).isTrue();
            assertThat(cron.isValid(at(2024, 1, 1, 12, 10, 10))).isFalse(); // Not a step of 15
            assertThat(cron.isValid(at(2024, 1, 1, 12, 31, 0))).isFalse();  // Out of range (0-30)
        }

        @Test
        @DisplayName("Should handle month names")
        void testMonthNames() {
            CronExpression cron = CronExpression.of("0 0 0 1 JAN,MAR,DEC ?");
            assertThat(cron.isValid(at(2024, 1, 1, 0, 0))).isTrue();
            assertThat(cron.isValid(at(2024, 3, 1, 0, 0))).isTrue();
            assertThat(cron.isValid(at(2024, 12, 1, 0, 0))).isTrue();
            assertThat(cron.isValid(at(2024, 2, 1, 0, 0))).isFalse();
        }

        @Test
        @DisplayName("Should respect Year field if provided")
        void testYearField() {
            CronExpression cron = CronExpression.of("0 0 0 1 1 ? 2024-2025");
            assertThat(cron.isValid(at(2024, 1, 1, 0, 0))).isTrue();
            assertThat(cron.isValid(at(2025, 1, 1, 0, 0))).isTrue();
            assertThat(cron.isValid(at(2023, 1, 1, 0, 0))).isFalse();
            assertThat(cron.isValid(at(2026, 1, 1, 0, 0))).isFalse();
        }
    }

    @Nested
    @DisplayName("Execution Time Logic")
    class ExecutionTests {

        @Test
        @DisplayName("Next execution should jump correctly")
        void testNext() {
            // Noon on the 1st of every month
            CronExpression cron = CronExpression.of("0 0 12 1 * ?");
            LocalDateTime start = at(2024, 1, 1, 13, 0); // After noon on Jan 1st

            LocalDateTime next = cron.next(start);
            assertThat(next).isEqualTo(at(2024, 2, 1, 12, 0));
        }

        @Test
        @DisplayName("Should handle year wrap-around in next()")
        void testNextYearWrap() {
            // Midnight on Jan 1st
            CronExpression cron = CronExpression.of("0 0 0 1 1 ?");
            LocalDateTime start = at(2024, 12, 31, 23, 59, 59);

            LocalDateTime next = cron.next(start);
            assertThat(next).isEqualTo(at(2025, 1, 1, 0, 0));
        }
    }
}