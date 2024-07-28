package beta.com.moderationdiscordbot.slashcommandsmanager.commands.punishmentsearchcommands;

import beta.com.moderationdiscordbot.slashcommandsmanager.commands.punishmentsearchcommands.typemanager.SearchTypeManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;

public class PunishmentSearchCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final HandleErrors errorManager;
    private final RateLimit rateLimit;

    public PunishmentSearchCommand(ServerSettings serverSettings, LanguageManager languageManager, HandleErrors errorManager, RateLimit rateLimit) {
        this.serverSettings = serverSettings;
        this.languageManager = languageManager;
        this.errorManager = errorManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.rateLimit = rateLimit;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("punishment-search")) {
            String typeOption = event.getOption("type").getAsString();
            String predicateOption = event.getOption("predicate").getAsString();
            String valueOption = event.getOption("value") != null ? event.getOption("value").getAsString() : null;

            if (rateLimit.isRateLimited(event, embedBuilderManager, serverSettings)) {
                return;
            }

            SearchTypeManager searchType;
            try {
                searchType = getSearchType(typeOption);
            } catch (IllegalArgumentException e) {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.punishmentsearch.invalid_type", null, serverSettings.getLanguage(event.getGuild().getId())).build())
                        .setEphemeral(true).queue();
                return;
            }

            if (!isValidPredicate(predicateOption, searchType)) {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.punishmentsearch.invalid_predicate", null, serverSettings.getLanguage(event.getGuild().getId())).build())
                        .setEphemeral(true).queue();
                return;
            }

            if (isPredicateRequiringValue(predicateOption) && (valueOption == null || valueOption.isEmpty())) {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.punishmentsearch.missing_value", null, serverSettings.getLanguage(event.getGuild().getId())).build())
                        .setEphemeral(true).queue();
                return;
            }

            executeSearch(event, searchType, predicateOption, valueOption);
        }
    }

    private SearchTypeManager getSearchType(String type) throws IllegalArgumentException {
        switch (type.toLowerCase()) {
            case "mute":
                return SearchTypeManager.MUTE;
            case "ban":
                return SearchTypeManager.BAN;
            case "warn":
                return SearchTypeManager.WARN;
            default:
                throw new IllegalArgumentException("Invalid punishment type");
        }
    }

    private boolean isValidPredicate(String predicate, SearchTypeManager searchType) {
        return Arrays.stream(searchType.getCategories()).anyMatch(category ->
                category == SearchTypeManager.SearchCategory.USERNAME || SearchTypeManager.SearchCategory.FILTERS == category &&
                        Arrays.stream(SearchTypeManager.SearchCategory.Filters.values()).anyMatch(filter ->
                                predicate.equalsIgnoreCase(filter.name()))
        );
    }

    private boolean isPredicateRequiringValue(String predicate) {
        return predicate.equalsIgnoreCase(SearchTypeManager.SearchCategory.Filters.REASON.name()) ||
                predicate.equalsIgnoreCase(SearchTypeManager.SearchCategory.Filters.ReasonFilter.CONTAINS.name()) ||
                predicate.equalsIgnoreCase(SearchTypeManager.SearchCategory.Filters.ReasonFilter.EQUALS.name()) ||
                predicate.equalsIgnoreCase(SearchTypeManager.SearchCategory.Filters.DATE_RANGE.name());
    }
    private void executeSearch(SlashCommandInteractionEvent event, SearchTypeManager searchType, String predicate, String value) {
        //TODO: Implement the search logic here
        String language = serverSettings.getLanguage(event.getGuild().getId());

        event.replyEmbeds(embedBuilderManager.createEmbed("commands.punishmentsearch.started", null, language)
                .addField(languageManager.getMessage("commands.punishmentsearch.field_type", language), searchType.name(), false)
                .addField(languageManager.getMessage("commands.punishmentsearch.field_predicate", language), predicate, false)
                .addField(languageManager.getMessage("commands.punishmentsearch.field_value", language), value != null ? value : "N/A", false)
                .build()).queue();
    }
}
