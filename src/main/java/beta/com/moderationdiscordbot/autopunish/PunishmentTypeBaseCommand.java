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
    protected final RateLimit rateLimit;
    protected final HandleErrors errorManager;

    public PunishmentTypeBaseCommand(ServerSettings serverSettings, LanguageManager languageManager, RateLimit rateLimit, HandleErrors errorManager) {
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.rateLimit = rateLimit;
        this.errorManager = errorManager;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!isCommandApplicable(event)) return;

        String serverId = event.getGuild().getId();
        if (!serverSettings.isAutoPunishEnabled(serverId)) {
            sendReply(event, "commands.punishment-type.auto_punish_disabled", serverId, "true");
            return;
        }

        String punishmentType = event.getOption("type").getAsString();
        if (isValidPunishmentType(punishmentType)) {
            setPunishmentType(serverId, punishmentType);
            sendReply(event, "commands.punishment-type.punishment_type_set", serverId, punishmentType, "false");
        } else {
            sendReply(event, "commands.punishment-type.invalid_type", serverId, "true");
        }
    }

    protected boolean isValidPunishmentType(String punishmentType) {
        return "warn".equalsIgnoreCase(punishmentType) || "mute".equalsIgnoreCase(punishmentType);
    }

    protected void sendReply(SlashCommandInteractionEvent event, String messageKey, String serverId, String... args) {
        var embed = embedBuilderManager.createEmbed(messageKey, null, serverSettings.getLanguage(serverId), args);
        if (args.length > 0 && Boolean.parseBoolean(args[0])) {
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        } else {
            event.replyEmbeds(embed.build()).queue();
        }
    }

    protected abstract boolean isCommandApplicable(SlashCommandInteractionEvent event);

    protected abstract void setPunishmentType(String serverId, String punishmentType);
}
