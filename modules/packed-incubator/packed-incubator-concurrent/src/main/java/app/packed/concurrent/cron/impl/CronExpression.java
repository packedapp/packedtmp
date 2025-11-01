package app.packed.concurrent.cron.impl;

import static java.util.Objects.requireNonNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A class representing a cron expression that can be used to determine scheduling times. This implementation supports
 * standard cron features plus additional ones like 'L' and 'W' modifiers.
 *
 * <p>
 * The expression format is: [seconds] [minutes] [hours] [day of month] [month] [day of week] [year]
 *
 * <p>
 * Supported special characters:
 * <ul>
 * <li>* - matches all values
 * <li>? - matches all values (used in day of month/week fields)
 * <li>, - value list separator
 * <li>- - range of values
 * <li>/ - step values
 * <li>L - last day of month/week
 * <li>W - nearest weekday
 * <li># - nth day of month (used in day of week field)
 * </ul>
 *
 * <p>
 * Examples:
 * <ul>
 * <li>"0 0 12 * * ?" - Fire at 12:00 PM (noon) every day
 * <li>"0 15 10 ? * MON-FRI" - Fire at 10:15 AM every Monday through Friday
 * <li>"0 0 12 L * ?" - Fire at noon on the last day of every month
 * </ul>
 */
public final class CronExpression {

    /** The minimum year supported for the year field. */
    private static final int MINIMUM_YEAR = 1970;

    /** The maximum year supported for the year field. */
    private static final int MAXIMUM_YEAR = 2137;

    /** Number of longs needed to store all years (each long stores 64 years) */
    private static final int YEARS_ARRAY_SIZE = 4; // Covers ~256 years
    private static final int BITS_PER_LONG = 64;

    // Bit flags for special day handling
    static final int DAY_OF_MONTH_L_BIT = 0; // Last day of month
    static final int DAY_OF_MONTH_W_BIT = 32; // Nearest weekday
    private static final int DAY_OF_WEEK_L_BIT = 8; // Last specific day of month
    private static final int DAY_OF_WEEK_HASH_BIT = 16; // Nth day of month

    // Combine related special day bits into single constants
    private static final int SPECIAL_DAY_BITS = 0x5000_0000; // Combines L and W bits
    private static final int SPECIAL_WEEK_BITS = 0x0018_0000; // Combines L# and # bits

    /** Lower bounds for each field type (seconds, minutes, hours, day, month, weekday, year) */
    private static final int[] LOWER_BOUNDS = { 0, 0, 0, 1, 1, 1, MINIMUM_YEAR };

    /** Upper bounds for each field type */
    private static final int[] UPPER_BOUNDS = { 59, 59, 23, 31, 12, 7, MAXIMUM_YEAR };

    /** Three-letter month names used for parsing */
    private static final String[] MONTH_NAMES = { "JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC" };

    /** Three-letter day names used for parsing */
    private static final String[] DAY_NAMES = { "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT" };

    /** The original cron expression string */
    private final String expression;

    /** Flag indicating if this expression uses special day handling (L or W) */
    private final boolean hasSpecialDayHandling;

    /** Flag indicating if this expression uses special week handling (L# or #) */
    private final boolean hasSpecialWeekHandling;

    // Bit fields for time components
    private final long seconds; // Bits 0-59 represent active seconds
    private final long minutes; // Bits 0-59 represent active minutes
    private final long hours; // Bits 0-23 represent active hours
    private final long dayOfMonth; // Bits 0-31 represent active days, plus special bits
    private final long month; // Bits 1-12 represent active months
    private final long dayOfWeek; // Bits 1-7 represent days (SUN-SAT), plus special bits
    private final long[] years; // Array of longs for year bits, null if not specified

    /**
     * Constructs a new CronExpression with the specified components.
     *
     * @param expression
     *            the original cron expression string
     * @param seconds
     *            bit field representing active seconds
     * @param minutes
     *            bit field representing active minutes
     * @param hours
     *            bit field representing active hours
     * @param dayOfMonth
     *            bit field representing active days of month
     * @param month
     *            bit field representing active months
     * @param dayOfWeek
     *            bit field representing active days of week
     * @param years
     *            array of bit fields representing active years, or null for all years
     */
    private CronExpression(String expression, long seconds, long minutes, long hours, long dayOfMonth, long month, long dayOfWeek, long[] years) {
        this.expression = requireNonNull(expression, "expression cannot be null");
        this.seconds = seconds;
        this.minutes = minutes;
        this.hours = hours;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = dayOfWeek;
        this.month = month;
        this.years = years;
        this.hasSpecialDayHandling = (dayOfMonth & SPECIAL_DAY_BITS) != 0;
        this.hasSpecialWeekHandling = (dayOfWeek & SPECIAL_WEEK_BITS) != 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CronExpression other)) {
            return false;
        }
        return Objects.equals(expression, other.expression);
    }

    /** @return the string used to create this expression */
    public String expression() {
        return expression;
    }

    /**
     * Finds the next potentially valid datetime by incrementing different components.
     */
    private LocalDateTime findNextValidDateTime(LocalDateTime current) {
        if (!isValidYear(current.getYear())) {
            return current.withMonth(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).plusYears(1);
        }

        if (!isSet(month, current.getMonthValue())) {
            return current.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).plusMonths(1);
        }

        if (!isValidDay(current.toLocalDate())) {
            return current.withHour(0).withMinute(0).withSecond(0).plusDays(1);
        }

        return incrementTime(current);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression);
    }

    /**
     * Increments the time components to find the next valid time.
     */
    private LocalDateTime incrementTime(LocalDateTime time) {
        int hour = nextSetBit(hours, time.getHour());
        if (hour <= time.getHour()) {
            return time.plusDays(1).withHour(nextSetBit(hours, 0)).withMinute(nextSetBit(minutes, 0)).withSecond(nextSetBit(seconds, 0));
        }

        int minute = nextSetBit(minutes, time.getMinute());
        if (minute <= time.getMinute()) {
            return time.withHour(hour).withMinute(nextSetBit(minutes, 0)).withSecond(nextSetBit(seconds, 0));
        }

        int second = nextSetBit(seconds, time.getSecond());
        if (second <= time.getSecond()) {
            return time.withHour(hour).withMinute(minute).withSecond(nextSetBit(seconds, 0));
        }

        return time.withHour(hour).withMinute(minute).withSecond(second);
    }

    /**
     * Validates if a date matches the 'W' (nearest weekday) modifier.
     */
    private boolean isNearestWeekdayValid(LocalDate date, DayOfWeek dayOfWeek, int dayOfMonth) {
        if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
            return isSet(this.dayOfMonth + DAY_OF_MONTH_W_BIT, dayOfMonth);
        }

        boolean isLastDayOfMonth = date.getDayOfMonth() == date.lengthOfMonth();

        if (dayOfWeek == DayOfWeek.SATURDAY) {
            int friday = dayOfMonth == 1 ? dayOfMonth + 2 : dayOfMonth - 1;
            return isSet(this.dayOfMonth + DAY_OF_MONTH_W_BIT, friday);
        } else { // SUNDAY
            int monday = isLastDayOfMonth ? dayOfMonth - 2 : dayOfMonth + 1;
            return isSet(this.dayOfMonth + DAY_OF_MONTH_W_BIT, monday);
        }
    }

    /**
     * Handles validation of special day cases like 'L' and 'W' modifiers.
     */
    private boolean isSpecialDayValid(LocalDate date, DayOfWeek dayOfWeek, int dayOfMonth) {
        boolean isLastDayOfMonth = date.getDayOfMonth() == date.lengthOfMonth();

        // Last day of month
        if (isSet(this.dayOfMonth, DAY_OF_MONTH_L_BIT) && isLastDayOfMonth) {
            return true;
        }

        // Nearest weekday
        if (isSet(this.dayOfMonth, DAY_OF_MONTH_W_BIT)) {
            return isNearestWeekdayValid(date, dayOfWeek, dayOfMonth);
        }

        // Last specific day of week in month
        if (isSet(this.dayOfWeek, DAY_OF_WEEK_L_BIT) && dayOfMonth > date.lengthOfMonth() - 7
                && isSet(this.dayOfWeek + DAY_OF_WEEK_L_BIT, dayOfWeek.getValue())) {
            return true;
        }

        // Nth day of week
        if (isSet(this.dayOfWeek, DAY_OF_WEEK_HASH_BIT)) {
            int weekNum = (dayOfMonth - 1) / 7 + 1;
            return isSet(this.dayOfWeek + DAY_OF_WEEK_HASH_BIT, (weekNum - 1) * 7 + dayOfWeek.getValue());
        }

        return false;
    }

    /**
     * Validates if the given datetime matches this cron expression.
     *
     * @param dateTime
     *            the datetime to validate
     * @return true if the datetime matches this expression
     */
    public boolean isValid(LocalDateTime dateTime) {
        requireNonNull(dateTime, "dateTime cannot be null");

        // Fast path: check year and month first as they're most likely to fail
        if (!isValidYear(dateTime.getYear()) || !isSet(month, dateTime.getMonthValue())) {
            return false;
        }

        // Then check time components
        if (!isValidTime(dateTime.toLocalTime())) {
            return false;
        }

        // Finally check day components
        return isValidDay(dateTime.toLocalDate());
    }

    /**
     * Validates if the given date matches the date components of this expression.
     */
    private boolean isValidDay(LocalDate date) {
        if (!hasSpecialDayHandling && !hasSpecialWeekHandling) {
            // Fast path for simple cases
            return isSet(dayOfMonth, date.getDayOfMonth()) || isSet(dayOfWeek, date.getDayOfWeek().getValue());
        }

        // Handle special day cases
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        int dayOfMonthValue = date.getDayOfMonth();

        // Check basic day of month and day of week matches
        if (isSet(this.dayOfMonth, dayOfMonthValue) || isSet(this.dayOfWeek, dayOfWeek.getValue())) {
            return true;
        }

        return isSpecialDayValid(date, dayOfWeek, dayOfMonthValue);
    }

    /**
     * Validates if the given time matches the time components of this expression.
     */
    private boolean isValidTime(LocalTime time) {
        return isSet(seconds, time.getSecond()) && isSet(minutes, time.getMinute()) && isSet(hours, time.getHour());
    }

    /**
     * Checks if a given year is valid according to this expression.
     */
    private boolean isValidYear(int year) {
        if (years == null) {
            return true;
        }
        if (year < MINIMUM_YEAR || year > MAXIMUM_YEAR) {
            return false;
        }

        int yearOffset = year - MINIMUM_YEAR;
        int arrayIndex = yearOffset / BITS_PER_LONG;
        int bitIndex = yearOffset % BITS_PER_LONG;

        return arrayIndex < years.length && (years[arrayIndex] & (1L << bitIndex)) != 0;
    }

    /**
     * Returns the next datetime after the specified time that matches this expression.
     *
     * @param current
     *            the current datetime
     * @return the next matching datetime
     * @throws IllegalStateException
     *             if no valid future dates exist within supported range
     */
    public LocalDateTime next(LocalDateTime current) {
        requireNonNull(current, "current time cannot be null");

        LocalDateTime candidate = current.plusSeconds(1);
        while (!isValid(candidate)) {
            candidate = findNextValidDateTime(candidate);
        }

        return candidate;
    }

    public ZonedDateTime nextInZone(ZonedDateTime current) {
        LocalDateTime local = next(current.toLocalDateTime());
        return local.atZone(current.getZone());
    }

    /**
     * Returns an infinite stream of future valid dates for this expression.
     *
     * @param current
     *            the starting datetime
     * @return stream of future valid datetimes
     */
    public Stream<LocalDateTime> stream(LocalDateTime current) {
        requireNonNull(current, "current time cannot be null");
        return Stream.iterate(next(current), this::next);
    }

    @Override
    public String toString() {
        return "CronExpression[" + expression + "]";
    }

    /**
     * Tests if a bit is set in the specified value.
     */
    private static boolean isSet(long value, int position) {
        return (value & (1L << position)) != 0;
    }

    /**
     * Validates if a given string is a valid cron expression.
     *
     * @param expression
     *            the expression to validate
     * @return true if the expression is valid
     */
    public static boolean isValid(String expression) {
        try {
            of(expression);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Finds the next set bit in a value after the specified position.
     */
    private static int nextSetBit(long value, int current) {
        long mask = value & (-1L << (current + 1));
        return mask == 0 ? Long.numberOfTrailingZeros(value) : Long.numberOfTrailingZeros(Long.lowestOneBit(mask));
    }

    public static CronExpression of(String expression) {
        return of(expression, IllegalArgumentException::new);
    }

    /**
     * Creates a new CronExpression from the specified string.
     *
     * @param expression
     *            the cron expression string
     * @return a new CronExpression instance
     * @throws IllegalArgumentException
     *             if the expression is invalid
     */
    static <X extends Throwable> CronExpression of(String expression, Function<? super String, ? extends X> exceptionSupplier) throws X {
        return parse(expression, true, exceptionSupplier);
    }

    static <X extends Throwable> CronExpression parse(String expression, boolean withSeconds, Function<? super String, ? extends X> exceptionSupplier)
            throws X {
        requireNonNull(expression, "expression is null");
        String[] fields = expression.trim().split("\\s+", withSeconds ? 7 : 6);

        if (fields.length < (withSeconds ? 6 : 5) || fields.length > (withSeconds ? 7 : 6)) {
            throw exceptionSupplier.apply("Invalid number of fields in expression: " + expression);
        }

        int startIndex = 0;
        long seconds = withSeconds ? parseField(fields[startIndex++], 0, exceptionSupplier) : 1L;

        return new CronExpression(expression, seconds, parseField(fields[startIndex++], 1, exceptionSupplier),
                parseField(fields[startIndex++], 2, exceptionSupplier), parseDayOfMonth(fields[startIndex++], exceptionSupplier),
                parseMonth(fields[startIndex++], exceptionSupplier), parseDayOfWeek(fields[startIndex++], exceptionSupplier),
                fields.length > startIndex ? parseYears(fields[startIndex], exceptionSupplier) : null);
    }

    static <X extends Throwable> long parseCreateMask(int start, int end, Function<? super String, ? extends X> exceptionSupplier) throws X {
        if (start < 0 || end > 63 || start > end) {
            throw exceptionSupplier.apply("Invalid range: " + start + "-" + end);
        }
        return ((1L << (end - start + 1)) - 1) << start;
    }

    static <X extends Throwable> long parseDayOfMonth(String field, Function<? super String, ? extends X> exceptionSupplier) throws X {
        // Handle ? the same way as * for day of month
        if ("?".equals(field) || "*".equals(field)) {
            return parseCreateMask(LOWER_BOUNDS[3], UPPER_BOUNDS[3], exceptionSupplier);
        }

        // Handle special cases first
        if (field.equals("L")) {
            return 1L << DAY_OF_MONTH_L_BIT;
        }

        if (field.equals("LW")) {
            return (1L << DAY_OF_MONTH_L_BIT) | (1L << DAY_OF_MONTH_W_BIT);
        }

        // Validate L modifier combinations
        if (field.contains("L") && !field.equals("L") && !field.equals("LW")) {
            throw exceptionSupplier.apply("Invalid L modifier combination: " + field);
        }

        // Split on commas for lists
        String[] parts = field.split(",");
        if (parts.length > 1) {
            // Check for W in lists
            if (Arrays.stream(parts).anyMatch(p -> p.contains("W"))) {
                throw exceptionSupplier.apply("W modifier cannot be used in lists: " + field);
            }
        }

        long bits = 0L;
        for (String part : parts) {
            // Handle W modifier
            if (part.contains("W")) {
                if (part.indexOf('W') != part.length() - 1) {
                    throw exceptionSupplier.apply("W modifier must be at the end of the value: " + part);
                }
                if (part.contains("-") || part.contains("/")) {
                    throw exceptionSupplier.apply("W modifier cannot be used with ranges or steps: " + part);
                }

                String dayStr = part.substring(0, part.length() - 1);
                try {
                    int day = Integer.parseInt(dayStr);
                    if (day < 1 || day > 31) {
                        throw exceptionSupplier.apply("Day value with W must be between 1 and 31: " + day);
                    }
                    bits |= (1L << DAY_OF_MONTH_W_BIT);
                    bits |= (1L << (day + DAY_OF_MONTH_W_BIT));
                } catch (NumberFormatException e) {
                    throw exceptionSupplier.apply("Invalid day value with W modifier: " + dayStr);
                }
                continue;
            }

            // Handle ranges and steps
            if (part.contains("-") || part.contains("/")) {
                // Validate no L in ranges
                if (part.contains("L")) {
                    throw exceptionSupplier.apply("L modifier cannot be used in ranges: " + part);
                }
                bits |= parseRange(part, 3, exceptionSupplier);
                continue;
            }

            // Handle single values
            try {
                int day = Integer.parseInt(part);
                if (day < 1 || day > 31) {
                    throw exceptionSupplier.apply("Day value must be between 1 and 31: " + day);
                }
                bits |= (1L << day);
            } catch (NumberFormatException e) {
                throw exceptionSupplier.apply("Invalid day value: " + part);
            }
        }

        return bits;
    }

    static <X extends Throwable> long parseRange(String range, int type, Function<? super String, ? extends X> exceptionSupplier) throws X {
        // Validate no L in ranges for day-of-month
        if (range.contains("L") && type == 3) {
            throw exceptionSupplier.apply("L modifier cannot be used in ranges: " + range);
        }

        // Handle step value parsing
        String[] stepParts = range.split("/");
        if (stepParts.length > 2) {
            throw exceptionSupplier.apply("Invalid step value format: " + range);
        }

        // Parse the range part
        String rangePart = stepParts[0];
        long bits;

        // Handle asterisk case
        if ("*".equals(rangePart)) {
            bits = parseCreateMask(LOWER_BOUNDS[type], UPPER_BOUNDS[type], exceptionSupplier);
        } else {
            // Parse range parts
            String[] rangeParts = rangePart.split("-");
            if (rangeParts.length > 2) {
                throw exceptionSupplier.apply("Invalid range format: " + rangePart);
            }

            // Parse start and end values
            int start = parseValue(rangeParts[0], type, exceptionSupplier);
            int end = rangeParts.length == 2 ? parseValue(rangeParts[1], type, exceptionSupplier) : start;

            if (start > end) {
                throw exceptionSupplier.apply("Invalid range: start value cannot be greater than end value: " + rangePart);
            }

            bits = parseCreateMask(start, end, exceptionSupplier);
        }

        // Handle step if present
        if (stepParts.length == 2) {
            try {
                int step = Integer.parseInt(stepParts[1]);
                if (step < 1) {
                    throw exceptionSupplier.apply("Step value cannot be less than 1, was " + step);
                }
                return parseRange0(bits, step);
            } catch (NumberFormatException e) {
                throw exceptionSupplier.apply("Invalid step value: " + stepParts[1]);
            }
        }

        return bits;
    }

    static <X extends Throwable> long parseDayOfWeek(String field, Function<? super String, ? extends X> exceptionSupplier) throws X {
        // Handle ? the same way as * for day of week
        if ("?".equals(field) || "*".equals(field)) {
            return parseCreateMask(LOWER_BOUNDS[5], UPPER_BOUNDS[5], exceptionSupplier);
        }

        field = parseReplaceDayNames(field.toUpperCase());

        // Handle L modifier - must be alone or a specific day like "5L"
        if (field.contains("L")) {
            if (field.contains("#")) {
                throw exceptionSupplier.apply("L and # modifiers cannot be combined in day-of-week field");
            }

            if (field.equals("L")) {
                return 1L << DAY_OF_WEEK_L_BIT;
            }

            // Handle specific day with L, like "5L"
            String dayPart = field.substring(0, field.length() - 1);
            try {
                int day = Integer.parseInt(dayPart);
                if (day < LOWER_BOUNDS[5] || day > UPPER_BOUNDS[5]) {
                    throw exceptionSupplier.apply("Invalid day-of-week value with L: " + dayPart);
                }
                return (1L << DAY_OF_WEEK_L_BIT) | (1L << (day + DAY_OF_WEEK_L_BIT));
            } catch (NumberFormatException e) {
                throw exceptionSupplier.apply("Invalid day-of-week format with L: " + field);
            }
        }

        long bits = 0L;
        String[] parts = field.split(",");

        for (String part : parts) {
            if (part.contains("#")) {
                String[] hashParts = part.split("#");
                if (hashParts.length != 2) {
                    throw exceptionSupplier.apply("Invalid day of week value: " + part);
                }
                int dayOfWeek = parseValue(hashParts[0], 5, exceptionSupplier);
                int nth = parseNumber(hashParts[1], 5, exceptionSupplier);
                if (nth < 1 || nth > 5) {
                    throw exceptionSupplier.apply("Invalid nth value: " + nth);
                }
                bits |= (1L << DAY_OF_WEEK_HASH_BIT);
                bits |= (1L << ((nth - 1) * 7 + dayOfWeek + DAY_OF_WEEK_HASH_BIT));
            } else {
                bits |= parseRange(part, 5, exceptionSupplier);
            }
        }

        return bits;
    }

    static <X extends Throwable> long parseField(String field, int type, Function<? super String, ? extends X> exceptionSupplier) throws X {
        if ("*".equals(field) || "?".equals(field)) {
            return parseCreateMask(LOWER_BOUNDS[type], UPPER_BOUNDS[type], exceptionSupplier);
        }

        long bits = 0L;
        String[] parts = field.split(",");

        for (String part : parts) {
            bits |= parseRange(part, type, exceptionSupplier);
        }

        return bits;
    }

    static <X extends Throwable> long parseMonth(String field, Function<? super String, ? extends X> exceptionSupplier) throws X {
        return parseField(parseReplaceMonthNames(field.toUpperCase()), 4, exceptionSupplier);
    }

    static <X extends Throwable> int parseNumber(String value, int type, Function<? super String, ? extends X> exceptionSupplier) throws X {
        try {
            int num = Integer.parseInt(value);
            if (num < LOWER_BOUNDS[type] || num > UPPER_BOUNDS[type]) {
                throw exceptionSupplier
                        .apply(String.format("Value %d is outside valid range [%d,%d] for field type %d", num, LOWER_BOUNDS[type], UPPER_BOUNDS[type], type));
            }
            return num;
        } catch (NumberFormatException e) {
            throw exceptionSupplier.apply("Invalid numeric value: " + value);
        }
    }

    /**
     * Applies a step value to a bit field.
     */
    static long parseRange0(long bits, int step) {
        long result = 0L;
        for (int i = 0; i < 64; i++) {
            if ((bits & (1L << i)) != 0 && i % step == 0) {
                result |= (1L << i);
            }
        }
        return result;
    }

    /**
     * Replaces day names with their numeric values.
     */
    static String parseReplaceDayNames(String value) {
        for (int i = 0; i < DAY_NAMES.length; i++) {
            value = value.replace(DAY_NAMES[i], String.valueOf(i + 1));
        }
        return value;
    }

    /**
     * Replaces month names with their numeric values.
     */
    static String parseReplaceMonthNames(String value) {
        for (int i = 0; i < MONTH_NAMES.length; i++) {
            value = value.replace(MONTH_NAMES[i], String.valueOf(i + 1));
        }
        return value;
    }

    static <X extends Throwable> int parseValue(String value, int type, Function<? super String, ? extends X> exceptionSupplier) throws X {
        if (type == 4) {
            value = parseReplaceMonthNames(value.toUpperCase());
        } else if (type == 5) {
            value = parseReplaceDayNames(value.toUpperCase());
        }
        return parseNumber(value, type, exceptionSupplier);
    }

    static <X extends Throwable> long[] parseYears(String field, Function<? super String, ? extends X> exceptionSupplier) throws X {
        if ("*".equals(field) || "?".equals(field)) {
            return null;
        }

        long[] years = new long[YEARS_ARRAY_SIZE];
        String[] parts = field.split(",");

        for (String part : parts) {
            String[] range = part.split("-");
            int start = parseNumber(range[0], 6, exceptionSupplier);
            int end = range.length > 1 ? parseNumber(range[1], 6, exceptionSupplier) : start;

            for (int year = start; year <= end; year++) {
                if (year < MINIMUM_YEAR || year > MAXIMUM_YEAR) {
                    throw exceptionSupplier.apply("Year out of range: " + year);
                }
                int yearOffset = year - MINIMUM_YEAR;
                int arrayIndex = yearOffset / BITS_PER_LONG;
                int bitIndex = yearOffset % BITS_PER_LONG;
                years[arrayIndex] |= (1L << bitIndex);
            }
        }

        return years;
    }
}
