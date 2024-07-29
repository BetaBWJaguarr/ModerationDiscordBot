package beta.com.moderationdiscordbot.voicemanager.commands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.voicemanager.VoiceManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.utils.ModLogEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class VoiceRequest extends ListenerAdapter {
    private final VoiceManager voiceManager;
    private final EmbedBuilderManager embedManager;
    private final ServerSettings serverSettings;
    private final RateLimit rateLimit;
    private final ModLogEmbed modLogEmbed;
    private final LanguageManager languageManager;

    public VoiceRequest(VoiceManager voiceManager, LanguageManager languageManager, ServerSettings serverSettings, RateLimit rateLimit) {
        this.voiceManager = voiceManager;
        this.languageManager = languageManager;
        this.embedManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.rateLimit = rateLimit;
        this.modLogEmbed = new ModLogEmbed(languageManager, serverSettings);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("voiceaction") || !event.getSubcommandName().equals("request")) {
            return;
        }

        String discordServerId = event.getGuild().getId();
        String language = serverSettings.getLanguage(discordServerId);

        if (rateLimit.isRateLimited(event, embedManager, serverSettings)) {
            return;
        }

        VoiceChannel voiceChannel = (VoiceChannel) event.getMember().getVoiceState().getChannel();
        if (voiceChannel == null || !serverSettings.getVoiceAction(discordServerId) || voiceManager.isBotInChannel(event.getGuild())) {
            event.replyEmbeds(embedManager.createEmbed(
                    "commands.voiceaction.error.title",
                    voiceChannel == null ? "commands.voiceaction.error.not_in_channel" :
                            !serverSettings.getVoiceAction(discordServerId) ? "commands.voiceaction.error.disabled" :
                                    "commands.voiceaction.error.bot_already_in_channel",
                    language).build()
            ).setEphemeral(true).queue();
            return;
        }

        voiceManager.joinAndStartRecording(voiceChannel);

        event.replyEmbeds(embedManager.createEmbed("commands.voiceaction.success.title", "commands.voiceaction.success.started_recording", language).build()).queue();

        modLogEmbed.sendLog(
                discordServerId,
                event,
                "commands.voiceaction.modlog.request.title",
                "commands.voiceaction.modlog.request.user",
                "commands.voiceaction.modlog.request.action",
                event.getMember().getEffectiveName(),
                languageManager.getMessage("commands.voiceaction.modlog.request.request", language)
        );
    }
}
