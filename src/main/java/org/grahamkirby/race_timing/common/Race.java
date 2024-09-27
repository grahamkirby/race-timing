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
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

public abstract class Race {

    // TODO document where dead heats can occur - not where result is directly recorded,
    // only where calculated from other results. E.g. DB overall vs lap time

    public record CategoryGroup(String combined_categories_title, List<String> category_names){}

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected static final String DUMMY_DURATION_STRING = "23:59:59";
    public static final Duration DUMMY_DURATION = parseTime(DUMMY_DURATION_STRING);

    public static final String SENIOR_RACE_KEY = "SENIOR_RACE";
    public static final String OPEN_PRIZE_CATEGORIES_KEY = "OPEN_PRIZE_CATEGORIES";
    public static final String SENIOR_PRIZE_CATEGORIES_KEY = "SENIOR_PRIZE_CATEGORIES";
    public static final String NUMBER_OF_OPEN_PRIZES_KEY = "NUMBER_OF_OPEN_PRIZES";
    public static final String NUMBER_OF_SENIOR_PRIZES_KEY = "NUMBER_OF_SENIOR_PRIZES";
    public static final String NUMBER_OF_CATEGORY_PRIZES_KEY = "NUMBER_OF_CATEGORY_PRIZES";
    public static final String MINIMUM_NUMBER_OF_RACES_KEY = "MINIMUM_NUMBER_OF_RACES";

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
    public abstract void configure() throws IOException;

    //////////////////////////////////////////////////////////////////////////////////////////////////

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

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static Duration parseTime(String element) {

        element = element.strip();
        if (element.startsWith(":")) element = "0" + element;
        if (element.endsWith(":")) element = element + "0";

        try {
            final String[] parts = element.split(":");
            final String time_as_ISO = "PT" + hours(parts) + minutes(parts) + seconds(parts);

            return Duration.parse(time_as_ISO);
        }
        catch (Exception e) {
            throw new RuntimeException("illegal time: " + element);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static Properties loadProperties(final Path config_file_path) throws IOException {

        try (final FileInputStream stream = new FileInputStream(config_file_path.toString())) {

            final Properties properties = new Properties();
            properties.load(stream);
            return properties;
        }
    }

    private static String hours(final String[] parts) {
        return parts.length > 2 ? parts[0] + "H" : "";
    }
    private static String minutes(final String[] parts) {
        return (parts.length > 2 ? parts[1] : parts[0]) + "M";
    }
    private static String seconds(final String[] parts) {
        return (parts.length > 2 ? parts[2] : parts[1]) + "S";
    }
}
