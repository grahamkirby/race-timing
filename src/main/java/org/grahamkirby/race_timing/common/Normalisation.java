package org.grahamkirby.race_timing.common;

import org.grahamkirby.race_timing.individual_race.IndividualRace;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

public class Normalisation {

    public static final int SECONDS_PER_HOUR = 3600;
    public static final int SECONDS_PER_MINUTE = 60;
    public static final double NANOSECONDS_PER_SECOND = 1000000000.0;

    public static String cleanName(String name) {

        // Remove double spaces, and surrounding whitespace.
        name = applyNormalisation(name, Map.of("  ", " "), false);
        return name.strip();
    }

    public static String getFirstName(final String name) {
        return name.split(" ")[0];
    }

    public static String getLastName(final String name) {

        return Arrays.stream(name.split(" ")).toList().getLast();
    }

    public static String normaliseClubName(final String club, final IndividualRace race) {

        return applyNormalisation(club, race.normalised_club_names, true);
    }

    // TODO Convert runner and club names to title case.
    public static String toTitleCase(String input) {
        StringBuilder titleCase = new StringBuilder();
        String[] words = input.split(" ");

        for (String word : words) {
            if (!word.isEmpty()) {
                titleCase.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return titleCase.toString().trim();
    }

    public static String htmlEncode(final String s, final Race race) {

        return applyNormalisation(s, race.normalised_html_entities, false);
    }

    private static String applyNormalisation(String s, final Map<String, String> normalisation_map, boolean only_replace_whole_string) {

        for (Map.Entry<String, String> normalisation : normalisation_map.entrySet()) {
            if (only_replace_whole_string) {
                if (s.equals(normalisation.getKey())) s = normalisation.getValue();
            }
            else {
                s = s.replaceAll(normalisation.getKey(), normalisation.getValue());
            }
        }
        return s;
    }

    public static Duration parseTime(String element) {

        element = element.strip();
        if (element.startsWith(":")) element = "0" + element;
        if (element.endsWith(":")) element = element + "0";

        try {
            final String[] parts = element.split(":");
            final String time_as_ISO = "PT" + hours(parts) + minutes(parts) + seconds(parts);

            return Duration.parse(time_as_ISO);
        }
        catch (Exception e) {
            throw new RuntimeException("illegal time: " + element);
        }
    }

    public static String format(final Duration duration) {

        final long s = duration.getSeconds();
        final int n = duration.getNano();

        String result = String.format("0%d:%02d:%02d", s / SECONDS_PER_HOUR, (s % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE, (s % SECONDS_PER_MINUTE));
        if (n > 0) {
            double fractional_seconds = n / NANOSECONDS_PER_SECOND;
            result += String.format("%1$,.3f", fractional_seconds).substring(1);
            while (result.endsWith("0")) result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private static String hours(final String[] parts) {
        return parts.length > 2 ? parts[0] + "H" : "";
    }
    private static String minutes(final String[] parts) {
        return (parts.length > 2 ? parts[1] : parts[0]) + "M";
    }
    private static String seconds(final String[] parts) {
        return (parts.length > 2 ? parts[2] : parts[1]) + "S";
    }
}
