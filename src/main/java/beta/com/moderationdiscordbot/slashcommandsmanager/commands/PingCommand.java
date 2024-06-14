package beta.com.moderationdiscordbot.slashcommandsmanager.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class PingCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("ping")) {
            long time = System.currentTimeMillis();
            event.reply("Pinging...").setEphemeral(true).queue(interactionHook -> {
                interactionHook.deleteOriginal().queue();
                long pingTime = System.currentTimeMillis() - time;
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle(":ping_pong: Pong!");
                embed.setDescription("Ping time is " + pingTime + "ms");
                embed.setColor(Color.CYAN);
                event.getHook().sendMessageEmbeds(embed.build()).queue();
            });
        }
    }
}