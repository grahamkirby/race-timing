package lap_race;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.List;

public class Results {

    ////////////////////////////////////////////  SET UP  ////////////////////////////////////////////
    //                                                                                              //
    //  1. Create new copy of directory src/main/resources/lap_race/devils_burdens/sample_config.   //
    //                                                                                              //
    //  2. Edit WORKING_DIRECTORY property to location of new directory.                            //
    //                                                                                              //
    //  3. Update year and mass start times.                                                        //
    //                                                                                              //
    //  4. Run Results, passing path of config file as command line parameter.                      //
    //                                                                                              //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String DNF_STRING = "DNF";
    public static final String DUMMY_DURATION = "23:59:59";
    public static final String OVERALL_RESULTS_HEADER = "Pos,No,Team,Category,";

    public static final List<Category> CATEGORY_REPORT_ORDER = Arrays.asList(
            Category.FEMALE_SENIOR,
            Category.OPEN_SENIOR,
            Category.FEMALE_40,
            Category.OPEN_40,
            Category.FEMALE_50,
            Category.OPEN_50,
            Category.FEMALE_60,
            Category.OPEN_60,
            Category.MIXED_SENIOR,
            Category.MIXED_40);

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final Font PDF_FONT = FontFactory.getFont(FontFactory.HELVETICA);
    private static final Font PDF_BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
    private static final Font PDF_BOLD_UNDERLINED_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, Font.DEFAULTSIZE, Font.UNDERLINE);
    private static final Font PDF_BOLD_LARGE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
    private static final Font PDF_ITALIC_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE);

    static Duration ZERO_TIME = RawResult.parseTime("0:0");

    public static final Duration DNF_DUMMY_LEG_TIME = RawResult.parseTime(DUMMY_DURATION);

    // Read from configuration file.
    Path working_directory_path;
    String entries_filename;
    String raw_results_filename;
    String year;
    int number_of_legs;
    String race_name_for_results;
    String race_name_for_filenames;
    String dnf_legs_string;

    // Derived.
    String overall_results_filename;
    String detailed_results_filename;
    String prizes_text_filename;
    String prizes_pdf_filename;

    Path input_directory_path;
    Path entries_path;
    Path raw_results_path;

    Path output_directory_path;
    Path overall_results_path;
    Path detailed_results_path;
    Path prizes_text_path;
    Path prizes_pdf_path;

    Team[] entries;
    RawResult[] raw_results;
    OverallResult[] results;
    Map<Category, List<Team>> prize_winners = new HashMap<>();

    Duration[] start_times_for_mass_starts;  // Relative to start of leg 1.
    boolean[] paired_legs;

    public Results(String config_file_path) throws IOException {
        this(readProperties(config_file_path));
    }

    public Results(Properties properties) {
        configure(properties);
    }

    public static void main(String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java Results <config file path>");
        else {
            new Results(args[0]).processResults();
        }
    }

    private static Properties readProperties(String config_file_path) throws IOException {

        try (FileInputStream in = new FileInputStream(config_file_path)) {

            Properties properties = new Properties();
            properties.load(in);
            return properties;
        }
    }

    private void configure(Properties properties) {

        readProperties(properties);
        constructFilePaths();
        configureMassStarts(properties);
        configurePairedLegs(properties);
    }

    public void processResults() throws IOException {

        loadEntries();
        loadRawResults();
        initialiseResults();

        fillLegFinishTimes();
        fillDNFs();
        fillLegStartTimes();
        calculateResults();

        printOverallResults();
        printDetailedResults();
        printLegResults();
        printPrizes();
    }

    private void readProperties(Properties properties) {

        working_directory_path = Paths.get(properties.getProperty("WORKING_DIRECTORY"));

        entries_filename = properties.getProperty("ENTRIES_FILENAME");
        raw_results_filename = properties.getProperty("RAW_RESULTS_FILENAME");
        year = properties.getProperty("YEAR");
        number_of_legs = Integer.parseInt(properties.getProperty("NUMBER_OF_LEGS"));
        race_name_for_results = properties.getProperty("RACE_NAME_FOR_RESULTS");
        race_name_for_filenames = properties.getProperty("RACE_NAME_FOR_FILENAMES");
        dnf_legs_string = properties.getProperty("DNF_LEGS");
    }

    private void constructFilePaths() {

        overall_results_filename = race_name_for_filenames + "_overall_" + year + ".csv";
        detailed_results_filename = race_name_for_filenames + "_detailed_" + year + ".csv";
        prizes_text_filename = race_name_for_filenames + "_prizes_" + year + ".txt";
        prizes_pdf_filename = race_name_for_filenames + "_prizes_" + year + ".pdf";

        input_directory_path = working_directory_path.resolve("input");
        entries_path = input_directory_path.resolve(entries_filename);
        raw_results_path = input_directory_path.resolve(raw_results_filename);

        output_directory_path = working_directory_path.resolve("output");
        overall_results_path = output_directory_path.resolve(overall_results_filename);
        detailed_results_path = output_directory_path.resolve(detailed_results_filename);
        prizes_text_path = output_directory_path.resolve(prizes_text_filename);
        prizes_pdf_path = output_directory_path.resolve(prizes_pdf_filename);
    }

    private void configureMassStarts(Properties properties) {

        String mass_start_elapsed_times_string = properties.getProperty("MASS_START_ELAPSED_TIMES");

        if (mass_start_elapsed_times_string.isBlank())
            mass_start_elapsed_times_string = DUMMY_DURATION + "," + DUMMY_DURATION + "," + DUMMY_DURATION + "," + DUMMY_DURATION;

        start_times_for_mass_starts = new Duration[number_of_legs];

        int leg_index = 0;
        for (String time_as_string : mass_start_elapsed_times_string.split(",")) {
            start_times_for_mass_starts[leg_index] = RawResult.parseTime(time_as_string);
            leg_index++;
        }

        // If there is no mass start configured for leg 2,
        // use the leg 3 mass start time for leg 2 too.
        // This covers the case where the leg 1 runner finishes after a mass start.
        if (start_times_for_mass_starts[1].equals(RawResult.parseTime(DUMMY_DURATION))) {
            start_times_for_mass_starts[1] = start_times_for_mass_starts[2];
        }
    }

    private void configurePairedLegs(Properties properties) {

        String paired_legs_string = properties.getProperty("PAIRED_LEGS");
        paired_legs = new boolean[number_of_legs];

        for (String s : paired_legs_string.split(",")) {
            paired_legs[Integer.parseInt(s) - 1] = true;
        }
    }

     private void loadEntries() throws IOException {

        List<String> lines = Files.readAllLines(entries_path);
        entries = new Team[lines.size()];

        for (int i = 0; i < entries.length; i++)
            entries[i] = new Team(lines.get(i).split("\t"));
    }

    private void loadRawResults() throws IOException {

        List<String> lines = Files.readAllLines(raw_results_path);
        raw_results = new RawResult[lines.size()];

        for (int i = 0; i < raw_results.length; i++)
            raw_results[i] = new RawResult(lines.get(i));
    }

    private void initialiseResults() {

        results = new OverallResult[entries.length];

        for (int i = 0; i < results.length; i++)
            results[i] = new OverallResult(entries[i], number_of_legs);
    }

    private void fillLegFinishTimes() {

        for (RawResult raw_result : raw_results) {

            try {
                int team_index = findIndexOfTeamWithBibNumber(raw_result.bib_number);
                OverallResult result = results[team_index];
                LegResult[] leg_results = result.leg_results;

                int leg_index = findIndexOfNextUnfilledLegResult(leg_results);
                leg_results[leg_index].finish_time = raw_result.recorded_finish_time;
                leg_results[leg_index].DNF = false;
            }
            catch (RuntimeException e) {
                throw new RuntimeException(e.getMessage() + raw_result.bib_number);
            }
        }
    }

    private void fillDNFs() {

        // This fills in the DNF results that were specified explicitly in the config
        // file, corresponding to cases where the runners reported not visiting all
        // checkpoints.

        // DNF cases where there is no recorded leg result are captured by the
        // default value of LegResult.DNF being true.

        if (!dnf_legs_string.isBlank()) {

            for (final String dnf_string : dnf_legs_string.split(",")) {

                String[] dnf = dnf_string.split(":");
                final int bib_number = Integer.parseInt(dnf[0]);
                final int leg_number = Integer.parseInt(dnf[1]);
                final int leg_index = leg_number - 1;

                OverallResult result = results[findIndexOfTeamWithBibNumber(bib_number)];
                result.leg_results[leg_index].DNF = true;
            }
        }
    }

    private void fillLegStartTimes() {

        for (OverallResult result : results) {

            LegResult[] leg_results = result.leg_results;
            leg_results[0].start_time = ZERO_TIME;   // All times are relative to start of leg 1.

            for (int leg_index = 1; leg_index < number_of_legs; leg_index++)
                fillLegStartTime(leg_results, leg_index);
        }
    }

    private void fillLegStartTime(LegResult[] leg_results, int leg_index) {

        // If there isn't a recorded finish time for the previous leg then we can't set
        // a start time for this one. Both legs will be DNF.

        // Even if the previous leg does have a recorded finish time it still might be
        // DNF, if the runners reported missing a checkpoint, but it can still be used
        // to set this leg's start time.

        // If the previous leg finish was after the mass start for this leg, allow for
        // this leg runner(s) starting in the mass start rather than when the previous
        // leg runner(s) finished.

        // This method is not called for the first leg, since it always starts at zero.
        assert leg_index > 0;

        final Duration mass_start_time = start_times_for_mass_starts[leg_index];
        final int previous_leg_index = leg_index - 1;

        //noinspection StatementWithEmptyBody
        if (leg_results[previous_leg_index].finish_time == null) {
            // No action since leg result will already be set to DNF.
        }
        else {
            leg_results[leg_index].start_time = earlierOf(leg_results[previous_leg_index].finish_time, mass_start_time);
        }
    }

    private void calculateResults() {
        Arrays.sort(results);
    }

    public void printOverallResults() throws IOException {

        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(overall_results_path))) {

            writer.append(OVERALL_RESULTS_HEADER).append("Total\n");
            printOverallResults(writer);
        }
    }

    private void printOverallResults(OutputStreamWriter writer) throws IOException {
        
        for (int i = 0; i < results.length; i++) {

            OverallResult result = results[i];

            if (!result.dnf()) writer.append(String.valueOf(i+1));
            writer.append(",").append(String.valueOf(results[i])).append("\n");
        }
    }

    public void printDetailedResults() throws IOException {

        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(detailed_results_path))) {

            printDetailedResultsHeader(writer);
            printDetailedResults(writer);
        }
    }

    private void printDetailedResultsHeader(OutputStreamWriter writer) throws IOException {

        writer.append(OVERALL_RESULTS_HEADER);

        for (int leg = 1; leg <= number_of_legs; leg++) {
            writer.append("Runners ").append(String.valueOf(leg)).append(",Leg ").append(String.valueOf(leg)).append(",");
            if (leg < number_of_legs) writer.append("Split ").append(String.valueOf(leg)).append(",");
        }

        writer.append("Total\n");
    }

    private void printDetailedResults(final OutputStreamWriter writer) throws IOException {

        int position = 1;

        for (OverallResult result : results) {

            Team team = result.team;
            int bib_number = team.bib_number;
            boolean any_previous_leg_dnf = false;

            if (!result.dnf()) writer.append(String.valueOf(position++));
            
            writer.append(",");
            writer.append(String.valueOf(bib_number)).append(",");
            writer.append(team.name).append(",");
            writer.append(team.category.toString()).append(",");
            
            for (int leg = 1; leg <= number_of_legs; leg++) {

                LegResult leg_result = result.leg_results[leg-1];

                writer.append(team.runners[leg-1]).append(",");
                writer.append(leg_result.DNF ? DNF_STRING : OverallResult.format(leg_result.duration())).append(",");
                writer.append(leg_result.DNF || any_previous_leg_dnf ? DNF_STRING : OverallResult.format(sumDurationsUpToLeg(result.leg_results, leg)));

                if (leg < number_of_legs) writer.append(",");
                if (leg_result.DNF) any_previous_leg_dnf = true;
            }

            writer.append("\n");
        }
    }

    private void printLegResults() throws IOException {

        for (int leg = 1; leg <= number_of_legs; leg++)
            printLegResults(leg);
    }

    private void printLegResults(int leg) throws IOException {

        Path leg_results_path = output_directory_path.resolve(race_name_for_filenames + "_leg_" + leg + "_" + year + ".csv");

        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(leg_results_path))) {

            printLegResultsHeader(leg, writer);
            printLegResults(getLegResults(leg), writer);
        }
    }

    private LegResult[] getLegResults(int leg) {

        LegResult[] leg_results = new LegResult[results.length];

        for (int i = 0; i < leg_results.length; i++)
            leg_results[i] = results[i].leg_results[leg-1];

        Arrays.sort(leg_results);
        return leg_results;
    }

    private static void printLegResults(LegResult[] leg_results, OutputStreamWriter writer) throws IOException {

        for (int i = 0; i < leg_results.length; i++) {

            LegResult leg_result = leg_results[i];

            if (!leg_result.DNF) {
                writer.append(String.valueOf(i+1)).append(",");
                writer.append(leg_result.toString()).append("\n");
            }
        }
    }

    private void printLegResultsHeader(int leg, OutputStreamWriter writer) throws IOException {

        writer.append("Pos,Runner");
        if (paired_legs[leg-1]) writer.append("s");
        writer.append(",Time\n");
    }

    public void printPrizes() throws IOException {

        // Allocate first prize in each category first, in decreasing order of category breadth.
        // This is because e.g. a 40+ team should win first in 40+ category before a subsidiary
        // prize in open category.
        allocateFirstPrizes();

        // Now consider other prizes (only available in senior categories).
        allocateMinorPrizes();

        printPrizesToText();
        printPrizesToPDF();
    }

    private void printPrizesToText() throws IOException {

        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(prizes_text_path))) {

            writer.append(race_name_for_results).append(" Results ").append(year).append("\n");
            writer.append("============================").append("\n\n");

            for (final Category category : CATEGORY_REPORT_ORDER) {

                final String header = "Category: " + category;

                writer.append(header).append("\n");
                writer.append("-".repeat(header.length())).append("\n\n");

                final List<Team> category_prize_winners = prize_winners.get(category);

                if (category_prize_winners.isEmpty())
                    writer.append("No results\n");

                int position = 1;
                for (final Team team : category_prize_winners) {

                    final OverallResult result = results[findIndexOfTeamWithBibNumber(team.bib_number)];

                    writer.append(String.valueOf(position++)).append(": ").
                            append(result.team.name).append(" (").
                            append(result.team.category.toString()).append(") ").
                            append(OverallResult.format(result.duration())).append("\n");
                }

                writer.append("\n\n");
            }
        }
    }

    private void printPrizesToPDF() throws IOException {

        final OutputStream pdf_file_output_stream = Files.newOutputStream(prizes_pdf_path);

        final Document document = new Document();
        PdfWriter.getInstance(document, pdf_file_output_stream);

        document.open();
        document.add(new Paragraph(race_name_for_results + " Results " + year, PDF_BOLD_LARGE_FONT));

        for (final Category category : CATEGORY_REPORT_ORDER) {

            final String header = "Category: " + category;

            final Paragraph category_header_paragraph = new Paragraph(48f, header, PDF_BOLD_UNDERLINED_FONT);
            category_header_paragraph.setSpacingAfter(12);
            document.add(category_header_paragraph);

            final List<Team> category_prize_winners = prize_winners.get(category);

            if (category_prize_winners.isEmpty())
                document.add(new Paragraph("No results", PDF_ITALIC_FONT));

            int position = 1;
            for (final Team team : category_prize_winners) {

                final OverallResult result = results[findIndexOfTeamWithBibNumber(team.bib_number)];

                final Paragraph paragraph = new Paragraph();
                paragraph.add(new Chunk(position++ + ": ", PDF_FONT));
                paragraph.add(new Chunk(result.team.name, PDF_BOLD_FONT));
                paragraph.add(new Chunk(" (" + result.team.category + ") ", PDF_FONT));
                paragraph.add(new Chunk(OverallResult.format(result.duration()), PDF_FONT));
                document.add(paragraph);
            }
        }
        document.close();
    }

    private void allocateFirstPrizes() {

        for (final Category category : Category.values()) {

            prize_winners.put(category, new ArrayList<>());

            for (final OverallResult result : results) {
                if (prizeWinner(result, category)) {
                    prize_winners.get(category).add(result.team);
                    break;
                }
            }
        }
    }

    private void allocateMinorPrizes() {

        for (final Category category : Category.values()) {

            int position = 2;

            for (OverallResult result : results) {
                
                if (position > category.number_of_prizes) break;
                
                if (prizeWinner(result, category)) {
                    prize_winners.get(category).add(result.team);
                    position++;
                }
            }
        }
    }

    private boolean prizeWinner(final OverallResult result, final Category category) {
        return !result.dnf() && category.includes(result.team.category) && !alreadyWonPrize(result.team);
    }

    private boolean alreadyWonPrize(final Team team) {
        for (List<Team> winners : prize_winners.values())
            if (winners.contains(team)) return true;
        return false;
    }

    private Duration earlierOf(final Duration duration1, final Duration duration2) {
        return duration1.compareTo(duration2) <= 0 ? duration1 : duration2;
    }

    private int findIndexOfNextUnfilledLegResult(final LegResult[] leg_results) {

        for (int i = 0; i < leg_results.length; i++)
            if (leg_results[i].finish_time == null) return i;

        throw new RuntimeException("surplus result recorded for team: ");
    }

    private int findIndexOfTeamWithBibNumber(final int bib_number) {

        for (int i = 0; i < results.length; i++)
            if (results[i].team.bib_number == bib_number) return i;

        throw new RuntimeException("unregistered team: ");
    }

    private Duration sumDurationsUpToLeg(final LegResult[] leg_results, final int leg) {

        Duration total = leg_results[0].duration();
        for (int i = 1; i < leg; i++)
            total = total.plus(leg_results[i].duration());
        return total;
    }
}


// code for dead heats
//        int number_of_results = overall_results.size();
//
//        for (int i = 0; i < number_of_results; i++) {
//
//            OverallResult result = overall_results.get(i);
//            int j = i;
//
//            while (j + 1 < number_of_results && result.overall_time.equals(overall_results.get(j + 1).overall_time)) j++;
//            if (j > i) {
//                for (int k = i; k <= j; k++)
//                    overall_results.get(k).position_string = i + 1 + "=";
//                i = j;
//            }
//            else
//                result.position_string = String.valueOf(i + 1);
//        }