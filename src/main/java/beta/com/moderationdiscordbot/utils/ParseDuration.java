package beta.com.moderationdiscordbot.utils;

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