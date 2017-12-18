package nl.biopet.tools.snptesttovcf

import java.io.File

import nl.biopet.utils.tool.AbstractOptParser

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
