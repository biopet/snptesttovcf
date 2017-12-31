/*
 * Copyright (c) 2014 Sequencing Analysis Support Core - Leiden University Medical Center
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
import java.util

import htsjdk.variant.variantcontext.{Allele, VariantContextBuilder}
import htsjdk.variant.variantcontext.writer.{
  AsyncVariantContextWriter,
  Options,
  VariantContextWriterBuilder
}
import htsjdk.variant.vcf.{
  VCFHeader,
  VCFHeaderLine,
  VCFHeaderLineType,
  VCFInfoHeaderLine
}
import nl.biopet.utils.ngs.fasta
import nl.biopet.utils.tool.ToolCommand

import scala.collection.JavaConversions._
import scala.io.Source

object SnptestToVcf extends ToolCommand[Args] {
  def emptyArgs: Args = Args()
  def argsParser = new ArgsParser(this)
  def main(args: Array[String]): Unit = {
    val cmdArgs = cmdArrayToArgs(args)

    logger.info("Start")

    val infoIt = Source.fromFile(cmdArgs.inputInfo).getLines()
    val infoHeader = infoIt.find(!_.startsWith("#"))

    infoHeader match {
      case Some(header) => parseLines(header, infoIt, cmdArgs)
      case _ =>
        writeEmptyVcf(cmdArgs.outputVcf, cmdArgs.referenceFasta)
        logger.info("No header and records found in file")
    }

    logger.info("Done")
  }

  def writeEmptyVcf(outputVcf: File, referenceFasta: File): Unit = {
    val vcfHeader = new VCFHeader()
    vcfHeader.setSequenceDictionary(fasta.getCachedDict(referenceFasta))
    val writer = new VariantContextWriterBuilder()
      .setOutputFile(outputVcf)
      .setReferenceDictionary(vcfHeader.getSequenceDictionary)
      .unsetOption(Options.INDEX_ON_THE_FLY)
      .build
    writer.writeHeader(vcfHeader)
    writer.close()
  }

  def parseLines(header: String,
                 lineIt: Iterator[String],
                 cmdArgs: Args): Unit = {
    val headerKeys = header.split(" ")
    val headerMap = headerKeys.zipWithIndex.toMap
    require(headerKeys.size == headerMap.size, "Duplicates header keys found")
    val metaLines = new util.HashSet[VCFHeaderLine]()
    for (key <- headerKeys if key != "rsid"
         if key != "chromosome"
         if key != "position"
         if key != "alleleA"
         if key != "alleleB"
         if key != "alleleA")
      metaLines.add(
        new VCFInfoHeaderLine(s"ST_$key", 1, VCFHeaderLineType.String, ""))

    require(fasta
              .getCachedDict(cmdArgs.referenceFasta)
              .getSequence(cmdArgs.contig) != null,
            s"contig '${cmdArgs.contig}' not found on reference")

    val vcfHeader = new VCFHeader(metaLines)
    vcfHeader.setSequenceDictionary(
      fasta.getCachedDict(cmdArgs.referenceFasta))
    val writer = new AsyncVariantContextWriter(
      new VariantContextWriterBuilder()
        .setOutputFile(cmdArgs.outputVcf)
        .setReferenceDictionary(vcfHeader.getSequenceDictionary)
        .unsetOption(Options.INDEX_ON_THE_FLY)
        .build)
    writer.writeHeader(vcfHeader)

    val infoKeys = for (key <- headerKeys if key != "rsid"
                        if key != "chromosome"
                        if key != "position"
                        if key != "alleleA"
                        if key != "alleleB"
                        if key != "alleleA") yield key

    var counter = 0
    for (line <- lineIt if !line.startsWith("#")) {
      val values = line.split(" ")
      require(values.size == headerKeys.size,
              "Number of values are not the same as number of header keys")
      val alleles = List(Allele.create(values(headerMap("alleleA")), true),
                         Allele.create(values(headerMap("alleleB"))))
      val start = values(headerMap("position")).toLong
      val end = alleles.head.length() + start - 1
      val rsid = values(headerMap("rsid"))
      val builder = (new VariantContextBuilder)
        .chr(cmdArgs.contig)
        .alleles(alleles)
        .start(start)
        .stop(end)
        .noGenotypes()

      val infoBuilder = infoKeys.foldLeft(builder) {
        case (a, b) =>
          a.attribute("ST_" + b, values(headerMap(b)).replaceAll(";", ","))
      }

      writer.add(infoBuilder.id(rsid.replaceAll(";", ",")).make())

      counter += 1
      if (counter % 10000 == 0) logger.info(s"$counter lines processed")
    }

    logger.info(s"$counter lines processed")

    writer.close()

  }

  def descriptionText: String =
    """
      |Converts a SNPTEST file to VCF.
    """.stripMargin

  def manualText: String =
    """
      |
      |This tool converts a SNPTEST file to VCF using a reference fasta.
      |It also needs the contig of the impute file.
    """.stripMargin

  def exampleText: String =
    s"""In order to convert a SNPTEST file to a VCF file:
       |
       |${example("-i",
                  "snptestOutputFile",
                  "-o",
                  "output.vcf",
                  "-R",
                  "reference.fasta",
                  "-c",
                  "contigofimputfile")}
     """.stripMargin
}
