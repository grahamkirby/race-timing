package relay_race;

import common.Race;
import common.RaceOutput;
import common.RaceResult;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public abstract class RelayRaceOutput extends RaceOutput {

    String detailed_results_filename, collated_times_filename;

    public RelayRaceOutput(final Race race) {
        super(race);
    }

    protected void constructFilePaths() {

        super.constructFilePaths();

        detailed_results_filename = race_name_for_filenames + "_detailed_" + year;
        collated_times_filename = "times_collated";
    }

    public void printLegResults() throws IOException {

        for (int leg = 1; leg <= ((RelayRace)race).number_of_legs; leg++)
            printLegResults(leg);
    }

    public void printLegResults(int leg) throws IOException {
        throw new UnsupportedOperationException();
    }

    Duration sumDurationsUpToLeg(final List<LegResult> leg_results, final int leg) {

        Duration total = Duration.ZERO;
        for (int i = 0; i < leg; i++)
            total = total.plus(leg_results.get(i).duration());
        return total;
    }

    List<LegResult> getLegResults(final int leg_number) {

        final List<LegResult> leg_results = new ArrayList<>();

        for (final RaceResult overall_result : race.getOverallResults())
            leg_results.add(((RelayRaceResult)overall_result).leg_results.get(leg_number - 1));

        // Sort in order of increasing overall leg time, as defined in LegResult.compareTo().
        // Ordering for DNF results doesn't matter since they're omitted in output.
        // Where two teams have the same overall time, the order in which their last leg runners were recorded is preserved.
        // OutputCSV.printLegResults deals with dead heats.
        leg_results.sort(LegResult::compareTo);

        return leg_results;
    }

    void addMassStartAnnotation(final OutputStreamWriter writer, final LegResult leg_result, final int leg) throws IOException {

        // Adds e.g. "(M3)" after names of runners that started in leg 3 mass start.
        if (leg_result.in_mass_start) {

            // Find the next mass start.
            int mass_start_leg = leg;
            while (!((RelayRace)race).mass_start_legs.get(mass_start_leg-1))
                mass_start_leg++;

            writer.append(" (M").append(String.valueOf(mass_start_leg)).append(")");
        }
    }
}
