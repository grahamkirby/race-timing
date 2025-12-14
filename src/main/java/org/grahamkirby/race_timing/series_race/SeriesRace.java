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
package org.grahamkirby.race_timing.series_race;

import org.grahamkirby.race_timing.categories.CategoriesProcessor;
import org.grahamkirby.race_timing.common.*;
import org.grahamkirby.race_timing.individual_race.IndividualRaceFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.grahamkirby.race_timing.common.Config.*;

public class SeriesRace implements RaceInternal {

    private static final String KEY_INDICATIVE_OF_SERIES_RACE_USING_INDIVIDUAL_TIMES = KEY_SCORE_FOR_MEDIAN_POSITION;
    private static final String KEY_INDICATIVE_OF_SERIES_RACE_USING_INDIVIDUAL_POSITIONS = KEY_SCORE_FOR_FIRST_PLACE;

    private List<SingleRaceInternal> races;
    private CategoriesProcessor categories_processor;
    private RaceResultsProcessor results_processor;
    private RaceOutput results_output;
    private NormalisationProcessor normalisation;
    private final Config config;
    private final NotesProcessor notes;

    private final List<Path> input_files_used_by_individual_races =  new ArrayList<>();

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public SeriesRace(final Config config) {

        this.config = config;
        notes = new NotesProcessor();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public RaceResults processResults() {

        try {
            loadRaces();

            results_processor.calculateResults();
            return results_processor;
        }
        catch (final Exception e) {
            notes.appendToNotes(e.getMessage());
            return null;
        }
    }

    @Override
    public void outputResults(final RaceResults results) throws IOException {

        try {
            config.checkUnusedInputFiles(input_files_used_by_individual_races);
            config.checkUnusedProperties();
        }
        catch (Exception e) {
            results_output.getNotes().appendToNotes(e.getMessage() + LINE_SEPARATOR);
        }

        results_output.outputResults(results);
    }

    @Override
    public void outputNotes() throws IOException {

        results_output.printNotes(notes);
    }

    @Override
    public void outputRacerList() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean configIsValid() {
        return results_processor != null;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initialise() throws IOException {

        categories_processor = new CategoriesProcessor(config);
        normalisation = new NormalisationProcessor(config);
        results_output = new SeriesRaceOutput(config);

        final SeriesRaceScorer scorer = getRaceScorer(config);
        results_processor = new SeriesRaceResultsProcessor(scorer, this);
    }

    @Override
    public void setOutput(final RaceOutput output) {
        this.results_output = output;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public CategoriesProcessor getCategoriesProcessor() {
        return categories_processor;
    }

    @Override
    public RaceResultsProcessor getResultsProcessor() {
        return results_processor;
    }

    @Override
    public NormalisationProcessor getNormalisationProcessor() {

        return normalisation;
    }

    @Override
    public NotesProcessor getNotesProcessor() {
        return notes;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public List<SingleRaceInternal> getRaces() {
        return races;
    }

    public int getNumberOfRacesTakenPlace() {

        return (int) races.stream().filter(Objects::nonNull).count();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private SeriesRaceScorer getRaceScorer(final Config config) {

        if (config.containsKey(KEY_INDICATIVE_OF_SERIES_RACE_USING_INDIVIDUAL_TIMES))
            return new IndividualTimesScorer(this);

        if (config.containsKey(KEY_INDICATIVE_OF_SERIES_RACE_USING_INDIVIDUAL_POSITIONS))
            return new IndividualPositionsScorer(this);

        return new AggregateTimesScorer(this);
    }

    private void loadRaces() throws IOException {

        final String races_string = config.getString(KEY_RACES);
        final List<String> race_config_paths = Arrays.asList(races_string.split(",", -1));

        final int number_of_races_in_series = (int) config.get(KEY_NUMBER_OF_RACES_IN_SERIES);
        if (race_config_paths.size() != number_of_races_in_series)
            throw new RuntimeException("invalid number of races specified in file '" + config.getConfigPath().getFileName() + "'");

        loadRaces(race_config_paths);
    }

    private void loadRaces(final List<String> race_config_paths) throws IOException {

        races = new ArrayList<>();
        final List<String> config_paths_seen = new ArrayList<>();

        for (int i = 0; i < race_config_paths.size(); i++) {

            final String race_config_path = race_config_paths.get(i);

            if (race_config_path.isBlank())
                // Race has not yet taken place.
                races.add(null);
            else {
                if (config_paths_seen.contains(race_config_path))
                    throw new RuntimeException("duplicate races specified in file '" + config.getConfigPath().getFileName() + "'");

                config_paths_seen.add(race_config_path);

                final SingleRaceInternal individual_race = getIndividualRace(race_config_path, i + 1);
                races.add(individual_race);

                individual_race.getConfig().checkUnusedInputFiles();
                input_files_used_by_individual_races.addAll(individual_race.getConfig().getUsedInputFiles());
            }
        }
    }

    private SingleRaceInternal getIndividualRace(final String individual_race_config_path, final int race_number) throws IOException {

        final Path config_path = config.interpretPath(Path.of(individual_race_config_path));

        if (!Files.exists(config_path))
            throw new RuntimeException("invalid config for race " + race_number + " in file '" + config.getConfigPath().getFileName() + "'");

        final SingleRaceInternal individual_race = (SingleRaceInternal) new IndividualRaceFactory().makeRace(config_path);
        individual_race.processResults();

        return individual_race;
    }
}
