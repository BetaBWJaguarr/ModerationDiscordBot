package beta.com.moderationdiscordbot.voicemanager.commands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.voicemanager.VoiceManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.utils.ModLogEmbed;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class VoiceRequest extends ListenerAdapter {
    private final VoiceManager voiceManager;
    private final EmbedBuilderManager embedManager;
    private final LanguageManager languageManager;
    private final ServerSettings serverSettings;
    private final RateLimit rateLimit;
    private final ModLogEmbed modLogEmbed;

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
        if (!event.getName().equals("voiceaction")) {
            return;
        }

        String subcommand = event.getSubcommandName();
        if (subcommand == null) {
            return;
        }

        if (subcommand.equals("request")) {
            handleRequest(event);
        }
    }

    private void handleRequest(SlashCommandInteractionEvent event) {
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

        if (!serverSettings.getVoiceAction(discordServerId)) {
            event.replyEmbeds(embedManager.createEmbed("commands.voiceaction.error.title", "commands.voiceaction.error.disabled", language).build()).setEphemeral(true).queue();
            return;
        }

        if (voiceManager.isBotInChannel(event.getGuild())) {
            event.replyEmbeds(embedManager.createEmbed("commands.voiceaction.error.title", "commands.voiceaction.error.bot_already_in_channel", language).build()).setEphemeral(true).queue();
            return;
        }

        voiceManager.joinAndStartRecording(voiceChannel);


        event.replyEmbeds(embedManager.createEmbed("commands.voiceaction.success.title", "commands.voiceaction.success.started_recording", language).build()).queue();


        String titleKey = "commands.voiceaction.modlog.request.title";
        String userKey = "commands.voiceaction.modlog.request.user";
        String actionKey = "commands.voiceaction.modlog.request.action";
        String actionMessage = languageManager.getMessage("commands.voiceaction.modlog.request.request", language);

        modLogEmbed.sendLog(
                discordServerId,
                event,
                titleKey,
                userKey,
                actionKey,
                event.getMember().getEffectiveName(),
                actionMessage
        );
    }
}
