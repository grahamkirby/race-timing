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

import static org.grahamkirby.race_timing_experimental.common.Config.UNKNOWN_BIB_NUMBER;
import static org.grahamkirby.race_timing_experimental.common.Normalisation.parseTime;

public class RawResult {

    private int bib_number;
    private String comment = "";
    public Duration recorded_finish_time;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public RawResult(final String file_line) {

        final String[] elements = file_line.split("\t");

        final String bib_number_as_string = elements[0];
        final String time_as_string = elements[1];

        bib_number = bib_number_as_string.equals("?") ? UNKNOWN_BIB_NUMBER : Integer.parseInt(bib_number_as_string);
        recorded_finish_time = time_as_string.equals("?") ? null : parseTime(time_as_string);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public int getBibNumber() {
        return bib_number;
    }

    public void setBibNumber(final int bib_number) {
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
}
