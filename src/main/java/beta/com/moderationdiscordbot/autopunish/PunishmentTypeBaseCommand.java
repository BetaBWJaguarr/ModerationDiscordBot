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
            if (!isCommandApplicable(event)) {
                return;
            }

            if (rateLimit.isRateLimited(event, embedBuilderManager, serverSettings)) {
                return;
            }

            String punishmentType = event.getOption("type").getAsString();
            String serverId = event.getGuild().getId();

            if (!serverSettings.isAutoPunishEnabled(serverId)) {
                event.replyEmbeds(embedBuilderManager.createEmbed(
                        "commands.punishment-type.auto_punish_disabled",
                        null,
                        serverSettings.getLanguage(serverId)
                ).build()).setEphemeral(true).queue();
                return;
            }

            if ("warn".equalsIgnoreCase(punishmentType) || "mute".equalsIgnoreCase(punishmentType)) {
                setPunishmentType(serverId, punishmentType);
                event.replyEmbeds(embedBuilderManager.createEmbed(
                        "commands.punishment-type.punishment_type_set",
                        null,
                        serverSettings.getLanguage(serverId),
                        punishmentType
                ).build()).queue();
            } else {
                event.replyEmbeds(embedBuilderManager.createEmbed(
                        "commands.punishment-type.invalid_type",
                        null,
                        serverSettings.getLanguage(serverId)
                ).build()).setEphemeral(true).queue();
            }
        } catch (Exception e) {
            errorManager.sendErrorMessage(e, event.getChannel().asTextChannel());
        }
    }

    protected abstract boolean isCommandApplicable(SlashCommandInteractionEvent event);

    protected abstract void setPunishmentType(String serverId, String punishmentType);
}
