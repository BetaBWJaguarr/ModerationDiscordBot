package beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands;

import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.utils.ModLogEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


public class ModerationManagerCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final HandleErrors errorHandle;
    private final ModLogEmbed modLogEmbed;

    public ModerationManagerCommand(ServerSettings serverSettings, LanguageManager languageManager, HandleErrors errorHandle) {
        this.languageManager = languageManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.serverSettings = serverSettings;
        this.errorHandle = errorHandle;
        this.modLogEmbed = new ModLogEmbed(languageManager, serverSettings);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try {
            if (!event.getName().equals("moderation-manager")) {
                return;
            }

            String typeStr = event.getOption("type").getAsString().toUpperCase();
            String predicateStr = event.getOption("predicates") != null ? event.getOption("predicates").getAsString() : null;

            Type type;
            try {
                type = Type.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.moderation_manager.invalid_type", null, serverSettings.getLanguage(event.getGuild().getId())).build()).setEphemeral(true).queue();
                return;
            }

            Predicate predicate;
            try {
                predicate = Predicate.valueOf(predicateStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.moderation_manager.invalid_predicate", null, serverSettings.getLanguage(event.getGuild().getId())).build()).setEphemeral(true).queue();
                return;
            }

            if (!isValidPredicateForType(type, predicate)) {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.moderation_manager.predicate_not_valid", null, serverSettings.getLanguage(event.getGuild().getId())).build()).setEphemeral(true).queue();
                return;
            }

            processCommand(type, predicate, event);

        } catch (Exception e) {
            errorHandle.sendErrorMessage(e, event.getChannel().asTextChannel());
        }
    }

    private boolean isValidPredicateForType(Type type, Predicate predicate) {
        switch (type) {
            case ANTISPAM:
                return predicate == Predicate.TIMELIMIT || predicate == Predicate.MESSAGELIMIT || predicate == Predicate.STATUS;
            case VOICEACTION:
            case ANTISWEAR:
                return predicate == Predicate.STATUS;
            default:
                return false;
        }
    }

    private void processCommand(Type type, Predicate predicate, SlashCommandInteractionEvent event) {
        switch (type) {
            case ANTISPAM:
                //TODO: Implement logic for anti-spam
                break;
            case VOICEACTION:
            case ANTISWEAR:
                //TODO: Implement logic for voice action and anti-swear
                break;
        }
    }

    private enum Type {
        VOICEACTION, ANTISWEAR, ANTISPAM
    }

    private enum Predicate {
        TIMELIMIT, MESSAGELIMIT, STATUS
    }
}
