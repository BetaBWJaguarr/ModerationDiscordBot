package beta.com.moderationdiscordbot.autopunish.antiswear.commands.subcommands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class PunishmentTypeCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final RateLimit rateLimit;
    private final HandleErrors errorManager;

    public PunishmentTypeCommand(ServerSettings serverSettings, LanguageManager languageManager, RateLimit rateLimit, HandleErrors errorManager) {
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

            if ("warn".equalsIgnoreCase(punishmentType) || "mute".equalsIgnoreCase(punishmentType)) {
                serverSettings.setAntiSwearPunishmentType(serverId, punishmentType);
                event.replyEmbeds(embedBuilderManager.createEmbed(
                        "commands.antiswear.punishment-type.punishment_type_set",
                        null,
                        serverSettings.getLanguage(serverId),
                        punishmentType
                ).build()).queue();
            } else {
                event.replyEmbeds(embedBuilderManager.createEmbed(
                        "commands.antiswear.punishment-type.invalid_type",
                        null,
                        serverSettings.getLanguage(serverId)
                ).build()).setEphemeral(true).queue();
            }
        } catch (Exception e) {
            errorManager.sendErrorMessage(e, event.getChannel().asTextChannel());
        }
    }

    private boolean isCommandApplicable(SlashCommandInteractionEvent event) {
        return "antiswear".equals(event.getName()) && "punishment-type".equals(event.getSubcommandName());
    }
}
