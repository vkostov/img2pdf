package sdg

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.File

/**
 * Created with IntelliJ IDEA.
 * User: vkostov
 * Date: 4/26/13
 * Time: 5:06 PM
 */

@RunWith(classOf[JUnitRunner])
class Img2PdfSuite extends FunSuite {
  import Utils._

  test("/kidstar/caarc/images/ICD110A/20130424/0177 is not envelope dir") {

    assert(isEnvelopeDirectory("/kidstar/caarc/images/ICD110A/20130424/0177") === false)

  }

  test("/kidstar/caarc/images/ICD110A/20130424/0177/0017/1.jpg is an envelope dir") {
    assert(isEnvelopeDirectory("/kidstar/caarc/images/ICD110A/20130424/0177/0017/1.jpg") === true)
  }

  test("/kidstar/caarc/images/ICD110A/20130424/0177/0017 is an envelope dir") {
    assert(isEnvelopeDirectory("/kidstar/caarc/images/ICD110A/20130424/0177/0017") === true)
  }

  test("/kidstar/caarc/images/ICD110A/20130424/0177/0017/ is an envelope dir") {
    assert(isEnvelopeDirectory("/kidstar/caarc/images/ICD110A/20130424/0177/0017/") === true)
  }

  test("Generate PDF generation") {
    val generator = new PdfGenerator(new File("/kidstar/images"), "/tmp", "test.pdf")

    val pdfFileName = generator.generate()

    assert(pdfFileName === "/tmp/test.pdf")
  }

}
