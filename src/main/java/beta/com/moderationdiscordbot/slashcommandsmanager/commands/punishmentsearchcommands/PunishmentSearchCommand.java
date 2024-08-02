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
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
        VALID_PREDICATES.put(SearchTypeManager.MUTE, Arrays.asList("username", "reason", "date_range", "duration"));
        VALID_PREDICATES.put(SearchTypeManager.BAN, Arrays.asList("username", "reason", "date_range", "permanent"));
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
            event.deferReply().queue();

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

    private SearchTypeManager getSearchType(String type) {
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
        return Arrays.asList(
                SearchTypeManager.SearchCategory.Filters.REASON.name(),
                SearchTypeManager.SearchCategory.Filters.ReasonFilter.CONTAINS.name(),
                SearchTypeManager.SearchCategory.Filters.ReasonFilter.EQUALS.name(),
                SearchTypeManager.SearchCategory.Filters.DATE_RANGE.name()
        ).contains(predicate.toUpperCase());
    }

    private void executeSearch(SlashCommandInteractionEvent event, SearchTypeManager searchType, String predicate, String value) {
        String language = serverSettings.getLanguage(event.getGuild().getId());

        performSearch(event, searchType, predicate, value, result -> {
            String resultsFormatted = formatResults(result);

            event.getHook().sendMessageEmbeds(embedBuilderManager.createEmbed("commands.punishmentsearch.result", null, language)
                    .addField(languageManager.getMessage("commands.punishmentsearch.field_result", language), resultsFormatted, false)
                    .build()).queue();
        });
    }

    private void performSearch(SlashCommandInteractionEvent event, SearchTypeManager searchType, String predicate, String value, Consumer<List<String>> callback) {
        String jsonPayload = createJsonPayload(searchType, predicate, value);
        try {
            String response = sendRequest(jsonPayload, searchType);
            List<String> results = parseResults(response);
            callback.accept(results);
        } catch (IOException e) {
            e.printStackTrace();
            event.replyEmbeds(embedBuilderManager.createEmbed("commands.punishmentsearch.error", null,
                            serverSettings.getLanguage(event.getGuild().getId())).build())
                    .setEphemeral(true).queue();
        }
    }

    private String createJsonPayload(SearchTypeManager searchType, String predicate, String value) {
        String dbName = "ModDatabase";
        String collectionName = getCollectionName(searchType);
        String connectionString = "mongodb+srv://tunarasimocak:zaUOcIge12qAf0rC@moddatabase.vrwz9ix.mongodb.net/?retryWrites=true&w=majority&appName=ModDatabase";

        JSONObject json = new JSONObject();
        json.put("db_name", dbName);
        json.put("collection_name", collectionName);
        json.put("connection_string", connectionString);

        switch (searchType) {
            case WARN:
                json.put("match", createMatchPayload(predicate, value));
                break;
            case MUTE:
                json.put("filter", createFilterPayload(predicate, value));
                break;
            case BAN:
                json.put("filters", createMultiFilterPayload(predicate, value));
                json.put("sort_data", "date");
                break;
            default:
                throw new IllegalArgumentException("Unsupported search type");
        }

        return json.toString(4);
    }

    private String getCollectionName(SearchTypeManager searchType) {
        switch (searchType) {
            case WARN:
                return "WarnLog";
            case MUTE:
                return "MuteLog";
            case BAN:
                return "BanLog";
            default:
                throw new IllegalArgumentException("Unsupported search type");
        }
    }

    private JSONObject createMatchPayload(String predicate, String value) {
        JSONObject matchPayload = new JSONObject();
        matchPayload.put("warns", new JSONObject().put("$elemMatch", new JSONObject().put(predicate, value)));
        return matchPayload;
    }

    private JSONObject createFilterPayload(String predicate, String value) {
        JSONObject filterPayload = new JSONObject();
        filterPayload.put(predicate, value);
        return filterPayload;
    }

    private JSONObject createMultiFilterPayload(String predicate, String value) {
        JSONObject multiFilterPayload = new JSONObject();
        multiFilterPayload.put("field", predicate);
        multiFilterPayload.put("value", value);
        return multiFilterPayload;
    }

    private String sendRequest(String jsonPayload, SearchTypeManager searchType) throws IOException {
        switch (searchType) {
            case WARN:
                return filterSortModule.postMatch(jsonPayload);
            case MUTE:
                return filterSortModule.postSort(jsonPayload);
            case BAN:
                return filterSortModule.postMultiFilter(jsonPayload);
            default:
                throw new IllegalArgumentException("Unsupported search type");
        }
    }

    private List<String> parseResults(String response) {
        return Arrays.stream(response.split("\n"))
                .filter(line -> !line.trim().isEmpty())
                .collect(Collectors.toList());
    }

    private String formatResults(List<String> results) {
        return results.stream()
                .map(result -> "- " + result)
                .collect(Collectors.joining("\n"));
    }
}