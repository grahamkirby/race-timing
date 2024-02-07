package common;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public abstract class Race {

    protected Properties properties;

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
}
