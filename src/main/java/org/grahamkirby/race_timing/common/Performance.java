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

public class Performance implements Comparable<Performance> {

    private final Object value;

    public Performance(final Integer value) {
        this.value = value;
    }

    public Performance(final Duration value) {
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(final Performance other) {

        return ((Comparable<Object>) value).compareTo(other.value);
    }

    @Override
    public String toString() {

        return value instanceof Integer ?
            value.toString() :
            renderDuration(this, DNF_STRING);
    }

    public Object getValue() {
        return value;
    }
}
