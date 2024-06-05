package common;

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

public abstract class RaceOutput {

    protected static final Font PDF_FONT = FontFactory.getFont(FontFactory.HELVETICA);
    protected  static final Font PDF_BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
    protected  static final Font PDF_BOLD_UNDERLINED_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, Font.DEFAULTSIZE, Font.UNDERLINE);
    public static final Font PDF_BOLD_LARGE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
    protected  static final Font PDF_ITALIC_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE);

    public static final String OVERALL_RESULTS_HEADER = "Pos,Runner,Club,Category";

    public static final String DNF_STRING = "DNF";

    protected final Race race;

    protected String year;
    protected String race_name_for_results;
    protected String race_name_for_filenames;
    protected String overall_results_filename;
    protected String prizes_filename;
    protected Path output_directory_path;

    public RaceOutput(final Race race) {

        this.race = race;
        configure();
    }

    protected void printOverallResultsHeader(OutputStreamWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }
    protected void printOverallResults(OutputStreamWriter writer) throws IOException {
        throw new UnsupportedOperationException();
    }
    public void printDetailedResults() throws IOException {
        throw new UnsupportedOperationException();
    }
    protected void printPrizes(Category category, Document document) throws IOException {
        throw new UnsupportedOperationException();
    }
    public void printCombined() throws IOException {
        throw new UnsupportedOperationException();
    }
    public void printOverallResults() throws IOException {
        throw new UnsupportedOperationException();
    }
    public void printPrizes() throws IOException {
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
}
