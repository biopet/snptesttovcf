# Manual

This tool converts a SNPTEST file to VCF using a reference fasta. It needs the contig of the impute file.


Example:

```bash
java -jar snptesttovcf-version.jar \
-i snptestoutputfile \
-o output.vcf \ 
-R reference.fasta \
-c contigofimputfile
```