package common;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class Race {

    protected static final String DUMMY_DURATION_STRING = "23:59:59";
    protected static final String ZERO_TIME_STRING = "0:0:0";
    public static final Duration DUMMY_DURATION = parseTime(DUMMY_DURATION_STRING);
    public static final Duration ZERO_TIME = parseTime(ZERO_TIME_STRING);

    protected final Properties properties;
    private final Path working_directory_path;

    static Map<String, String> NORMALISED_CLUB_NAMES = new HashMap<>();

    static {
        NORMALISED_CLUB_NAMES.put("", "Unatt.");
        NORMALISED_CLUB_NAMES.put("Unattached", "Unatt.");
        NORMALISED_CLUB_NAMES.put("U/A", "Unatt.");
        NORMALISED_CLUB_NAMES.put("None", "Unatt.");
        NORMALISED_CLUB_NAMES.put("Fife Athletic Club", "Fife AC");
        NORMALISED_CLUB_NAMES.put("Dundee HH", "Dundee Hawkhill Harriers");
        NORMALISED_CLUB_NAMES.put("Leven Las Vegas", "Leven Las Vegas RC");
        NORMALISED_CLUB_NAMES.put("Leven Las Vegas Running Club", "Leven Las Vegas RC");
        NORMALISED_CLUB_NAMES.put("Haddies", "Anster Haddies");
        NORMALISED_CLUB_NAMES.put("Dundee Hawkhill", "Dundee Hawkhill Harriers");
        NORMALISED_CLUB_NAMES.put("DRR", "Dundee Road Runners");
        NORMALISED_CLUB_NAMES.put("Perth RR", "Perth Road Runners");
        NORMALISED_CLUB_NAMES.put("Kinross RR", "Kinross Road Runners");
        NORMALISED_CLUB_NAMES.put("Falkland TR", "Falkland Trail Runners");
        NORMALISED_CLUB_NAMES.put("PH Racing Club", "PH Racing");
        NORMALISED_CLUB_NAMES.put("DHH", "Dundee Hawkhill Harriers");
        NORMALISED_CLUB_NAMES.put("Carnegie H", "Carnegie Harriers");
        NORMALISED_CLUB_NAMES.put("Dundee RR", "Dundee Road Runners");
        NORMALISED_CLUB_NAMES.put("Recreational Running", "Recreational Runners");
    }

    // String read from configuration file specifying all the runners who did have a finish
    // time recorded but were declared DNF.
    protected String dnf_string;

    protected RawResult[] raw_results;

    public Categories categories;

    public Race(final Path config_file_path) throws IOException {

        working_directory_path = config_file_path.getParent().getParent();
        properties = readProperties(config_file_path);
    }

    protected abstract void configure() throws IOException;

    public abstract void processResults() throws IOException;

    public Properties getProperties() {
        return properties;
    }

    private static Properties readProperties(final Path config_file_path) throws IOException {

        try (final FileInputStream in = new FileInputStream(config_file_path.toString())) {

            final Properties properties = new Properties();
            properties.load(in);
            return properties;
        }
    }

    public Category lookupCategory(final String short_name) {

        for (Category category : categories.getCategoriesInDecreasingGeneralityOrder()) {
            if (category.getShortName().equals(short_name)) return category;
        }

        throw new RuntimeException("Category not found: " + short_name);
    }

    protected void readProperties() {

        dnf_string = properties.getProperty("DNF_LEGS");
    }

    public Path getWorkingDirectoryPath() {
        return working_directory_path;
    }

    public RawResult[] getRawResults() {
        return raw_results;
    }

    protected String getPropertyWithDefault(final String property_key, final String default_value) {

        final String value = properties.getProperty(property_key);
        return value == null || value.isBlank() ? default_value : value;
    }

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

    public static String format(final Duration duration) {

        final long s = duration.getSeconds();
        return String.format("0%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
    }

    public static String normaliseClubName(final String club) {

        return NORMALISED_CLUB_NAMES.getOrDefault(club, club);
    }

    static String hours(final String[] parts) {
        return parts.length > 2 ? parts[0] + "H" : "";
    }

    static String minutes(final String[] parts) {
        return (parts.length > 2 ? parts[1] : parts[0]) + "M";
    }

    static String seconds(final String[] parts) {
        return (parts.length > 2 ? parts[2] : parts[1]) + "S";
    }
}
