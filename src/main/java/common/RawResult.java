package common;

import lap_race.LapRace;

import java.time.Duration;

public class RawResult {

    // Leg number is optional, depending on whether it was recorded on paper sheet.
    final int bib_number, leg_number;
    Duration recorded_finish_time;  // Relative to start of leg 1.
    boolean interpolated_time = false;

    public RawResult(final String file_line) {

        final String[] elements = file_line.split("\t");

        final String bib_number_as_string = elements[0];
        final String time_as_string = elements[1];

        bib_number = Integer.parseInt(bib_number_as_string);
        recorded_finish_time = time_as_string.equals("?") ? null : Race.parseTime(time_as_string);
        leg_number = elements.length > 2 ? Integer.parseInt(elements[2]) : 0;
    }

    public int getBibNumber() {
        return bib_number;
    }

    public Duration getRecordedFinishTime() {
        return recorded_finish_time;
    }

    public void setRecordedFinishTime(Duration finish_time) {
        recorded_finish_time = finish_time;
    }

    public boolean isInterpolatedTime() {
        return interpolated_time;
    }

    public void setInterpolatedTime(boolean interpolated_time) {
        this.interpolated_time = interpolated_time;
    }

    public int getLegNumber() {
        return leg_number;
    }
}
