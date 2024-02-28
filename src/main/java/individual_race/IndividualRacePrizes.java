package individual_race;

import common.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IndividualRacePrizes {

    final IndividualRace race;

    public IndividualRacePrizes(final IndividualRace race) {
        this.race = race;
    }

    public void allocatePrizes() {

        List<IndividualRaceCategory> categories = new ArrayList<>(Arrays.asList(IndividualRaceCategory.values()));
        if (!race.open_category)
            categories.remove(IndividualRaceCategory.OPEN_SENIOR);

        for (final Category category : categories)
            race.prize_winners.put(category, getPrizeWinners(category));
    }

    private List<IndividualRaceEntry> getPrizeWinners(final Category category) {

        final List<IndividualRaceEntry> prize_winners = new ArrayList<>();

        int position = 1;

        for (final IndividualRaceResult result : race.overall_results) {

            if (position <= category.numberOfPrizes() && prizeWinner(result, category)) {

                prize_winners.add(result.entry);
                position++;
            }
        }
        return prize_winners;
    }

    private boolean prizeWinner(final IndividualRaceResult result, final Category category) {

        return !result.dnf() && category.includes(result.entry.runner.category()) && !alreadyWonPrize(result.entry);
    }

    private boolean alreadyWonPrize(final IndividualRaceEntry entry) {

        for (List<IndividualRaceEntry> winners : race.prize_winners.values())
            if (winners.contains(entry)) return true;

        return false;
    }
}
