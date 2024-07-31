package beta.com.moderationdiscordbot.slashcommandsmanager.commands.punishmentsearchcommands;

import beta.com.moderationdiscordbot.filtersortmodule.FilterSortModule;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.punishmentsearchcommands.typemanager.SearchTypeManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class PunishmentSearchCommand extends ListenerAdapter {

    private final EmbedBuilderManager embedBuilderManager;
    private final ServerSettings serverSettings;
    private final LanguageManager languageManager;
    private final HandleErrors errorManager;
    private final RateLimit rateLimit;
    private final FilterSortModule filterSortModule;

    private static final Map<SearchTypeManager, List<String>> VALID_PREDICATES;

    static {
        VALID_PREDICATES = new EnumMap<>(SearchTypeManager.class);
        VALID_PREDICATES.put(SearchTypeManager.MUTE, Arrays.asList("username", "reason", "date_range"));
        VALID_PREDICATES.put(SearchTypeManager.BAN, Arrays.asList("username", "reason", "date_range"));
        VALID_PREDICATES.put(SearchTypeManager.WARN, Arrays.asList("username", "reason", "date_range"));
    }

    public PunishmentSearchCommand(ServerSettings serverSettings, LanguageManager languageManager, HandleErrors errorManager, RateLimit rateLimit, String sessionId) {
        this.serverSettings = serverSettings;
        this.languageManager = languageManager;
        this.errorManager = errorManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.rateLimit = rateLimit;
        this.filterSortModule = new FilterSortModule(sessionId);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("punishment-search")) {
            String typeOption = Optional.ofNullable(event.getOption("type"))
                    .map(option -> option.getAsString().toLowerCase())
                    .orElse(null);

            String predicateOption = Optional.ofNullable(event.getOption("predicate"))
                    .map(option -> option.getAsString().toLowerCase())
                    .orElse(null);

            String valueOption = Optional.ofNullable(event.getOption("value"))
                    .map(option -> option.getAsString())
                    .orElse(null);

            if (typeOption == null || predicateOption == null) {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.punishmentsearch.missing_arguments", null,
                                serverSettings.getLanguage(event.getGuild().getId())).build())
                        .setEphemeral(true).queue();
                return;
            }

            if (rateLimit.isRateLimited(event, embedBuilderManager, serverSettings)) {
                return;
            }

            SearchTypeManager searchType;
            try {
                searchType = getSearchType(typeOption);
            } catch (IllegalArgumentException e) {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.punishmentsearch.invalid_type", null,
                                serverSettings.getLanguage(event.getGuild().getId())).build())
                        .setEphemeral(true).queue();
                return;
            }

            if (!isValidPredicate(predicateOption, searchType)) {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.punishmentsearch.invalid_predicate", null,
                                serverSettings.getLanguage(event.getGuild().getId())).build())
                        .setEphemeral(true).queue();
                return;
            }

            if (isPredicateRequiringValue(predicateOption) && (valueOption == null || valueOption.isEmpty())) {
                event.replyEmbeds(embedBuilderManager.createEmbed("commands.punishmentsearch.missing_value", null,
                                serverSettings.getLanguage(event.getGuild().getId())).build())
                        .setEphemeral(true).queue();
                return;
            }

            executeSearch(event, searchType, predicateOption, valueOption);
        }
    }

    private SearchTypeManager getSearchType(String type) throws IllegalArgumentException {
        try {
            return SearchTypeManager.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid punishment type");
        }
    }

    private boolean isValidPredicate(String predicate, SearchTypeManager searchType) {
        return VALID_PREDICATES.get(searchType).contains(predicate.toLowerCase());
    }

    private boolean isPredicateRequiringValue(String predicate) {
        return predicate.equalsIgnoreCase(SearchTypeManager.SearchCategory.Filters.REASON.name()) ||
                predicate.equalsIgnoreCase(SearchTypeManager.SearchCategory.Filters.ReasonFilter.CONTAINS.name()) ||
                predicate.equalsIgnoreCase(SearchTypeManager.SearchCategory.Filters.ReasonFilter.EQUALS.name()) ||
                predicate.equalsIgnoreCase(SearchTypeManager.SearchCategory.Filters.DATE_RANGE.name());
    }
    private void executeSearch(SlashCommandInteractionEvent event, SearchTypeManager searchType, String predicate, String value) {
        String language = serverSettings.getLanguage(event.getGuild().getId());

        performSearch(event, predicate, value, result -> {
            String resultsFormatted = formatResults(result);

            event.replyEmbeds(embedBuilderManager.createEmbed("commands.punishmentsearch.result", null, language)
                    .addField(languageManager.getMessage("commands.punishmentsearch.field_result", language), resultsFormatted, false)
                    .build()).queue();
        });
    }

    private void performSearch(SlashCommandInteractionEvent event, String predicate, String value, Consumer<List<String>> callback) {
        String jsonPayload = createJsonPayload(predicate, value);
        try {
            String response = filterSortModule.postMatch(jsonPayload);
            List<String> results = parseResults(response);
            callback.accept(results);
        } catch (IOException e) {
            e.printStackTrace();
            event.replyEmbeds(embedBuilderManager.createEmbed("commands.punishmentsearch.error", null,
                            serverSettings.getLanguage(event.getGuild().getId())).build())
                    .setEphemeral(true).queue();
        }
    }

    private String createJsonPayload(String predicate, String value) {
        String dbName = "";
        String collectionName = "";
        String connectionString = "";

        return String.format("{\n" +
                        "  \"db_name\": \"%s\",\n" +
                        "  \"collection_name\": \"%s\",\n" +
                        "  \"connection_string\": \"%s\",\n" +
                        "  \"match\": {\n" +
                        "    \"users\": {\n" +
                        "      \"$elemMatch\": {\n" +
                        "        \"%s\": \"%s\"\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}",
                dbName, collectionName, connectionString, predicate, value);
    }

    private List<String> parseResults(String response) {
        return Arrays.asList(response.split("\n"));
    }

    private String formatResults(List<String> results) {
        return String.join("\n", results);
    }
}
