package nl.biopet.tools.snptesttovcf

import java.io.File

case class Args(inputInfo: File = null,
                outputVcf: File = null,
                referenceFasta: File = null,
                contig: String = null)
