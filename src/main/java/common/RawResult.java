package common;

import lap_race.LapRace;

import java.time.Duration;

public class RawResult {

    final int bib_number;
    final Duration recorded_finish_time;  // Relative to start of leg 1.

    public RawResult(final String file_line) {

        final String[] elements = file_line.split("\t");

        final String bib_number_as_string = elements[0];

        bib_number = Integer.parseInt(bib_number_as_string);
        recorded_finish_time = Race.parseTime(elements[1]);
    }

    public int getBibNumber() {
        return bib_number;
    }

    public Duration getRecordedFinishTime() {
        return recorded_finish_time;
    }
}