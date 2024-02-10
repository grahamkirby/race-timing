package common;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Properties;

public abstract class Race {


    protected static final String DUMMY_DURATION_STRING = "23:59:59";
    public static final Duration DUMMY_DURATION = parseTime(DUMMY_DURATION_STRING);
    protected static final String ZERO_TIME_STRING = "0:0:0";
    public static final Duration ZERO_TIME = parseTime(ZERO_TIME_STRING);

    protected Properties properties;
    protected Path working_directory_path;

    // String read from configuration file specifying all the runners who did have a finish
    // time recorded but were declared DNF.
    protected String dnf_string;

    protected RawResult[] raw_results;

    public Race(final String config_file_path) throws IOException {

        this(readProperties(config_file_path));
    }

    public Race(final Properties properties) throws IOException {

        this.properties = properties;
        configure();
    }

    protected abstract void configure() throws IOException;

    public abstract void processResults() throws IOException;

    public Properties getProperties() {
        return properties;
    }

    protected static Properties readProperties(final String config_file_path) throws IOException {

        try (final FileInputStream in = new FileInputStream(config_file_path)) {

            final Properties properties = new Properties();
            properties.load(in);
            return properties;
        }
    }

    protected void readProperties() {
        working_directory_path = Paths.get(properties.getProperty("WORKING_DIRECTORY"));
        dnf_string = properties.getProperty("DNF_LEGS");
    }

    public Path getWorkingDirectoryPath() {
        return working_directory_path;
    }

    protected String getPropertyWithDefault(final String property_key, final String default_value) {

        final String value = properties.getProperty(property_key);
        return value == null || value.isBlank() ? default_value : value;
    }




    protected static Duration parseTime(String element) {

        element = element.strip();

        try {
            final String[] parts = element.split(":");
            final String time_as_ISO = "PT" + hours(parts) + minutes(parts) + seconds(parts);

            return Duration.parse(time_as_ISO);
        }
        catch (Exception e) {
            throw new RuntimeException("illegal time: " + element);
        }
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
