package com.ecommerce.audit;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.format.DateTimeFormatter;


@Component
public class ExcelAuditWriter {

    private static final String SHEET_NAME =
            "Audit Log";


    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm:ss"
            );


    private final Path auditFile;


    public ExcelAuditWriter(
            @Value("${audit.excel.path:logs/audit-log.xlsx}")
            String auditFilePath) {

        this.auditFile =
                Paths.get(auditFilePath);
    }


    public synchronized void write(
            AuditRecord record) {

        try {

            Path parent =
                    auditFile.getParent();


            if (parent != null) {
                Files.createDirectories(parent);
            }


            Workbook workbook;


            if (Files.exists(auditFile)) {

                try (
                        FileInputStream inputStream =
                                new FileInputStream(
                                        auditFile.toFile()
                                )
                ) {

                    workbook =
                            new XSSFWorkbook(
                                    inputStream
                            );
                }

            } else {

                workbook =
                        new XSSFWorkbook();

                createHeader(workbook);
            }


            Sheet sheet =
                    workbook.getSheet(
                            SHEET_NAME
                    );


            int rowNumber =
                    sheet.getLastRowNum() + 1;


            Row row =
                    sheet.createRow(
                            rowNumber
                    );


            row.createCell(0)
                    .setCellValue(
                            record.timestamp()
                                    .format(FORMATTER)
                    );

            row.createCell(1)
                    .setCellValue(
                            record.actor()
                    );

            row.createCell(2)
                    .setCellValue(
                            record.action()
                    );

            row.createCell(3)
                    .setCellValue(
                            record.entity()
                    );

            row.createCell(4)
                    .setCellValue(
                            record.entityId()
                    );

            row.createCell(5)
                    .setCellValue(
                            record.method()
                    );

            row.createCell(6)
                    .setCellValue(
                            record.details()
                    );

            row.createCell(7)
                    .setCellValue(
                            record.status()
                    );


            try (
                    FileOutputStream outputStream =
                            new FileOutputStream(
                                    auditFile.toFile()
                            )
            ) {

                workbook.write(
                        outputStream
                );
            }


            workbook.close();

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Failed to write audit log",
                    exception
            );
        }
    }


    private void createHeader(
            Workbook workbook) {

        Sheet sheet =
                workbook.createSheet(
                        SHEET_NAME
                );


        Row header =
                sheet.createRow(0);


        header.createCell(0)
                .setCellValue("Timestamp");

        header.createCell(1)
                .setCellValue("Actor");

        header.createCell(2)
                .setCellValue("Action");

        header.createCell(3)
                .setCellValue("Entity");

        header.createCell(4)
                .setCellValue("Entity ID");

        header.createCell(5)
                .setCellValue("Method");

        header.createCell(6)
                .setCellValue("Details");

        header.createCell(7)
                .setCellValue("Status");
    }
}