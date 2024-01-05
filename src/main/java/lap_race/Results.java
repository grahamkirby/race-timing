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

    record IndividualLegStart(int bib_number, int leg_number, Duration start_time) {}

    public static final String DNF_STRING = "DNF";
    public static final String DUMMY_DURATION_STRING = "23:59:59";
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

    static final Duration ZERO_TIME = RawResult.parseTime("0:0");

    public static final Duration DUMMY_DURATION = RawResult.parseTime(DUMMY_DURATION_STRING);

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
    String prizes_filename;

    Path input_directory_path;
    Path entries_path;
    Path raw_results_path;

    Path output_directory_path;

    Team[] entries;
    RawResult[] raw_results;
    OverallResult[] overall_results;
    final Map<Category, List<Team>> prize_winners = new HashMap<>();

    Duration[] start_times_for_mass_starts;  // Relative to start of leg 1.

    // Subtly different from start_times_for_mass_starts: there may be a mass start time recorded for a leg even though
    // it's not a leg with a mass start, to allow for a leg 1 runner finishing after a leg 3 mass start - see configureMassStarts().
    boolean[] mass_start_legs;

    boolean[] paired_legs;

    IndividualLegStart[] individual_leg_starts;

    OutputCSV output_CSV;
    OutputHTML output_HTML;

    public Results(final String config_file_path) throws IOException {
        this(readProperties(config_file_path));
    }

    public Results(final Properties properties) {
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

    private static Properties readProperties(final String config_file_path) throws IOException {

        try (final FileInputStream in = new FileInputStream(config_file_path)) {

            final Properties properties = new Properties();
            properties.load(in);
            return properties;
        }
    }

    private void configure(final Properties properties) {

        readProperties(properties);
        constructFilePaths();
        configureMassStarts(properties);
        configurePairedLegs(properties);
        configureIndividualLegStarts(properties);
    }

    public void processResults() throws IOException {

        loadEntries();
        loadRawResults();
        initialiseResults();

        fillLegFinishTimes();
        fillDNFs();
        fillLegStartTimes();
        calculateResults();

        configureOutput();
        printOverallResults();
        printDetailedResults();
        printLegResults();
        printPrizes();
    }

    private void readProperties(final Properties properties) {

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

        overall_results_filename = race_name_for_filenames + "_overall_" + year;
        detailed_results_filename = race_name_for_filenames + "_detailed_" + year;
        prizes_filename = race_name_for_filenames + "_prizes_" + year;

        input_directory_path = working_directory_path.resolve("input");
        entries_path = input_directory_path.resolve(entries_filename);
        raw_results_path = input_directory_path.resolve(raw_results_filename);

        output_directory_path = working_directory_path.resolve("output");
    }

    private void configureMassStarts(final Properties properties) {

        String mass_start_elapsed_times_string = properties.getProperty("MASS_START_ELAPSED_TIMES");

        if (mass_start_elapsed_times_string.isBlank())
            mass_start_elapsed_times_string = (DUMMY_DURATION_STRING + ",").repeat(number_of_legs - 1) + DUMMY_DURATION_STRING;

        start_times_for_mass_starts = new Duration[number_of_legs];
        mass_start_legs = new boolean[number_of_legs];

        int leg_index = 0;
        Duration previous_mass_start_time = null;

        for (String time_as_string : mass_start_elapsed_times_string.split(",")) {
            Duration mass_start_time;
            try {
                mass_start_time = RawResult.parseTime(time_as_string);
            }
            catch (Exception e) {
                throw new RuntimeException("illegal mass start time: " + time_as_string);
            }
            start_times_for_mass_starts[leg_index] = mass_start_time;
            mass_start_legs[leg_index] = !mass_start_time.equals(DUMMY_DURATION);
            if (previous_mass_start_time != null && !(previous_mass_start_time.equals(DUMMY_DURATION)) && previous_mass_start_time.compareTo(start_times_for_mass_starts[leg_index]) > 0)
                throw new RuntimeException("illegal mass start time order");
            previous_mass_start_time = start_times_for_mass_starts[leg_index];
            leg_index++;
        }

        // If there is no mass start configured for leg 2,
        // use the leg 3 mass start time for leg 2 too.
        // This covers the case where the leg 1 runner finishes after a mass start.
        if (start_times_for_mass_starts[1].equals(RawResult.parseTime(DUMMY_DURATION_STRING))) {
            start_times_for_mass_starts[1] = start_times_for_mass_starts[2];
        }
    }

    private void configurePairedLegs(final Properties properties) {

        final String paired_legs_string = properties.getProperty("PAIRED_LEGS");
        paired_legs = new boolean[number_of_legs];

        for (String s : paired_legs_string.split(",")) {
            paired_legs[Integer.parseInt(s) - 1] = true;
        }
    }

    private void configureIndividualLegStarts(final Properties properties) {

        String individual_leg_starts_string = properties.getProperty("INDIVIDUAL_LEG_STARTS");
        if (individual_leg_starts_string == null)
            individual_leg_starts = new IndividualLegStart[0];
        else {
            String[] strings = individual_leg_starts_string.split(",");
            individual_leg_starts = new IndividualLegStart[strings.length];

            for (int i = 0; i < strings.length; i++) {
                String[] split = strings[i].split("/");
                final int bib_number = Integer.parseInt(split[0]);
                final int leg_number = Integer.parseInt(split[1]);
                final Duration start_time = RawResult.parseTime(split[2]);
                individual_leg_starts[i] = new IndividualLegStart(bib_number, leg_number, start_time);
            }
        }
    }

     private void loadEntries() throws IOException {

        final List<String> lines = Files.readAllLines(entries_path);
        entries = new Team[lines.size()];

        for (int i = 0; i < entries.length; i++) {
            String[] strings = lines.get(i).split("\t");
            if (strings.length != number_of_legs + 3)
                throw new RuntimeException("illegal composition for team: " + strings[0]);
            entries[i] = new Team(strings);
        }
    }

    private void loadRawResults() throws IOException {

        final List<String> lines = Files.readAllLines(raw_results_path);
        raw_results = new RawResult[lines.size()];

        RawResult previous_result = null;

        for (int i = 0; i < raw_results.length; i++) {

            final RawResult result = new RawResult(lines.get(i));
            if (previous_result != null && previous_result.recorded_finish_time.compareTo(result.recorded_finish_time) > 0)
                throw new RuntimeException("result " + (i+1) + " out of order");

            raw_results[i] = result;
            previous_result = result;
        }
    }

    private void initialiseResults() {

        overall_results = new OverallResult[entries.length];

        for (int i = 0; i < overall_results.length; i++)
            overall_results[i] = new OverallResult(entries[i], number_of_legs, this);
    }

    private void fillLegFinishTimes() {

        for (final RawResult raw_result : raw_results) {

            try {
                final int team_index = findIndexOfTeamWithBibNumber(raw_result.bib_number);
                final OverallResult result = overall_results[team_index];
                final LegResult[] leg_results = result.leg_results;

                final int leg_index = findIndexOfNextUnfilledLegResult(leg_results);
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

                try {
                    final String[] dnf = dnf_string.split("/");
                    final int bib_number = Integer.parseInt(dnf[0]);
                    final int leg_number = Integer.parseInt(dnf[1]);
                    final int leg_index = leg_number - 1;

                    final OverallResult result = overall_results[findIndexOfTeamWithBibNumber(bib_number)];
                    result.leg_results[leg_index].DNF = true;
                }
                catch (Exception e) {
                    throw new RuntimeException("illegal DNF time");
                }
            }
        }
    }

    private void fillLegStartTimes() {

        for (final OverallResult result : overall_results) {

            final LegResult[] leg_results = result.leg_results;
            leg_results[0].start_time = ZERO_TIME;   // All times are relative to start of leg 1.

            for (int leg_index = 0; leg_index < number_of_legs; leg_index++)
                fillLegStartTime(leg_results, leg_index);
        }
    }

    private void fillLegStartTime(final LegResult[] leg_results, final int leg_index) {

        // If there isn't a recorded finish time for the previous leg then we can't set
        // a start time for this one. Both legs will be DNF.

        // Even if the previous leg does have a recorded finish time it still might be
        // DNF, if the runners reported missing a checkpoint, but it can still be used
        // to set this leg's start time.

        // If the previous leg finish was after the mass start for this leg, allow for
        // this leg runner(s) starting in the mass start rather than when the previous
        // leg runner(s) finished.

        // First leg always starts at zero, but there might be an individual start time
        // recorded for a leg 1 runner.


        // Look for an individual start time.
        Duration individual_start_time = null;

        //IndividualLegStart[] individual_leg_starts;

        for (IndividualLegStart individual_leg_start : individual_leg_starts) {
            if (individual_leg_start.bib_number == leg_results[leg_index].team.bib_number && individual_leg_start.leg_number == leg_index + 1)
                individual_start_time = individual_leg_start.start_time;
        }

        if (individual_start_time != null) {
            leg_results[leg_index].start_time = individual_start_time;
        }
        else {
            if (leg_index > 0) {

                final Duration mass_start_time = start_times_for_mass_starts[leg_index];
                final int previous_leg_index = leg_index - 1;

                if (leg_results[previous_leg_index].finish_time == null) {
                    // Leg result will already be set to DNF.
                    leg_results[leg_index].in_mass_start = mass_start_legs[leg_index];
                } else {
                    leg_results[leg_index].start_time = earlierOf(mass_start_time, leg_results[previous_leg_index].finish_time);
                    //leg_results[leg_index].in_mass_start = mass_start_legs[leg_index] && mass_start_time.compareTo(leg_results[previous_leg_index].finish_time) < 0;
                    leg_results[leg_index].in_mass_start = mass_start_time.compareTo(leg_results[previous_leg_index].finish_time) < 0;
                }
            }
        }
    }

    private void calculateResults() {
        Arrays.sort(overall_results);
    }

    public void configureOutput() throws IOException {

        output_CSV = new OutputCSV(this);
        output_HTML = new OutputHTML(this);
    }

    public void printOverallResults() throws IOException {

        output_CSV.printOverallResultsCSV();
        output_HTML.printOverallResultsHTML();
    }

    public void printDetailedResults() throws IOException {

        output_CSV.printDetailedResultsCSV();
        output_HTML.printDetailedResultsHTML();
    }

    private void printLegResults() throws IOException {

        for (int leg = 1; leg <= number_of_legs; leg++)
            printLegResults(leg);
    }

    private void printLegResults(final int leg) throws IOException {

        final Path leg_results_csv_path = output_directory_path.resolve(race_name_for_filenames + "_leg_" + leg + "_" + year + ".csv");
        final Path leg_results_html_path = output_directory_path.resolve(race_name_for_filenames + "_leg_" + leg + "_" + year + ".html");

        try (final OutputStreamWriter csv_writer = new OutputStreamWriter(Files.newOutputStream(leg_results_csv_path));
                final OutputStreamWriter html_writer = new OutputStreamWriter(Files.newOutputStream(leg_results_html_path))) {

            printLegResultsCSVHeader(leg, csv_writer);
            printLegResultsCSV(getLegResults(leg), csv_writer);

            printLegResultsHTMLHeader(leg, html_writer);
            printLegResultsHTML(getLegResults(leg), html_writer);
            printLegResultsHTMLFooter(html_writer);
        }
    }

    private LegResult[] getLegResults(final int leg) {

        final LegResult[] leg_results = new LegResult[overall_results.length];

        for (int i = 0; i < leg_results.length; i++)
            leg_results[i] = overall_results[i].leg_results[leg-1];

        Arrays.sort(leg_results);
        return leg_results;
    }

    Integer getRecordedLegPosition(final int bib_number, final int leg_number) {

        int count = 0;

        for (int i = 0; i < raw_results.length; i++) {
            if (raw_results[i].bib_number == bib_number) {
                count++;
                if (count == leg_number) return i + 1;
            }
        }

        return Integer.MAX_VALUE;
    }

    private static void printLegResultsCSV(final LegResult[] leg_results, final OutputStreamWriter writer) throws IOException {

        final int number_of_results = leg_results.length;

        // Deal with dead heats in legs 2-4.
        for (int i = 0; i < number_of_results; i++) {

            final LegResult result = leg_results[i];
            if (result.leg_number == 1) {
                result.position_string = String.valueOf(i + 1);
            }
            else {
                int j = i;

                while (j + 1 < number_of_results && result.duration().equals(leg_results[j + 1].duration())) j++;
                if (j > i) {
                    for (int k = i; k <= j; k++)
                        leg_results[k].position_string = i + 1 + "=";
                    i = j;
                } else
                    result.position_string = String.valueOf(i + 1);
            }
        }

        for (final LegResult leg_result : leg_results) {

            if (!leg_result.DNF) {
                writer.append(leg_result.position_string).append(",");
                writer.append(leg_result.toString()).append("\n");
            }
        }
    }

    private void printLegResultsHTML(final LegResult[] leg_results, final OutputStreamWriter writer) throws IOException {

        for (final LegResult leg_result : leg_results) {

            if (!leg_result.DNF) {
                writer.append("""
                                <tr>
                                <td>""");
                writer.append(leg_result.position_string);
                writer.append("""
                                </td>
                                <td>""");
                writer.append(leg_result.team.runners[leg_result.leg_number-1]);
                writer.append("""
                                </td>
                                <td>""");
                writer.append(OverallResult.format(leg_result.duration()));
                writer.append("""
                                </td>
                            </tr>""");
            }
        }
    }

    private void printLegResultsCSVHeader(final int leg, final OutputStreamWriter writer) throws IOException {

        writer.append("Pos,Runner");
        if (paired_legs[leg-1]) writer.append("s");
        writer.append(",Time\n");
    }

    private void printLegResultsHTMLHeader(final int leg, final OutputStreamWriter writer) throws IOException {

        writer.append("""
            <table class="fac-table">
                <thead>
                    <tr>
                        <th>Pos</th>
                        <th>Runner
            """);

        if (paired_legs[leg-1]) writer.append("s");

        writer.append("""
            </th>
                        <th>Time</th>
                    </tr>
                </thead>
                <tbody>
            """);
    }

    private void printLegResultsHTMLFooter(final OutputStreamWriter writer) throws IOException {

        writer.append("""
                </tbody>
            </table>
            """);
    }

    public void printPrizes() throws IOException {

        // Allocate first prize in each category first, in decreasing order of category breadth.
        // This is because e.g. a 40+ team should win first in 40+ category before a subsidiary
        // prize in open category.
        allocateFirstPrizes();

        // Now consider other prizes (only available in senior categories).
        allocateMinorPrizes();

        printPrizesText();
        printPrizesPDF();
    }

    private void printPrizesText() throws IOException {

        final Path prizes_text_path = output_directory_path.resolve(prizes_filename + ".txt");

        try (final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(prizes_text_path))) {

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

                    final OverallResult result = overall_results[findIndexOfTeamWithBibNumber(team.bib_number)];

                    writer.append(String.valueOf(position++)).append(": ").
                            append(result.team.name).append(" (").
                            append(result.team.category.toString()).append(") ").
                            append(OverallResult.format(result.duration())).append("\n");
                }

                writer.append("\n\n");
            }
        }
    }

    private void printPrizesPDF() throws IOException {

        final Path prizes_pdf_path = output_directory_path.resolve(prizes_filename + ".pdf");
        final OutputStream pdf_file_output_stream = Files.newOutputStream(prizes_pdf_path);

        final Document document = new Document();
        PdfWriter.getInstance(document, pdf_file_output_stream);

        document.open();
        document.add(new Paragraph(race_name_for_results + " " + year + " Category Prizes", PDF_BOLD_LARGE_FONT));

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

                final OverallResult result = overall_results[findIndexOfTeamWithBibNumber(team.bib_number)];

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

            for (final OverallResult result : overall_results) {
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

            for (final OverallResult result : overall_results) {
                
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

        for (int i = 0; i < overall_results.length; i++)
            if (overall_results[i].team.bib_number == bib_number) return i;

        throw new RuntimeException("unregistered team: ");
    }

    Duration sumDurationsUpToLeg(final LegResult[] leg_results, final int leg) {

        Duration total = leg_results[0].duration();
        for (int i = 1; i < leg; i++)
            total = total.plus(leg_results[i].duration());
        return total;
    }
}
