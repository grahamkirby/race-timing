package fife_ac_races.midweek;

import common.Category;
import common.RacePrizes;
import common.RaceResult;

import java.util.ArrayList;
import java.util.List;

public class MidweekRacePrizes extends RacePrizes {

    public MidweekRacePrizes(final MidweekRace race) {
        super(race);
    }

    protected List<RaceResult> getPrizeWinners(final Category category) {

        final List<RaceResult> prize_winners = new ArrayList<>();

        int position = 1;

        for (final MidweekRaceResult result : ((MidweekRace)race).getOverallResults()) {

            if (position <= category.numberOfPrizes() && prizeWinner(result, category)) {

                prize_winners.add(result);
                position++;
            }
        }

        return prize_winners;
    }

    private boolean prizeWinner(final MidweekRaceResult result, final Category category) {

        return result.completed() && race.categories.includes(category, result.runner.category) && notYetWonPrize(result.runner);
    }
}
