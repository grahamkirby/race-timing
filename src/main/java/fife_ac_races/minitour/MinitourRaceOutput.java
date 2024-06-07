package fife_ac_races.minitour;

import common.Category;
import common.Runner;
import series_race.SeriesRace;
import series_race.SeriesRaceOutput;

import java.util.List;

public abstract class MinitourRaceOutput extends SeriesRaceOutput {

    public MinitourRaceOutput(final SeriesRace race) {

        super(race);
    }

    MinitourRaceResult[] getMinitourRacePrizeResults(final Category category) {

        final List<Runner> category_prize_winners = race.prize_winners.get(category);
        final MinitourRaceResult[] category_prize_winner_results = new MinitourRaceResult[category_prize_winners.size()];

        for (int i = 0; i < category_prize_winners.size(); i++)
            for (final MinitourRaceResult result : ((MinitourRace)race).overall_results) {
                if (result.runner.equals(category_prize_winners.get(i))) {
                    category_prize_winner_results[i] = result;
                    break;
                }
            }

        return category_prize_winner_results;
    }
}
