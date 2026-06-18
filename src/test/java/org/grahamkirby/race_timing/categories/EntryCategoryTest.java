/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2026 Graham Kirby (race-timing@kirby-family.net)
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
package org.grahamkirby.race_timing.categories;

import com.code_intelligence.jazzer.junit.FuzzTest;
import com.code_intelligence.jazzer.mutation.annotation.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntryCategoryTest {

    @FuzzTest
    void fuzzNewEntryCategory(@NotNull final String components) {

        try {
            new EntryCategory(components);
        }
        catch (final RuntimeException e) {
            if (!messageIsExpected(e.getMessage()))
                throw e;
        }
    }

    private final List<String> expected_exception_message_roots = List.of(
        "too few category elements",
        "invalid age range for category",
        "illegal age range"
    );

    private boolean messageIsExpected(final String message) {
        for (final String s : expected_exception_message_roots)
            if (message.startsWith(s)) return true;
        return false;
    }
}
