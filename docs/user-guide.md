# User Guide

Welcome to the SeqSpectre User Guide!

This guide explains how to use SeqSpectre to analyse FASTQ sequencing data, configure quality control settings and interpret the generated results.

No Java programming knowledge is required to use the application.

For installation instructions, see the [Installation Guide](installation.md).

---

## 1. What does SeqSpectre do?

SeqSpectre is a command-line quality control (QC) application for FASTQ sequencing data.

The application examines sequencing reads and reports:

- **Beginning Quality:** Quality of bases at the beginning of reads.
- **Ending Quality:** Quality of bases at the end of reads.
- **Duplicates:** Percentage of repeated nucleotide sequences.
- **GC Percentage:** Percentage of guanine and cytosine bases.
- **Sequence Length:** Shortest and longest sequencing reads.
- **Passing Reads:** Percentage of reads meeting the quality requirements.
- **Final Status:** An overall PASS or FAIL result.

These measurements help identify potential quality problems before sequencing data is used for further analysis.

## 2. Preparing your input

SeqSpectre accepts the following FASTQ file extensions:

| File extension | Description |
|---|---|
| `.fastq` | Standard FASTQ |
| `.fq` | Alternative FASTQ extension |
| `.fastq.gz` | Gzip-compressed FASTQ |
| `.fq.gz` | Gzip-compressed alternative FASTQ |

A FASTQ record consists of four lines:

```text
@read1
ACGTACGT
+
IIIIIIII
```

The first line identifies the read, the second contains the nucleotide sequence, the third separates sequence and quality, and the fourth contains Phred+33 encoded quality values.

Each nucleotide must have one corresponding quality character.

SeqSpectre can analyse one file or multiple FASTQ files located directly inside a directory.

**Important:** FASTQ files in nested subdirectories are not automatically included.

## 3. Starting SeqSpectre

Open a terminal in the SeqSpectre project directory.

Run a FASTQ analysis with:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq
```

On Windows, the equivalent command is:

```powershell
java -jar build\libs\seqspectre-all.jar -i testdata\test.fastq
```

The `-i` argument identifies the input file or directory.

After processing, SeqSpectre:

1. Displays the selected QC results in the terminal.
2. Saves the results in a tab-separated file.
3. Displays a message identifying the output location.

By default, the output file is named `results.tsv`.

## 4. Analysing multiple samples

To analyse multiple FASTQ files, specify a directory instead of a single file:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata
```

Each detected FASTQ file is analysed separately.

The resulting TSV report contains one row per processed FASTQ file.

Other file types are ignored.

## 5. Choosing your QC settings

SeqSpectre uses default settings when no additional analysis options are provided.

| Setting | Default | Description |
|---|---|---|
| Read-end percentage | 10% | Portion of each read end to analyse |
| Minimum quality score | Q30 | Minimum required Phred score per base |
| Required passing reads | 80% | Minimum percentage of reads passing both quality checks |
| Maximum duplicates | 100% | Maximum allowed percentage of duplicate sequences |
| Minimum length | Not set | Optional minimum read length |

The user can modify these values using command-line arguments.

For example:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --end-percent 20 --min-quality-score 30
```

This analyses 20% of the bases at each end of every read using a minimum Phred quality score of Q30.

### Quality thresholds

A read passes its quality assessment when at least 90% of the analysed bases at **both** read ends meet the chosen Phred threshold.

For the complete FASTQ file to meet the quality requirement, the percentage of passing reads must be at least the value specified by `--required-reads-percent`.

### Duplicate thresholds

SeqSpectre detects duplicate sequences through exact nucleotide sequence matching.

Use:

```bash
--max-duplicate-percent 20
```

to allow no more than 20% duplicate reads.

The default value of 100% means the duplicate threshold does not normally cause a FAIL result.

### Minimum sequence length

Use:

```bash
--min-length 100
```

to exclude reads shorter than 100 bases from subsequent QC calculations.

If all reads are excluded, the analysis stops with an error.

For a complete explanation of every option, including permitted values, see the [CLI Command Reference](commands.md).

## 6. Selecting which results to display

By default, SeqSpectre reports all available QC measurements.

Alternatively, you can select individual categories:

| Option | Displayed information |
|---|---|
| `--quality` | Beginning and ending quality |
| `--duplicates` | Duplicate percentage |
| `--gc` | GC percentage |
| `--length` | Minimum and maximum sequence length |
| `--passing-reads` | Percentage of passing reads |

For example:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --gc --duplicates
```

Only the requested measurements are included, alongside the sample name, file type and final status.

You can combine several display options.

**Note:** Selecting fewer columns does not disable the underlying QC calculations or change the final PASS/FAIL criteria.

## 7. Example: Analysing test.fastq

The repository contains `testdata/test.fastq`, a small FASTQ file containing four sequencing reads.

This file can be used to verify that SeqSpectre is working correctly.

### Step 1 — Run the analysis

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq
```

No additional settings are necessary.

### Step 2 — Open the results

SeqSpectre generates:

```text
results.tsv
```

The file contains the following results from an actual execution of SeqSpectre:

| Measurement | Result |
|---|---|
| Sample | test.fastq |
| Type | FASTQ |
| Beginning Quality | 100.0% |
| Ending Quality | 100.0% |
| Duplicates | 0.0% |
| GC Percentage | 43.75% |
| Minimum Length | 16 |
| Maximum Length | 16 |
| Passing Reads | 100.0% |
| Final Status | PASS |

### Step 3 — Interpret the results

**Beginning Quality — 100.0%**

All bases selected at the beginning of the reads meet the minimum quality threshold.

**Ending Quality — 100.0%**

All bases selected at the end of the reads meet the minimum quality threshold.

**Duplicates — 0.0%**

No duplicate nucleotide sequences were detected.

**GC Percentage — 43.75%**

43.75% of the analysed nucleotides consist of guanine or cytosine.

**Minimum and Maximum Length — 16**

All four reads contain exactly 16 nucleotides.

**Passing Reads — 100.0%**

All four reads pass the quality requirements at both ends.

**Final Status — PASS**

The file meets both the minimum passing-read requirement and the maximum duplicate threshold.

### Understanding the read-end calculation

By default, SeqSpectre analyses 10% of each read end.

For the example file, every read contains 16 nucleotides.

```text
16 × 0.10 = 1.6
```

SeqSpectre rounds this upwards to two bases.

Therefore, the first two and last two bases of every read are analysed independently.

### Important limitation

This sample contains only four reads. It is suitable for demonstrating the application but is too small to provide a representative assessment of biological sequencing quality.

## 8. Understanding PASS and FAIL

SeqSpectre assigns a final status to each sample.

A sample receives **PASS** when:

- the percentage of passing reads is at least the configured minimum; and
- the duplicate percentage is no greater than the configured maximum.

For example, suppose a sample produces:

```text
Passing Reads: 85%
Duplicates: 15%
```

Using:

```text
--required-reads-percent 80
--max-duplicate-percent 20
```

the final status is `PASS`.

If the passing-read percentage decreases below 80%, or the duplicate percentage increases above 20%, the final status is `FAIL`.

The final status reflects SeqSpectre's configured QC requirements. It does not guarantee that the sequencing data is suitable for every downstream biological analysis.

## 9. Understanding the output file

The output is saved as a tab-separated values file (`.tsv`).

A TSV file contains one header row followed by one row per analysed sample. Columns are separated using tab characters.

It can be opened using spreadsheet applications, text editors or data-analysis software such as R.

The following columns are always present:

- `Sample`
- `Type`
- `Final Status`

Additional columns depend on the selected display options.

### Changing the output location

Use `-o` or `--output`:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq -o output/my_results.tsv
```

The specified output file is created after successful processing.

If a file already exists at that location, it is overwritten.

## 10. Common mistakes

### The input file cannot be found

Check that the path is correct and that the file exists.

Relative paths are interpreted from the current terminal working directory.

### No FASTQ files were found

Make sure the specified input file has a supported FASTQ extension, or that the selected directory directly contains supported FASTQ files.

### Invalid quality settings

Check that the chosen percentages and Phred thresholds are within the supported ranges.

### Invalid FASTQ format

Check that each read has four lines, begins with a valid header, and contains equal-length sequence and quality strings.

### No reads remain after filtering

If the minimum length is higher than every read length, the application cannot continue the QC analysis.

Try a lower `--min-length` or use a dataset containing longer reads.

For more troubleshooting information, see the [Troubleshooting Guide](troubleshooting.md).

---

## Further documentation

- [Installation Guide](installation.md)
- [CLI Command Reference](commands.md)
- [Developer Guide](developer-guide.md)
- [Troubleshooting](troubleshooting.md)

[Back to SeqSpectre Home](../README.md)
