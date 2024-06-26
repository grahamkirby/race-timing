package common;

import common.categories.Categories;
import common.categories.Category;
import common.output.RaceOutput;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

public abstract class Race {

    public record CategoryGroup(String combined_categories_title, List<String> category_names){};

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected static final String DUMMY_DURATION_STRING = "23:59:59";
    public static final Duration DUMMY_DURATION = parseTime(DUMMY_DURATION_STRING);

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private final Path working_directory_path;
    private final Properties properties;

    public final Map<Category, List<RaceResult>> prize_winners;
    protected final List<RaceResult> overall_results;

    public Categories categories;
    protected RacePrizes prizes;

    public RaceInput input;
    public RaceOutput output_CSV, output_HTML, output_text, output_PDF;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public Race(final Path config_file_path) throws IOException {

        working_directory_path = config_file_path.getParent().getParent();
        properties = loadProperties(config_file_path);

        prize_winners = new HashMap<>();
        overall_results = new ArrayList<>();

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

    public Category lookupCategory(final String short_name) {

        for (final Category category : categories.getCategoriesInDecreasingGeneralityOrder())
            if (category.getShortName().equals(short_name)) return category;

        throw new RuntimeException("Category not found: " + short_name);
    }

    public String getPropertyWithDefault(final String property_key, final String default_value) {

        final String value = properties.getProperty(property_key);
        return value == null || value.isBlank() ? default_value : value;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static Duration parseTime(final String element) {

        try {
            final String[] parts = element.strip().split(":");
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
