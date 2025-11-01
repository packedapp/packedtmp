package app.packed.concurrent.cron.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CronExpressionTest {

    @Test
    void testBasicExpressionParsing() {

        CronExpression.of("0 0 12 * * L#3");

        // Test valid expressions
        assertThat(CronExpression.isValid("0 0 12 * * ?")).isTrue();
        assertThat(CronExpression.isValid("0 15 10 ? * MON-FRI")).isTrue();
        assertThat(CronExpression.isValid("0 0 12 L * ?")).isTrue();

        // Test invalid expressions
        assertThat(CronExpression.isValid("")).isFalse();
        assertThat(CronExpression.isValid("* * * *")).isFalse();
        assertThat(CronExpression.isValid("invalid")).isFalse();
        assertThat(CronExpression.isValid("* * * * * *")).isFalse(); // Missing seconds
    }

    @Test
    void testInvalidExpressionThrowsException() {
        assertThatThrownBy(() -> CronExpression.of(""))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> CronExpression.of("* * * * * * * *"))
            .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> CronExpression.of("invalid"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testEqualsAndHashCode() {
        CronExpression expr1 = CronExpression.of("0 0 12 * * *");
        CronExpression expr2 = CronExpression.of("0 0 12 * * *");
        CronExpression expr3 = CronExpression.of("0 0 13 * * *");

        assertThat(expr1)
            .isEqualTo(expr2)
            .hasSameHashCodeAs(expr2)
            .isNotEqualTo(expr3)
            .isNotEqualTo(null)
            .isNotEqualTo(new Object());
    }

    @Test
    void testToString() {
        String cronStr = "0 0 12 * * *";
        CronExpression expr = CronExpression.of(cronStr);
        assertThat(expr.toString()).contains(cronStr);
    }

    @Test
    void testCronExpressions() {
        // Basic expressions
        assertValid("0 0 12 * * ?");           // Every day at noon
        assertValid("0 15 10 ? * MON-FRI");    // 10:15 AM Mon-Fri
        assertValid("0 0 0 1 1 ? *");          // Every January 1st at midnight

        // Every possible special character
        assertValid("* * * * * ?");            // Every second
        assertValid("*/5 * * * * ?");          // Every 5 seconds
        assertValid("0-4,8-12 * * * * ?");     // Ranges and lists
        assertValid("0 0 0 L * ?");            // Last day of month
        assertValid("0 0 0 LW * ?");           // Last weekday of month
        assertValid("0 0 0 ? * L");            // Last day of week
        assertValid("0 0 0 ? * 2#1");          // First Monday
        assertValid("0 0 0 1W * ?");           // Nearest weekday to 1st

        // Edge cases for time fields
        assertValid("0 0 0 * * ?");            // Midnight
        assertValid("59 59 23 * * ?");         // Last second of day
        assertValid("*/59 */59 */23 * * ?");   // Maximum steps
        assertValid("0,59 0,59 0,23 * * ?");   // Min/max values in lists

        // Complex day of month patterns
        assertValid("0 0 12 1-7,15,L * ?");    // First week, 15th, and last
        assertValid("0 0 12 LW * ?");          // Last weekday
        assertValid("0 0 12 15W * ?");         // Nearest weekday to 15th
        assertValid("0 0 12 1W,15W * ?");      // Multiple W modifiers
        assertValid("0 0 12 L-3 * ?");         // 3 days before end
        assertValid("0 0 12 L-5W * ?");        // Weekday 5 days before end

        // Complex day of week patterns
        assertValid("0 0 12 ? * MON#1");       // First Monday
        assertValid("0 0 12 ? * SUN#5");       // Fifth Sunday
        assertValid("0 0 12 ? * MON#1,FRI#L"); // First Monday and last Friday
        assertValid("0 0 12 ? * MON-FRI");     // Weekdays
        assertValid("0 0 12 ? * SAT,SUN");     // Weekends

        // Month patterns
        assertValid("0 0 12 * JAN,FEB,MAR ?"); // First quarter
        assertValid("0 0 12 * JAN-JUN ?");     // First half year
        assertValid("0 0 12 * */3 ?");         // Every 3rd month
        assertValid("0 0 12 * 1,6,12 ?");      // Specific months

        // Year patterns
        assertValid("0 0 12 * * ? 2024");      // Specific year
        assertValid("0 0 12 * * ? 2024-2025"); // Year range
        assertValid("0 0 12 * * ? 2024,2026"); // Specific years
        assertValid("0 0 12 * * ? *");         // Any year

        // Mix of patterns
        assertValid("*/15 */30 */6 1-15 * ?");          // Complex time with simple date
        assertValid("0 0 12 1-5 JAN-JUN MON-FRI");      // Workdays in first half year
        assertValid("0 0 12 L * MON-FRI");              // Last workday of month
        assertValid("0 0 12 ? * MON#1,WED#2,FRI#3");    // Multiple nth days

        // Invalid expressions that should fail
        assertInvalid("");                               // Empty string
        assertInvalid("* * *");                         // Too few fields
        assertInvalid("* * * * * * * *");              // Too many fields
        assertInvalid("a 0 12 * * ?");                 // Invalid characters
        assertInvalid("-1 0 12 * * ?");                // Below minimum value
        assertInvalid("60 0 12 * * ?");                // Above maximum value
        assertInvalid("0 0 24 * * ?");                 // Invalid hour
        assertInvalid("0 0 12 32 * ?");                // Invalid day of month
        assertInvalid("0 0 12 0 * ?");                 // Zero day of month
        assertInvalid("0 0 12 * 13 ?");                // Invalid month
        assertInvalid("0 0 12 * 0 ?");                 // Zero month
        assertInvalid("0 0 12 * * 8");                 // Invalid day of week
        assertInvalid("0 0 12 * * ?#6");               // Invalid nth value
        assertInvalid("0 0 12 * * L#3");               // L with #
        assertInvalid("0 0 12 LW,15W * ?");            // Multiple LW
        assertInvalid("0 0 12 * * ? 1969");            // Year below minimum
        assertInvalid("0 0 12 * * ? 2138");            // Year above maximum
        assertInvalid("0 0 12 ? * ?");                 // Both ? fields
        assertInvalid("0 0 12 * * *");                 // No ? field
        assertInvalid("0 0 12 L * L");                 // L in both fields
        assertInvalid("0 0 12 * * MON#6");             // nth value too high
        assertInvalid("0 0 12 * * MON#0");             // nth value too low

        // Edge cases for parsing
        assertValid("0    0   12    *    *    ?");     // Extra spaces
        assertValid("0,0,0 0 12 * * ?");               // Duplicate values
        assertValid("0 0 12 * * ? 2024,2024");         // Duplicate years
        assertValid("\t0\t0\t12\t*\t*\t?");           // Tabs
        assertValid("000 000 012 * * ?");              // Leading zeros
        assertValid("0 0 12 * * SUN-SAT");             // Full week range
        assertValid("0 0 12 * JAN-DEC ?");             // Full month range
        assertValid("59 59 23 31 12 ? 2099");          // All maximum values
        assertValid("0 0 0 1 1 ? 1970");               // All minimum values

        // Complex combinations
        assertValid("*/15,30-45 */30,15-25 0-12/2 1-15/3,L-3 JAN-JUN/2 MON#1-FRI#3");
        assertValid("0,15,30,45 0,30 0,12 1,15,L * MON,WED,FRI");
        assertValid("5-10,20-25,40-45,50-55 */15 8-16/2 ? * MON-FRI");
        assertValid("0 15 10 ? * MON-FRI 2024-2025");
        assertValid("0 0/5 14,18 * * ?");              // Every 5 mins between 2-6 PM
        assertValid("0 0/15 */2 1/2 * ?");             // Every other hour on odd days
        assertValid("0 0 12 1/7 * ?");                 // Every 7 days starting on 1st
        assertValid("0 0 12 1-7 * SUN");               // Sundays in first week

        // Expressions with special significance
        assertValid("0 30 9 ? * MON-FRI");             // Daily scrum at 9:30 AM
        assertValid("0 0 12 1 * ?");                   // Monthly maintenance
        assertValid("0 0 0 1 1 ? *");                  // New Year
        assertValid("0 0 0 25 12 ?");                  // Christmas
        assertValid("0 0 0 L 12 ?");                   // New Year's Eve
        assertValid("0 0 9-17 ? * MON-FRI");           // Business hours
        assertValid("0 0 0 1 */3 ?");                  // Quarterly tasks
        assertValid("0 0 12 ? * 2#1");                 // First Monday lunch
        assertValid("30 30 1 ? * MON-FRI");            // Night batch at 1:30:30 AM
        assertValid("0 */30 9-17 ? * MON-FRI");        // Every 30 mins during work
    }

    private void assertValid(String expression) {
            CronExpression.of(expression);
    }

    private void assertInvalid(String expression) {
        assertThrows(IllegalArgumentException.class, () -> CronExpression.of(expression),
                    "Expression should be invalid: " + expression);
    }
}