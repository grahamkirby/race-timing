package org.grahamkirby.race_timing.common;

import org.grahamkirby.race_timing.common.categories.EntryCategory;

import java.util.List;

public record Team(String name, EntryCategory category, List<String> runner_names) {
}
