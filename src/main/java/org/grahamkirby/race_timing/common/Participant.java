package org.grahamkirby.race_timing.common;

import org.grahamkirby.race_timing.common.categories.EntryCategory;

public abstract class Participant {

    public String name;
    public EntryCategory category;

    protected Participant(final String name, final EntryCategory category) {
        this.name = name;
        this.category = category;
    }
}
