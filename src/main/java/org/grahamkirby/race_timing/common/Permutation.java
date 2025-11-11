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

import java.util.List;
import java.util.stream.IntStream;

public class Permutation<T> {

    private final List<Integer> ordering;

    public Permutation(final List<Integer> ordering) {
        this.ordering = ordering;
    }

    public Permutation(final int size) {
        this.ordering = IntStream.rangeClosed(1, size).boxed().toList();
    }

    public List<T> permute(final List<T> list) {

        return IntStream.rangeClosed(0, list.size() - 1).boxed().toList().stream().
            map(i -> ordering.get(i) - 1).
            map(list::get).
            toList();
    }

    public List<Integer> getOrdering() {
        return ordering;
    }
}
