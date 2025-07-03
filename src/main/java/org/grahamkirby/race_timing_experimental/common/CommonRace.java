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
import org.grahamkirby.race_timing_experimental.individual_race.IndividualRaceOutputText;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class CommonRace implements Race {

    public Path config_file_path;
    public RacePrizes prizes;

    public Normalisation normalisation;
    protected RaceInput input;

    /**
     * List of prize categories.
     * Value is read from configuration file using key KEY_CATEGORIES_PRIZE_PATH.
     */
    public List<PrizeCategoryGroup> prize_category_groups;
    private CategoriesProcessor categories_processor;
    private CategoryDetails category_details;
    private RaceData race_data;
    private Config config;
    private ConfigProcessor config_processor;
    private RaceDataProcessor race_data_processor;
    private RaceResults race_results;

    public CommonRace(final Path config_file_path) throws IOException {

        this.config_file_path = config_file_path;
    }

    @Override
    public void completeConfiguration() {

        config = config_processor.loadConfig(config_file_path);
        normalisation = new Normalisation(this);
    }

    @Override
    public void setPrizes(final RacePrizes prizes) {
        this.prizes = prizes;
    }

    public Config getConfig() {
        return config;
    }

    @Override
    public Normalisation getNormalisation() {
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
    public StringBuilder getNotes() {
        return results_calculator.getNotes();
    }

    @Override
    public RaceResults getRaceResults() {
        return race_results;
    }

    @Override
    public void setInput(final RaceInput input) {
        this.input = input;
    }

    ResultsCalculator results_calculator;
    ResultsOutput results_output;

    @Override
    public void processResults() throws IOException {

        category_details = categories_processor.getCategoryDetails();
        race_data = race_data_processor.getRaceData();
        race_results = results_calculator.calculateResults();

        results_output.outputResults();
    }

    /** Resolves the given path relative to either the project root, if it's specified as an absolute
     *  path, or to the race configuration file. */
    @Override
    public Path getFullPath(final String path) {

        if (path.isEmpty()) return config_file_path;

        if (path.startsWith("/")) return makeRelativeToProjectRoot(path);

        return getPathRelativeToRaceConfigFile(path);
    }

    @Override
    public Path getFullPath(Path path) {

        return getFullPath(path.toString());
    }

    @Override
    public void setConfigProcessor(ConfigProcessor config_processor) {

        this.config_processor = config_processor;
        config_processor.setRace(this);
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
    public void setResultsCalculator(final ResultsCalculator results_calculator) {
        this.results_calculator = results_calculator;
        results_calculator.setRace(this);
    }

    @Override
    public void setResultsOutput(final ResultsOutput results_output) {
        this.results_output = results_output;
        results_output.setRace(this);
    }

    private static Path makeRelativeToProjectRoot(final String path) {

        // Path is specified as absolute path, should be reinterpreted relative to project root.
        return Path.of(path.substring(1));
    }

    private Path getPathRelativeToRaceConfigFile(final String path) {

        return config_file_path.resolveSibling(path);
    }
}
