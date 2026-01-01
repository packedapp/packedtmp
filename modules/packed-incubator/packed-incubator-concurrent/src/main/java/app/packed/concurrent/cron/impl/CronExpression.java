package app.packed.concurrent.cron.impl;

import static java.util.Objects.requireNonNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A bitmask-based Standard Cron expression evaluator.
 *
 * <p>Format: [seconds] [minutes] [hours] [day of month] [month] [day of week] [year]
 *
 * <p>Standard Cron rules:
 * <ul>
 *   <li>Monday is 1, Sunday is 7.</li>
 *   <li>Day of Month and Day of Week fields use <b>OR</b> logic: if both are specified,
 *       the expression matches if either the day of month OR the day of week matches.</li>
 *   <li>'?' is treated as a synonym for '*'.</li>
 * </ul>
 */
public final class CronExpression {

    private static final int MIN_YEAR = 1970;
    private static final int MAX_YEAR = 2137;
    private static final int YEARS_ARRAY_SIZE = 4;
    private static final int BITS_PER_LONG = 64;

    private static final int[] LOWER_BOUNDS = {0, 0, 0, 1, 1, 1, MIN_YEAR};
    private static final int[] UPPER_BOUNDS = {59, 59, 23, 31, 12, 7, MAX_YEAR};

    private static final String[] MONTH_NAMES = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
    private static final String[] DAY_NAMES = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};

    private final String expression;
    private final long seconds;
    private final long minutes;
    private final long hours;
    private final long dayOfMonth;
    private final long month;
    private final long dayOfWeek;
    private final long[] years;

    // Advanced Modifiers
    private final boolean domWildcard;
    private final boolean dowWildcard;
    private final boolean lastDom;
    private final int lastDomOffset; // L-n
    private final boolean lastWeekdayDom; // LW
    private final int nearestWeekdayDom; // nW
    private final int lastDow; // nL
    private final long nthDowMask; // n#m (Packed bits)

    private CronExpression(String expression, long seconds, long minutes, long hours, long dayOfMonth, long month, long dayOfWeek, long[] years,
                           boolean domWildcard, boolean dowWildcard, boolean lastDom, int lastDomOffset, boolean lastWeekdayDom, int nearestWeekdayDom,
                           int lastDow, long nthDowMask) {
        this.expression = requireNonNull(expression);
        this.seconds = seconds;
        this.minutes = minutes;
        this.hours = hours;
        this.dayOfMonth = dayOfMonth;
        this.month = month;
        this.dayOfWeek = dayOfWeek;
        this.years = years;
        this.domWildcard = domWildcard;
        this.dowWildcard = dowWildcard;
        this.lastDom = lastDom;
        this.lastDomOffset = lastDomOffset;
        this.lastWeekdayDom = lastWeekdayDom;
        this.nearestWeekdayDom = nearestWeekdayDom;
        this.lastDow = lastDow;
        this.nthDowMask = nthDowMask;
    }

    /** Creates a CronExpression from the given string. */
    public static CronExpression of(String expression) {
        return parse(expression, IllegalArgumentException::new);
    }

    /** Validates if the given string is a valid cron expression. */
    public static boolean isValid(String expression) {
        try {
            of(expression);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Returns the original cron expression string. */
    public String expression() {
        return expression;
    }

    /** Checks if the provided LocalDateTime matches the cron expression. */
    public boolean isValid(LocalDateTime dateTime) {
        if (!isValidYear(dateTime.getYear()) || !isSet(month, dateTime.getMonthValue())) {
            return false;
        }
        if (!isSet(hours, dateTime.getHour()) || !isSet(minutes, dateTime.getMinute()) || !isSet(seconds, dateTime.getSecond())) {
            return false;
        }
        return isValidDay(dateTime.toLocalDate());
    }

    private boolean isValidDay(LocalDate date) {
        int d = date.getDayOfMonth();
        int dow = date.getDayOfWeek().getValue(); // 1=MON, 7=SUN

        // Evaluate Day of Month
        boolean domMatch = isSet(dayOfMonth, d);
        if (!domMatch && lastDom) domMatch = (d == (date.lengthOfMonth() - lastDomOffset));
        if (!domMatch && lastWeekdayDom) domMatch = (d == findNearestWeekday(date, date.lengthOfMonth()));
        if (!domMatch && nearestWeekdayDom > 0) domMatch = (d == findNearestWeekday(date, nearestWeekdayDom));

        // Evaluate Day of Week
        boolean dowMatch = isSet(dayOfWeek, dow);
        if (!dowMatch && lastDow > 0) dowMatch = (dow == lastDow && d > date.lengthOfMonth() - 7);
        if (!dowMatch && nthDowMask != 0) {
            int week = (d - 1) / 7; // 0-4
            dowMatch = (nthDowMask & (1L << (week * 7 + (dow - 1)))) != 0;
        }

        // Standard Cron Logic: If both are specified (not wildcard), use OR logic.
        // If one is wildcard, the other field's result is used.
        if (domWildcard && dowWildcard) return true;
        if (domWildcard) return dowMatch;
        if (dowWildcard) return domMatch;
        return domMatch || dowMatch;
    }

    private int findNearestWeekday(LocalDate date, int targetDay) {
        LocalDate target = date.withDayOfMonth(targetDay);
        DayOfWeek dow = target.getDayOfWeek();
        if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) return targetDay;
        if (dow == DayOfWeek.SATURDAY) return (targetDay == 1) ? 3 : targetDay - 1;
        // Sunday
        return (targetDay == target.lengthOfMonth()) ? targetDay - 2 : targetDay + 1;
    }

    /** Returns the next execution time after current. */
    public LocalDateTime next(LocalDateTime current) {
        LocalDateTime candidate = current.plusSeconds(1).withNano(0);
        for (int i = 0; i < 100_000; i++) {
            if (isValid(candidate)) return candidate;
            candidate = findNextPotential(candidate);
        }
        throw new IllegalStateException("No match found in supported range");
    }

    private LocalDateTime findNextPotential(LocalDateTime curr) {
        if (!isValidYear(curr.getYear())) {
            return curr.withMonth(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).plusYears(1);
        }
        if (!isSet(month, curr.getMonthValue())) {
            return curr.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).plusMonths(1);
        }
        if (!isValidDay(curr.toLocalDate())) {
            return curr.withHour(0).withMinute(0).withSecond(0).plusDays(1);
        }
        if (!isSet(hours, curr.getHour())) {
            return curr.withMinute(0).withSecond(0).plusHours(1);
        }
        if (!isSet(minutes, curr.getMinute())) {
            return curr.withSecond(0).plusMinutes(1);
        }
        return curr.plusSeconds(1);
    }

    public ZonedDateTime nextInZone(ZonedDateTime current) {
        return next(current.toLocalDateTime()).atZone(current.getZone());
    }

    public Stream<LocalDateTime> stream(LocalDateTime start) {
        return Stream.iterate(next(start), this::next);
    }

    private boolean isValidYear(int y) {
        if (years == null) return true;
        int offset = y - MIN_YEAR;
        return (offset >= 0 && offset < 256 && (years[offset / 64] & (1L << (offset % 64))) != 0);
    }

    private static boolean isSet(long mask, int bit) {
        return (mask & (1L << bit)) != 0;
    }

    private static <X extends Throwable> CronExpression parse(String expr, Function<String, X> err) throws X {
        String[] fields = expr.trim().split("\\s+");
        if (fields.length < 6 || fields.length > 7) throw err.apply("Invalid number of fields: " + expr);

        long sec = parseField(fields[0], 0, err);
        long min = parseField(fields[1], 1, err);
        long hour = parseField(fields[2], 2, err);
        long mon = parseField(fields[4], 4, err);

        // Day of Month
        String domStr = fields[3];
        boolean domW = domStr.equals("*") || domStr.equals("?");
        boolean lDom = domStr.contains("L"), lWDom = domStr.equals("LW");
        int lDomOff = 0, nWDom = 0;
        if (lDom && domStr.contains("-")) {
            lDomOff = Integer.parseInt(domStr.substring(domStr.indexOf("-") + 1));
        }
        if (domStr.endsWith("W") && !lWDom) {
            nWDom = Integer.parseInt(domStr.substring(0, domStr.length() - 1));
        }
        long domM = (domW || lDom || nWDom > 0) ? 0 : parseField(domStr, 3, err);
        if (domW) domM = createMask(1, 31);

        // Day of Week
        String dowStr = fields[5];
        boolean dowW = dowStr.equals("*") || dowStr.equals("?");
        int lastD = 0;
        long nthM = 0;
        if (dowStr.endsWith("L") && !dowStr.equals("L")) {
            lastD = mapValue(dowStr.substring(0, dowStr.length() - 1), 5, err);
        } else if (dowStr.contains("#")) {
            String[] parts = dowStr.split("#");
            int d = mapValue(parts[0], 5, err);
            int n = Integer.parseInt(parts[1]);
            if (n < 1 || n > 5) throw err.apply("Invalid nth value: " + n);
            nthM = 1L << ((n - 1) * 7 + (d - 1));
        }
        long dowM = (dowW || lastD > 0 || nthM > 0 || dowStr.equals("L")) ? 0 : parseField(dowStr, 5, err);
        if (dowW) dowM = createMask(1, 7);
        if (dowStr.equals("L")) dowM = (1L << 7); // Standard L for DOW is Sunday (7)

        long[] years = fields.length == 7 ? parseYears(fields[6], err) : null;

        return new CronExpression(expr, sec, min, hour, domM, mon, dowM, years, domW, dowW, lDom, lDomOff, lWDom, nWDom, lastD, nthM);
    }

    private static <X extends Throwable> long parseField(String field, int type, Function<String, X> err) throws X {
        if (field.equals("*") || field.equals("?")) return createMask(LOWER_BOUNDS[type], UPPER_BOUNDS[type]);
        long mask = 0;
        for (String part : field.split(",")) {
            int step = 1;
            if (part.contains("/")) {
                String[] s = part.split("/");
                step = Integer.parseInt(s[1]);
                part = s[0];
            }
            int start, end;
            if (part.equals("*") || part.equals("?")) {
                start = LOWER_BOUNDS[type];
                end = UPPER_BOUNDS[type];
            } else if (part.contains("-")) {
                String[] r = part.split("-");
                start = mapValue(r[0], type, err);
                end = mapValue(r[1], type, err);
            } else {
                start = end = mapValue(part, type, err);
            }
            if (start < LOWER_BOUNDS[type] || end > UPPER_BOUNDS[type] || start > end || step < 1) {
                throw err.apply("Value out of range for field " + type + ": " + part);
            }
            for (int i = start; i <= end; i += step) mask |= (1L << i);
        }
        return mask;
    }

    private static <X extends Throwable> int mapValue(String v, int type, Function<String, X> err) throws X {
        if (type == 4) { // Month
            for (int i = 0; i < 12; i++) if (v.equalsIgnoreCase(MONTH_NAMES[i])) return i + 1;
        } else if (type == 5) { // DOW
            for (int i = 0; i < 7; i++) if (v.equalsIgnoreCase(DAY_NAMES[i])) return i + 1;
        }
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            throw err.apply("Invalid value: " + v);
        }
    }

    private static <X extends Throwable> long[] parseYears(String field, Function<String, X> err) throws X {
        if (field.equals("*") || field.equals("?")) return null;
        long[] y = new long[YEARS_ARRAY_SIZE];
        for (String part : field.split(",")) {
            int start, end;
            if (part.contains("-")) {
                String[] r = part.split("-");
                start = Integer.parseInt(r[0]);
                end = Integer.parseInt(r[1]);
            } else {
                start = end = Integer.parseInt(part);
            }
            if (start < MIN_YEAR || end > MAX_YEAR || start > end) throw err.apply("Year out of range: " + start);
            for (int i = start; i <= end; i++) {
                int off = i - MIN_YEAR;
                y[off / BITS_PER_LONG] |= (1L << (off % BITS_PER_LONG));
            }
        }
        return y;
    }

    private static long createMask(int start, int end) {
        long m = 0;
        for (int i = start; i <= end; i++) m |= (1L << i);
        return m;
    }

    @Override public String toString() { return "CronExpression[" + expression + "]"; }
    @Override public int hashCode() { return Objects.hash(expression); }
    @Override public boolean equals(Object o) {
        return (o instanceof CronExpression) && expression.equals(((CronExpression) o).expression);
    }
}