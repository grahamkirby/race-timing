package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.Normalisation;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.single_race.SingleRaceInput;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class UntimedIndividualRaceInput extends SingleRaceInput {

    private int next_fake_bib_number = 1;

    private final Function<String, RaceResult> race_result_mapper = line -> makeRaceResult(new ArrayList<>(Arrays.stream(line.split("\t")).toList()));

    public UntimedIndividualRaceInput(UntimedIndividualRace untimedIndividualRace) {
        super(untimedIndividualRace);
    }

    @Override
    protected TimedIndividualRaceEntry makeRaceEntry(List<String> elements) {
        return null;
    }

    @Override
    protected int getNumberOfEntryColumns() {
        return 0;
    }

    public List<RaceResult> loadOverallResults() throws IOException {

        // Only used when loading external results.
        if (overall_results_path == null) return new ArrayList<>();

        return Files.readAllLines(race.getPath(overall_results_path)).stream().
            filter(Predicate.not(String::isBlank)).
            map(race_result_mapper).
            filter(Objects::nonNull).
            toList();
    }

    private RaceResult makeRaceResult(final List<String> elements) {
        // Only used when loading external results.

        elements.addFirst(String.valueOf(next_fake_bib_number++));

        final UntimedIndividualRaceEntry entry = new UntimedIndividualRaceEntry(elements, race);
        final Duration finish_time = Normalisation.parseTime(elements.getLast());
        final UntimedIndividualRaceResult result = new UntimedIndividualRaceResult((UntimedIndividualRace) race, entry, finish_time);

//        result.completion_status = CompletionStatus.COMPLETED;

        return result;
    }
}
