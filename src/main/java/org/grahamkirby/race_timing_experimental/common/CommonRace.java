package org.grahamkirby.race_timing_experimental.common;

import org.grahamkirby.race_timing.common.Normalisation;
import org.grahamkirby.race_timing.common.RaceInput;
import org.grahamkirby.race_timing.common.RacePrizes;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;
import org.grahamkirby.race_timing.common.output.RaceOutputPDF;
import org.grahamkirby.race_timing.common.output.RaceOutputText;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.grahamkirby.race_timing.common.Race.*;

public class CommonRace implements Race {

    public Path config_file_path;

    private RaceImpl race_impl;
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

    public CommonRace(final Path config_file_path) throws IOException {

        this.config_file_path = config_file_path;
    }

    @Override
    public void setPrizes(final RacePrizes prizes) {
        this.prizes = prizes;
    }

    @Override
    public void setInput(final RaceInput input) {
        this.input = input;
    }

    @Override
    public void setRaceImpl(final RaceImpl race_impl) {
        this.race_impl = race_impl;
    }

    @Override
    public void processResults() throws IOException {

        properties = loadProperties(config_file_path);

        notes = new StringBuilder();
        race_impl.processProperties();

        configureCategories();
        race_impl.configureInputData();

        overall_results = race_impl.calculateResults();

        race_impl.outputResults(overall_results);
    }

    private void configureCategories() throws IOException {

        entry_categories = Files.readAllLines(getPath(getRequiredProperty(KEY_CATEGORIES_ENTRY_PATH))).stream().filter(line -> !line.startsWith(COMMENT_SYMBOL)).map(EntryCategory::new).toList();
        prize_category_groups = new ArrayList<>();
        loadPrizeCategoryGroups(getPath(getRequiredProperty(KEY_CATEGORIES_PRIZE_PATH)));
    }

    /** Resolves the given path relative to either the project root, if it's specified as an absolute
     *  path, or to the race configuration file. */
    public Path getPath(final String path) {

        return path.startsWith("/") ?
            getPathRelativeToProjectRoot(path) :
            getPathRelativeToRaceConfigFile(path);
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

    public String getOptionalProperty(final String key) {

        return properties.getProperty(key);
    }

    public String getProperty(final String key, final String default_value) {

        return properties.getProperty(key, default_value);
    }

    /** Loads prize category groups from the given file. */
    private void loadPrizeCategoryGroups(final Path prize_categories_path) throws IOException {

        Files.readAllLines(prize_categories_path).stream().
            filter(line -> !line.startsWith(COMMENT_SYMBOL)).
            forEachOrdered(this::recordGroup);
    }

    private void recordGroup(final String line) {

        final String group_name = line.split(",")[PRIZE_CATEGORY_GROUP_NAME_INDEX];
        final PrizeCategoryGroup group = getGroupByName(group_name);

        group.categories().add(new PrizeCategory(line));
    }

    private PrizeCategoryGroup getGroupByName(final String group_name) {

        return prize_category_groups.stream().
            filter(g -> g.group_title().equals(group_name)).
            findFirst().
            orElseGet(() -> newGroup(group_name));
    }

    private PrizeCategoryGroup newGroup(final String group_name) {

        final PrizeCategoryGroup group = new PrizeCategoryGroup(group_name, new ArrayList<>());
        prize_category_groups.add(group);
        return group;
    }
}
