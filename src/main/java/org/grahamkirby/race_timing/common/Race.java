/*
 * Copyright 2024 Graham Kirby:
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

import com.itextpdf.io.font.constants.StandardFonts;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.output.RaceOutput;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

import static org.grahamkirby.race_timing.common.Normalisation.parseTime;

public abstract class Race {

    public record PrizeCategoryGroup(String combined_categories_title, List<PrizeCategory> categories){}

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String PRIZE_FONT_NAME = StandardFonts.HELVETICA;
    public static final String PRIZE_FONT_BOLD_NAME = StandardFonts.HELVETICA_BOLD;
    public static final String PRIZE_FONT_ITALIC_NAME = StandardFonts.HELVETICA_OBLIQUE;
    public static final int PRIZE_FONT_SIZE = 24;

    public static final String DUMMY_DURATION_STRING = "23:59:59";
    public static final Duration DUMMY_DURATION = parseTime(DUMMY_DURATION_STRING);
    public static final String COMMENT_SYMBOL = "#";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String KEY_ENTRIES_PATH = "ENTRIES_PATH";
    public static final String KEY_RAW_RESULTS_PATH = "RAW_RESULTS_PATH";
    public static final String KEY_PAPER_RESULTS_PATH = "PAPER_RESULTS_PATH";
    public static final String KEY_ANNOTATIONS_PATH = "ANNOTATIONS_PATH";

    public static final String KEY_CATEGORIES_ENTRY_PATH = "CATEGORIES_ENTRY_PATH";
    public static final String KEY_CATEGORIES_PRIZE_PATH = "CATEGORIES_PRIZE_PATH";

    public static final String KEY_MINIMUM_NUMBER_OF_RACES = "MINIMUM_NUMBER_OF_RACES";

    public static final String KEY_RACE_NAME_FOR_RESULTS = "RACE_NAME_FOR_RESULTS";
    public static final String KEY_RACE_NAME_FOR_FILENAMES = "RACE_NAME_FOR_FILENAMES";

    public static final String KEY_ENTRY_MAP = "ENTRY_MAP_PATH";
    public static final String KEY_NORMALISED_CLUB_NAMES = "NORMALISED_CLUB_NAMES_PATH";
    public static final String KEY_CAPITALISATION_STOP_WORDS = "CAPITALISATION_STOP_WORDS_PATH";
    public static final String KEY_NORMALISED_HTML_ENTITIES_PATH = "NORMALISED_HTML_ENTITIES_PATH";

    public static final String KEY_SELF_TIMED = "SELF_TIMED";
    public static final String KEY_SECOND_WAVE_CATEGORIES = "SECOND_WAVE_CATEGORIES";

    public static final String KEY_TIME_TRIAL = "TIME_TRIAL";
    public static final String KEY_WAVE_START_OFFSETS = "WAVE_START_OFFSETS";

    public static final String KEY_NUMBER_OF_LEGS = "NUMBER_OF_LEGS";
    public static final String KEY_PAIRED_LEGS = "PAIRED_LEGS";
    public static final String KEY_DNF_LEGS = "DNF_LEGS";
    public static final String KEY_INDIVIDUAL_LEG_STARTS = "INDIVIDUAL_LEG_STARTS";
    public static final String KEY_MASS_START_ELAPSED_TIMES = "MASS_START_ELAPSED_TIMES";

    public static final String KEY_START_OFFSET = "START_OFFSET";
    public static final String KEY_RACES = "RACES";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final String DEFAULT_ENTRY_MAP_PATH = "/src/main/resources/configuration/default_entry_map.csv";
    private static final String DEFAULT_NORMALISED_HTML_ENTITIES_PATH = "/src/main/resources/configuration/html_entities.csv";
    private static final String DEFAULT_NORMALISED_CLUB_NAMES_PATH = "/src/main/resources/configuration/club_names.csv";
    private static final String DEFAULT_CAPITALISATION_STOP_WORDS_PATH = "/src/main/resources/configuration/capitalisation_stop_words.csv";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private final Path config_file_path;
    private final Properties properties;

    public final Map<PrizeCategory, List<RaceResult>> prize_winners;
    protected final List<RaceResult> overall_results;

    protected RacePrizes prizes;
    protected StringBuilder notes;

    public RaceInput input;
    public RaceOutput output_CSV, output_HTML, output_text, output_PDF;
    public Normalisation normalisation;

    public Map<String, String> normalised_html_entities;
    public List<String> capitalisation_stop_words;
    public Set<String> non_title_case_words;
    public Map<String, String> normalised_club_names;

    public Map<String, String> entry_map;
    public String entry_column_map_string;

    public List<EntryCategory> entry_categories;
    public List<PrizeCategoryGroup> prize_category_groups;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public Race(final Path config_file_path) throws IOException {

        this.config_file_path = config_file_path;
        properties = loadProperties(config_file_path);

        prize_winners = new HashMap<>();
        overall_results = new ArrayList<>();
        notes = new StringBuilder();

        configure();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public abstract void processResults() throws IOException;
    public abstract boolean allowEqualPositions();
    public abstract boolean isEligibleForGender(EntryCategory entry_category, PrizeCategory prize_category);

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public final boolean isEligibleFor(final EntryCategory entry_category, final PrizeCategory prize_category) {

        return isEligibleForGender(entry_category, prize_category) && isEligibleForAge(entry_category, prize_category);
    }

    public boolean isEligibleForAge(final EntryCategory entry_category, final PrizeCategory prize_category) {

        return entry_category.getMinimumAge() >= prize_category.getMinimumAge() &&
                entry_category.getMaximumAge() <= prize_category.getMaximumAge();
    }

    public Path getPath(final String path) {

        if (path.startsWith("/")) return getPathRelativeToProjectRoot(path);
        else return getPathRelativeToRaceConfigFile(path);
    }

    public static Path getPathRelativeToProjectRoot(final String path) {

        return Paths.get(path.substring(1));
    }

    public Path getPathRelativeToRaceConfigFile(final String path) {

        return config_file_path.getParent().resolve(path);
    }

    public static Path getTestResourcesRootPath(final String individual_test_resource_root) {

        return getPathRelativeToProjectRoot("/src/test/resources/" + individual_test_resource_root);
    }

    public void configure() throws IOException {

        configureNormalisation();
        configureImportCategoryMap();
        configureCategories();
    }

    private void configureCategories() throws IOException {

        entry_categories = Files.readAllLines(getPath(getProperty(KEY_CATEGORIES_ENTRY_PATH))).stream().map(EntryCategory::new).toList();
        prize_category_groups = getPrizeCategoryGroups(getPath(getProperty(KEY_CATEGORIES_PRIZE_PATH)));
    }

    public List<PrizeCategory> getPrizeCategories() {

        List<PrizeCategory> prize_categories = new ArrayList<>();
        for (final PrizeCategoryGroup group : prize_category_groups) {
            prize_categories.addAll(group.categories);
        }
        return prize_categories;
    }

    private List<PrizeCategoryGroup> getPrizeCategoryGroups(final Path prize_categories_path) throws IOException {

        List<PrizeCategoryGroup> groups = new ArrayList<>();

        for (final String line : Files.readAllLines(prize_categories_path)) {

            final String group_name = line.split(",")[5];
            
            if (groups.isEmpty())
                groups.add(new PrizeCategoryGroup(group_name, new ArrayList<>()));

            PrizeCategoryGroup group = groups.getLast();

            if (!group.combined_categories_title.equals(group_name)) {
                group = new PrizeCategoryGroup(group_name, new ArrayList<>());
                groups.add(group);
            }

            group.categories.add(new PrizeCategory(line));
        }

        return groups;
    }

    public List<RaceResult> getOverallResults() {
        return overall_results;
    }

    public List<RaceResult> getResultsByCategory(List<PrizeCategory> ignore) {
        return overall_results;
    }

    public void allocatePrizes() {
        prizes.allocatePrizes();
    }

    public String getProperty(final String key) {
        return properties.getProperty(key);
    }

    public String getProperty(final String property_key, final String default_value) {

        final String value = properties.getProperty(property_key);
        return value == null || value.isBlank() ? default_value : value;
    }

    public StringBuilder getNotes() {
        return notes;
    }

    public EntryCategory lookupCategory(final String short_name) {

        return entry_categories.stream().filter(category -> category.getShortName().equals(short_name)).findFirst().
                orElseThrow(() -> new RuntimeException("Category not found: " + short_name));
    }

    public String mapCategory(final String category) {

        final String result = entry_map.get(category);
        return result == null ? category : result;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static Properties loadProperties(final Path config_file_path) throws IOException {

        try (final FileInputStream stream = new FileInputStream(config_file_path.toString())) {

            final Properties properties = new Properties();
            properties.load(stream);
            return properties;
        }
    }

    protected void configureNormalisation() throws IOException {

        normalisation = new Normalisation(this);

        normalised_club_names = loadNormalisationMap(KEY_NORMALISED_CLUB_NAMES, DEFAULT_NORMALISED_CLUB_NAMES_PATH);
        normalised_html_entities = loadNormalisationMap(KEY_NORMALISED_HTML_ENTITIES_PATH, DEFAULT_NORMALISED_HTML_ENTITIES_PATH);
        capitalisation_stop_words = Files.readAllLines(getPath(getProperty(KEY_CAPITALISATION_STOP_WORDS, DEFAULT_CAPITALISATION_STOP_WORDS_PATH)));
        non_title_case_words = new HashSet<>();
    }

    protected void configureImportCategoryMap() throws IOException {

        entry_map = loadImportCategoryMap(KEY_ENTRY_MAP, DEFAULT_ENTRY_MAP_PATH);
    }

    private Map<String, String> loadImportCategoryMap(final String path_key, final String default_path) throws IOException {

        final String path = getProperty(path_key, default_path);

        final Map<String, String> map = new HashMap<>();

        if (path != null) {

            final List<String> lines = Files.readAllLines(getPath(path));

            int index = 0;
            entry_column_map_string = lines.get(index);
            while (entry_column_map_string.startsWith(COMMENT_SYMBOL))
                entry_column_map_string = lines.get(++index);

            for (final String line : lines.subList(index + 1, lines.size())) {

                if (!line.startsWith(COMMENT_SYMBOL)) {
                    final String[] parts = line.split(",");
                    map.put(parts[0], parts[1]);
                }
            }
        }

        return map;
    }

    protected Map<String, String> loadNormalisationMap(final String path_key, final String default_path) throws IOException {

        return loadMap(getProperty(path_key, default_path));
    }

    private Map<String, String> loadMap(final String path_string) throws IOException {

        final Map<String, String> map = new HashMap<>();

        if (path_string != null) {

            for (final String line : Files.readAllLines(getPath(path_string))) {

                final String[] parts = line.split(",");
                map.put(parts[0], parts[1]);
            }
        }

        return map;
    }
}
