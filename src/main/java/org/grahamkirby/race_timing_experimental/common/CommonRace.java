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

import org.grahamkirby.race_timing.common.Normalisation;
import org.grahamkirby.race_timing.common.RaceInput;
import org.grahamkirby.race_timing.common.RacePrizes;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;
import org.grahamkirby.race_timing.common.output.RaceOutputPDF;
import org.grahamkirby.race_timing.common.output.RaceOutputText;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public class CommonRace implements Race {

    public Path config_file_path;

    private Properties properties;

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

    public void setRaceDataProcessor() {
    }

    public void setCategoriesProcessor() {
    }

    @Override
    public void setInput(final RaceInput input) {
        this.input = input;
    }

    ResultsCalculator results_calculator;
    ResultsOutput results_output;

    @Override
    public void setResultsCalculator(final ResultsCalculator results_calculator) {
        this.results_calculator = results_calculator;
    }

    @Override
    public void setResultsOutput(final ResultsOutput results_output) {
        this.results_output = results_output;
    }

    @Override
    public void processResults() {

        config = config_processor.loadConfig(config_file_path);
        category_details = categories_processor.getCategoryDetails();
        race_data = race_data_processor.getRaceData();
        race_results = results_calculator.calculateResults();
        results_output.outputResults();
    }

//    private void configureCategories() throws IOException {
//
//        entry_categories = Files.readAllLines(getPath(getRequiredProperty(KEY_CATEGORIES_ENTRY_PATH))).stream().filter(line -> !line.startsWith(COMMENT_SYMBOL)).map(EntryCategory::new).toList();
//        prize_category_groups = new ArrayList<>();
//        loadPrizeCategoryGroups(getPath(getRequiredProperty(KEY_CATEGORIES_PRIZE_PATH)));
//    }

    /** Resolves the given path relative to either the project root, if it's specified as an absolute
     *  path, or to the race configuration file. */
    @Override
    public Path getPath(final String path) {

        return path.startsWith("/") ?
            getPathRelativeToProjectRoot(path) :
            getPathRelativeToRaceConfigFile(path);
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

    private static Path getPathRelativeToProjectRoot(final String path) {

        return Paths.get(path.substring(1));
    }

    private Path getPathRelativeToRaceConfigFile(final String path) {

        return config_file_path.getParent().resolve(path);
    }

    public String getRequiredProperty(final String key) {

        final String property = properties.getProperty(key);

        if (property == null)
            throw new RuntimeException(STR."no entry for key '\{key}' in file '\{config_file_path.getFileName()}'");

        return property;
    }

    @Override
    public Properties getProperties() {

        return properties;
    }

//    /** Loads prize category groups from the given file. */
//    private void loadPrizeCategoryGroups(final Path prize_categories_path) throws IOException {
//
//        Files.readAllLines(prize_categories_path).stream().
//            filter(line -> !line.startsWith(COMMENT_SYMBOL)).
//            forEachOrdered(this::recordGroup);
//    }
//
//    private void recordGroup(final String line) {
//
//        final String group_name = line.split(",")[PRIZE_CATEGORY_GROUP_NAME_INDEX];
//        final PrizeCategoryGroup group = getGroupByName(group_name);
//
//        group.categories().add(new PrizeCategory(line));
//    }
//
//    private PrizeCategoryGroup getGroupByName(final String group_name) {
//
//        return prize_category_groups.stream().
//            filter(g -> g.group_title().equals(group_name)).
//            findFirst().
//            orElseGet(() -> newGroup(group_name));
//    }
//
//    private PrizeCategoryGroup newGroup(final String group_name) {
//
//        final PrizeCategoryGroup group = new PrizeCategoryGroup(group_name, new ArrayList<>());
//        prize_category_groups.add(group);
//        return group;
//    }
}
