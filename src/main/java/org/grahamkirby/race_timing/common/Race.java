/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (race-timing@kirby-family.net)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.common;


import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;
import org.grahamkirby.race_timing.common.output.RaceOutputPDF;
import org.grahamkirby.race_timing.common.output.RaceOutputText;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

import static org.grahamkirby.race_timing_experimental.common.Config.*;

/** Base class for all types of race. */
@SuppressWarnings("IncorrectFormatting")
public abstract class Race {

    // TODO rationalise Female/Women gender categories.
    // TODO consolidate input validation.
    // TODO output runner list and duplicate runners in series to processing notes.
    // TODO allow negative early starts.
    // TODO add junior hill races.
    // TODO add individual names to team prize results.
    // TODO allow explicitly recorded dead heat in single race.
    // TODO allow overall dead heat in relay race only where at least one team in a mass start.
    // TODO use tree structured set of result comparators.
    // TODO tests - check existence of required config fields.
    // TODO tests - validate required config fields.
    // TODO tests - validate optional config fields.
    // TODO test for illegal bib number in raw times.
    // TODO mutation tests.
    // TODO fuzz tests.
    // TODO test missing output directory.
    // TODO test input directory with different name.
    // TODO test running from jar.
    // TODO update README (https://www.makeareadme.com).
    // TODO generate racer list for PocketTimer.
    // TODO suppress prize output in individual tour races.

    /** Index of prize category group name within the relevant config file. */
    public static final int PRIZE_CATEGORY_GROUP_NAME_INDEX = 6;

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

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected Race(final Path config_file_path) throws IOException {

        this.config_file_path = config_file_path;
        properties = loadProperties(config_file_path);

        configure();
    }

    public boolean areRecordedBibNumbersUnique() {
        return true;
    }

    @FunctionalInterface
    public interface RaceFactory {

        Race apply(String config_file_path) throws Exception;
    }

    public static void commonMain(final String[] args, final RaceFactory factory) {

        // Path to configuration file should be first argument.

        try {
            factory.apply(readConfigIfNotSupplied(args)[0]).processResults();
        } catch (final Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static String[] readConfigIfNotSupplied(final String[] args) {

        if (args.length == 0)
            try (final Scanner scanner = new Scanner(System.in)) {
                System.out.println("Enter path to configuration file:");
                return new String[]{scanner.nextLine()};
            }

        return args;
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

    public List<String> getTeamPrizes() {
        return List.of();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void processResults() throws IOException {

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

        return Path.of(path.substring(1));
    }

    private Path getPathRelativeToRaceConfigFile(final String path) {

        return config_file_path.getParent().resolve(path);
    }

    public static Path getTestResourcesRootPath(final String individual_test_resource_root) {

        return getPathRelativeToProjectRoot(STR."/src/test/resources/\{individual_test_resource_root}");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

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

        final Predicate<RaceResult> prize_category_filter = result -> result.isResultEligibleInSomePrizeCategory(prize_categories);
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

        return Normalisation.getFirstName(r1.getParticipantName()).compareTo(Normalisation.getFirstName(r2.getParticipantName()));
    }

    /** Compares two results based on alphabetical ordering of the runners' last names. */
    protected static int compareRunnerLastName(final RaceResult r1, final RaceResult r2) {

        return Normalisation.getLastName(r1.getParticipantName()).compareTo(Normalisation.getLastName(r2.getParticipantName()));
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

        for (int result_index = 0; result_index < results.size(); result_index++) {

            final RaceResult result = results.get(result_index);

            if (result.shouldDisplayPosition()) {
                if (allow_equal_positions) {

                    // Skip over any following results with the same performance.
                    // Defined in terms of performance rather than duration, since in some races ranking is determined
                    // by scores rather than times.
                    final int highest_index_with_same_performance = getHighestIndexWithSamePerformance(results, result_index);

                    if (highest_index_with_same_performance > result_index) {

                        // There are results following this one with the same performance.
                        recordEqualPositions(results, result_index, highest_index_with_same_performance);
                        result_index = highest_index_with_same_performance;
                    } else
                        // The following result has a different performance, so just record current position for this one.
                        result.position_string = String.valueOf(result_index + 1);
                } else {
                    result.position_string = String.valueOf(result_index + 1);
                }
            } else {
                result.position_string = "-";
            }
        }
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

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void configure() throws IOException {

        initialise();
        readProperties();

        configureCategories();
        configureHelpers();
        configureInputData();
    }

    private void initialise() {

        notes = new StringBuilder();
    }

    private void configureCategories() throws IOException {

        final Path path = getPath(getRequiredProperty(KEY_ENTRY_CATEGORIES_PATH)).toAbsolutePath();
        if (!Files.exists(path))
            throw new RuntimeException(STR."invalid entry '\{path.getFileName()}' for key '\{KEY_ENTRY_CATEGORIES_PATH}' in file '\{config_file_path.getFileName()}'");

        final Path path2 = getPath(getRequiredProperty(KEY_PRIZE_CATEGORIES_PATH)).toAbsolutePath();
            if (!Files.exists(path2))
                throw new RuntimeException(STR."invalid entry '\{path2.getFileName()}' for key '\{KEY_PRIZE_CATEGORIES_PATH}' in file '\{config_file_path.getFileName()}'");

        entry_categories = Files.readAllLines(getPath(getRequiredProperty(KEY_ENTRY_CATEGORIES_PATH))).stream().filter(line -> !line.startsWith(COMMENT_SYMBOL)).map(EntryCategory::new).toList();
        prize_category_groups = new ArrayList<>();
        loadPrizeCategoryGroups(getPath(getRequiredProperty(KEY_PRIZE_CATEGORIES_PATH)));
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    public static Properties loadProperties(final Path config_file_path) throws IOException {

        if (!Files.exists(config_file_path))
            throw new RuntimeException(STR."missing config file: '\{config_file_path}'");

        try (final InputStream stream = Files.newInputStream(config_file_path)) {

            final Properties properties = new Properties();
            properties.load(stream);
            return properties;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void allocatePrizes() {
        prizes.allocatePrizes();
    }

    /** Sorts all results by relevant comparators. */
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

    public static List<RaceResult> makeMutable(final List<? extends RaceResult> results) {
        return new ArrayList<>(results);
    }
}
