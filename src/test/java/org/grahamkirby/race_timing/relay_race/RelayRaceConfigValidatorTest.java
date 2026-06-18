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
package org.grahamkirby.race_timing.relay_race;

import com.code_intelligence.jazzer.junit.FuzzTest;
import com.code_intelligence.jazzer.mutation.annotation.NotNull;
import org.grahamkirby.race_timing.categories.PrizeCategory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RelayRaceConfigValidatorTest {

    @FuzzTest
    void fuzzValidateDNFRecords(@NotNull final String dnf_string) {

        try {
            RelayRaceConfigValidator.validateDNFRecords(dnf_string, "config.txt");
        }
        catch (final RuntimeException e) {
            if (!messageIsExpected(e.getMessage()))
                throw e;
        }
    }

    @FuzzTest
    void fuzzValidateMassStartTimes(@NotNull final String mass_start_elapsed_times) {

        try {
            RelayRaceConfigValidator.validateMassStartTimes(mass_start_elapsed_times, 4, "config.txt");
        }
        catch (final RuntimeException e) {
            if (!messageIsExpected(e.getMessage()))
                throw e;
        }
    }

    private final List<String> expected_exception_message_roots = List.of(
        "invalid entry '",
        "invalid mass start time",
        "invalid leg number"
    );

    private boolean messageIsExpected(final String message) {
        for (final String s : expected_exception_message_roots)
            if (message.startsWith(s)) return true;
        return false;
    }
}
