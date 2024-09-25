package common.output;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import common.Race;
import common.categories.Category;

import java.io.IOException;

public abstract class RaceOutputPDF extends RaceOutput {

    public RaceOutputPDF(Race race) {
        super(race);
    }

    @Override
    public void printPrizes() throws IOException {

        final PdfWriter writer = new PdfWriter(output_directory_path.resolve(prizes_filename + ".pdf").toString());

        try (final Document document = new Document(new PdfDocument(writer))) {

            final PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            document.add(new Paragraph().setFont(font).setFontSize(24)
                    .add(race_name_for_results + " " + year + " Category Prizes"));

            for (final Category category : race.categories.getPrizeCategoriesInReportOrder())
                printPrizes(document, category);
        }
    }

    protected static void addCategoryHeader(final Category category, final Document document) throws IOException {

        final PdfFont bold_font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        final Paragraph category_header_paragraph = new Paragraph("Category: " + category.getLongName()).setFont(bold_font).setUnderline().setPaddingTop(24);

        document.add(category_header_paragraph);
    }

    protected static void printPrizePDF(final Document document, final String position_string, final String name, final String detail1, final String detail2) throws IOException {

        final PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        final PdfFont bold_font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        final Paragraph paragraph = new Paragraph().setFont(font).setMarginBottom(0);

        paragraph.add(new Text(position_string + ": ").setFont(font));
        paragraph.add(new Text(name).setFont(bold_font));
        paragraph.add(new Text(" (" + detail1 + ") " + detail2).setFont(font));

        document.add(paragraph);
    }

    protected void printPrizes(final Document document, final Category category) throws IOException {
        throw new UnsupportedOperationException();
    }
}
