/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package internal.app.packed.concurrent.cron;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.function.Function;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 *
 */
public class CronExpressionFieldTest {

    private static final Function<String, RuntimeException> EXCEPTION_SUPPLIER = message -> new RuntimeException(message);

    @Nested
    @DisplayName("Day of Month Field Tests")
    class DayOfMonthFieldTests {

        @Test
        @DisplayName("Should validate invalid day values")
        void testInvalidDayValues() {
            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("A", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid day value: A");

            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("0", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Day value must be between 1 and 31: 0");
        }

        @Test
        @DisplayName("Should handle basic L modifier")
        void testLastDayOfMonth() {
            long lastDay = CronExpression.parseDayOfMonth("L", EXCEPTION_SUPPLIER);
            assertThat(lastDay & (1L << CronExpression.DAY_OF_MONTH_L_BIT)).isNotZero(); // L bit should be set
            assertThat(Long.bitCount(lastDay)).isEqualTo(1);
        }

        @Test
        @DisplayName("Should validate L modifier combinations")
        void testLastDayOfMonthCombinations() {
            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("L-15", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid L modifier combination: L-15");

            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("L,15", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid L modifier combination: L,15");

            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("15-L", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid L modifier combination: 15-L");

            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("L/2", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid L modifier combination: L/2");
        }

        @Test
        @DisplayName("Should validate LW combination")
        void testLastWeekdayCombination() {
            // Valid LW usage
            long lastWeekday = CronExpression.parseDayOfMonth("LW", EXCEPTION_SUPPLIER);
            assertThat(lastWeekday & (1L << CronExpression.DAY_OF_MONTH_L_BIT)).isNotZero();
            assertThat(lastWeekday & (1L << CronExpression.DAY_OF_MONTH_W_BIT)).isNotZero();

            // Invalid combinations with LW
            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("LW-15", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid L modifier combination: LW-15");

            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("LW,15", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid L modifier combination: LW,15");
        }

        @Test
        @DisplayName("Should validate range values")
        void testRangeValidation() {
            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("0-31", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Value 0 is outside valid range [1,31] for field type 3");

            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("1-32", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Value 32 is outside valid range [1,31] for field type 3");

            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("5-3", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid range: start value cannot be greater than end value: 5-3");

            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("1--5", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid range format: 1--5");
        }

        @Test
        @DisplayName("Should validate step values")
        void testStepValueValidation() {
            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("1-31/0", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Step value cannot be less than 1, was 0");

            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("*/0", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Step value cannot be less than 1, was 0");

            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("1/X", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid step value: X");
        }

        @Test
        @DisplayName("Should handle valid patterns")
        void testValidPatterns() {
            // Test single value
            assertThatNoException().isThrownBy(() -> CronExpression.parseDayOfMonth("15", EXCEPTION_SUPPLIER));

            // Test range
            assertThatNoException().isThrownBy(() -> CronExpression.parseDayOfMonth("1-15", EXCEPTION_SUPPLIER));

            // Test list
            assertThatNoException().isThrownBy(() -> CronExpression.parseDayOfMonth("1,15,31", EXCEPTION_SUPPLIER));

            // Test step values
            assertThatNoException().isThrownBy(() -> CronExpression.parseDayOfMonth("1-30/5", EXCEPTION_SUPPLIER));

            // Test asterisk with step
            assertThatNoException().isThrownBy(() -> CronExpression.parseDayOfMonth("*/5", EXCEPTION_SUPPLIER));
        }

        @Test
        @DisplayName("Should validate W modifier combinations")
        void testWeekdayModifierCombinations() {
            // Valid W usage
            assertThatNoException().isThrownBy(() -> CronExpression.parseDayOfMonth("15W", EXCEPTION_SUPPLIER));

            // Invalid W positions
            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("W15", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("W modifier must be at the end of the value: W15");

            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("1,15W", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("W modifier cannot be used in lists: 1,15W");

            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("1-15W", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("W modifier cannot be used with ranges or steps: 1-15W");

            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("15W-20", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("W modifier must be at the end of the value: 15W-20");

            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("15W/2", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("W modifier must be at the end of the value: 15W/2");
        }

        @Test
        @DisplayName("Should validate numeric day values with W modifier")
        void testWeekdayNumericValidation() {
            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("0W", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Day value with W must be between 1 and 31: 0");

            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("32W", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Day value with W must be between 1 and 31: 32");

            assertThatThrownBy(() -> CronExpression.parseDayOfMonth("XW", EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid day value with W modifier: X");
        }
    }

    @Nested
    @DisplayName("Day of Week Field Tests")
    class DayOfWeekFieldTests {


        @Test
        @DisplayName("Should validate hash combinations")
        void testHashCombinations() {
            // Test invalid nth values
            assertThatThrownBy(() -> CronExpression.parseDayOfWeek("2#0", EXCEPTION_SUPPLIER))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Value 0 is outside valid range [1,7] for field type 5");

            assertThatThrownBy(() -> CronExpression.parseDayOfWeek("2#6", EXCEPTION_SUPPLIER))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid nth value: 6");

            // Test invalid day with hash
            assertThatThrownBy(() -> CronExpression.parseDayOfWeek("8#3", EXCEPTION_SUPPLIER))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Value 8 is outside valid range [1,7] for field type 5");

            // Test invalid hash format
            assertThatThrownBy(() -> CronExpression.parseDayOfWeek("2#3#4", EXCEPTION_SUPPLIER))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid day of week value: 2#3#4");
        }


        @Test
        @DisplayName("Should validate L modifier combinations")
        void testLModifierCombinations() {
            // Test invalid L combinations
            assertThatThrownBy(() -> CronExpression.parseDayOfWeek("1,5L", EXCEPTION_SUPPLIER))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid day-of-week format with L: 1,5L");

            assertThatThrownBy(() -> CronExpression.parseDayOfWeek("1-5L", EXCEPTION_SUPPLIER))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid day-of-week format with L: 1-5L");

            // Test invalid day with L
            assertThatThrownBy(() -> CronExpression.parseDayOfWeek("8L", EXCEPTION_SUPPLIER))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid day-of-week value with L: 8");

            // Test multiple L modifiers
            assertThatThrownBy(() -> CronExpression.parseDayOfWeek("1L,5L", EXCEPTION_SUPPLIER))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid day-of-week format with L: 1L,5L");
        }

        @ParameterizedTest
        @CsvSource({ "SUN,1", "MON,2", "TUE,3", "WED,4", "THU,5", "FRI,6", "SAT,7" })
        @DisplayName("Should parse day names")
        void testDayNames(String dayName, int dayNumber) {
            assertThat(CronExpression.parseDayOfWeek(dayName, EXCEPTION_SUPPLIER))
                    .isEqualTo(CronExpression.parseDayOfWeek(String.valueOf(dayNumber), EXCEPTION_SUPPLIER));
        }

        @Test
        @DisplayName("Should parse day ranges")
        void testDayRanges() {
            // Test weekdays
            long weekdays = CronExpression.parseDayOfWeek("MON-FRI", EXCEPTION_SUPPLIER);
            for (int day = 2; day <= 6; day++) {
                assertThat(weekdays & (1L << day)).isNotZero();
            }
            assertThat(weekdays & (1L << 1)).isZero(); // Sunday
            assertThat(weekdays & (1L << 7)).isZero(); // Saturday
        }

        @Test
        @DisplayName("Should parse last occurrence of specific day")
        void testLastOccurrence() {
            // Test last Friday (5L)
            long lastFriday = CronExpression.parseDayOfWeek("5L", EXCEPTION_SUPPLIER);
            assertThat(lastFriday & (1L << 8)).isNotZero(); // L bit
            assertThat(lastFriday & (1L << (5 + 8))).isNotZero(); // Day bit

            // Test last day of week (L)
            long lastDay = CronExpression.parseDayOfWeek("L", EXCEPTION_SUPPLIER);
            assertThat(lastDay & (1L << 8)).isNotZero();
        }

        @Test
        @DisplayName("Should parse nth occurrence of day")
        void testNthOccurrence() {
            // Test third Wednesday (4#3)
            long thirdWednesday = CronExpression.parseDayOfWeek("4#3", EXCEPTION_SUPPLIER);
            assertThat(thirdWednesday & (1L << 16)).isNotZero(); // # bit
            assertThat(thirdWednesday & (1L << (4 + (2 * 7) + 16))).isNotZero(); // nth day bit

            // Test first Monday (2#1)
            long firstMonday = CronExpression.parseDayOfWeek("2#1", EXCEPTION_SUPPLIER);
            assertThat(firstMonday & (1L << 16)).isNotZero(); // # bit
            assertThat(firstMonday & (1L << (2 + 16))).isNotZero(); // nth day bit
        }
    }

    @Nested
    @DisplayName("Hour Field Tests")
    class HourFieldTests {
        @Test
        @DisplayName("Should parse business hours")
        void testBusinessHours() {
            long hours = CronExpression.parseField("9-17", 2, EXCEPTION_SUPPLIER);
            assertThat(Long.bitCount(hours)).isEqualTo(9);
            for (int hour = 9; hour <= 17; hour++) {
                assertThat(hours & (1L << hour)).as("Hour %d should be set", hour).isNotZero();
            }
            assertThat(hours & (1L << 8)).as("Hour 8 should not be set").isZero();
            assertThat(hours & (1L << 18)).as("Hour 18 should not be set").isZero();
        }

        @Test
        @DisplayName("Should parse hour steps")
        void testHourSteps() {
            long evenHours = CronExpression.parseField("*/2", 2, EXCEPTION_SUPPLIER);
            for (int hour = 0; hour <= 22; hour += 2) {
                assertThat(evenHours & (1L << hour)).as("Hour %d should be set", hour).isNotZero();
            }
            for (int hour = 1; hour <= 23; hour += 2) {
                assertThat(evenHours & (1L << hour)).as("Hour %d should not be set", hour).isZero();
            }
        }
    }

    @Nested
    @DisplayName("Minute Field Tests")
    class MinuteFieldTests {
        @Test
        @DisplayName("Should parse all possible minute values")
        void testAllMinutes() {
            long minutes = CronExpression.parseField("*", 1, EXCEPTION_SUPPLIER);
            assertThat(Long.bitCount(minutes)).isEqualTo(60);
            for (int i = 0; i < 60; i++) {
                assertThat(minutes & (1L << i)).as("Minute %d should be set", i).isNotZero();
            }
        }

        @Test
        @DisplayName("Should parse complex minute patterns")
        void testComplexMinutePatterns() {
            // Test combination of ranges and lists
            long result = CronExpression.parseField("0-15,30,45-59", 1, EXCEPTION_SUPPLIER);

            // Check first range (0-15)
            for (int i = 0; i <= 15; i++) {
                assertThat(result & (1L << i)).isNotZero();
            }

            // Check single value (30)
            assertThat(result & (1L << 30)).isNotZero();

            // Check second range (45-59)
            for (int i = 45; i <= 59; i++) {
                assertThat(result & (1L << i)).isNotZero();
            }
        }

        @Test
        @DisplayName("Should handle minute steps with ranges")
        void testMinuteStepsWithRanges() {
            // Test steps within specific ranges
            long result = CronExpression.parseField("0-30/5", 1, EXCEPTION_SUPPLIER);
            for (int i = 0; i <= 30; i += 5) {
                assertThat(result & (1L << i)).as("Minute %d should be set", i).isNotZero();
            }
            assertThat(result & (1L << 35)).as("Minute 35 should not be set").isZero();
        }
    }

    @Nested
    @DisplayName("Month Field Tests")
    class MonthFieldTests {
        @ParameterizedTest
        @CsvSource({ "JAN,1", "FEB,2", "MAR,3", "APR,4", "MAY,5", "JUN,6", "JUL,7", "AUG,8", "SEP,9", "OCT,10", "NOV,11", "DEC,12" })
        @DisplayName("Should parse month names")
        void testMonthNames(String monthName, int monthNumber) {
            assertThat(CronExpression.parseMonth(monthName, EXCEPTION_SUPPLIER))
                    .isEqualTo(CronExpression.parseMonth(String.valueOf(monthNumber), EXCEPTION_SUPPLIER));
        }

        @Test
        @DisplayName("Should parse month ranges with names")
        void testMonthRangesWithNames() {
            // Test quarter ranges
            long firstQuarter = CronExpression.parseMonth("JAN-MAR", EXCEPTION_SUPPLIER);
            for (int month = 1; month <= 3; month++) {
                assertThat(firstQuarter & (1L << month)).isNotZero();
            }

            // Test multiple ranges and individual months
            long complexRange = CronExpression.parseMonth("MAR-MAY,SEP,NOV-DEC", EXCEPTION_SUPPLIER);
            for (int month : new int[] { 3, 4, 5, 9, 11, 12 }) {
                assertThat(complexRange & (1L << month)).as("Month %d should be set", month).isNotZero();
            }
        }
    }

    //////////////////

    @Nested
    @DisplayName("Parse Create Mask Tests")
    class ParseCreateMaskTests {
        @Test
        @DisplayName("Should create correct bit masks")
        void testCreateMask() {
            // Test basic ranges
            assertThat(CronExpression.parseCreateMask(0, 2, EXCEPTION_SUPPLIER))
                .isEqualTo(0b111L);
            assertThat(CronExpression.parseCreateMask(1, 4, EXCEPTION_SUPPLIER))
                .isEqualTo(0b11110L);

            // Test single value
            assertThat(CronExpression.parseCreateMask(0, 0, EXCEPTION_SUPPLIER))
                .isEqualTo(0b1L);

            // Test edge cases
            assertThat(CronExpression.parseCreateMask(0, 62, EXCEPTION_SUPPLIER))
                .isEqualTo((1L << 63) - 1);

            // Test invalid ranges
            assertThatThrownBy(() -> CronExpression.parseCreateMask(-1, 5, EXCEPTION_SUPPLIER))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid range: -1-5");

            assertThatThrownBy(() -> CronExpression.parseCreateMask(5, 64, EXCEPTION_SUPPLIER))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid range: 5-64");

            assertThatThrownBy(() -> CronExpression.parseCreateMask(5, 3, EXCEPTION_SUPPLIER))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Invalid range: 5-3");
        }
    }


    @Nested
    @DisplayName("Parse Number Tests")
    class ParseNumberTests {
        @Test
        @DisplayName("Should handle invalid numbers")
        void testInvalidNumbers() {
            // Test non-numeric values
            assertThatThrownBy(() -> CronExpression.parseNumber("abc", 0, EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid numeric value: abc");

            // Test out of range values for different types
            assertThatThrownBy(() -> CronExpression.parseNumber("60", 0, EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Value 60 is outside valid range [0,59] for field type 0");

            assertThatThrownBy(() -> CronExpression.parseNumber("24", 2, EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Value 24 is outside valid range [0,23] for field type 2");

            assertThatThrownBy(() -> CronExpression.parseNumber("0", 3, EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Value 0 is outside valid range [1,31] for field type 3");

            assertThatThrownBy(() -> CronExpression.parseNumber("13", 4, EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Value 13 is outside valid range [1,12] for field type 4");

            assertThatThrownBy(() -> CronExpression.parseNumber("8", 5, EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Value 8 is outside valid range [1,7] for field type 5");
        }

        @Test
        @DisplayName("Should parse valid numbers for different field types")
        void testValidNumbers() {
            // Test seconds/minutes (type 0,1)
            assertThat(CronExpression.parseNumber("0", 0, EXCEPTION_SUPPLIER)).isEqualTo(0);
            assertThat(CronExpression.parseNumber("59", 0, EXCEPTION_SUPPLIER)).isEqualTo(59);

            // Test hours (type 2)
            assertThat(CronExpression.parseNumber("0", 2, EXCEPTION_SUPPLIER)).isEqualTo(0);
            assertThat(CronExpression.parseNumber("23", 2, EXCEPTION_SUPPLIER)).isEqualTo(23);

            // Test day of month (type 3)
            assertThat(CronExpression.parseNumber("1", 3, EXCEPTION_SUPPLIER)).isEqualTo(1);
            assertThat(CronExpression.parseNumber("31", 3, EXCEPTION_SUPPLIER)).isEqualTo(31);

            // Test month (type 4)
            assertThat(CronExpression.parseNumber("1", 4, EXCEPTION_SUPPLIER)).isEqualTo(1);
            assertThat(CronExpression.parseNumber("12", 4, EXCEPTION_SUPPLIER)).isEqualTo(12);

            // Test day of week (type 5)
            assertThat(CronExpression.parseNumber("1", 5, EXCEPTION_SUPPLIER)).isEqualTo(1);
            assertThat(CronExpression.parseNumber("7", 5, EXCEPTION_SUPPLIER)).isEqualTo(7);
        }
    }

    @Nested
    @DisplayName("Parse Value Tests")
    class ParseValueTests {
        @Test
        @DisplayName("Should handle invalid names")
        void testInvalidNames() {
            assertThatThrownBy(() -> CronExpression.parseValue("FOO", 4, EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid numeric value: FOO");

            assertThatThrownBy(() -> CronExpression.parseValue("BAR", 5, EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid numeric value: BAR");
        }

        @Test
        @DisplayName("Should parse day and month names")
        void testNameParsing() {
            // Test month names
            assertThat(CronExpression.parseValue("JAN", 4, EXCEPTION_SUPPLIER)).isEqualTo(1);
            assertThat(CronExpression.parseValue("DEC", 4, EXCEPTION_SUPPLIER)).isEqualTo(12);

            // Test day names
            assertThat(CronExpression.parseValue("SUN", 5, EXCEPTION_SUPPLIER)).isEqualTo(1);
            assertThat(CronExpression.parseValue("SAT", 5, EXCEPTION_SUPPLIER)).isEqualTo(7);

            // Test case insensitivity
            assertThat(CronExpression.parseValue("jan", 4, EXCEPTION_SUPPLIER)).isEqualTo(1);
            assertThat(CronExpression.parseValue("sun", 5, EXCEPTION_SUPPLIER)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Second Field Tests")
    class SecondFieldTests {
        @Test
        @DisplayName("Should parse all possible second values")
        void testAllSeconds() {
            long seconds = CronExpression.parseField("*", 0, EXCEPTION_SUPPLIER);
            assertThat(Long.bitCount(seconds)).isEqualTo(60);
            for (int i = 0; i < 60; i++) {
                assertThat(seconds & (1L << i)).as("Second %d should be set", i).isNotZero();
            }
        }

        @ParameterizedTest
        @ValueSource(strings = { "0", "15", "30", "45", "59" })
        @DisplayName("Should parse individual seconds")
        void testIndividualSeconds(String second) {
            long result = CronExpression.parseField(second, 0, EXCEPTION_SUPPLIER);
            assertThat(Long.bitCount(result)).isEqualTo(1);
            assertThat(result & (1L << Integer.parseInt(second))).isNotZero();
        }

        @Test
        @DisplayName("Should handle invalid second values")
        void testInvalidSeconds() {
            // Test out of range values
            assertThatThrownBy(() -> CronExpression.parseField("60", 0, EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Value 60 is outside valid range [0,59] for field type 0");

            // Test invalid steps
            assertThatThrownBy(() -> CronExpression.parseField("*/0", 0, EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Step value cannot be less than 1, was 0");

            // Test invalid ranges
            assertThatThrownBy(() -> CronExpression.parseField("5-3", 0, EXCEPTION_SUPPLIER)).isInstanceOf(RuntimeException.class)
                    .hasMessage("Invalid range: start value cannot be greater than end value: 5-3");
        }

        @Test
        @DisplayName("Should parse second ranges")
        void testSecondRanges() {
            // Test various ranges
            assertThat(CronExpression.parseField("0-5", 0, EXCEPTION_SUPPLIER)).isEqualTo(0b111111L);
            assertThat(CronExpression.parseField("58-59", 0, EXCEPTION_SUPPLIER)).isEqualTo(((1L << 59) | (1L << 58)));
            assertThat(CronExpression.parseField("0-59", 0, EXCEPTION_SUPPLIER)).isEqualTo(CronExpression.parseField("*", 0, EXCEPTION_SUPPLIER));
        }

        @Test
        @DisplayName("Should parse second steps")
        void testSecondSteps() {
            // Test various step values
            long every15Seconds = CronExpression.parseField("*/15", 0, EXCEPTION_SUPPLIER);
            assertThat(Long.bitCount(every15Seconds)).isEqualTo(4);
            for (int second : new int[] { 0, 15, 30, 45 }) {
                assertThat(every15Seconds & (1L << second)).isNotZero();
            }

            // Test steps with ranges
            long rangeWithStep = CronExpression.parseField("0-30/10", 0, EXCEPTION_SUPPLIER);
            assertThat(Long.bitCount(rangeWithStep)).isEqualTo(4);
            for (int second : new int[] { 0, 10, 20, 30 }) {
                assertThat(rangeWithStep & (1L << second)).isNotZero();
            }
        }
    }
}
