package nl.biopet.tools.snptesttovcf

import java.io.File

import nl.biopet.utils.test.tools.ToolTest
import org.testng.annotations.Test

class SnptestToVcfTest extends ToolTest[Args] {
  def toolCommand: SnptestToVcf.type = SnptestToVcf


  /**
    * Given the simplicity of the tool. Override the min description length.
    * @return minimum amount of description words.
    */
  override def minDescriptionWords = 5

  /**
    * Given the simplicity of the tool. Override the min manual length.
    * @return minimum amount of manual words.
    */
  override def minManualWords: Int = 20


  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      SnptestToVcf.main(Array())
    }
  }

  @Test
  def testSnptest(): Unit = {
    val output = File.createTempFile("test.", ".vcf.gz")
    output.deleteOnExit()
    SnptestToVcf.main(
      Array(
        "--inputInfo",
        resourcePath("/test.snptest"),
        "--outputVcf",
        output.getAbsolutePath,
        "--referenceFasta",
        resourcePath("/fake_chrQ.fa"),
        "--contig",
        "chrQ"
      ))
  }

  @Test
  def testEmptySnptest(): Unit = {
    val output = File.createTempFile("test.", ".vcf.gz")
    output.deleteOnExit()
    SnptestToVcf.main(
      Array(
        "--inputInfo",
        resourcePath("/test.empty.snptest"),
        "--outputVcf",
        output.getAbsolutePath,
        "--referenceFasta",
        resourcePath("/fake_chrQ.fa"),
        "--contig",
        "chrQ"
      ))
  }

}
