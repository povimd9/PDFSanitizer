# PDFSanitizer
Convert untrusted PDF documents to trusted Image based PDF documents.

### Introduction
PDFSanitizer is a simple Java class based on Apache PDFBox.

This class takes a file path argument to a untrusted PDF file, and returns a "_clean.pdf" PDF file.

To compose a clean PDF file, the class exports the original PDF pages as images, and imports these images to a new PDF file.
This means the clean pdf will not contain ANY active content.

Note! since the original PDF document is converted to images, all text/form/other content will only show as an image.

### Usage Example
```java
java -jar target\pdfsanitizer-jar-with-dependencies.jar DataMining-ch1.pdf
```
Output:
```java
Success: DataMining-ch1_clean.pdf
```
