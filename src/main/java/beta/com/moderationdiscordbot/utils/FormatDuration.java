package beta.com.moderationdiscordbot.utils;

public class FormatDuration {

    public static String formatDuration(long seconds) {
        if (seconds < 0) {
            return "Permanent";
        }

        long years = seconds / (365 * 24 * 60 * 60);
        seconds %= (365 * 24 * 60 * 60);
        long months = seconds / (30 * 24 * 60 * 60);
        seconds %= (30 * 24 * 60 * 60);
        long days = seconds / (24 * 60 * 60);
        seconds %= (24 * 60 * 60);
        long hours = seconds / (60 * 60);
        seconds %= (60 * 60);
        long minutes = seconds / 60;
        seconds %= 60;

        StringBuilder sb = new StringBuilder();
        if (years > 0) sb.append(years).append("y ");
        if (months > 0) sb.append(months).append("mo ");
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0) sb.append(seconds).append("s");

        return sb.toString().trim();
    }
}
