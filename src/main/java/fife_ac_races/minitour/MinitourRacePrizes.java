package fife_ac_races.minitour;

import common.Category;
import common.RacePrizes2;
import common.RaceResult;

import java.util.ArrayList;
import java.util.List;

public class MinitourRacePrizes extends RacePrizes2 {

    final MinitourRace race;

    public MinitourRacePrizes(final MinitourRace race) {
        super(race);
        this.race = race;
    }

    protected RaceResult[] getPrizeWinners(final Category category) {

        final List<RaceResult> prize_winners = new ArrayList<>();

        int position = 1;

        for (final MinitourRaceResult result : race.getOverallResults()) {

            if (position <= category.numberOfPrizes() && prizeWinner(result, category)) {

                prize_winners.add(result);
                position++;
            }
        }
        return prize_winners.toArray(new RaceResult[0]);
    }

    private boolean prizeWinner(final MinitourRaceResult result, final Category category) {

        return result.completed() && race.categories.includes(category, result.runner.category) && notYetWonPrize(result.runner);
    }
}
