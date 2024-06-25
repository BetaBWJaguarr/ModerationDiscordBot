package beta.com.moderationdiscordbot.startup;

import java.time.LocalDateTime;
import java.util.List;

public class Information {
    private static int activeCommands = 0;
    private static int activeEvents = 0;
    private static LocalDateTime startTime;
    private static String botName;
    private static String botVersion;
    private static String developerName;
    private static String status;
    private static int userCount;
    private static int serverCount;
    private static String botId;
    private static List<String> commandList;
    private static List<String> eventList;

    public Information(String name, String version, String developer, String id) {
        botName = name;
        botVersion = version;
        developerName = developer;
        botId = id;
        startTime = LocalDateTime.now();
        status = "Running";
    }

    public void incrementCommands() {
        activeCommands++;
    }

    public void incrementEvents() {
        activeEvents++;
    }

    public void setUserCount(int count) {
        userCount = count;
    }

    public void setServerCount(int count) {
        serverCount = count;
    }

    public void setCommandList(List<String> commands) {
        commandList = commands;
    }

    public void setEventList(List<String> events) {
        eventList = events;
    }

    public void printInformation() {
        System.out.println("==================================");
        System.out.println("Bot Name: " + botName);
        System.out.println("Bot Version: " + botVersion);
        System.out.println("Developer: " + developerName);
        System.out.println("Bot ID: " + botId);
        System.out.println("Start Time: " + startTime);
        System.out.println("Status: " + status);
        System.out.println("Number of active commands: " + activeCommands);
        System.out.println("Number of active events: " + activeEvents);
        System.out.println("Number of users: " + userCount);
        System.out.println("Number of servers: " + serverCount);
        System.out.println("Command List: " + commandList);
        System.out.println("Event List: " + eventList);
        System.out.println("==================================");
    }
}
