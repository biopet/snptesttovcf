/*
 * Copyright (c) 2014 Biopet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.biopet.tools.snptesttovcf

import java.io.File

import nl.biopet.utils.tool.{AbstractOptParser, ToolCommand}

class ArgsParser(toolCommand: ToolCommand[Args])
    extends AbstractOptParser[Args](toolCommand) {
  opt[File]('i', "inputInfo") required () maxOccurs 1 valueName "<file>" action {
    (x, c) =>
      c.copy(inputInfo = x)
  } text "Input info fields"
  opt[File]('o', "outputVcf") required () maxOccurs 1 valueName "<file>" action {
    (x, c) =>
      c.copy(outputVcf = x)
  } text "Output vcf file"
  opt[File]('R', "referenceFasta") required () maxOccurs 1 valueName "<file>" action {
    (x, c) =>
      c.copy(referenceFasta = x)
  } text "reference fasta file"
  opt[String]('c', "contig") required () maxOccurs 1 valueName "<file>" action {
    (x, c) =>
      c.copy(contig = x)
  } text "contig of impute file"
}
