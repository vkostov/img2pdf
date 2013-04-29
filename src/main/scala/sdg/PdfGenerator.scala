package sdg

import java.io.{FileOutputStream, File}
import com.itextpdf.text.{Paragraph, Document}
import com.itextpdf.text.pdf.{RandomAccessFileOrArray, PdfWriter}
import com.itextpdf.text.pdf.codec.TiffImage

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

    try {

      val outputFileName = outputDir + File.separator + pdfFileName
      PdfWriter.getInstance(doc, new FileOutputStream(outputFileName))
      doc.open()

      doc.add(new Paragraph("Images for: " + inputDir.getAbsolutePath))

      addImagesToDocument(inputDir, doc)
      outputFileName
    }
    finally {
      doc.close()
    }
  }

  def addImagesToDocument(file: File, pdfDoc: Document): Unit = {
    if (file.isFile)
      addSingleFileToDocument(file, pdfDoc)
    else {
      val children = file.listFiles()
      if (children != null) {
        for (child <- children)
          addImagesToDocument(child, pdfDoc)
      }
    }
  }

  def addSingleFileToDocument(file: File, pdfDoc: Document): Unit = {

    file.getName.toUpperCase match {
      case name if name.endsWith(".JPG") =>
        val image = com.itextpdf.text.Image.getInstance(file.getAbsolutePath)
        pdfDoc.add(new Paragraph("File: " + file.getAbsolutePath))
        pdfDoc.add(image)
        pdfDoc.newPage()

      case name if name.endsWith(".TIF") =>
        val ra = new RandomAccessFileOrArray(file.getAbsolutePath)
        val pages = TiffImage.getNumberOfPages(ra)
        pdfDoc.add(new Paragraph("File: " + file.getAbsolutePath + " pages: " + pages))

        for (i <- 1 until pages) {
          pdfDoc.add(TiffImage.getTiffImage(ra, i))
        }
        pdfDoc.newPage()

      case _ =>
    }
  }

}
