package org.grahamkirby.race_timing.individual_race;

import java.util.List;

import static org.grahamkirby.race_timing.single_race.SingleRace.KEY_DNF_FINISHERS;

public class TimedIndividualRaceInput extends TimedRaceInput {

    public TimedIndividualRaceInput(TimedIndividualRace timedIndividualRace) {
        super(timedIndividualRace);
    }

//    public List<TimedIndividualRaceEntry> loadEntries() throws IOException {
//
//        if (entries_path == null) return new ArrayList<>();
//
//        final List<TimedIndividualRaceEntry> entries = Files.readAllLines(race.getPath(entries_path)).stream().
//            filter(Predicate.not(String::isBlank)).
//            filter(s -> !s.startsWith(COMMENT_SYMBOL)).
//            map(line -> makeRaceEntry(Arrays.stream(line.split("\t")).toList())).
//            toList();
//
//        assertNoDuplicateEntries(entries);
//
//        return entries;
//    }

    @Override
    protected TimedRaceEntry makeRaceEntry(final List<String> elements) {

        return new TimedIndividualRaceEntry(elements, race);
    }

    @Override
    protected void checkConfig() {

        final String dnf_string = race.getOptionalProperty(KEY_DNF_FINISHERS);
        if (dnf_string != null && !dnf_string.isBlank())
            for (final String bib_number : dnf_string.split(",")) {
                try {
                    Integer.parseInt(bib_number);

                } catch (final NumberFormatException e) {
                    throw new RuntimeException(STR."invalid entry '\{bib_number}' for key '\{KEY_DNF_FINISHERS}' in file '\{race.config_file_path.getFileName()}'", e);
                }
            }
    }

//    private void assertNoDuplicateEntries(final Iterable<? extends TimedIndividualRaceEntry> entries) {
//
//        for (final TimedIndividualRaceEntry entry1 : entries)
//            for (final TimedIndividualRaceEntry entry2 : entries)
//                if (entry1 != entry2 && entry1.equals(entry2))
//                    throw new RuntimeException(STR."duplicate entry '\{entry1}' in file '\{Paths.get(entries_path).getFileName()}'");
//    }
}
