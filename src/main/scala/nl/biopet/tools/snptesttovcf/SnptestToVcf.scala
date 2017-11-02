package nl.biopet.tools.snptesttovcf

import java.io.File
import java.util

import htsjdk.variant.variantcontext.{Allele, VariantContextBuilder}
import htsjdk.variant.variantcontext.writer.{AsyncVariantContextWriter, Options, VariantContextWriterBuilder}
import htsjdk.variant.vcf.{VCFHeader, VCFHeaderLine, VCFHeaderLineType, VCFInfoHeaderLine}
import nl.biopet.utils.ngs.fasta
import nl.biopet.utils.tool.ToolCommand

import scala.collection.JavaConversions._
import scala.io.Source

object SnptestToVcf extends ToolCommand[Args] {
  def emptyArgs: Args = Args()
  def argsParser = new ArgsParser(toolName)
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

  def parseLines(header: String, lineIt: Iterator[String], cmdArgs: Args): Unit = {
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
      metaLines.add(new VCFInfoHeaderLine(s"ST_$key", 1, VCFHeaderLineType.String, ""))

    require(fasta.getCachedDict(cmdArgs.referenceFasta).getSequence(cmdArgs.contig) != null,
      s"contig '${cmdArgs.contig}' not found on reference")

    val vcfHeader = new VCFHeader(metaLines)
    vcfHeader.setSequenceDictionary(fasta.getCachedDict(cmdArgs.referenceFasta))
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
        case (a, b) => a.attribute("ST_" + b, values(headerMap(b)).replaceAll(";", ","))
      }

      writer.add(infoBuilder.id(rsid.replaceAll(";", ",")).make())

      counter += 1
      if (counter % 10000 == 0) logger.info(s"$counter lines processed")
    }

    logger.info(s"$counter lines processed")

    writer.close()

  }
}
