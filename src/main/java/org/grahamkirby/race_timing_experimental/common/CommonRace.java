/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (graham.kirby@st-andrews.ac.uk)
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
package org.grahamkirby.race_timing_experimental.common;

import org.grahamkirby.race_timing.common.RaceInput;
import org.grahamkirby.race_timing.common.RacePrizes;
import org.grahamkirby.race_timing.common.categories.PrizeCategoryGroup;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class CommonRace implements Race {

    public Path config_file_path;
    public RacePrizes prizes;

    public Normalisation normalisation;
    protected RaceInput input;

    private CategoriesProcessor categories_processor;
    private CategoryDetails category_details;
    private RaceData race_data;
    private Config config;
    private ConfigProcessor config_processor;
    private RaceDataProcessor race_data_processor;
    private SpecificRace specific;

    public CommonRace(final Path config_file_path) throws IOException {

        this.config_file_path = config_file_path;
    }

    private void completeConfiguration() {

//        config = config_processor.loadConfig(config_file_path);

        if (specific != null) {
            specific.completeConfiguration();
        }
    }

    public Config getConfig() {
        return config;
    }

    @Override
    public synchronized Normalisation getNormalisation() {
        if (normalisation == null) {
            normalisation = new Normalisation(this);
        }
        return normalisation;
    }

    @Override
    public CategoryDetails getCategoryDetails() {
        return category_details;
    }

    @Override
    public RaceData getRaceData() {
        return race_data;
    }

    @Override
    public void appendToNotes(String s) {
        results_calculator.getNotes().append(s);
    }

    @Override
    public String getNotes() {
        return results_calculator.getNotes().toString();
    }

    RaceResultsCalculator results_calculator;
    ResultsOutput results_output;

    @Override
    public void processResults() throws IOException {

        category_details = categories_processor.getCategoryDetails();
        race_data = race_data_processor.getRaceData();
        completeConfiguration();
        results_calculator.calculateResults();

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
    @Override
    public Path interpretPath(Path path) {

        // Absolute paths originate from config file where path starting with "/" denotes
        // a path relative to the project root.
        // Can't test with isAbsolute() since that will return false on Windows.
        if (path.startsWith("/")) return makeRelativeToProjectRoot(path);

        return getPathRelativeToRaceConfigFile(path);
    }

    @Override
    public void setSpecific(SpecificRace specific) {
        this.specific = specific;
        specific.setRace(this);
    }

    @Override
    public Path getOutputDirectoryPath() {

        // This assumes that the config file is in the "input" directory
        // which is at the same level as the "output" directory.
        return config_file_path.getParent().resolveSibling("output");
    }

    @Override
    public void setConfigProcessor(ConfigProcessor config_processor) {

        this.config_processor = config_processor;
        config_processor.setRace(this);
        config = config_processor.loadConfig(config_file_path);
    }

    @Override
    public void setCategoriesProcessor(CategoriesProcessor categories_processor) {

        this.categories_processor = categories_processor;
        categories_processor.setRace(this);
    }

    @Override
    public void setRaceDataProcessor(RaceDataProcessor race_data_processor) {

        this.race_data_processor = race_data_processor;
        race_data_processor.setRace(this);
    }

    @Override
    public SpecificRace getSpecific() {
        return specific;
    }

    @Override
    public void setResultsCalculator(final RaceResultsCalculator results_calculator) {
        this.results_calculator = results_calculator;
        results_calculator.setRace(this);
    }

    @Override
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
