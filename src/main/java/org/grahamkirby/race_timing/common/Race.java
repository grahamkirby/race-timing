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

import org.grahamkirby.race_timing.categories.CategoriesProcessor;
import org.grahamkirby.race_timing.categories.CategoryDetails;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Race implements Race2 {

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
    // TODO individual race with no results - generate output with dummy times to check runner/bib duplicates.
    // TODO individual race with no results -  generate racer list for PocketTimer.
    // TODO series race with separate entries - output sorted runner names to notes.
    // TODO individual race with team prizes - output sorted club names to notes
    // TODO suppress prize output in individual tour races.
    // TODO control category order in prize list output.

    public Path config_file_path;

    public Normalisation normalisation;

    private CategoriesProcessor categories_processor;
    private CategoryDetails category_details;
    private RaceData race_data;
    private RaceDataProcessor race_data_processor;
    private SpecificRace specific;
    RaceResultsCalculator results_calculator;
    ResultsOutput results_output;
    private final List<ConfigProcessor> config_processors = new ArrayList<>();
    private Config config;

    public Race(final Path config_file_path) {

        try {
            this.config_file_path = config_file_path;
            loadConfig();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void completeConfiguration() {

        specific.completeConfiguration();
    }

    public Config getConfig() {
        return config;
    }

    public synchronized Normalisation getNormalisation() {

        if (normalisation == null)
            normalisation = new Normalisation(this);

        return normalisation;
    }

    public CategoryDetails getCategoryDetails() {
        return category_details;
    }

    public RaceData getRaceData() {
        return race_data;
    }

    public void appendToNotes(String s) {
        results_calculator.getNotes().append(s);
    }

    public String getNotes() {
        return results_calculator.getNotes().toString();
    }

    public void processResults() {

        category_details = categories_processor.getCategoryDetails();
        if (race_data_processor != null) race_data = race_data_processor.getRaceData();
        completeConfiguration();
        results_calculator.calculateResults();
    }

    public void outputResults() throws IOException {
        results_output.outputResults();
    }

    /**
     * Resolves the given path relative to either the race configuration file,
     * if it's specified as a relative path, or to the project root. Examples:
     *
     * Relative to race configuration:
     * entries.txt -> /Users/gnck/Desktop/myrace/input/entries.txt
     *
     * Relative to project root:
     * /src/main/resources/configuration/categories_entry_individual_senior.csv ->
     *    src/main/resources/configuration/categories_entry_individual_senior.csv
     */
    @SuppressWarnings("JavadocBlankLines")
    public Path interpretPath(final Path path) {

        // Absolute paths originate from config file where path starting with "/" denotes
        // a path relative to the project root.
        // Can't test with isAbsolute() since that will return false on Windows.
        if (path.startsWith("/")) return makeRelativeToProjectRoot(path);

        return getPathRelativeToRaceConfigFile(path);
    }

    public void setSpecific(final SpecificRace specific) {

        this.specific = specific;
        specific.setRace(this);
    }

    public Path getOutputDirectoryPath() {

        // This assumes that the config file is in the "input" directory
        // which is at the same level as the "output" directory.
        return config_file_path.getParent().resolveSibling("output");
    }

    public void addConfigProcessor(final ConfigProcessor processor) {

        config_processors.add(processor);
    }

    public void loadConfig() throws IOException {

        config = new Config(config_file_path);

        for (final ConfigProcessor processor : config_processors) {

            processor.processConfig(this);
        }
    }

    public String getStringConfig(final String key) {

        return (String) getConfig().get(key);
    }

    public Path getPathConfig(final String key) {

        return (Path) getConfig().get(key);
    }

    public void setCategoriesProcessor(final CategoriesProcessor categories_processor) {

        this.categories_processor = categories_processor;
        categories_processor.setRace(this);
    }

    public void setRaceDataProcessor(final RaceDataProcessor race_data_processor) {

        this.race_data_processor = race_data_processor;
        race_data_processor.setRace(this);
    }

    public SpecificRace getSpecific() {
        return specific;
    }

    public void setResultsCalculator(final RaceResultsCalculator results_calculator) {
        this.results_calculator = results_calculator;
        results_calculator.setRace(this);
    }

    public void setResultsOutput(final ResultsOutput results_output) {
        this.results_output = results_output;
        results_output.setRace(this);
    }

    private static Path makeRelativeToProjectRoot(final Path path) {

        // Path is specified as absolute path, should be reinterpreted relative to project root.
        return path.subpath(0, path.getNameCount());
    }

    private Path getPathRelativeToRaceConfigFile(final Path path) {

        return config_file_path.resolveSibling(path);
    }

    public RaceResultsCalculator getResultsCalculator() {
        return results_calculator;
    }
}
