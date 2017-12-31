package github.scarsz.examinator.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimeUtil {

    private static final Date date = new Date();
    private static final SimpleDateFormat format = new SimpleDateFormat("EEE, d. MMM yyyy HH:mm:ss z");

    public static String timestamp() {
        return timestamp(System.currentTimeMillis());
    }

    public static String timestamp(long milliseconds) {
        date.setTime(milliseconds);
        return format.format(date);
    }

    public static String formatDuration(long duration) {
        duration = Math.abs(duration);
        if (duration == 0) return "∞";

        long days = (long) Math.floor(TimeUnit.MILLISECONDS.toDays(duration));
        duration -= TimeUnit.DAYS.toMillis(days);
        long hours = (long) Math.floor(TimeUnit.MILLISECONDS.toHours(duration));
        duration -= TimeUnit.HOURS.toMillis(hours);
        long minutes = (long) Math.floor(TimeUnit.MILLISECONDS.toMinutes(duration));
        duration -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = (long) Math.floor(TimeUnit.MILLISECONDS.toSeconds(duration));

        List<String> result = new LinkedList<>();
        if (days > 0) result.add(days + " days");
        if (hours > 0) result.add(hours + " hours");
        if (minutes > 0) result.add(minutes + " minutes");
        if (seconds > 0) result.add(seconds + " seconds");
        return String.join(", ", result);
    }

}
