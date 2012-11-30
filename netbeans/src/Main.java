
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        new Main().run();
    }

    void run() throws IOException {
        PdfReader reader = new PdfReader("test.pdf");
        int numberOfPages = reader.getNumberOfPages();
        for (int i = 1; i <= numberOfPages; i++) {
            Rectangle size = reader.getPageSize(i);
            System.out.println("Page " + i + ": " + size.getWidth() / 72
                    + " x " + size.getHeight() / 72 + " in");
        }
    }
}
