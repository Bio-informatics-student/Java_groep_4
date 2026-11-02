# CLI Command Reference

This page documents all available command-line options in SeqSpectre.

[Home](../README.md) · [Installation](installation.md) · [User Guide](user-guide.md) · [Developer Guide](developer-guide.md) · [Troubleshooting](troubleshooting.md)

## 1. Command syntax

After building the executable JAR, run:

```bash
java -jar build/libs/seqspectre-all.jar -i <FASTQ_FILE_OR_DIRECTORY> [OPTIONS]
```

Windows example:

```powershell
java -jar build\libs\seqspectre-all.jar -i testdata\test.fastq
```

The `-i` or `--input` argument is required. Other options are optional and may be combined.

Paths containing spaces must be enclosed in double quotation marks.

## 2. Complete command reference

| Option | Default | Allowed values | Description |
|---|---|---|---|
| `-i`, `--input` | Required | Existing file or directory | Input FASTQ file or folder |
| `-o`, `--output` | `results.tsv` | Writable file path | Location of output report |
| `--end-percent` | `10` | 1–50 | Percentage analysed at each read end |
| `--required-reads-percent` | `80` | 0–100 | Minimum percentage of reads that must pass |
| `--max-duplicate-percent` | `100` | 0–100 | Maximum duplicate percentage allowed |
| `--min-quality-score` | `30` | Integer 0–93 | Minimum Phred score per base |
| `--min-length` | Not set | Integer ≥1 | Exclude reads shorter than this length |
| `--quality` | Off | Flag | Show beginning and ending quality |
| `--duplicates` | Off | Flag | Show duplicate percentage |
| `--gc` | Off | Flag | Show GC percentage |
| `--length` | Off | Flag | Show minimum and maximum read lengths |
| `--passing-reads` | Off | Flag | Show percentage of passing reads |
| `-h`, `--help` | — | Flag | Show command-line help |
| `-V`, `--version` | — | Flag | Request version information; a release version is not currently configured |

The five display flags control which results are shown. They do not disable the underlying calculations.

If none of the display flags is specified, all available measurements are shown.

## 3. Input and output

### `-i` / `--input`

Analyse a single file:

```bash
java -jar build/libs/seqspectre-all.jar --input testdata/test.fastq
```

Analyse all supported FASTQ files directly inside a directory:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata
```

Supported extensions:

- `.fastq`
- `.fq`
- `.fastq.gz`
- `.fq.gz`

Directory scanning is not recursive. FASTQ files inside nested folders are not included.

### `-o` / `--output`

Set the output file:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq -o reports/qc.tsv
```

The default output is `results.tsv`, located in the current working directory.

SeqSpectre attempts to create missing parent directories. If the output file already exists, it is overwritten.

TSV is recommended because the report uses tab-separated values.

## 4. Quality analysis settings

### `--end-percent`

Controls how much of each read end is analysed.

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --end-percent 25
```

The number of bases per end is calculated using:

```text
ceil(read length × percentage / 100)
```

For a read containing 16 bases and the default percentage of 10:

```text
ceil(16 × 0.10) = 2
```

Two bases are analysed at the beginning and two at the end.

Decimal values such as `12.5` are accepted. Use a decimal point instead of a comma.

### `--min-quality-score`

Sets the minimum required Phred score for individual bases.

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --min-quality-score 20
```

The default is Q30.

SeqSpectre uses Phred+33 encoding. Each read end passes when at least 90% of the analysed bases have a score equal to or higher than the selected threshold.

Both ends must pass for the read to pass.

The 90% per-end requirement is fixed in the current implementation.

### `--required-reads-percent`

Determines the minimum percentage of reads that must pass both quality checks.

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --required-reads-percent 95
```

The default is 80%.

A sample meets its quality requirement if the observed percentage of passing reads is at least the specified value.

### `--max-duplicate-percent`

Determines the maximum allowed percentage of duplicate sequences.

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --max-duplicate-percent 10
```

The default is 100%.

Duplicate detection uses exact sequence matching.

Calculation:

```text
Duplicate percentage =
(total reads - unique sequences) / total reads × 100
```

A file fails the duplicate requirement when its duplicate percentage exceeds the specified threshold.

## 5. Sequence length filtering

### `--min-length`

Exclude reads shorter than the specified number of bases.

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --min-length 12
```

When the minimum length is specified:

1. The original minimum and maximum read lengths are calculated.
2. The percentage of reads below the minimum is calculated.
3. Shorter reads are removed.
4. The remaining reads are used for quality, GC, duplicate and passing-read calculations.

Combine `--min-length` with `--length` to display length statistics:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --min-length 12 --length
```

**Important:** All four reads in the example `test.fastq` are 16 bases long. Using `--min-length 100` would remove every read and produce an error.

## 6. Selecting report columns

| Flag | Output columns |
|---|---|
| `--quality` | `Beginning Quality`, `Ending Quality` |
| `--duplicates` | `Duplicates` |
| `--gc` | `GC Percentage` |
| `--length` | `Minimum Length`, `Maximum Length` |
| `--passing-reads` | `Passing Reads` |

When `--min-length` is set and length statistics are displayed, SeqSpectre also includes `% Reads Below Minimum Length`.

The columns `Sample`, `Type` and `Final Status` are always included.

### Show all results

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq
```

### Show only GC percentage

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --gc
```

### Show quality and duplicates

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --quality --duplicates
```

### Show passing reads and GC percentage

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --passing-reads --gc
```

### Show sequence lengths and length filtering information

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --length --min-length 12
```

## 7. Complete examples

### Standard analysis

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq
```

This uses all default QC thresholds and displays all measurements.

### Custom quality thresholds

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --end-percent 20 --min-quality-score 30 --required-reads-percent 90
```

### Limit duplicate percentage

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq --max-duplicate-percent 20
```

### Custom output location

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq -o reports/results.tsv
```

### Combined analysis

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq -o reports/strict.tsv --end-percent 20 --min-quality-score 30 --required-reads-percent 90 --max-duplicate-percent 10 --min-length 12
```

This command changes the quality thresholds, applies a minimum length and writes the results to a custom file.

## 8. Understanding PASS / FAIL

SeqSpectre assigns `PASS` only when both requirements are met:

```text
Passing Reads >= required-reads-percent
AND
Duplicates <= max-duplicate-percent
```

GC percentage and sequence length are descriptive measurements and do not independently determine PASS or FAIL.

The minimum length setting filters reads but does not add a third condition to the final PASS/FAIL calculation.

## 9. Actual test output

Running:

```bash
java -jar build/libs/seqspectre-all.jar -i testdata/test.fastq
```

produced the following results:

| Measurement | Value |
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

The file contains four reads with 16 bases per read.

These results were obtained using SeqSpectre's default settings.

## 10. Running through Gradle

Developers can run the program without manually creating a new JAR.

Windows:

```powershell
.\gradlew.bat run --args="-i testdata/test.fastq --quality --gc"
```

macOS/Linux:

```bash
./gradlew run --args="-i testdata/test.fastq --quality --gc"
```

## 11. Getting help

Show the available command-line options:

```bash
java -jar build/libs/seqspectre-all.jar --help
```

Invalid arguments or values outside supported ranges result in an error.

Examples of invalid values:

```text
--end-percent 0
--min-quality-score 94
--min-length 0
```

For information about resolving errors, see the [Troubleshooting Guide](troubleshooting.md).

---

[Back to SeqSpectre Home](../README.md)
