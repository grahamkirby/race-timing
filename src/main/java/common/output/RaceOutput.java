package common.output;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import common.Race;
import common.RaceResult;
import common.categories.Category;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public abstract class RaceOutput {

    public interface ResultPrinter {
        void printResult(RaceResult result) throws IOException;
        void printNoResults() throws IOException;
    }

    protected static final Font PDF_FONT = FontFactory.getFont(FontFactory.HELVETICA);
    protected static final Font PDF_BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
    protected static final Font PDF_BOLD_UNDERLINED_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, Font.DEFAULTSIZE, Font.UNDERLINE);
    protected static final Font PDF_BOLD_LARGE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
    protected static final Font PDF_ITALIC_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE);

    public static final String DNF_STRING = "DNF";

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

    protected void printResults(final List<? extends RaceResult> results, final ResultPrinter printer) throws IOException {

        for (final RaceResult result : results)
            printer.printResult(result);

        if (results.isEmpty())
            printer.printNoResults();
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

    public static void addCategoryHeader(Category category, Document document) {

        final Paragraph category_header_paragraph = new Paragraph(48f, "Category: " + category.getLongName(), PDF_BOLD_UNDERLINED_FONT);
        category_header_paragraph.setSpacingAfter(12);
        document.add(category_header_paragraph);
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

    protected void setPositionStrings(final List<? extends RaceResult> results, final boolean allow_equal_positions) {

        // Sets position strings for dead heats.
        // E.g. if results 3 and 4 have the same time, both will be set to "3=".

        for (int result_index = 0; result_index < results.size(); result_index++) {

            final RaceResult result = results.get(result_index);

            if (allow_equal_positions)
                // Skip over any following results with the same times.
                result_index = groupEqualResultsAndReturnFollowingIndex(results, result, result_index);
            else
                result.position_string = String.valueOf(result_index + 1);
        }
    }

    private int groupEqualResultsAndReturnFollowingIndex(final List<? extends RaceResult> results, final RaceResult result, final int result_index) {

        final int highest_index_with_same_duration = getHighestIndexWithSameResult(results, result, result_index);

        if (highest_index_with_same_duration > result_index)

            // Record the same position for all the results with equal times.
            for (int i = result_index; i <= highest_index_with_same_duration; i++)
                results.get(i).position_string = result_index + 1 + "=";

        else
            result.position_string = String.valueOf(result_index + 1);

        return highest_index_with_same_duration;
    }

    private int getHighestIndexWithSameResult(final List<? extends RaceResult> results, final RaceResult result, final int result_index) {

        int highest_index_with_same_result = result_index;

        while (highest_index_with_same_result + 1 < results.size() && result.comparePerformanceTo(results.get(highest_index_with_same_result + 1)) == 0)
            highest_index_with_same_result++;

        return highest_index_with_same_result;
    }

    public void printOverallResults() throws IOException {
        throw new UnsupportedOperationException();
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
    protected void printPrizes(OutputStreamWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }
    protected void printPrizes(Document document, Category category) throws IOException {
        throw new UnsupportedOperationException();
    }
    protected void printPrizes(OutputStreamWriter writer, Category category) throws IOException {
        throw new UnsupportedOperationException();
    }

    protected void printCategoryResults(final OutputStreamWriter writer, final List<String> category_names) throws IOException {
        throw new UnsupportedOperationException();
    }
    protected ResultPrinter getResultPrinter(OutputStreamWriter writer) {
        throw new UnsupportedOperationException();
    }
    protected boolean allowEqualPositions() {
        throw new UnsupportedOperationException();
    }
    protected void printPrizes(OutputStreamWriter writer, List<RaceResult> results) throws IOException {
        throw new UnsupportedOperationException();
    }
}
