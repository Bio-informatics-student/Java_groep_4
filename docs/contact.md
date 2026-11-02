# Contact & Support

Thank you for your interest in SeqSpectre!

SeqSpectre is a Java-based bioinformatics application developed to perform quality control on FASTQ sequencing data.

We welcome questions, feedback, bug reports and suggestions for improving the application.

This page explains how to contact the development team and where to find support.

---

## 1. General Questions

Do you have questions about SeqSpectre, its functionality or how to use the application?

Before contacting the development team, we recommend checking the available documentation:

- [Installation Guide](installation.md) — Installing and running SeqSpectre.
- [User Guide](user-guide.md) — Using SeqSpectre and interpreting QC results.
- [CLI Commands](commands.md) — All supported commands, parameters and options.
- [Developer Guide](developer-guide.md) — Project architecture and development.
- [Troubleshooting](troubleshooting.md) — Common problems and solutions.

If your question is not answered in the documentation, you can submit it through [GitHub Issues](https://github.com/Bio-informatics-student/Java_groep_4/issues).

Please provide a clear description of your question so that the development team can understand your request.

---

## 2. Reporting Bugs

Have you encountered an error or unexpected behaviour while using SeqSpectre?

Bug reports help us identify problems and improve the reliability of the application.

### How to report a bug

1. Open the [SeqSpectre Issues page](https://github.com/Bio-informatics-student/Java_groep_4/issues).
2. Select **New issue**.
3. Give your issue a short, descriptive title.
4. Explain what went wrong.
5. Include the information listed below.
6. Submit your report.

### Information to include

When reporting a bug, please provide:

| Information | Description |
|---|---|
| Operating system | Windows, macOS or Linux |
| Java version | Output of `java -version` |
| SeqSpectre command | The exact command used |
| Input format | FASTQ, FQ or gzip-compressed FASTQ |
| Expected behaviour | What you expected to happen |
| Actual behaviour | What happened instead |
| Error message | The complete relevant error message |
| Reproduction steps | Instructions for reproducing the issue |

### Example bug report

**Title:** Error when processing a compressed FASTQ file

**Description:**

SeqSpectre fails when I attempt to analyse a `.fastq.gz` file.

**Command used:**

```bash
java -jar build/libs/seqspectre-all.jar -i sample.fastq.gz
```

**Expected behaviour:**

The application reads the compressed FASTQ file and generates a TSV report.

**Actual behaviour:**

The application terminates with an error before generating the report.

**Additional information:**

- Operating system: Windows
- Java version: Include output of `java -version`
- Steps to reproduce: Run the command above with the affected input file.

This is an illustrative example, not a confirmed bug.

**Important:** Do not upload confidential, personal or sensitive sequencing data to a public GitHub issue. If an example file is needed, use a small synthetic dataset that reproduces the problem.

---

## 3. Feature Requests & Suggestions

We welcome suggestions for new features and improvements.

Examples include:

- Support for additional sequencing file formats.
- Additional quality control measurements.
- Improvements to the command-line interface.
- Alternative output formats.
- Improved reporting and visualization.
- Performance improvements for large datasets.
- Additional configurable QC thresholds.

### How to suggest a feature

1. Open [GitHub Issues](https://github.com/Bio-informatics-student/Java_groep_4/issues).
2. Select **New issue**.
3. Use a descriptive title, such as `Feature request: Add HTML report output`.
4. Explain the proposed functionality.
5. Describe why the feature would be useful.
6. Submit the suggestion.

### Suggested feature request format

**Feature name:**

A short description of the proposed functionality.

**Current limitation:**

What is currently missing or difficult to accomplish?

**Proposed solution:**

Explain how the new functionality could work.

**Benefit:**

Describe how this improvement would help SeqSpectre users.

**Additional information:**

Include relevant examples or references where appropriate.

Submitting a suggestion does not guarantee that it will be implemented.

---

## 4. Development & Contributions

SeqSpectre is available in a public GitHub repository.

Users can:

- View the source code.
- Download the project.
- Review the documentation.
- Report issues.
- Suggest improvements.

The original repository can only be modified directly by users with the necessary GitHub permissions.

If you would like to propose a code change, please open an issue first to discuss the proposed improvement.

External contributions may be reviewed at the discretion of the development team.

The availability of the source code does not automatically grant permission to redistribute or modify it under an open-source license. Refer to the repository's license information for applicable terms.

---

## 5. Contacting the Development Team

SeqSpectre was developed as an educational software project within the Bioinformatics programme at Hanze University of Applied Sciences.

The project is maintained through its GitHub repository.

**Project:** SeqSpectre

**Repository:** [Bio-informatics-student/Java_groep_4](https://github.com/Bio-informatics-student/Java_groep_4)

**Questions, bugs and feature requests:** [GitHub Issues](https://github.com/Bio-informatics-student/Java_groep_4/issues)

Please note that response times may vary. Support and continued development are not guaranteed.

---

## 6. Before Submitting a Request

To help keep the project organized, please:

- Check whether your question is already answered in the documentation.
- Search existing GitHub issues for similar reports.
- Use a clear and descriptive issue title.
- Include sufficient technical information.
- Avoid sharing confidential or sensitive data.
- Keep discussions constructive and relevant to SeqSpectre.

Thank you for helping improve SeqSpectre!

---

[Back to SeqSpectre Home](../README.md)
