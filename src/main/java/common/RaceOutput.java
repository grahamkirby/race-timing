package common;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public abstract class RaceOutput {

    public interface ResultPrinter {
        void printResult(RaceResult result) throws IOException;
        void printNoResults() throws IOException;
    }

    protected static final Font PDF_FONT = FontFactory.getFont(FontFactory.HELVETICA);
    protected static final Font PDF_BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
    protected static final Font PDF_BOLD_UNDERLINED_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, Font.DEFAULTSIZE, Font.UNDERLINE);
    public static final Font PDF_BOLD_LARGE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
    protected static final Font PDF_ITALIC_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE);

    public static final String OVERALL_RESULTS_HEADER = "Pos,Runner,Club,Category";

    public static final String DNF_STRING = "DNF";

    static Map<String, String> NORMALISED_CLUB_NAMES = new HashMap<>();

    static {
        NORMALISED_CLUB_NAMES.put("", "Unatt.");
        NORMALISED_CLUB_NAMES.put("Unattached", "Unatt.");
        NORMALISED_CLUB_NAMES.put("U/A", "Unatt.");
        NORMALISED_CLUB_NAMES.put("None", "Unatt.");
        NORMALISED_CLUB_NAMES.put("Fife Athletic Club", "Fife AC");
        NORMALISED_CLUB_NAMES.put("Dundee HH", "Dundee Hawkhill Harriers");
        NORMALISED_CLUB_NAMES.put("Leven Las Vegas", "Leven Las Vegas RC");
        NORMALISED_CLUB_NAMES.put("Leven Las Vegas Running Club", "Leven Las Vegas RC");
        NORMALISED_CLUB_NAMES.put("Haddies", "Anster Haddies");
        NORMALISED_CLUB_NAMES.put("Dundee Hawkhill", "Dundee Hawkhill Harriers");
        NORMALISED_CLUB_NAMES.put("DRR", "Dundee Road Runners");
        NORMALISED_CLUB_NAMES.put("Perth RR", "Perth Road Runners");
        NORMALISED_CLUB_NAMES.put("Kinross RR", "Kinross Road Runners");
        NORMALISED_CLUB_NAMES.put("Falkland TR", "Falkland Trail Runners");
        NORMALISED_CLUB_NAMES.put("PH Racing Club", "PH Racing");
        NORMALISED_CLUB_NAMES.put("DHH", "Dundee Hawkhill Harriers");
        NORMALISED_CLUB_NAMES.put("Carnegie H", "Carnegie Harriers");
        NORMALISED_CLUB_NAMES.put("Dundee RR", "Dundee Road Runners");
        NORMALISED_CLUB_NAMES.put("Recreational Running", "Recreational Runners");
    }

    public final Race race;

    public String year;
    public String race_name_for_results;
    public String race_name_for_filenames;
    public String overall_results_filename;
    public String prizes_filename;
    public Path output_directory_path;

    public RaceOutput(final Race race) {

        this.race = race;
        configure();
    }

    protected void printResults(final RaceResult[] results, final ResultPrinter printer) throws IOException {

        setPositionStrings(results);

        for (final RaceResult result : results)
            printer.printResult(result);

        if (results.length == 0)
            printer.printNoResults();
    }

    public void printOverallResults() throws IOException {

        final Path overall_results_csv_path = output_directory_path.resolve(overall_results_filename + ".csv");

        try (final OutputStreamWriter csv_writer = new OutputStreamWriter(Files.newOutputStream(overall_results_csv_path))) {

            printOverallResultsHeader(csv_writer);
            printOverallResults(csv_writer);
        }
    }

    public void printDetailedResults() throws IOException {
        throw new UnsupportedOperationException();
    }
    public void printPrizes() throws IOException {
        throw new UnsupportedOperationException();
    }
    public void printCombined() throws IOException {
        throw new UnsupportedOperationException();
    }

    protected void printOverallResults(OutputStreamWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }
    protected void printOverallResultsHeader(OutputStreamWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }
    public void printPrizes(Category category, OutputStreamWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }
    public void printPrizes(Category category, Document document) throws IOException {
        throw new UnsupportedOperationException();
    }

    private void configure() {

        readProperties();
        constructFilePaths();
    }

    protected void constructFilePaths() {

        overall_results_filename = race_name_for_filenames + "_overall_" + year;
        prizes_filename = race_name_for_filenames + "_prizes_" + year;

        output_directory_path = race.getWorkingDirectoryPath().resolve("output");
    }

    protected void readProperties() {

        year = race.getProperties().getProperty("YEAR");

        race_name_for_results = race.getProperties().getProperty("RACE_NAME_FOR_RESULTS");
        race_name_for_filenames = race.getProperties().getProperty("RACE_NAME_FOR_FILENAMES");
    }

    public void printPrizesHTML() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(prizes_filename + ".html"));

        try (final OutputStreamWriter html_writer = new OutputStreamWriter(stream)) {
            printPrizesHTML(html_writer);
        }
    }

    protected void printPrizesHTML(OutputStreamWriter html_writer) throws IOException {

        html_writer.append("<h4>Prizes</h4>\n");

        for (final Category category : race.categories.getCategoriesInReportOrder())
            printPrizes(category, html_writer);
    }

    public void printPrizesPDF() throws IOException {

        final Path prizes_pdf_path = output_directory_path.resolve(prizes_filename + ".pdf");
        final OutputStream pdf_file_output_stream = Files.newOutputStream(prizes_pdf_path);

        final Document document = new Document();
        PdfWriter.getInstance(document, pdf_file_output_stream);

        document.open();
        document.add(new Paragraph(race_name_for_results + " " + year + " Category Prizes", PDF_BOLD_LARGE_FONT));

        for (final Category category : race.categories.getCategoriesInReportOrder())
            printPrizes(category, document);

        document.close();
    }

    public static void printPrizePDF(Document document, String positionString, String name, String detail, Duration duration) {

        final Paragraph paragraph = new Paragraph();

        paragraph.add(new com.lowagie.text.Chunk(positionString + ": ", PDF_FONT));
        paragraph.add(new Chunk(name, PDF_BOLD_FONT));
        paragraph.add(new Chunk(" (" + detail + ") ", PDF_FONT));
        paragraph.add(new Chunk(format(duration), PDF_FONT));

        document.add(paragraph);
    }

    public void printPrizesText() throws IOException {

        final Path prizes_text_path = output_directory_path.resolve(prizes_filename + ".txt");

        try (final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(prizes_text_path))) {

            writer.append(race_name_for_results).append(" Results ").append(year).append("\n");
            writer.append("============================").append("\n\n");

            for (final Category category : race.categories.getCategoriesInReportOrder())
                printPrizes(category, writer);
        }
    }

    public static void addCategoryHeader(Category category, Document document) {

        final Paragraph category_header_paragraph = new Paragraph(48f, "Category: " + category.getLongName(), PDF_BOLD_UNDERLINED_FONT);
        category_header_paragraph.setSpacingAfter(12);
        document.add(category_header_paragraph);
    }

    public static String normaliseClubName(final String club) {

        return NORMALISED_CLUB_NAMES.getOrDefault(club, club);
    }

    public static String htmlEncode(String s) {

        return s.replaceAll("è", "&egrave;").
                replaceAll("á", "&aacute;").
                replaceAll("é", "&eacute;").
                replaceAll("ü", "&uuml;").
                replaceAll("ö", "&ouml;").
                replaceAll("’", "&acute;");
    }

    public static String format(final Duration duration) {

        final long s = duration.getSeconds();
        return String.format("0%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
    }

    protected void setPositionStrings(final RaceResult[] results) {

        // Sets position strings for dead heats.
        // E.g. if results 3 and 4 have the same time, both will be set to "3=".

        for (int result_index = 0; result_index < results.length; result_index++) {

            final RaceResult result = results[result_index];

            // Skip over any following results with the same times.
            result_index = groupEqualResultsAndReturnFollowingIndex(results, result, result_index);
        }
    }

    public int groupEqualResultsAndReturnFollowingIndex(final RaceResult[] results, final RaceResult result, final int result_index) {

        final int highest_index_with_same_duration = getHighestIndexWithSameResult(results, result, result_index);

        if (highest_index_with_same_duration > result_index)

            // Record the same position for all the results with equal times.
            for (int i = result_index; i <= highest_index_with_same_duration; i++)
                results[i].position_string = result_index + 1 + "=";

        else
            result.position_string = String.valueOf(result_index + 1);

        return highest_index_with_same_duration;
    }

    public int getHighestIndexWithSameResult(final RaceResult[] results, final RaceResult result, final int result_index) {

        int highest_index_with_same_result = result_index;

        while (highest_index_with_same_result + 1 < results.length &&
//                result.duration().equals(results[highest_index_with_same_result + 1].duration()))
                result.comparePerformanceTo(results[highest_index_with_same_result + 1]) == 0)

            highest_index_with_same_result++;

        return highest_index_with_same_result;
    }

}
