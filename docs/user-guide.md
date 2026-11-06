# User Guide

Welcome to the SeqSpectre User Guide!

This guide explains how to analyse FASTQ sequencing data, configure QC settings and interpret the generated results.

No Java programming knowledge is required to use SeqSpectre.

[Home](../README.md) · [Installation](installation.md) · [CLI Commands](commands.md) · [Developer Guide](developer-guide.md) · [Troubleshooting](troubleshooting.md) · [Contact & Support](contact.md)

---

## 1. What is SeqSpectre?

SeqSpectre is a command-line quality control application for FASTQ sequencing data.

The application can:

- Count the total number of sequencing reads.
- Analyse Phred quality at both ends of reads.
- Identify exact duplicate sequences.
- Calculate GC percentage.
- Determine minimum and maximum read lengths.
- Filter reads below a specified minimum length.
- Calculate the percentage of reads passing the quality checks.
- Assign a final PASS or FAIL status.
- Generate TSV reports.

## 2. Preparing FASTQ input

Supported file extensions:

| Extension | Description |
|---|---|
| `.fastq` | Standard FASTQ file |
| `.fq` | Alternative FASTQ extension |
| `.fastq.gz` | Gzip-compressed FASTQ |
| `.fq.gz` | Gzip-compressed FASTQ |

Each FASTQ record consists of four lines:

```text
@read1
ACGTACGT
+
IIIIIIII
```

These contain:

1. A header beginning with `@`.
2. The nucleotide sequence.
3. A separator beginning with `+`.
4. A Phred+33 encoded quality string.

The sequence and quality string must have equal lengths.

SeqSpectre accepts one FASTQ file or a directory containing FASTQ files. Directory scanning is not recursive.

## 3. Starting an analysis

After building the executable JAR, open a terminal in the repository's root directory.

On Windows:

```powershell
java -jar build\libs\seqspectre-all.jar -i testdata\test.fastq
```

On macOS/Linux:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq
```

The application:

1. Validates the input path and QC settings.
2. Reads the FASTQ records.
3. Calculates original read counts and lengths.
4. Applies optional minimum-length filtering.
5. Performs the remaining QC analyses.
6. Prints a report in the terminal.
7. Saves the report to a TSV file.

By default, the report is saved as `results.tsv`.

## 4. Analysing a directory

To analyse multiple FASTQ files:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata
```

Each supported FASTQ file directly inside the directory is processed independently.

The generated report contains one row per file.

Unsupported files are ignored.

## 5. Default QC settings

When no additional options are provided, SeqSpectre uses:

| Setting | Default |
|---|---|
| Read-end percentage | 10% |
| Minimum Phred score | Q30 |
| Required passing reads | 80% |
| Maximum duplicate percentage | 100% |
| Minimum read length | No filter |
| Output file | `results.tsv` |
| Report columns | All available measurements |

### Read-end quality

SeqSpectre selects a configurable percentage of bases at the beginning and end of each read.

The number of bases selected at each end is rounded upwards.

For a read containing 16 bases with the default percentage of 10%:

```text
ceil(16 × 10 / 100) = 2 bases
```

The first two and last two bases are analysed independently.

A read passes when at least 90% of the selected bases at both ends individually meet the minimum Phred threshold.

### Required passing reads

The default is:

```text
--required-reads-percent 80
```

At least 80% of the analysed reads must pass both read-end quality checks for the file to satisfy the quality requirement.

### Duplicate threshold

The default is:

```text
--max-duplicate-percent 100
```

This allows any duplicate percentage between 0% and 100%.

Users can apply a stricter threshold, for example:

```text
--max-duplicate-percent 20
```

## 6. Changing QC settings

Example:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --end-percent 20 --min-quality-score 30 --required-reads-percent 90 --max-duplicate-percent 20
```

This command:

- Analyses 20% of each read end.
- Requires bases to meet Q30.
- Requires at least 90% of reads to pass.
- Allows at most 20% duplicate reads.

The complete allowed ranges are documented in the [CLI Command Reference](commands.md).

## 7. Minimum length filtering

The `--min-length` option excludes reads shorter than the specified length.

Example:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --min-length 12 --length
```

When the filter is enabled:

1. SeqSpectre counts all original reads.
2. It determines the original minimum and maximum read lengths.
3. It calculates the percentage of reads shorter than the selected minimum.
4. Reads below the minimum are removed.
5. Quality, duplicate, GC and passing-read calculations use the remaining reads.
6. The reported minimum length is recalculated using the remaining reads.
7. The reported maximum length retains the original maximum.

For example, suppose the original lengths are:

```text
50, 80, 100, 150
```

With `--min-length 90`:

```text
Total Reads: 4
Reads Below Minimum: 50%
Minimum Length: 100
Maximum Length: 150
```

Only the reads of 100 and 150 bases are used for the subsequent QC calculations.

**Important:** `Total Reads` always represents the original read count before filtering. It does not represent the number of reads remaining after filtering.

If no reads remain, the application stops with an error.

## 8. Selecting report measurements

By default, SeqSpectre displays all available QC measurements.

To display only specific measurements, use one or more of these options:

| Option | Result columns |
|---|---|
| `--quality` | Beginning Quality, Ending Quality |
| `--duplicates` | Duplicates |
| `--gc` | GC Percentage |
| `--length` | Minimum Length, Maximum Length |
| `--passing-reads` | Passing Reads |

For example:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --gc --duplicates
```

The following columns are always present, even when output selection is used:

- `Sample`
- `Type`
- `Total Reads`
- `Final Status`

If `--min-length` is specified and length statistics are displayed, the report also contains `% Reads Below Minimum Length`.

Display flags only change the visible columns. They do not disable calculations or change the PASS/FAIL criteria.

## 9. Example: test.fastq

The repository contains:

```text
testdata/test.fastq
```

This file contains four FASTQ records, each with a sequence length of 16 bases.

Run:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq
```

### Expected results

Using the default settings, the current processing and formatting code produces:

| Measurement | Result |
|---|---|
| Sample | test.fastq |
| Type | FASTQ |
| Total Reads | 4 |
| Beginning Quality | 100.00% |
| Ending Quality | 100.00% |
| Duplicates | 0.00% |
| GC Percentage | 43.75% |
| Minimum Length | 16 |
| Maximum Length | 16 |
| Passing Reads | 100.00% |
| Final Status | PASS |

**Total Reads — 4**

The input contains four FASTQ records.

**Beginning Quality — 100.00%**

All selected bases at the beginnings of reads meet the default Q30 threshold.

**Ending Quality — 100.00%**

All selected bases at the ends of reads meet the default Q30 threshold.

**Duplicates — 0.00%**

No exact duplicate nucleotide sequences were found.

**GC Percentage — 43.75%**

43.75% of the sequence characters are G or C.

**Minimum Length — 16**

The shortest read contains 16 bases.

**Maximum Length — 16**

The longest read also contains 16 bases.

**Passing Reads — 100.00%**

All four reads meet the quality requirements at both ends.

**Final Status — PASS**

The file meets both the passing-read and duplicate thresholds.

The example demonstrates application functionality but is not representative of a full sequencing experiment.

## 10. Understanding the output file

SeqSpectre generates a TSV file containing a header row and one row per input sample.

The complete default report contains:

```text
Sample
Type
Total Reads
Beginning Quality
Ending Quality
Duplicates
GC Percentage
Minimum Length
Maximum Length
Passing Reads
Final Status
```

The values are separated by tab characters, not line breaks; the list above shows the column names for readability.

Percentage values are formatted to two decimal places using a decimal point, for example:

```text
100.00%
43.75%
0.00%
```

When a relevant numerical percentage is unavailable, the writer can display `N/A`.

### Custom output location

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq -o reports/qc_results.tsv
```

SeqSpectre attempts to create the parent directory when necessary.

An existing file at the same output path will be overwritten.

TSV files can be opened using text editors, spreadsheet software or data-analysis applications such as R.

## 11. PASS and FAIL

A file receives `PASS` only when:

```text
Passing Reads >= required-reads-percent
AND
Duplicates <= max-duplicate-percent
```

Otherwise, the final status is `FAIL`.

GC percentage and sequence length are not independent PASS/FAIL conditions.

The selected minimum length filters reads but does not directly add another status condition.

## 12. Help and common problems

Display command-line help:

```bash
java -jar build/libs/seqspectre-all.jar --help
```

If an error occurs:

- Confirm that the FASTQ path exists.
- Verify that the input format is supported.
- Check that quality strings and nucleotide sequences have equal lengths.
- Check numerical argument ranges.
- Ensure the output location is writable.
- Check whether minimum-length filtering removes all reads.

See [Troubleshooting](troubleshooting.md) for detailed solutions.

---

[Back to SeqSpectre Home](../README.md)
