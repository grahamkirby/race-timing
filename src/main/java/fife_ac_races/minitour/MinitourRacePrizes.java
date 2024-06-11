package fife_ac_races.minitour;

import common.Category;
import common.RacePrizes;
import common.RaceResult;

import java.util.ArrayList;
import java.util.List;

public class MinitourRacePrizes extends RacePrizes {

    public MinitourRacePrizes(final MinitourRace race) {
        super(race);
    }

    protected List<RaceResult> getPrizeWinners(final Category category) {

        final List<RaceResult> prize_winners = new ArrayList<>();

        int position = 1;

        for (final RaceResult result : ((MinitourRace)race).getOverallResults()) {

            if (position <= category.numberOfPrizes() && prizeWinner(result, category)) {

                prize_winners.add(result);
                position++;
            }
        }
        return prize_winners;
    }

    private boolean prizeWinner(final RaceResult result, final Category category) {

        return ((MinitourRaceResult)result).completed() && race.categories.includes(category, ((MinitourRaceResult)result).runner.category) && notYetWonPrize(((MinitourRaceResult)result).runner);
    }
}
