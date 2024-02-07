package lap_race;

import common.Category;

import java.util.ArrayList;
import java.util.List;

public class LapRacePrizes {

    final LapRace results;

    public LapRacePrizes(final LapRace results) {

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

        for (final TeamResult result : results.overall_results) {
            if (prizeWinner(result, category)) {
                results.prize_winners.get(category).add(result.team);
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

        for (final TeamResult result : results.overall_results) {

            if (position > category.numberOfPrizes()) return;

            if (prizeWinner(result, category)) {
                results.prize_winners.get(category).add(result.team);
                position++;
            }
        }
    }

    private boolean prizeWinner(final TeamResult result, final Category category) {

        return !result.dnf() && category.includes(result.team.category) && !alreadyWonPrize(result.team);
    }

    private boolean alreadyWonPrize(final Team team) {

        for (List<Team> winners : results.prize_winners.values())
            if (winners.contains(team)) return true;

        return false;
    }
}
