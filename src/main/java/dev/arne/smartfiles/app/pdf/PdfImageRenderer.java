package dev.arne.smartfiles.app.pdf;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public final class PdfImageRenderer implements Closeable {

    private final PDDocument document;
    private final PDFRenderer renderer;
    private final HashMap<Integer, Image> imageCache = new HashMap<>();
    private final Object renderLock = new Object();

    public PdfImageRenderer(File file) throws IOException {
        document = Loader.loadPDF(file);
        renderer = new PDFRenderer(document);
    }

    public int getNumberOfPages() {
        return document.getNumberOfPages();
    }

    public Image renderPage(int pageIndex) throws IOException {

        if (pageIndex < 0 || pageIndex >= document.getNumberOfPages()) return null;
        synchronized (renderLock) {

            if (imageCache.containsKey(pageIndex)) return imageCache.get(pageIndex);

            final double scale = 1.0;

            float dpi = (float) (150 * scale * 2.0); // multiplier 2.0 for crisp result
            BufferedImage bim = renderer.renderImageWithDPI(pageIndex, dpi, ImageType.RGB);
            var result = SwingFXUtils.toFXImage(bim, null);

            imageCache.put(pageIndex, result);

            return result;
        }
    }

    @Override
    public void close() throws IOException {
        imageCache.clear();
        if (document != null) {
            document.close();
        }
    }
}
