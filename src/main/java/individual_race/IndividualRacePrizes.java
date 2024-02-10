package individual_race;

import common.Category;
import lap_race.LapRace;
import lap_race.LapRaceCategory;
import lap_race.Team;
import lap_race.TeamResult;

import java.util.ArrayList;
import java.util.List;

public class IndividualRacePrizes {

    final IndividualRace results;

    public IndividualRacePrizes(final IndividualRace results) {

        this.results = results;
    }

    public void allocatePrizes() {

        // Allocate first prize in each category first, in decreasing order of category breadth.
        // This is because e.g. a 40+ team should win first in 40+ category before a subsidiary
        // prize in open category.
        allocateFirstPrizes();

        // Now consider other prizes (only available in senior categories).
        allocateMinorPrizes();
    }

    private void allocateFirstPrizes() {

        for (final Category category : LapRaceCategory.values()) {

            results.prize_winners.put(category, new ArrayList<>());
            allocateFirstPrize(category);
        }
    }

    private void allocateFirstPrize(final Category category) {

        for (final Result result : results.overall_results) {
            if (prizeWinner(result, category)) {
                results.prize_winners.get(category).add(result.runner);
                return;
            }
        }
    }

    private void allocateMinorPrizes() {

        for (final Category category : LapRaceCategory.values())
            allocateMinorPrizes(category);
    }

    private void allocateMinorPrizes(final Category category) {

        int position = 2;

        for (final Result result : results.overall_results) {

            if (position > category.numberOfPrizes()) return;

            if (prizeWinner(result, category)) {
                results.prize_winners.get(category).add(result.runner);
                position++;
            }
        }
    }

    private boolean prizeWinner(final Result result, final Category category) {

        return !result.dnf() && category.includes(result.runner.category) && !alreadyWonPrize(result.runner);
    }

    private boolean alreadyWonPrize(final Runner runner) {

        for (List<Runner> winners : results.prize_winners.values())
            if (winners.contains(runner)) return true;

        return false;
    }
}
