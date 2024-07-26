package beta.com.moderationdiscordbot.voicemanager.commands;

import beta.com.moderationdiscordbot.voicemanager.VoiceManager;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class VoiceRequestEnd extends ListenerAdapter {
    private final VoiceManager voiceManager;
    private final EmbedBuilderManager embedManager;
    private final LanguageManager languageManager;
    private final ServerSettings serverSettings;
    private final RateLimit rateLimit;

    public VoiceRequestEnd(VoiceManager voiceManager, LanguageManager languageManager, ServerSettings serverSettings, RateLimit rateLimit) {
        this.voiceManager = voiceManager;
        this.languageManager = languageManager;
        this.embedManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.rateLimit = rateLimit;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("voiceaction")) {
            return;
        }

        String subcommand = event.getSubcommandName();
        if (subcommand == null) {
            return;
        }

        if (subcommand.equals("end")) {
            handleEnd(event);
        }
    }

    private void handleEnd(SlashCommandInteractionEvent event) {
        String discordServerId = event.getGuild().getId();
        String language = serverSettings.getLanguage(discordServerId);

        if (rateLimit.isRateLimited(event, embedManager, serverSettings)) {
            return;
        }

        VoiceChannel voiceChannel = (VoiceChannel) event.getMember().getVoiceState().getChannel();
        if (voiceChannel == null) {
            event.replyEmbeds(embedManager.createEmbed("commands.voiceaction.error.title", "commands.voiceaction.error.not_in_channel", language).build()).setEphemeral(true).queue();
            return;
        }

        if (!voiceManager.isBotInChannel(event.getGuild())) {
            event.replyEmbeds(embedManager.createEmbed("commands.voiceaction.error.title", "commands.voiceaction.error.bot_not_in_channel", language).build()).setEphemeral(true).queue();
            return;
        }

        voiceManager.stopRecordingAndLeaveChannel(voiceChannel);
        event.replyEmbeds(embedManager.createEmbed("commands.voiceaction.success.title", "commands.voiceaction.success.stopped_recording", language).build()).queue();
    }
}
