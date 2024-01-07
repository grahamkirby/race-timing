package lap_race;

import java.time.Duration;

public class RawResult {

    final int bib_number;
    final Duration recorded_finish_time;  // Relative to start of leg 1.

    public RawResult(final String file_line) {

        final String[] elements = file_line.split("\t");

        bib_number = Integer.parseInt(elements[0]);
        recorded_finish_time = Results.parseTime(elements[1]);
    }
}
