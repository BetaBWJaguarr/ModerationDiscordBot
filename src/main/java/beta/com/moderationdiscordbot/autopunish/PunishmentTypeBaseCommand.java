package beta.com.moderationdiscordbot.autopunish;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class PunishmentTypeBaseCommand extends ListenerAdapter {

    protected final EmbedBuilderManager embedBuilderManager;
    protected final ServerSettings serverSettings;
    protected final LanguageManager languageManager;
    protected final RateLimit rateLimit;
    protected final HandleErrors errorManager;

    public PunishmentTypeBaseCommand(ServerSettings serverSettings, LanguageManager languageManager, RateLimit rateLimit, HandleErrors errorManager) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.rateLimit = rateLimit;
        this.errorManager = errorManager;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
            if (!isCommandApplicable(event) || rateLimit.isRateLimited(event, embedBuilderManager, serverSettings)) {
                return;
            }

            String serverId = event.getGuild().getId();
            if (!serverSettings.isAutoPunishEnabled(serverId)) {
                sendEphemeralReply(event, "commands.punishment-type.auto_punish_disabled", serverId);
                return;
            }

            String punishmentType = event.getOption("type").getAsString();
            if (isValidPunishmentType(punishmentType)) {
                setPunishmentType(serverId, punishmentType);
                sendReply(event, "commands.punishment-type.punishment_type_set", serverId, punishmentType);
            } else {
                sendEphemeralReply(event, "commands.punishment-type.invalid_type", serverId);
            }
        } catch (Exception e) {
            errorManager.sendErrorMessage(e, event.getChannel().asTextChannel());
        }
    }

    protected boolean isValidPunishmentType(String punishmentType) {
        return "warn".equalsIgnoreCase(punishmentType) || "mute".equalsIgnoreCase(punishmentType);
    }

    protected void sendReply(SlashCommandInteractionEvent event, String messageKey, String serverId, String... args) {
        event.replyEmbeds(
                embedBuilderManager.createEmbed(messageKey, null, serverSettings.getLanguage(serverId), args).build()
        ).queue();
    }

    protected void sendEphemeralReply(SlashCommandInteractionEvent event, String messageKey, String serverId) {
        event.replyEmbeds(
                embedBuilderManager.createEmbed(messageKey, null, serverSettings.getLanguage(serverId)).build()
        ).setEphemeral(true).queue();
    }

    protected abstract boolean isCommandApplicable(SlashCommandInteractionEvent event);

    protected abstract void setPunishmentType(String serverId, String punishmentType);
}
