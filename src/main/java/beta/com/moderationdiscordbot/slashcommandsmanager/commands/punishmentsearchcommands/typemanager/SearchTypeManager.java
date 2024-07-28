package beta.com.moderationdiscordbot.slashcommandsmanager.commands.punishmentsearchcommands.typemanager;

public enum SearchTypeManager {
    WARN(SearchCategory.USERNAME, SearchCategory.FILTERS),
    MUTE(SearchCategory.USERNAME, SearchCategory.FILTERS),
    BAN(SearchCategory.USERNAME, SearchCategory.FILTERS);

    private final SearchCategory[] categories;

    SearchTypeManager(SearchCategory... categories) {
        this.categories = categories;
    }

    public SearchCategory[] getCategories() {
        return categories;
    }

    public enum SearchCategory {
        USERNAME,
        FILTERS;

        public enum Filters {
            REASON(ReasonFilter.CONTAINS, ReasonFilter.EQUALS),
            DATE_RANGE(DateRangeFilter.START_DATE, DateRangeFilter.END_DATE);

            private final Enum<?>[] subcategories;

            Filters(Enum<?>... subcategories) {
                this.subcategories = subcategories;
            }

            public Enum<?>[] getSubcategories() {
                return subcategories;
            }

            public enum ReasonFilter {
                CONTAINS,
                EQUALS;
            }

            public enum DateRangeFilter {
                START_DATE,
                END_DATE;
            }
        }
    }
}
