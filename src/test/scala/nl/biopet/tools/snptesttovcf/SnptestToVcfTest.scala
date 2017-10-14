package nl.biopet.tools.snptesttovcf

import java.io.File

import nl.biopet.test.BiopetTest
import org.testng.annotations.Test

class SnptestToVcfTest extends BiopetTest {
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
