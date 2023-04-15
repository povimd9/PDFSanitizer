package com.blumo.pdfsanitizer;

import java.io.*;
import java.nio.file.Files;
import java.awt.image.BufferedImage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

public class pdfsanitizer {

    public static void main(String[] args) throws IOException {
        String logMsg;

        if (args.length == 0) {
            System.out.println("Error: No PDF file specified");
            System.exit(1);
        }

        String doCleanPdfResults = doCleanPdf(args[0]);
        if (doCleanPdfResults.startsWith("Error")) {
            System.out.println(doCleanPdfResults);
            System.exit(1);
        }
        System.out.println(doCleanPdfResults);
        System.exit(0);
    }

    private static String doCleanPdf(String filename) throws IOException {
        try {
            
            PDDocument document = PDDocument.load(new File(filename));
            String[] imageFileList = new String[document.getNumberOfPages()];
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(
                page, 300, ImageType.RGB);
                String tmpOutputFileName = String.format("%s_clean_page-%d.%s", filename, page + 1, "png");
                ImageIOUtil.writeImage(bim, tmpOutputFileName, 300);
                imageFileList[page] = tmpOutputFileName;
            }
            document.close();
            String outputFilename = filename.replaceAll(".pdf", "_clean.pdf");
            return combineImagesIntoPDF(outputFilename, imageFileList);
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    private static String combineImagesIntoPDF(String pdfPath, String... inputDirsAndFiles) throws IOException {
        try (PDDocument targetDoc = new PDDocument()) {
            for (String input : inputDirsAndFiles) {
                addImageAsNewPage(targetDoc, input);
            }
            targetDoc.save(pdfPath);
            File savedFile = new File(pdfPath);
            String absolutePath = savedFile.getAbsolutePath();
            return "Success: " + absolutePath;
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * @author Muhammed Demirbaş
     * @param doc
     * @param imagePath
     * @throws IOException
     */
    private static void addImageAsNewPage(PDDocument doc, String imagePath) throws IOException {
        try {
            PDImageXObject image          = PDImageXObject.createFromFile(imagePath, doc);
            PDRectangle    pageSize       = PDRectangle.A4;

            int            originalWidth  = image.getWidth();
            int            originalHeight = image.getHeight();
            float          pageWidth      = pageSize.getWidth();
            float          pageHeight     = pageSize.getHeight();
            float          ratio          = Math.min(pageWidth / originalWidth, pageHeight / originalHeight);
            float          scaledWidth    = originalWidth  * ratio;
            float          scaledHeight   = originalHeight * ratio;
            float          x              = (pageWidth  - scaledWidth ) / 2;
            float          y              = (pageHeight - scaledHeight) / 2;

            PDPage         page           = new PDPage(pageSize);
            doc.addPage(page);
            try (PDPageContentStream contents = new PDPageContentStream(doc, page)) {
                contents.drawImage(image, x, y, scaledWidth, scaledHeight);
            }
            Files.deleteIfExists(new File(imagePath).toPath());
        } catch (IOException e) {
            String logMsg = String.format("Error: adding image %s to PDF: %s", imagePath, e.getMessage());
            System.out.println(logMsg);
            throw new IOException(logMsg);
        }
    }
}