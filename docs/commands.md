# CLI Command Reference

This page documents the command-line interface of SeqSpectre, including input options, QC settings, reporting options and accepted values.

[Home](../README.md) · [Installation](installation.md) · [User Guide](user-guide.md) · [Developer Guide](developer-guide.md) · [Troubleshooting](troubleshooting.md) · [Contact & Support](contact.md)

---

## 1. Command Syntax

After building the executable JAR:

```bash
java -jar build/libs/seqspectre-all.jar -i <FASTQ_FILE_OR_DIRECTORY> [OPTIONS]
```

Windows example:

```powershell
java -jar build\libs\seqspectre-all.jar -i testdata\test.fastq
```

The input argument is required for a normal analysis. Optional arguments may be combined.

Paths containing spaces must be enclosed in double quotation marks.

## 2. Complete Command Reference

| Option | Default | Allowed values | Description |
|---|---|---|---|
| `-i`, `--input` | Required | Existing FASTQ file or directory | Input location |
| `-o`, `--output` | `results.tsv` | Writable file path | Output report location |
| `--end-percent` | `10` | 1–50 | Percentage analysed at each read end |
| `--required-reads-percent` | `80` | 0–100 | Minimum percentage of passing reads |
| `--max-duplicate-percent` | `100` | 0–100 | Maximum duplicate percentage |
| `--min-quality-score` | `30` | Integer 0–93 | Minimum Phred score |
| `--min-length` | Not set | Integer ≥1 | Minimum read length |
| `--quality` | Off | Flag | Display read-end quality |
| `--duplicates` | Off | Flag | Display duplicate percentage |
| `--gc` | Off | Flag | Display GC percentage |
| `--length` | Off | Flag | Display sequence length measurements |
| `--passing-reads` | Off | Flag | Display percentage of passing reads |
| `-h`, `--help` | — | Flag | Display command-line help |
| `-V`, `--version` | — | Flag | Request version information |

The application currently has no explicit release version configured in its PicoCLI annotation.

The source code also contains a hidden developer-only option, documented separately at the end of this page.

## 3. Input and Output Options

### `-i`, `--input`

Specifies a single FASTQ file:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq
```

Or a directory:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata
```

Supported formats:

```text
.fastq
.fq
.fastq.gz
.fq.gz
```

Directory scanning is not recursive.

### `-o`, `--output`

Specifies the report location.

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq -o reports/qc.tsv
```

Default:

```text
results.tsv
```

Missing parent directories are created when possible.

Existing output files are overwritten.

## 4. Quality Analysis Options

### `--end-percent`

Controls the percentage of bases analysed independently at the start and end of every read.

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --end-percent 20
```

Allowed values: 1–50, including decimal percentages.

The number of bases per end is calculated as:

```text
ceil(read length × end percentage / 100)
```

For 16-base reads and the default value of 10%:

```text
ceil(16 × 0.10) = 2 bases
```

### `--min-quality-score`

Sets the minimum Phred score for an individual base to meet the quality threshold.

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --min-quality-score 20
```

Allowed values: whole numbers from 0 through 93.

Default: Q30.

A read passes when at least 90% of the selected bases at both ends individually meet the chosen threshold.

The 90% requirement within each read end is fixed in the current implementation.

### `--required-reads-percent`

Sets the minimum percentage of analysed reads that must pass the quality checks.

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --required-reads-percent 90
```

Allowed values: 0–100.

Default: 80.

### `--max-duplicate-percent`

Sets the maximum permitted duplicate percentage.

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --max-duplicate-percent 20
```

Allowed values: 0–100.

Default: 100.

Duplicate sequences are counted using exact nucleotide sequence equality.

Calculation:

```text
Duplicate Percentage =
(Total Analysed Reads - Unique Sequences)
/
Total Analysed Reads
× 100
```

## 5. Minimum Length Filtering

### `--min-length`

Excludes reads shorter than the selected minimum.

Example:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --min-length 12
```

Allowed values: positive integers.

When filtering is enabled:

- `Total Reads` counts the original FASTQ records.
- `% Reads Below Minimum Length` describes the original reads below the threshold.
- `Minimum Length` describes the shortest remaining read.
- `Maximum Length` retains the original maximum read length.
- Quality, duplicate, GC and passing-read percentages use the remaining reads.

If every read is removed, the application reports an error.

### Example

Original read lengths:

```text
50, 80, 100, 150
```

With:

```text
--min-length 90
```

the resulting length statistics are:

```text
Total Reads: 4
Minimum Length: 100
Maximum Length: 150
% Reads Below Minimum Length: 50.00%
```

## 6. Report Selection Flags

| Flag | Displayed measurements |
|---|---|
| `--quality` | Beginning Quality, Ending Quality |
| `--duplicates` | Duplicates |
| `--gc` | GC Percentage |
| `--length` | Minimum Length, Maximum Length |
| `--passing-reads` | Passing Reads |

The following columns are always included:

```text
Sample
Type
Total Reads
Final Status
```

When `--min-length` is supplied, the `% Reads Below Minimum Length` column is included if length output is selected.

If no display selection flag is provided, all measurements are displayed.

### Show all measurements

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq
```

### Show only GC percentage

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --gc
```

### Show quality and duplicate percentage

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --quality --duplicates
```

### Show passing reads and GC percentage

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --passing-reads --gc
```

### Display sequence lengths after filtering

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --min-length 12 --length
```

## 7. Combined Command Examples

### Standard analysis

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq
```

### Stricter QC requirements

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --end-percent 20 --min-quality-score 30 --required-reads-percent 90 --max-duplicate-percent 20
```

### Custom output file

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq -o reports/analysis.tsv
```

### Minimum length and selected output

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --min-length 12 --length --quality --gc
```

### Directory analysis

```bash
java -jar build/libs/seqspectre-all.jar -i testdata -o reports/all_samples.tsv
```

## 8. Output Format

Results are printed to the terminal and saved as TSV.

Percentages are formatted to two decimal places:

```text
100.00%
43.75%
0.00%
```

Read counts and read lengths are displayed as integers.

The default report header is:

```text
Sample    Type    Total Reads    Beginning Quality    Ending Quality    Duplicates    GC Percentage    Minimum Length    Maximum Length    Passing Reads    Final Status
```

The actual TSV separates columns using tab characters.

### Example Result

For `testdata/test.fastq`, the expected values using the current application code are:

| Measurement | Value |
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

## 9. PASS / FAIL Criteria

The sample receives `PASS` when both conditions are satisfied:

```text
Passing Reads >= required-reads-percent

AND

Duplicates <= max-duplicate-percent
```

Otherwise the final status is `FAIL`.

GC percentage and sequence length are not independent status requirements.

## 10. Help and Version

Display built-in help:

```bash
java -jar build/libs/seqspectre-all.jar --help
```

The short form is:

```bash
java -jar build/libs/seqspectre-all.jar -h
```

PicoCLI also recognizes:

```bash
java -jar build/libs/seqspectre-all.jar --version
```

However, a formal release version is not currently configured, so the version output should not be used as the source for the installed Java or Gradle version.

## 11. Developer-Only Option

The code includes a hidden Easter egg:

```text
--bart-mode
```

This flag is intentionally omitted from normal PicoCLI help.

When used in a successful analysis, it activates an additional interactive terminal prompt after the QC report has been written.

Example:

```powershell
java -jar build\libs\seqspectre-all.jar -i testdata\test.fastq --bart-mode
```

The program asks:

```text
Is your name Bart Barnard? (yes/no)
```

If the user answers `yes` or `y`, the program prints a special message.

This option does not affect QC calculations or the TSV report. It is intended as an internal Easter egg rather than a normal quality control feature.

---

[Back to SeqSpectre Home](../README.md)
