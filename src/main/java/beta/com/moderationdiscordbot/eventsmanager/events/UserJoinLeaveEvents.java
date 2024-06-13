package beta.com.moderationdiscordbot.eventsmanager.events;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class UserJoinLeaveEvents extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        TextChannel channel = (TextChannel) event.getGuild().getDefaultChannel();
        if (channel != null) {
            channel.sendMessage(event.getUser().getName() + " has joined the server.").queue();
        }
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        TextChannel channel = (TextChannel) event.getGuild().getDefaultChannel();
        if (channel != null) {
            channel.sendMessage(event.getUser().getName() + " has left the server.").queue();
        }
    }
}