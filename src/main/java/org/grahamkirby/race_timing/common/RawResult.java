/*
 * Copyright 2024 Graham Kirby:
 * <https://github.com/grahamkirby/race-timing>
 *
 * This file is part of the module race-timing.
 *
 * race-timing is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * race-timing is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with race-timing. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.common;

import java.time.Duration;

public class RawResult {

    Integer bib_number;
    public Duration recorded_finish_time;
    String comment = "";

    // Only used for relay race. Leg number is optional, depending on whether it was recorded on paper sheet.
    private final Integer leg_number;

    public RawResult(final String file_line) {

        final String[] elements = file_line.split("\t");

        final String bib_number_as_string = elements[0];
        final String time_as_string = elements[1];

        bib_number = bib_number_as_string.equals("?") ? null : Integer.parseInt(bib_number_as_string);
        recorded_finish_time = time_as_string.equals("?") ? null : Race.parseTime(time_as_string);
        leg_number = elements.length == 2 ? 0 : Integer.parseInt(elements[2]);
    }

    public Integer getBibNumber() {
        return bib_number;
    }

    public void setBibNumber(final Integer bib_number) {
        this.bib_number = bib_number;
    }

    public Duration getRecordedFinishTime() {
        return recorded_finish_time;
    }

    public void setRecordedFinishTime(final Duration finish_time) {
        recorded_finish_time = finish_time;
    }

    public String getComment() {
        return comment;
    }

    public void appendComment(final String comment) {
        if (!this.comment.isEmpty()) this.comment += " ";
        this.comment += comment;
    }

    public Integer getLegNumber() {
        return leg_number;
    }
}
