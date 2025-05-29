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

    // TODO consolidate input validation.
    // TODO add junior hill races.
    // TODO tests - check existence of required config fields.
    // TODO tests - validate required config fields.
    // TODO tests - validate optional config fields.
    // TODO mutation tests.
    // TODO fuzz tests.
    // TODO test missing output directory.
    // TODO test input directory with different name.
    // TODO test missing config file for individual race in series.
    // TODO prompt for config file if not supplied as arg.
    // TODO update README (https://www.makeareadme.com)

    /** Comment symbol used within configuration files. */
    public static final String COMMENT_SYMBOL = "#";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    // Configuration file keys.
    public static final String KEY_YEAR = "YEAR";
    public static final String KEY_RACE_NAME_FOR_RESULTS = "RACE_NAME_FOR_RESULTS";
    public static final String KEY_RACE_NAME_FOR_FILENAMES = "RACE_NAME_FOR_FILENAMES";
    private static final String KEY_CATEGORIES_ENTRY_PATH = "CATEGORIES_ENTRY_PATH";
    private static final String KEY_CATEGORIES_PRIZE_PATH = "CATEGORIES_PRIZE_PATH";
    private static final String KEY_GENDER_ELIGIBILITY_MAP_PATH = "GENDER_ELIGIBILITY_MAP_PATH";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Platform-specific line separator used in creating output files. */
    public static final String LINE_SEPARATOR = System.lineSeparator();

    /** Used when a result is recorded without a bib number. */
    public static final int UNKNOWN_BIB_NUMBER = 0;

    /** Index of prize category group name within the relevant config file. */
    private static final int PRIZE_CATEGORY_GROUP_NAME_INDEX = 6;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public final Path config_file_path;
    private final Properties properties;

    public RacePrizes prizes;
    private StringBuilder notes;

    public Normalisation normalisation;
    protected RaceInput input;
    protected RaceOutputCSV output_CSV;
    protected RaceOutputHTML output_HTML;
    protected RaceOutputText output_text;
    private RaceOutputPDF output_PDF;

    /** Overall race results. */
    protected List<RaceResult> overall_results;

    /**
     * List of valid entry categories.
     * Value is read from configuration file using key KEY_CATEGORIES_ENTRY_PATH.
     */
    private List<EntryCategory> entry_categories;

    /**
     * List of prize categories.
     * Value is read from configuration file using key KEY_CATEGORIES_PRIZE_PATH.
     */
    public List<PrizeCategoryGroup> prize_category_groups;

    /**
     * Map from entry gender to eligible prize gender.
     * Value is read from configuration file using key KEY_GENDER_ELIGIBILITY_MAP_PATH.
     */
    private Map<String, String> gender_eligibility_map;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected Race(final Path config_file_path) throws IOException {

        this.config_file_path = config_file_path;
        properties = loadProperties(config_file_path);

        configure();
    }

    @FunctionalInterface
    protected interface RaceFactory {

        Race apply(String config_file_path) throws Exception;
    }

    protected static void commonMain(final String[] args, final RaceFactory factory, final String class_name) {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println(STR."usage: java \{class_name} <config file path>");
        else {
            try {
                factory.apply(args[0]).processResults();
            } catch (@SuppressWarnings("OverlyBroadCatchBlock") final Throwable e) {
                System.err.println(e.getMessage());
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Defines whether or not equal positions are allowed. Typically this depends on
     * whether the race is an individual race (no equal positions) or a series race (equal positions).
     * @return true if equal positions are allowed
     */
    public abstract boolean areEqualPositionsAllowed();
    public abstract void calculateResults();

    protected abstract RaceInput getInput();
    protected abstract RaceOutputCSV getOutputCSV();
    protected abstract RaceOutputHTML getOutputHTML();
    protected abstract RaceOutputText getOutputText();
    protected abstract RaceOutputPDF getOutputPDF();

    protected abstract void readProperties() throws IOException;
    protected abstract void configureInputData() throws IOException;
    protected abstract void outputResults() throws IOException;
    protected abstract List<Comparator<RaceResult>> getComparators();

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void processResults() throws IOException {

        calculateResults();
        outputResults();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void configureHelpers() throws IOException {

        input = getInput();

        output_CSV = getOutputCSV();
        output_HTML = getOutputHTML();
        output_text = getOutputText();
        output_PDF = getOutputPDF();

        prizes = new RacePrizes(this);
        normalisation = new Normalisation(this);
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

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Resolves the given path relative to either the project root, if it's specified as an absolute
     *  path, or to the race configuration file. */
    public Path getPath(final String path) {

        return path.startsWith("/") ?
            getPathRelativeToProjectRoot(path) :
            getPathRelativeToRaceConfigFile(path);
    }

    private static Path getPathRelativeToProjectRoot(final String path) {

        return Paths.get(path.substring(1));
    }

    private Path getPathRelativeToRaceConfigFile(final String path) {

        return config_file_path.getParent().resolve(path);
    }

    public static Path getTestResourcesRootPath(final String individual_test_resource_root) {

        return getPathRelativeToProjectRoot(STR."/src/test/resources/\{individual_test_resource_root}");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Tests whether the given entry category is eligible for the given prize category. */
    final boolean isResultEligibleForPrizeCategory(final RaceResult result, final PrizeCategory prize_category) {

        return isResultEligibleForPrizeCategoryByGender(result, prize_category) &&
            isResultEligibleForPrizeCategoryByAge(result, prize_category) &&
            isResultEligibleForPrizeCategoryByClub(result, prize_category);
    }

    /** Tests whether the given entry category is eligible in any of the given prize categories. */
    private boolean isResultEligibleInSomePrizeCategory(final RaceResult result, final Collection<PrizeCategory> prize_categories) {

        return prize_categories.stream().
            anyMatch(category -> isResultEligibleForPrizeCategory(result, category));
    }

    public List<PrizeCategory> getPrizeCategories() {

        return prize_category_groups.stream().
            flatMap(group -> group.categories().stream()).
            toList();
    }

    public List<RaceResult> getOverallResults() {

        return overall_results;
    }

    /** Gets all the results eligible for the given prize categories. */
    public List<RaceResult> getOverallResults(final Collection<PrizeCategory> prize_categories) {

        final Predicate<RaceResult> prize_category_filter = result -> isResultEligibleInSomePrizeCategory(result, prize_categories);
        final List<RaceResult> results = overall_results.stream().filter(prize_category_filter).toList();
        setPositionStrings(results);
        return results;
    }

    public String getRequiredProperty(final String key) {

        final String property = properties.getProperty(key);

        if (property == null)
            throw new RuntimeException(STR."no entry for key '\{key}' in file '\{config_file_path.getFileName()}'");

        return property;
    }

    public String getOptionalProperty(final String key) {

        return properties.getProperty(key);
    }

    public String getProperty(final String key, final String default_value) {

        return properties.getProperty(key, default_value);
    }

    public StringBuilder getNotes() {
        return notes;
    }

    public EntryCategory lookupEntryCategory(final String short_name) {

        return entry_categories.stream().
            filter(category -> category.getShortName().equals(short_name)).
            findFirst().
            orElseThrow();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected static Comparator<RaceResult> penaliseDNF(final Comparator<? super RaceResult> base_comparator) {

        return (r1, r2) -> {

            if (!r1.canComplete() && r2.canComplete()) return 1;
            if (r1.canComplete() && !r2.canComplete()) return -1;

            return base_comparator.compare(r1, r2);
        };
    }

    protected static Comparator<RaceResult> ignoreIfEitherResultIsDNF(final Comparator<? super RaceResult> base_comparator) {

        return (r1, r2) -> {

            if (!r1.canComplete() || !r2.canComplete()) return 0;
            else return base_comparator.compare(r1, r2);
        };
    }

    protected static Comparator<RaceResult> ignoreIfBothResultsAreDNF(final Comparator<? super RaceResult> base_comparator) {

        return (r1, r2) -> {

            if (!r1.canComplete() && !r2.canComplete()) return 0;
            else return base_comparator.compare(r1, r2);
        };
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Compares two results based on their performances, which may be based on a single or aggregate time,
     *  or a score. Gives a negative result if the first result has a better performance than the second. */
    protected static int comparePerformance(final RaceResult r1, final RaceResult r2) {

        return r1.comparePerformanceTo(r2);
    }

    /** Compares two results based on alphabetical ordering of the runners' first names. */
    protected static int compareRunnerFirstName(final RaceResult r1, final RaceResult r2) {

        return Normalisation.getFirstName(r1.getIndividualRunnerName()).compareTo(Normalisation.getFirstName(r2.getIndividualRunnerName()));
    }

    /** Compares two results based on alphabetical ordering of the runners' last names. */
    protected static int compareRunnerLastName(final RaceResult r1, final RaceResult r2) {

        return Normalisation.getLastName(r1.getIndividualRunnerName()).compareTo(Normalisation.getLastName(r2.getIndividualRunnerName()));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Sets the position string for each result. These are recorded as strings rather than ints so
     *  that equal results can be recorded as e.g. "13=". Whether or not equal positions are allowed
     *  is determined by the particular race type. */
    void setPositionStrings(final List<? extends RaceResult> results) {

        setPositionStrings(results, areEqualPositionsAllowed());
    }

    /** Sets the position string for each result. These are recorded as strings rather than ints so
     *  that equal results can be recorded as e.g. "13=". Whether or not equal positions are allowed
     *  is determined by the second parameter. */
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
                // by scores rather than times.
                final int highest_index_with_same_performance = getHighestIndexWithSamePerformance(results, result_index);

                if (highest_index_with_same_performance > result_index) {

                    // There are results following this one with the same performance.
                    recordEqualPositions(results, result_index, highest_index_with_same_performance);
                    result_index = highest_index_with_same_performance;
                } else
                    // The following result has a different performance.
                    setPositionStringByPosition(results, result_index);
            }
        } else
            for (int result_index = 0; result_index < results.size(); result_index++)
                setPositionStringByPosition(results, result_index);
    }

    /** Records the same position for the given range of results. */
    private static void recordEqualPositions(final List<? extends RaceResult> results, final int start_index, final int end_index) {

        final String position_string = STR."\{start_index + 1}=";

        for (int i = start_index; i <= end_index; i++)
            results.get(i).position_string = position_string;
    }

    /** Finds the highest index for which the performance is the same as the given index. */
    private static int getHighestIndexWithSamePerformance(final List<? extends RaceResult> results, final int start_index) {

        int highest_index_with_same_result = start_index;

        while (highest_index_with_same_result < results.size() - 1 &&
            results.get(highest_index_with_same_result).comparePerformanceTo(results.get(highest_index_with_same_result + 1)) == 0)

            highest_index_with_same_result++;

        return highest_index_with_same_result;
    }

    /** Sets the position string according to the position itself. */
    private static void setPositionStringByPosition(final List<? extends RaceResult> results, final int result_index) {
        results.get(result_index).position_string = String.valueOf(result_index + 1);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void configure() throws IOException {

        initialise();
        readProperties();

        configureCategories();
        configureHelpers();
        configureInputData();
        configureGenderEligibilityMap();
    }

    private void initialise() {

        notes = new StringBuilder();
    }

    private void configureCategories() throws IOException {

        entry_categories = Files.readAllLines(getPath(getRequiredProperty(KEY_CATEGORIES_ENTRY_PATH))).stream().filter(line -> !line.startsWith(COMMENT_SYMBOL)).map(EntryCategory::new).toList();
        prize_category_groups = new ArrayList<>();
        loadPrizeCategoryGroups(getPath(getRequiredProperty(KEY_CATEGORIES_PRIZE_PATH)));
    }

    private void configureGenderEligibilityMap() throws IOException {

        gender_eligibility_map = new HashMap<>();

        final String gender_eligibility_map_path = getOptionalProperty(KEY_GENDER_ELIGIBILITY_MAP_PATH);

        if (gender_eligibility_map_path != null)
            Files.readAllLines(getPath(gender_eligibility_map_path)).stream().
                filter(line -> !line.startsWith(COMMENT_SYMBOL)).
                forEachOrdered(line -> {
                    final String[] elements = line.split(",");
                    gender_eligibility_map.put(elements[0], elements[1]);
                });
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    public static Properties loadProperties(final Path config_file_path) throws IOException {

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

    /** Sorts all results by relevant comparators, and then the DNF results separately. */
    protected void sortResults() {

        overall_results.sort(combineComparators(getComparators()));
    }

    /** Combines multiple comparators into a single comparator. */
    protected static Comparator<RaceResult> combineComparators(final Collection<Comparator<RaceResult>> comparators) {

        return comparators.stream().
            reduce((_, _) -> 0, Comparator::thenComparing);
    }

    /** Loads prize category groups from the given file. */
    private void loadPrizeCategoryGroups(final Path prize_categories_path) throws IOException {

        Files.readAllLines(prize_categories_path).stream().
            filter(line -> !line.startsWith(COMMENT_SYMBOL)).
            forEachOrdered(this::recordGroup);
    }

    private void recordGroup(final String line) {

        final String group_name = line.split(",")[PRIZE_CATEGORY_GROUP_NAME_INDEX];
        final PrizeCategoryGroup group = getGroupByName(group_name);

        group.categories().add(new PrizeCategory(line));
    }

    private PrizeCategoryGroup getGroupByName(final String group_name) {

        return prize_category_groups.stream().
            filter(g -> g.group_title().equals(group_name)).
            findFirst().
            orElseGet(() -> newGroup(group_name));
    }

    private PrizeCategoryGroup newGroup(final String group_name) {

        final PrizeCategoryGroup group = new PrizeCategoryGroup(group_name, new ArrayList<>());
        prize_category_groups.add(group);
        return group;
    }

    @SuppressWarnings("DataFlowIssue")
    private boolean isResultEligibleForPrizeCategoryByGender(final RaceResult result, final PrizeCategory prize_category) {

        // It's possible for the entry category to be null in a series race, where some of the individual
        // race results may not include entry categories.
        final EntryCategory entry_category = result.getCategory();
        if (entry_category != null && entry_category.getGender().equals(prize_category.getGender())) return true;

        return gender_eligibility_map.keySet().stream().
            filter(entry_gender -> entry_category.getGender().equals(entry_gender)).
            anyMatch(entry_gender -> prize_category.getGender().equals(gender_eligibility_map.get(entry_gender)));
    }

    private static boolean isResultEligibleForPrizeCategoryByAge(final RaceResult result, final PrizeCategory prize_category) {

        // It's possible for the entry category to be null in a series race, where some of the individual
        // race results may not include entry categories.
        final EntryCategory entry_category = result.getCategory();

        return entry_category != null &&
            entry_category.getMinimumAge() >= prize_category.getMinimumAge() &&
            entry_category.getMaximumAge() <= prize_category.getMaximumAge();
    }

    private static boolean isResultEligibleForPrizeCategoryByClub(final RaceResult result, final PrizeCategory prize_category) {

        final String club = result.getIndividualRunnerClub();
        final Set<String> eligible_clubs = prize_category.getEligibleClubs();

        if (club == null || eligible_clubs.isEmpty()) return true;

        return eligible_clubs.contains(club);
    }
}
