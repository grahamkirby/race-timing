package common;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public abstract class RaceOutput {

    protected static final Font PDF_FONT = FontFactory.getFont(FontFactory.HELVETICA);
    protected  static final Font PDF_BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
    protected  static final Font PDF_BOLD_UNDERLINED_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, Font.DEFAULTSIZE, Font.UNDERLINE);
    protected  static final Font PDF_BOLD_LARGE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
    protected  static final Font PDF_ITALIC_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE);

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
    }

    public void printPrizes() throws IOException {

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

    protected abstract void printPrizes(final Category category, final Document document) throws IOException;

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
