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

    private final Path config_file_path;
    private Normalisation normalisation;
    private CategoriesProcessor categories_processor;
    private CategoryDetails category_details;
    private SpecificRace specific;
    private RaceResultsCalculator results_calculator;
    private ResultsOutput results_output;
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

    public void appendToNotes(String s) {
        results_calculator.getNotes().append(s);
    }

    public String getNotes() {
        return results_calculator.getNotes().toString();
    }

    public void processResults() {

        category_details = categories_processor.getCategoryDetails();
        completeConfiguration();
        results_calculator.calculateResults();
    }

    public void outputResults() throws IOException {
        results_output.outputResults();
    }

    public void setSpecific(final SpecificRace specific) {

        this.specific = specific;
        specific.setRace(this);
    }

    public void addConfigProcessor(final ConfigProcessor processor) {

        config_processors.add(processor);
    }

    public void loadConfig() throws IOException {

        config = new Config(config_file_path);

        for (final ConfigProcessor processor : config_processors)
            processor.processConfig(config);
    }

    @Override
    public List<RawResult> getRawResults() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<RaceEntry> getEntries() {
        throw new UnsupportedOperationException();
    }

    public void setCategoriesProcessor(final CategoriesProcessor categories_processor) {

        this.categories_processor = categories_processor;
        categories_processor.setRace(this);
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

    public RaceResultsCalculator getResultsCalculator() {
        return results_calculator;
    }
}
