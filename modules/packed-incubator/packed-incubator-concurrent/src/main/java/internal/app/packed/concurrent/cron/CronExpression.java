package internal.app.packed.concurrent.cron;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

public class CronExpression {

    private final String expression;

    private final Set<Integer> minutes = new HashSet<>();
    private final Set<Integer> hours = new HashSet<>();
    private final Set<Integer> daysOfMonth = new HashSet<>();
    private final Set<Integer> months = new HashSet<>();
    private final Set<Integer> daysOfWeek = new HashSet<>();

    public CronExpression(String expression) throws Exception {
        this.expression = expression.trim();
        parseExpression(this.expression);
    }

    public ZonedDateTime getNextValidTimeAfter(ZonedDateTime dateTime) {
        ZonedDateTime nextTime = dateTime.withSecond(0).withNano(0).plusMinutes(1);

        while (true) {
            if ((months.contains(nextTime.getMonthValue()))
                    && (daysOfMonth.contains(nextTime.getDayOfMonth()))
                    && (daysOfWeek.contains((nextTime.getDayOfWeek().getValue()) % 7))
                    && (hours.contains(nextTime.getHour()))
                    && (minutes.contains(nextTime.getMinute()))) {
                return nextTime;
            }

            nextTime = nextTime.plusMinutes(1);

            // Break condition to prevent infinite loops (set a limit, e.g., 1 year)
            if (Duration.between(dateTime, nextTime).toDays() > 366) {
                return null;
            }
        }
    }

    private void parseExpression(String expression) throws Exception {
        String[] fields = expression.split("\\s+");
        if (fields.length != 5) {
            throw new IllegalArgumentException("Cron expression must consist of 5 fields (minute, hour, day of month, month, and day of week)");
        }

        parseField(fields[0], 0, 59, minutes); // Minute
        parseField(fields[1], 0, 23, hours); // Hour
        parseField(fields[2], 1, 31, daysOfMonth); // Day of Month
        parseField(fields[3], 1, 12, months); // Month
        parseField(fields[4], 0, 6, daysOfWeek); // Day of Week (0=Monday)
    }

    private void parseField(String field, int min, int max, Set<Integer> set) throws Exception {
        if (field.equals("*")) {
            for (int i = min; i <= max; i++) {
                set.add(i);
            }
            return;
        }

        String[] parts = field.split(",");
        for (String part : parts) {
            if (part.contains("/")) {
                String[] stepParts = part.split("/");
                if (stepParts.length != 2) {
                    throw new IllegalArgumentException("Invalid syntax in field: " + field);
                }
                int step = Integer.parseInt(stepParts[1]);
                parseRange(stepParts[0], min, max, set, step);
            } else {
                parseRange(part, min, max, set, 1);
            }
        }
    }

    private void parseRange(String range, int min, int max, Set<Integer> set, int step) throws Exception {
        int rangeMin = min;
        int rangeMax = max;

        if (!range.equals("*")) {
            if (range.contains("-")) {
                String[] rangeParts = range.split("-");
                if (rangeParts.length != 2) {
                    throw new IllegalArgumentException("Invalid range: " + range);
                }
                rangeMin = Integer.parseInt(rangeParts[0]);
                rangeMax = Integer.parseInt(rangeParts[1]);
            } else {
                rangeMin = rangeMax = Integer.parseInt(range);
            }
        }

        if (rangeMin < min || rangeMax > max || rangeMin > rangeMax) {
            throw new IllegalArgumentException("Range out of bounds: " + range);
        }

        for (int i = rangeMin; i <= rangeMax; i += step) {
            set.add(i);
        }
    }
}
