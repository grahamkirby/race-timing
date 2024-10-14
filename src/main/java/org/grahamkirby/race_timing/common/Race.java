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

import org.grahamkirby.race_timing.common.categories.Categories;
import org.grahamkirby.race_timing.common.categories.Category;
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

    // TODO document where dead heats can occur - not where result is directly recorded,
    // only where calculated from other results. E.g. DB overall vs lap time

    public record CategoryGroup(String combined_categories_title, List<String> category_names){}

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String DUMMY_DURATION_STRING = "23:59:59";
    public static final Duration DUMMY_DURATION = parseTime(DUMMY_DURATION_STRING);

    public static final String KEY_SENIOR_RACE = "SENIOR_RACE";
    public static final String KEY_OPEN_PRIZE_CATEGORIES = "OPEN_PRIZE_CATEGORIES";
    public static final String KEY_SENIOR_PRIZE_CATEGORIES = "SENIOR_PRIZE_CATEGORIES";
    public static final String KEY_NUMBER_OF_OPEN_PRIZES = "NUMBER_OF_OPEN_PRIZES";
    public static final String KEY_NUMBER_OF_SENIOR_PRIZES = "NUMBER_OF_SENIOR_PRIZES";
    public static final String KEY_NUMBER_OF_CATEGORY_PRIZES = "NUMBER_OF_CATEGORY_PRIZES";
    public static final String KEY_MINIMUM_NUMBER_OF_RACES = "MINIMUM_NUMBER_OF_RACES";
    public static final String KEY_NORMALISED_HTML_ENTITIES_PATH = "NORMALISED_HTML_ENTITIES_PATH";

    public static final String KEY_RACE_NAME_FOR_RESULTS = "RACE_NAME_FOR_RESULTS";
    public static final String KEY_RACE_NAME_FOR_FILENAMES = "RACE_NAME_FOR_FILENAMES";

    public static final String KEY_DNF_LEGS = "DNF_LEGS";

    public static final String KEY_ENTRY_MAP = "ENTRY_MAP_PATH";
    public static final String KEY_NORMALISED_CLUB_NAMES = "NORMALISED_TEAM_NAMES_PATH";
    public static final String KEY_CAPITALISATION_STOP_WORDS = "INTERNALLY_CAPITALISED_NAMES_PATH";

    public static final String KEY_SELF_TIMED = "SELF_TIMED";
    public static final String KEY_TIME_TRIAL = "TIME_TRIAL";
    public static final String KEY_WAVE_START_OFFSETS = "WAVE_START_OFFSETS";

    public static final String KEY_CATEGORY_PRIZES = "CATEGORY_PRIZES";

    public static final String KEY_ENTRIES_FILENAME = "ENTRIES_FILENAME";
    public static final String KEY_RAW_RESULTS_FILENAME = "RAW_RESULTS_FILENAME";

    public static final String KEY_PAIRED_LEGS = "PAIRED_LEGS";
    public static final String KEY_INDIVIDUAL_LEG_STARTS = "INDIVIDUAL_LEG_STARTS";
    public static final String KEY_MASS_START_ELAPSED_TIMES = "MASS_START_ELAPSED_TIMES";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String KEY_NUMBER_OF_LEGS = "NUMBER_OF_LEGS";
    public static final String KEY_START_OFFSET = "START_OFFSET";
    public static final String KEY_SENIOR_PRIZES = "SENIOR_PRIZES";
    public static final String KEY_PAPER_RESULTS_FILENAME = "PAPER_RESULTS_FILENAME";
    public static final String KEY_ANNOTATIONS_FILENAME = "ANNOTATIONS_FILENAME";

    public static final String KEY_RACES = "RACES";

    private static final String DEFAULT_NORMALISED_HTML_ENTITIES_PATH = "src/main/resources/configuration/html_entities.csv";
    protected static final String DEFAULT_NORMALISED_CLUB_NAMES_PATH = "src/main/resources/configuration/club_names.csv";
    protected static final String DEFAULT_CAPITALISATION_STOP_WORDS_PATH = "src/main/resources/configuration/capitalisation_stop_words.csv";

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private final Path working_directory_path;
    private final Properties properties;

    public final Map<Category, List<RaceResult>> prize_winners;
    protected final List<RaceResult> overall_results;

    public Categories categories;
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
    private String entry_column_map_string;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public Race(final Path config_file_path) throws IOException {

        working_directory_path = config_file_path.getParent().getParent();
        properties = loadProperties(config_file_path);

        prize_winners = new HashMap<>();
        overall_results = new ArrayList<>();
        notes = new StringBuilder();

        configure();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public abstract void processResults() throws IOException;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void configure() throws IOException {

        configureNormalisation();
        configureImportCategoryMap(KEY_ENTRY_MAP);
    }

    public List<RaceResult> getOverallResults() {
        return overall_results;
    }

    public List<RaceResult> getResultsByCategory(List<Category> ignore) {
        return overall_results;
    }

    public void allocatePrizes() {
        prizes.allocatePrizes();
    }

    public List<CategoryGroup> getResultCategoryGroups() {
        return List.of(new CategoryGroup("Everything", List.of()));
    }

    public Path getWorkingDirectoryPath() {
        return working_directory_path;
    }

    public Properties getProperties() {
        return properties;
    }

    public StringBuilder getNotes() {
        return notes;
    }

    public Category lookupCategory(final String short_name) {

        for (final Category category : categories.getRunnerCategories())
            if (category.getShortName().equals(short_name)) return category;

        throw new RuntimeException("Category not found: " + short_name);
    }

    public String getPropertyWithDefault(final String property_key, final String default_value) {

        final String value = properties.getProperty(property_key);
        return value == null || value.isBlank() ? default_value : value;
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

    private void configureNormalisation() throws IOException {

        normalisation = new Normalisation(this);

        normalised_club_names = loadNormalisationMap(KEY_NORMALISED_CLUB_NAMES, DEFAULT_NORMALISED_CLUB_NAMES_PATH);
        normalised_html_entities = loadNormalisationMap(KEY_NORMALISED_HTML_ENTITIES_PATH, DEFAULT_NORMALISED_HTML_ENTITIES_PATH);
        capitalisation_stop_words = Files.readAllLines(Paths.get(getPropertyWithDefault(KEY_CAPITALISATION_STOP_WORDS, DEFAULT_CAPITALISATION_STOP_WORDS_PATH)));
        non_title_case_words = new HashSet<>();
    }

    private void configureImportCategoryMap(final String path_key) throws IOException {

        String s = properties.getProperty(path_key);

        Map<String, String> map = new HashMap<>();

        if (s != null) {

            Path path = Paths.get(s);
            final List<String> lines = Files.readAllLines(path);

            entry_column_map_string = lines.getFirst();

            for (final String line : lines.subList(1, lines.size())) {
                final String[] parts = line.split(",");
                map.put(parts[0], parts[1]);
            }
        }

        entry_map = map;
    }

    protected Map<String, String> loadNormalisationMap(String path_key, String default_path) throws IOException {

        return loadMap(getPropertyWithDefault(path_key, default_path));
    }

    private static Map<String, String> loadMap(String s) throws IOException {

        Map<String, String> map = new HashMap<>();

        if (s != null) {

            Path path = Paths.get(s);
            final List<String> lines = Files.readAllLines(path);

            for (final String line : lines) {
                final String[] parts = line.split(",");
                map.put(parts[0], parts[1]);
            }
        }

        return map;
    }
}
