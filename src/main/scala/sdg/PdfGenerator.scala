package sdg

import java.io.{FileOutputStream, File}
import com.itextpdf.text.{Paragraph, Document}
import com.itextpdf.text.pdf.PdfWriter

/**
 * Created with IntelliJ IDEA.
 * User: vkostov
 * Date: 4/26/13
 * Time: 5:46 PM
 *
 */
class PdfGenerator(inputDir: File, outputDir: String, pdfFileName: String) {

  /**
   * Reads all gif, tif, and jpg files from the inputDir and its subdirectories and adds
   * them into a pdf file
   * @return the absolute path and name of the generated file
   */
  def generate(): String = {
    val doc = new Document
    val outputFileName = outputDir + File.separator + pdfFileName
    PdfWriter.getInstance(doc, new FileOutputStream(outputFileName))
    doc.open()

    doc.add(new Paragraph("Images for: " + inputDir.getAbsolutePath))

    doc.close()

    outputFileName
  }

}
