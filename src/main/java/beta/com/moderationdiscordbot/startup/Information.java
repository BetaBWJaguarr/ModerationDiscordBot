package beta.com.moderationdiscordbot.startup;

public class Information {
    private static int activeCommands = 0;
    private static int activeEvents = 0;

    public void incrementCommands() {
        activeCommands++;
    }

    public void incrementEvents() {
        activeEvents++;
    }

    public void printInformation() {
        System.out.println("==================================");
        System.out.println("Number of active commands: " + activeCommands);
        System.out.println("Number of active events: " + activeEvents);
        System.out.println("==================================");
    }
}