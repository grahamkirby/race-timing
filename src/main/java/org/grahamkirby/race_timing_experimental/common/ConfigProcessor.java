package org.grahamkirby.race_timing_experimental.common;

import java.nio.file.Path;

public interface ConfigProcessor {
    Config loadConfig(Path configFilePath);
}
