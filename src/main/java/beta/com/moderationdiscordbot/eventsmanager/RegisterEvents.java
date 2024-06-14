package beta.com.moderationdiscordbot.eventsmanager;

import beta.com.moderationdiscordbot.startup.Information;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RegisterEvents {
    private JDA jda;
    private Information information;

    public RegisterEvents(JDA jda, Information information) {
        this.jda = jda;
        this.information = information;
    }

    public RegisterEvents register(ListenerAdapter event) {
        jda.addEventListener(event);
        information.incrementEvents();
        return this;
    }
}