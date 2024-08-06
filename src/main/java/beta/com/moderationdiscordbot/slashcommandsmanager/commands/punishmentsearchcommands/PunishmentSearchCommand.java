package beta.com.moderationdiscordbot.slashcommandsmanager.commands.punishmentsearchcommands;

import beta.com.moderationdiscordbot.envmanager.Env;
import beta.com.moderationdiscordbot.filtersortmodule.FilterSortModule;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.punishmentsearchcommands.typemanager.SearchTypeManager;
import beta.com.moderationdiscordbot.utils.EmbedBuilderManager;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.expectionmanagement.HandleErrors;
import beta.com.moderationdiscordbot.slashcommandsmanager.RateLimit;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
    private String discordid;
    private final Env env;

    private static final Map<SearchTypeManager, List<String>> VALID_PREDICATES;

    static {
        VALID_PREDICATES = new EnumMap<>(SearchTypeManager.class);
        VALID_PREDICATES.put(SearchTypeManager.MUTE, Arrays.asList("userId", "reason"));
        VALID_PREDICATES.put(SearchTypeManager.BAN, Arrays.asList("userId", "reason", "duration"));
        VALID_PREDICATES.put(SearchTypeManager.WARN, Arrays.asList("userId", "reason", "moderator", "warnid"));
    }

    public PunishmentSearchCommand(ServerSettings serverSettings, LanguageManager languageManager, HandleErrors errorManager, RateLimit rateLimit, String sessionId, Env env) {
        this.serverSettings = serverSettings;
        this.languageManager = languageManager;
        this.errorManager = errorManager;
        this.embedBuilderManager = new EmbedBuilderManager(languageManager);
        this.rateLimit = rateLimit;
        this.filterSortModule = new FilterSortModule(sessionId);
        this.env = env;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("punishment-search")) {
            event.deferReply().queue();

            discordid = event.getGuild().getId();

            String typeOption = Optional.ofNullable(event.getOption("type"))
                    .map(option -> option.getAsString().toLowerCase())
                    .orElse(null);

            String predicateOption = Optional.ofNullable(event.getOption("predicate"))
                    .map(option -> option.getAsString())
                    .orElse(null);

            String valueOption = Optional.ofNullable(event.getOption("value"))
                    .map(option -> option.getAsString())
                    .orElse(null);

            if (typeOption == null || predicateOption == null) {
                event.getHook().sendMessageEmbeds(embedBuilderManager.createEmbed("commands.punishmentsearch.missing_arguments", null,
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
                event.getHook().sendMessageEmbeds(embedBuilderManager.createEmbed("commands.punishmentsearch.invalid_type", null,
                                serverSettings.getLanguage(event.getGuild().getId())).build())
                        .setEphemeral(true).queue();
                return;
            }

            if (!isValidPredicate(predicateOption, searchType)) {
                event.getHook().sendMessageEmbeds(embedBuilderManager.createEmbed("commands.punishmentsearch.invalid_predicate", null,
                                serverSettings.getLanguage(event.getGuild().getId())).build())
                        .setEphemeral(true).queue();
                return;
            }

            if (isPredicateRequiringValue(predicateOption) && (valueOption == null || valueOption.isEmpty())) {
                event.getHook().sendMessageEmbeds(embedBuilderManager.createEmbed("commands.punishmentsearch.missing_value", null,
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
        return VALID_PREDICATES.get(searchType).contains(predicate);
    }

    private boolean isPredicateRequiringValue(String predicate) {
        return Arrays.asList(
                SearchTypeManager.SearchCategory.Filters.UserIdFilter.EQUALS.name(),
                SearchTypeManager.SearchCategory.Filters.REASON.name(),
                SearchTypeManager.SearchCategory.Filters.ReasonFilter.CONTAINS.name(),
                SearchTypeManager.SearchCategory.Filters.ReasonFilter.EQUALS.name(),
                SearchTypeManager.SearchCategory.Filters.DATE_RANGE.name()
        ).contains(predicate.toUpperCase());
    }

    private void executeSearch(SlashCommandInteractionEvent event, SearchTypeManager searchType, String predicate, String value) {
        String language = serverSettings.getLanguage(event.getGuild().getId());
        performSearch(event, searchType, predicate, value, result -> {
            String resultsFormatted = formatResults(result, searchType);
            event.getHook().sendMessageEmbeds(embedBuilderManager.createEmbed("commands.punishmentsearch.result", null, language)
                    .addField(languageManager.getMessage("commands.punishmentsearch.field_result", language), resultsFormatted, false)
                    .build()).queue();
        });
    }

    private void performSearch(SlashCommandInteractionEvent event, SearchTypeManager searchType, String predicate, String value, Consumer<List<String>> callback) {
        try {
            String jsonPayload = createJsonPayload(searchType, predicate, value);
            String response = sendRequest(jsonPayload, searchType);
            List<String> results = parseResults(response);
            callback.accept(results);
        } catch (IOException e) {
            e.printStackTrace();
            event.replyEmbeds(embedBuilderManager.createEmbed("commands.punishmentsearch.error", null, serverSettings.getLanguage(event.getGuild().getId())).build())
                    .setEphemeral(true).queue();
        }
    }

    private String createJsonPayload(SearchTypeManager searchType, String predicate, String value) {
        String dbName = env.getProperty("MONGODB_DATABASE_NAME");
        String collectionName = getCollectionName(searchType);
        String connectionString = env.getProperty("MONGODB_CONNECTION_STRING");

        JSONObject json = new JSONObject();
        json.put("db_name", dbName);
        json.put("collection_name", collectionName);
        json.put("connection_string", connectionString);

        switch (searchType) {
            case WARN:
                json.put("match", createMatchPayload(predicate, value));
                json.put("projection", new JSONObject().put("_id", 0).put("warns.$", 1));
                break;
            case MUTE:
                json.put("filter", createFilterPayload(predicate, value));
                json.put("sort", new JSONObject().put("duration", 1));
                break;
            case BAN:
                json.put("filters", createBanFilterPayload(predicate, value));
                json.put("sort_data", new JSONObject().put("users.duration", -1));
                json.put("projection", new JSONObject().put("_id", 0).put("users.userId", 1).put("users.reason", 1).put("users.duration", 1));
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
        JSONObject elemMatch = new JSONObject();
        filterPayload.put("mutes", new JSONObject().put("$elemMatch", elemMatch));
        elemMatch.put(predicate, value);
        return filterPayload;
    }

    private JSONArray createBanFilterPayload(String predicate, String value) {
        JSONArray filters = new JSONArray();

        if (predicate.equalsIgnoreCase("userId")) {
            JSONObject userIdFilter = new JSONObject();
            userIdFilter.put("users.userId", value);
            filters.put(userIdFilter);
        }

        if (predicate.equalsIgnoreCase("reason")) {
            JSONObject reasonFilter = new JSONObject();
            reasonFilter.put("users.reason", value);
            filters.put(reasonFilter);
        }

        if (predicate.equalsIgnoreCase("duration")) {
            String currentDate = DateTimeFormatter.ISO_INSTANT
                    .format(Instant.now().atOffset(ZoneOffset.UTC));

            JSONObject durationFilter = new JSONObject();
            JSONObject durationRange = new JSONObject();
            durationRange.put("$gte", value);
            durationRange.put("$lt", currentDate);
            durationFilter.put("users.duration", durationRange);
            filters.put(durationFilter);
        }

        return filters;
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

    private String formatResults(List<String> result, SearchTypeManager searchType) {
        String combinedResponse = String.join("\n", result);
        JSONArray jsonArray = new JSONArray(combinedResponse);
        StringBuilder formattedResults = new StringBuilder();
        String language = serverSettings.getLanguage(discordid);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            switch (searchType) {
                case WARN:
                    formatWarnResults(jsonObject, formattedResults, language);
                    break;
                case MUTE:
                    formatMuteResults(jsonObject, formattedResults, language);
                    break;
                case BAN:
                    formatBanResults(jsonObject, formattedResults, language);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported search type");
            }
        }
        return formattedResults.toString();
    }

    private void formatWarnResults(JSONObject jsonObject, StringBuilder formattedResults, String language) {
        JSONArray warnsArray = jsonObject.optJSONArray("warns");
        if (warnsArray != null) {
            for (int j = 0; j < warnsArray.length(); j++) {
                JSONObject warnObject = warnsArray.getJSONObject(j);
                formattedResults.append(languageManager.getMessage("commands.punishmentsearch.warn_user_id", language)).append(warnObject.optString("userId", "N/A")).append("\n")
                        .append(languageManager.getMessage("commands.punishmentsearch.warn_reason", language)).append(warnObject.optString("reason", "N/A")).append("\n")
                        .append(languageManager.getMessage("commands.punishmentsearch.warn_warning_id", language)).append(warnObject.optString("warningId", "N/A")).append("\n")
                        .append(languageManager.getMessage("commands.punishmentsearch.warn_moderator", language)).append(warnObject.optString("moderator", "N/A")).append("\n")
                        .append("---------\n");
            }
        }
    }

    private void formatMuteResults(JSONObject jsonObject, StringBuilder formattedResults, String language) {
        JSONArray mutesArray = jsonObject.optJSONArray("mutes");
        if (mutesArray != null) {
            for (int j = 0; j < mutesArray.length(); j++) {
                JSONObject muteObject = mutesArray.getJSONObject(j);
                formattedResults.append(languageManager.getMessage("commands.punishmentsearch.mute_user_id", language)).append(muteObject.optString("userId", "N/A")).append("\n")
                        .append(languageManager.getMessage("commands.punishmentsearch.mute_reason", language)).append(muteObject.optString("reason", "N/A")).append("\n")
                        .append(languageManager.getMessage("commands.punishmentsearch.mute_duration", language)).append(muteObject.optString("duration", "N/A")).append("\n")
                        .append("---------\n");
            }
        }
    }

    private void formatBanResults(JSONObject jsonObject, StringBuilder formattedResults, String language) {
        JSONArray bansArray = jsonObject.optJSONArray("users");
        if (bansArray != null) {
            for (int j = 0; j < bansArray.length(); j++) {
                JSONObject banObject = bansArray.getJSONObject(j);
                formattedResults.append(languageManager.getMessage("commands.punishmentsearch.ban_user_id", language)).append(banObject.optString("userId", "N/A")).append("\n")
                        .append(languageManager.getMessage("commands.punishmentsearch.ban_reason", language)).append(banObject.optString("reason", "N/A")).append("\n")
                        .append(languageManager.getMessage("commands.punishmentsearch.ban_duration", language)).append(banObject.optString("duration", "N/A")).append("\n")
                        .append("---------\n");
            }
        }
    }
}
