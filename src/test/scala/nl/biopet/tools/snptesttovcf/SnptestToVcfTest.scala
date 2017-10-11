package nl.biopet.tools.snptesttovcf

import nl.biopet.test.BiopetTest
import org.testng.annotations.Test

object SnptestToVcfTest extends BiopetTest {
  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      SnptestToVcf.main(Array())
    }
  }
}
