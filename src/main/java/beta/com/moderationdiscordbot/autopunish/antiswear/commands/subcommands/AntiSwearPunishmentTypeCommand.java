package beta.com.moderationdiscordbot.autopunish.antiswear.commands.subcommands;


import beta.com.moderationdiscordbot.autopunish.PunishmentTypeBaseCommand;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class AntiSwearPunishmentTypeCommand extends PunishmentTypeBaseCommand {

    public AntiSwearPunishmentTypeCommand(ServerSettings serverSettings, LanguageManager languageManager, RateLimit rateLimit, HandleErrors errorManager) {
        super(serverSettings, languageManager, rateLimit, errorManager);
    }

    @Override
    protected boolean isCommandApplicable(SlashCommandInteractionEvent event) {
        return "antiswear".equals(event.getName()) && "punishment-type".equals(event.getSubcommandName());
    }

    @Override
    protected void setPunishmentType(String serverId, String punishmentType) {
        serverSettings.setAntiSwearPunishmentType(serverId, punishmentType);
    }
}
