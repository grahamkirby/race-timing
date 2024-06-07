package fife_ac_races.minitour;

import common.Category;
import common.RaceResult;
import series_race.SeriesRace;
import series_race.SeriesRaceOutput;

public abstract class MinitourRaceOutput extends SeriesRaceOutput {

    public MinitourRaceOutput(final SeriesRace race) {

        super(race);
    }

    RaceResult[] getMinitourRacePrizeResults(final Category category) {

//        final List<Runner> category_prize_winners = race.prize_winners.get(category);
        final RaceResult[] category_prize_winner_results = race.prize_winners.get(category);

//        for (int i = 0; i < category_prize_winners.size(); i++)
//            for (final MinitourRaceResult result : ((MinitourRace)race).overall_results) {
//                if (result.runner.equals(category_prize_winners.get(i))) {
//                    category_prize_winner_results[i] = result;
//                    break;
//                }
//            }

        return category_prize_winner_results;
    }
}
