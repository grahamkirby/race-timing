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

import java.io.IOException;
import java.nio.file.Path;

public interface Race {

    void completeConfiguration();

    void setPrizes(RacePrizes prizes);

    RaceResults getRaceResults();

    void setInput(RaceInput input);

    void setResultsCalculator(ResultsCalculator results_calculator);

    void setResultsOutput(ResultsOutput results_output);

    void processResults() throws IOException;

//    Path getFullPath(String path);
    Path getFullPath(Path path);

    Path getConfigPath();

    Path getOutputDirectoryPath();

    void setConfigProcessor(ConfigProcessor config_processor);

    void setCategoriesProcessor(CategoriesProcessor categories_processor);

    void setRaceDataProcessor(RaceDataProcessor race_data_processor);

    Config getConfig();

    Normalisation getNormalisation();

    CategoryDetails getCategoryDetails();

    RaceData getRaceData();

    void appendToNotes(String s);

    StringBuilder getNotes();
}
