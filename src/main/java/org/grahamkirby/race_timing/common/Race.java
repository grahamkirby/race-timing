/*
 * Copyright 2025 Graham Kirby:
 * <https://github.com/grahamkirby/race-timing>
 *
 * This file is part of the module race-timing.
 *
 * race-timing is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * race-timing is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with race-timing. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.common;

import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;
import org.grahamkirby.race_timing.common.output.RaceOutputPDF;
import org.grahamkirby.race_timing.common.output.RaceOutputText;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

/** Base class for all types of race. */
@SuppressWarnings("IncorrectFormatting")
public abstract class Race {

    /** Comment symbol used within configuration files. */
    public static final String COMMENT_SYMBOL = "#";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    // Configuration file keys.
    public static final String KEY_YEAR = "YEAR";
    public static final String KEY_RACE_NAME_FOR_RESULTS = "RACE_NAME_FOR_RESULTS";
    public static final String KEY_RACE_NAME_FOR_FILENAMES = "RACE_NAME_FOR_FILENAMES";
    public static final String KEY_CATEGORIES_ENTRY_PATH = "CATEGORIES_ENTRY_PATH";
    public static final String KEY_CATEGORIES_PRIZE_PATH = "CATEGORIES_PRIZE_PATH";
    private static final String KEY_ENTRY_MAP_PATH = "ENTRY_MAP_PATH";
    private static final String KEY_NORMALISED_CLUB_NAMES_PATH = "NORMALISED_CLUB_NAMES_PATH";
    private static final String KEY_CAPITALISATION_STOP_WORDS_PATH = "CAPITALISATION_STOP_WORDS_PATH";
    private static final String KEY_NORMALISED_HTML_ENTITIES_PATH = "NORMALISED_HTML_ENTITIES_PATH";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Platform-specific line separator used in creating output files. */
    public static final String LINE_SEPARATOR = System.lineSeparator();

    public static final String SUFFIX_CSV = ".csv";
    public static final String SUFFIX_PDF = ".pdf";

    /** Used when a result is recorded without a bib number. */
    public static final int UNKNOWN_BIB_NUMBER = 0;
    protected static final int UNKNOWN_RACE_POSITION = 0;
    private static final int PRIZE_CATEGORY_GROUP_NAME_INDEX = 6;

    private static final String DEFAULT_ENTRY_MAP_PATH = STR."/src/main/resources/configuration/default_entry_map\{SUFFIX_CSV}";
    private static final String DEFAULT_NORMALISED_HTML_ENTITIES_PATH = STR."/src/main/resources/configuration/html_entities\{SUFFIX_CSV}";
    private static final String DEFAULT_NORMALISED_CLUB_NAMES_PATH = STR."/src/main/resources/configuration/club_names\{SUFFIX_CSV}";
    private static final String DEFAULT_CAPITALISATION_STOP_WORDS_PATH = STR."/src/main/resources/configuration/capitalisation_stop_words\{SUFFIX_CSV}";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private final Path config_file_path;
    private final Properties properties;

    protected List<RaceResult> overall_results;

    public RacePrizes prizes;
    private StringBuilder notes;

    protected RaceInput input;
    protected RaceOutputCSV output_CSV;
    protected RaceOutputHTML output_HTML;
    protected RaceOutputText output_text;
    public RaceOutputPDF output_PDF;
    public Normalisation normalisation;

    Map<String, String> normalised_html_entities;
    Set<String> capitalisation_stop_words;
    Map<String, String> normalised_club_names;
    public Set<String> non_title_case_words;

    private Map<String, String> entry_map;
    public String entry_column_map_string;

    private List<EntryCategory> entry_categories;
    public List<PrizeCategoryGroup> prize_category_groups;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected Race(final Path config_file_path) throws IOException {

        this.config_file_path = config_file_path;
        properties = loadProperties(config_file_path);

        configure();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract void calculateResults();
    public abstract boolean areEqualPositionsAllowed();

    protected abstract RaceInput getInput();
    protected abstract RaceOutputCSV getOutputCSV();
    protected abstract RaceOutputHTML getOutputHTML();
    protected abstract RaceOutputText getOutputText();
    protected abstract RaceOutputPDF getOutputPDF();

    protected abstract void readProperties() throws IOException;
    protected abstract void configureInputData() throws IOException;
    protected abstract void outputResults() throws IOException;
    protected abstract List<Comparator<RaceResult>> getComparators();
    protected abstract List<Comparator<RaceResult>> getDNFComparators();
    protected abstract boolean isEntryCategoryEligibleForPrizeCategoryByGender(EntryCategory entry_category, PrizeCategory prize_category);
    protected abstract EntryCategory getEntryCategory(RaceResult result);

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void processResults() throws IOException {

        calculateResults();
        outputResults();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void configureHelpers() {

        input = getInput();

        output_CSV = getOutputCSV();
        output_HTML = getOutputHTML();
        output_text = getOutputText();
        output_PDF = getOutputPDF();

        prizes = new RacePrizes(this);
    }

    protected void printOverallResults() throws IOException {

        output_CSV.printResults();
        output_HTML.printResults();
    }

    protected void printPrizes() throws IOException {

        output_PDF.printPrizes();
        output_HTML.printPrizes();
        output_text.printPrizes();
    }

    protected void printNotes() throws IOException {

        output_text.printNotes();
    }

    protected void printCombined() throws IOException {

        output_HTML.printCombined();
    }

    public Path getPath(final String path) {

        return path.startsWith("/") ?
            getPathRelativeToProjectRoot(path) :
            getPathRelativeToRaceConfigFile(path);
    }

    final boolean isEntryCategoryEligibleForPrizeCategory(final EntryCategory entry_category, final PrizeCategory prize_category) {

        return isEntryCategoryEligibleForPrizeCategoryByGender(entry_category, prize_category) && isEntryCategoryEligibleForPrizeCategoryByAge(entry_category, prize_category);
    }

    private boolean isEntryCategoryEligibleInSomePrizeCategory(final EntryCategory entry_category, final Collection<PrizeCategory> prize_categories) {

        return prize_categories.stream().
            map(category -> isEntryCategoryEligibleForPrizeCategory(entry_category, category)).
            reduce(Boolean::logicalOr).
            orElseThrow();
    }

    public List<PrizeCategory> getPrizeCategories() {

        return prize_category_groups.stream().
            flatMap(group -> group.categories().stream()).
            toList();
    }

    public List<RaceResult> getOverallResults() {

        return overall_results;
    }

    public List<RaceResult> getOverallResults(final Collection<PrizeCategory> prize_categories) {

        final Predicate<RaceResult> prize_category_filter = result -> isEntryCategoryEligibleInSomePrizeCategory(getEntryCategory(result), prize_categories);
        return getOverallResults(prize_category_filter);
    }

    private List<RaceResult> getOverallResults(final Predicate<? super RaceResult> inclusion_filter) {

        final List<RaceResult> results = overall_results.stream().filter(inclusion_filter).toList();
        setPositionStrings(results, areEqualPositionsAllowed());
        return results;
    }

    public String getProperty(final String key) {
        return properties.getProperty(key);
    }

    public String getProperty(final String property_key, final String default_value) {

        final String value = getProperty(property_key);
        return value == null || value.isBlank() ? default_value : value;
    }

    public StringBuilder getNotes() {
        return notes;
    }

    public EntryCategory lookupEntryCategory(final String short_name) {

        return entry_categories.stream().
            filter(category -> category.getShortName().equals(short_name)).
            findFirst().
            orElseThrow(() -> new RuntimeException(STR."Category not found: \{short_name}"));
    }

    public String mapCategory(final String category) {

        final String result = entry_map.get(category);
        return result == null ? category : result;
    }

    public static Path getTestResourcesRootPath(final String individual_test_resource_root) {

        return getPathRelativeToProjectRoot(STR."/src/test/resources/\{individual_test_resource_root}");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected static int compareCompletion(final RaceResult r1, final RaceResult r2) {

        return Boolean.compare(r1.getCompletionStatus() != CompletionStatus.COMPLETED, r2.getCompletionStatus() != CompletionStatus.COMPLETED);
    }

    protected static int comparePerformance(final RaceResult r1, final RaceResult r2) {

        return r1.comparePerformanceTo(r2);
    }

    protected static int compareRunnerFirstName(final RaceResult r1, final RaceResult r2) {

        return Normalisation.getFirstName(r1.getIndividualRunnerName()).compareTo(Normalisation.getFirstName(r2.getIndividualRunnerName()));
    }

    protected static int compareRunnerLastName(final RaceResult r1, final RaceResult r2) {

        return Normalisation.getLastName(r1.getIndividualRunnerName()).compareTo(Normalisation.getLastName(r2.getIndividualRunnerName()));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected static void setPositionStrings(final List<? extends RaceResult> results, final boolean allow_equal_positions) {

        // Sets position strings for dead heats, if allowed by the allow_equal_positions flag.
        // E.g. if results 3 and 4 have the same time, both will be set to "3=".

        // The flag is passed in rather than using race.allowEqualPositions() since that applies to the race overall.
        // In a series race the individual races don't allow equal positions, but the race overall does.
        // Conversely in a relay race the legs after the first leg do allow equal positions.

        if (allow_equal_positions) {

            for (int result_index = 0; result_index < results.size(); result_index++) {

                // Skip over any following results with the same performance.
                // Defined in terms of performance rather than duration, since in some races ranking is determined
                // by points rather than times.
                final int highest_index_with_same_performance = getHighestIndexWithSamePerformance(results, result_index);

                if (highest_index_with_same_performance > result_index) {

                    recordEqualPositions(results, result_index, highest_index_with_same_performance);
                    result_index = highest_index_with_same_performance;
                } else
                    setPositionStringByPosition(results, result_index);
            }
        } else
            for (int result_index = 0; result_index < results.size(); result_index++)
                setPositionStringByPosition(results, result_index);
    }

    private static void recordEqualPositions(final List<? extends RaceResult> results, final int start_index, final int end_index) {

        final int equal_position = start_index + 1;

        // Record the same position for all the results with equal times.
        for (int i = start_index; i <= end_index; i++)
            results.get(i).position_string = STR."\{equal_position}=";
    }

    private static int getHighestIndexWithSamePerformance(final List<? extends RaceResult> results, final int start_index) {

        final RaceResult first_result_considered = results.get(start_index);
        int highest_index_with_same_result = start_index;

        while (highest_index_with_same_result + 1 < results.size()) {

            final RaceResult next_result_considered = results.get(highest_index_with_same_result + 1);
            if (first_result_considered.comparePerformanceTo(next_result_considered) != 0) break;
            highest_index_with_same_result++;
        }

        return highest_index_with_same_result;
    }

    private static void setPositionStringByPosition(final List<? extends RaceResult> results, final int result_index) {
        results.get(result_index).position_string = String.valueOf(result_index + 1);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void configure() throws IOException {

        initialise();

        configureNormalisation();
        configureImportCategoryMap();
        configureCategories();

        readProperties();

        configureHelpers();
        configureInputData();
    }

    private void initialise() {

        overall_results = new ArrayList<>();
        notes = new StringBuilder();
    }

    private void configureNormalisation() throws IOException {

        normalisation = new Normalisation(this);

        normalised_club_names = loadNormalisationMap(KEY_NORMALISED_CLUB_NAMES_PATH, DEFAULT_NORMALISED_CLUB_NAMES_PATH, false);
        normalised_html_entities = loadNormalisationMap(KEY_NORMALISED_HTML_ENTITIES_PATH, DEFAULT_NORMALISED_HTML_ENTITIES_PATH, true);
        capitalisation_stop_words = new HashSet<>(Files.readAllLines(getPath(getProperty(KEY_CAPITALISATION_STOP_WORDS_PATH, DEFAULT_CAPITALISATION_STOP_WORDS_PATH))));
        non_title_case_words = new HashSet<>();
    }

    private void configureImportCategoryMap() throws IOException {

        entry_map = loadImportCategoryMap();
    }

    private void configureCategories() throws IOException {

        entry_categories = Files.readAllLines(getPath(getProperty(KEY_CATEGORIES_ENTRY_PATH))).stream().map(EntryCategory::new).toList();
        prize_category_groups = getPrizeCategoryGroups(getPath(getProperty(KEY_CATEGORIES_PRIZE_PATH)));
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    private static Properties loadProperties(final Path config_file_path) throws IOException {

        try (final FileInputStream stream = new FileInputStream(config_file_path.toString())) {

            final Properties properties = new Properties();
            properties.load(stream);
            return properties;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void allocatePrizes() {
        prizes.allocatePrizes();
    }

    protected void sortResults() {

        sortAllResults();
        sortDNFResults();
    }

    protected void sortAllResults() {

        overall_results.sort(combineComparators(getComparators()));
    }

    protected void sortDNFResults() {

        overall_results.sort(dnfOnly(combineComparators(getDNFComparators())));
    }

    protected static Comparator<RaceResult> combineComparators(final Collection<Comparator<RaceResult>> comparators) {

        return comparators.
            stream().
            reduce(Comparator::thenComparing).
            orElse((_, _) -> 0);
    }

    private static Comparator<RaceResult> dnfOnly(final Comparator<? super RaceResult> comparator) {

        return (r1, r2) -> r1.getCompletionStatus() != CompletionStatus.DNF || r2.getCompletionStatus() != CompletionStatus.DNF ? 0 : comparator.compare(r1, r2);
    }

    private static List<PrizeCategoryGroup> getPrizeCategoryGroups(final Path prize_categories_path) throws IOException {

        final List<PrizeCategoryGroup> groups = new ArrayList<>();

        Files.readAllLines(prize_categories_path).stream().
            filter(line -> !line.startsWith(COMMENT_SYMBOL)).
            forEachOrdered(line -> recordGroup(line, groups));

        return groups;
    }

    private static void recordGroup(final String line, final Collection<PrizeCategoryGroup> groups) {

        final String group_name = line.split(",")[PRIZE_CATEGORY_GROUP_NAME_INDEX];
        final PrizeCategoryGroup group = getGroupWithName(groups, group_name);

        group.categories().add(new PrizeCategory(line));
    }

    private static PrizeCategoryGroup getGroupWithName(final Collection<PrizeCategoryGroup> groups, final String group_name) {

        PrizeCategoryGroup group = groups.stream().
            filter(g -> g.group_title().equals(group_name)).
            findFirst().
            orElse(null);

        if (group == null) {
            group = new PrizeCategoryGroup(group_name, new ArrayList<>());
            groups.add(group);
        }

        return group;
    }

    private static boolean isEntryCategoryEligibleForPrizeCategoryByAge(final EntryCategory entry_category, final PrizeCategory prize_category) {

        if (entry_category == null) return true;
        return entry_category.getMinimumAge() >= prize_category.getMinimumAge() &&
            entry_category.getMaximumAge() <= prize_category.getMaximumAge();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static Path getPathRelativeToProjectRoot(final String path) {

        return Paths.get(path.substring(1));
    }

    private Path getPathRelativeToRaceConfigFile(final String path) {

        return config_file_path.getParent().resolve(path);
    }

    private Map<String, String> loadImportCategoryMap() throws IOException {

        final Map<String, String> map = new HashMap<>();
        final Path category_map_path = getPath(getProperty(KEY_ENTRY_MAP_PATH, DEFAULT_ENTRY_MAP_PATH));

        Files.readAllLines(category_map_path).stream().
            filter(line -> !line.isEmpty()).
            filter(line -> !line.startsWith(COMMENT_SYMBOL)).
            forEachOrdered(line -> processLine(line, map));

        return map;
    }

    @SuppressWarnings("BoundedWildcard")
    private void processLine(final String line, final Map<String, String> category_map) {

        if (entry_column_map_string == null)

            // First non-comment line contains column mapping.
            entry_column_map_string = line;

        else {
            // Subsequent non-comment lines contain category mappings.
            final String[] parts = line.split(",");
            category_map.put(parts[0], parts[1]);
        }
    }

    private Map<String, String> loadNormalisationMap(final String path_key, final String default_path, final boolean key_case_sensitive) throws IOException {

        final Map<String, String> map = key_case_sensitive ? new HashMap<>() : new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        loadMap(getPath(getProperty(path_key, default_path)), map);

        return map;
    }

    @SuppressWarnings("BoundedWildcard")
    private static void loadMap(final Path path, final Map<String, String> map) throws IOException {

        Files.readAllLines(path).forEach(line -> {

            final String[] parts = line.split(",");
            map.put(parts[0], parts[1]);
        });
    }
}
