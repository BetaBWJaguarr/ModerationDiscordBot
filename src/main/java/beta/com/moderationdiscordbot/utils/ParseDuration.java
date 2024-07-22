package beta.com.moderationdiscordbot.utils;

/**
 * The {@code ParseDuration} class provides a utility method for parsing a duration string into seconds.
 * The duration string can contain days, hours, minutes, and seconds, specified by the characters 'd', 'h', 'm', and 's' respectively.
 * For example, the string "1d2h30m15s" will be parsed into the equivalent number of seconds.
 * <p>
 * This class is useful for converting user-friendly duration inputs into a format that can be used programmatically.
 *
 * <p><b>Methods:</b></p>
 * <ul>
 * <li>{@code static long parse(String durationStr)}: Parses the given duration string into seconds.</li>
 * </ul>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * {@code
 * long durationInSeconds = ParseDuration.parse("1d2h30m15s");
 * }
 * </pre>
 */

public class ParseDuration {

    public static long parse(String durationStr) {
        int durationInSeconds = 0;
        String number = "";
        for (int i = 0; i < durationStr.length(); i++) {
            char c = durationStr.charAt(i);
            if (Character.isDigit(c)) {
                number += c;
            } else if (Character.isLetter(c)) {
                switch (c) {
                    case 'd':
                        durationInSeconds += Integer.parseInt(number) * 60 * 60 * 24;
                        break;
                    case 'h':
                        durationInSeconds += Integer.parseInt(number) * 60 * 60;
                        break;
                    case 'm':
                        durationInSeconds += Integer.parseInt(number) * 60;
                        break;
                    case 's':
                        durationInSeconds += Integer.parseInt(number);
                        break;
                }
                number = "";
            }
        }
        return durationInSeconds;
    }
}