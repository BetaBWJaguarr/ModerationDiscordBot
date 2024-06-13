package beta.com.moderationdiscordbot.eventsmanager;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RegisterEvents {
    private JDA jda;

    public RegisterEvents(JDA jda) {
        this.jda = jda;
    }

    public void register(ListenerAdapter event) {
        jda.addEventListener(event);
    }
}