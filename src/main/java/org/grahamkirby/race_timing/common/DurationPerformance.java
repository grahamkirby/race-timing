/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (race-timing@kirby-family.net)
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
package org.grahamkirby.race_timing.common;

import java.time.Duration;

import static org.grahamkirby.race_timing.common.Config.DNF_STRING;
import static org.grahamkirby.race_timing.common.Normalisation.renderDuration;

public class DurationPerformance extends Performance{

    private final Duration performance;

    public DurationPerformance(final Duration performance) {
        this.performance = performance;
    }

    public Object getValue() {
        return performance;
    }

    @Override
    public String toString() {

        return renderDuration(performance, DNF_STRING);
    }

    @Override
    public int compareTo(final Performance d) {

        return d instanceof final DurationPerformance duration ? performance.compareTo(duration.performance) : 0;
    }

    @Override
    public boolean equals(final Object o) {

        return o instanceof final DurationPerformance other && performance.equals(other.performance);
    }
}
