package notai.pdf.result;

import java.io.File;

public record PdfSaveResult(
        String pdfName,
        String pdfUrl,
        File pdf,
        Integer totalPages
) {
    public static PdfSaveResult of(
            String pdfName, File pdf, Integer totalPages
    ) {
        return new PdfSaveResult(pdfName, convertPdfUrl(pdfName), pdf, totalPages);
    }

    private static String convertPdfUrl(String pdfName) {
        return String.format("pdf/%s", pdfName);
    }
}
