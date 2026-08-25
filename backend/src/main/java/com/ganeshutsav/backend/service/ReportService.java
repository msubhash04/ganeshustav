package com.ganeshutsav.backend.service;

// Project Entities & Repositories
import com.ganeshutsav.backend.entity.Donation;
import com.ganeshutsav.backend.entity.Expense;
import com.ganeshutsav.backend.repository.DonationRepository;
import com.ganeshutsav.backend.repository.ExpenseRepository;

// iText 8 PDF Imports
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell; // Primary 'Cell' used in PDF generation helper methods
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

// Apache POI Excel Imports (Explicitly listed without wildcard to prevent class conflicts)
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// Lombok & Spring
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Java Standard Libraries
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final DonationRepository donationRepository;
    private final ExpenseRepository expenseRepository;

    public byte[] generatePdfReport(LocalDate startDate, LocalDate endDate) throws IOException {
        List<Donation> donations = filterDonations(startDate, endDate);
        List<Expense> expenses = filterExpenses(startDate, endDate);

        BigDecimal totalCollection = donations.stream().map(Donation::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal balance = totalCollection.subtract(totalExpenses);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document doc = new Document(pdfDoc);

        DeviceRgb saffron = new DeviceRgb(255, 153, 51);

        doc.add(new Paragraph("Ganesh Utsav Expense Tracker")
                .setFontSize(20).setBold().setFontColor(saffron).setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph("Income & Expense Statement (Balance Sheet)")
                .setFontSize(13).setTextAlignment(TextAlignment.CENTER));
        String rangeLabel = (startDate != null && endDate != null)
                ? "Period: " + startDate + " to " + endDate : "Period: All time";
        doc.add(new Paragraph(rangeLabel).setTextAlignment(TextAlignment.CENTER).setFontSize(10));
        doc.add(new Paragraph(" "));

        // Summary
        Table summary = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
        summary.setWidth(UnitValue.createPercentValue(100));
        summary.addCell(cell("Total Collection")).addCell(cell("Rs. " + totalCollection));
        summary.addCell(cell("Total Expenses")).addCell(cell("Rs. " + totalExpenses));
        summary.addCell(cell("Balance Remaining")).addCell(cell("Rs. " + balance));
        doc.add(summary);
        doc.add(new Paragraph(" "));

        // Category-wise expense summary
        doc.add(new Paragraph("Category-wise Expense Summary").setBold().setFontSize(13));
        Map<String, BigDecimal> categoryTotals = new TreeMap<>();
        for (Expense e : expenses) {
            categoryTotals.merge(e.getCategory().getLabel(), e.getAmount(), BigDecimal::add);
        }
        Table catTable = new Table(UnitValue.createPercentArray(new float[]{2, 1}));
        catTable.setWidth(UnitValue.createPercentValue(100));
        catTable.addHeaderCell(headerCell("Category")).addHeaderCell(headerCell("Amount (Rs.)"));
        categoryTotals.forEach((cat, amt) -> catTable.addCell(cell(cat)).addCell(cell(amt.toString())));
        doc.add(catTable);
        doc.add(new Paragraph(" "));

        // Donations table
        doc.add(new Paragraph("Donations / Collections").setBold().setFontSize(13));
        Table donTable = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1, 1, 1}));
        donTable.setWidth(UnitValue.createPercentValue(100));
        donTable.addHeaderCell(headerCell("Receipt#")).addHeaderCell(headerCell("Donor"))
                .addHeaderCell(headerCell("Date")).addHeaderCell(headerCell("Mode")).addHeaderCell(headerCell("Amount"));
        for (Donation d : donations) {
            donTable.addCell(cell(d.getReceiptNumber())).addCell(cell(d.getDonorName()))
                    .addCell(cell(d.getDonationDate().toString())).addCell(cell(d.getPaymentMode().toString()))
                    .addCell(cell(d.getAmount().toString()));
        }
        doc.add(donTable);
        doc.add(new Paragraph(" "));

        // Expenses table
        doc.add(new Paragraph("Expenses").setBold().setFontSize(13));
        Table expTable = new Table(UnitValue.createPercentArray(new float[]{2, 1, 1, 1, 1}));
        expTable.setWidth(UnitValue.createPercentValue(100));
        expTable.addHeaderCell(headerCell("Description")).addHeaderCell(headerCell("Category"))
                .addHeaderCell(headerCell("Date")).addHeaderCell(headerCell("Paid To")).addHeaderCell(headerCell("Amount"));
        for (Expense e : expenses) {
            expTable.addCell(cell(e.getDescription())).addCell(cell(e.getCategory().getLabel()))
                    .addCell(cell(e.getExpenseDate().toString())).addCell(cell(e.getPaidTo()))
                    .addCell(cell(e.getAmount().toString()));
        }
        doc.add(expTable);

        doc.close();
        return baos.toByteArray();
    }

//    public byte[] generateExcelReport(LocalDate startDate, LocalDate endDate) throws IOException {
//        List<Donation> donations = filterDonations(startDate, endDate);
//        List<Expense> expenses = filterExpenses(startDate, endDate);
//
//        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
//            CellStyle headerStyle = workbook.createCellStyle();
//            Font headerFont = workbook.createFont();
//            headerFont.setBold(true);
//            headerStyle.setFont(headerFont);
//            headerStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
//            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//
//            // Donations sheet
//            Sheet donSheet = workbook.createSheet("Collections");
//            Row donHeader = donSheet.createRow(0);
//            String[] donCols = {"Receipt No", "Donor Name", "Phone", "Amount", "Payment Mode", "Date"};
//            for (int i = 0; i < donCols.length; i++) {
//                Cell c = (Cell) donHeader.createCell(i);
//                c.setCellValue(donCols[i]);
//                c.setCellStyle(headerStyle);
//            }
//            int r = 1;
//            for (Donation d : donations) {
//                Row row = donSheet.createRow(r++);
//                row.createCell(0).setCellValue(d.getReceiptNumber());
//                row.createCell(1).setCellValue(d.getDonorName());
//                row.createCell(2).setCellValue(d.getPhoneNumber());
//                row.createCell(3).setCellValue(d.getAmount().doubleValue());
//                row.createCell(4).setCellValue(d.getPaymentMode().toString());
//                row.createCell(5).setCellValue(d.getDonationDate().toString());
//            }
//            for (int i = 0; i < donCols.length; i++) donSheet.autoSizeColumn(i);
//
//            // Expenses sheet
//            Sheet expSheet = workbook.createSheet("Expenses");
//            Row expHeader = expSheet.createRow(0);
//            String[] expCols = {"Description", "Category", "Amount", "Paid To", "Payment Mode", "Date"};
//            for (int i = 0; i < expCols.length; i++) {
//                Cell c = (Cell) expHeader.createCell(i);
//                c.setCellValue(expCols[i]);
//                c.setCellStyle(headerStyle);
//            }
//            r = 1;
//            for (Expense e : expenses) {
//                Row row = expSheet.createRow(r++);
//                row.createCell(0).setCellValue(e.getDescription());
//                row.createCell(1).setCellValue(e.getCategory().getLabel());
//                row.createCell(2).setCellValue(e.getAmount().doubleValue());
//                row.createCell(3).setCellValue(e.getPaidTo());
//                row.createCell(4).setCellValue(e.getPaymentMode().toString());
//                row.createCell(5).setCellValue(e.getExpenseDate().toString());
//            }
//            for (int i = 0; i < expCols.length; i++) expSheet.autoSizeColumn(i);
//
//            // Summary sheet
//            Sheet summarySheet = workbook.createSheet("Summary");
//            BigDecimal totalCollection = donations.stream().map(Donation::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
//            BigDecimal totalExpenses = expenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
//            String[][] summaryData = {
//                    {"Total Collection", totalCollection.toString()},
//                    {"Total Expenses", totalExpenses.toString()},
//                    {"Balance Remaining", totalCollection.subtract(totalExpenses).toString()}
//            };
//            for (int i = 0; i < summaryData.length; i++) {
//                Row row = summarySheet.createRow(i);
//                row.createCell(0).setCellValue(summaryData[i][0]);
//                row.createCell(1).setCellValue(Double.parseDouble(summaryData[i][1]));
//            }
//            summarySheet.autoSizeColumn(0);
//
//            workbook.write(baos);
//            return baos.toByteArray();
//        }
//    }

    public byte[] generateExcelReport(LocalDate startDate, LocalDate endDate) throws IOException {
        List<Donation> donations = filterDonations(startDate, endDate);
        List<Expense> expenses = filterExpenses(startDate, endDate);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Donations sheet
            Sheet donSheet = workbook.createSheet("Collections");
            Row donHeader = donSheet.createRow(0);
            String[] donCols = {"Receipt No", "Donor Name", "Phone", "Amount", "Payment Mode", "Date"};
            for (int i = 0; i < donCols.length; i++) {
                org.apache.poi.ss.usermodel.Cell c = donHeader.createCell(i);
                c.setCellValue(donCols[i]);
                c.setCellStyle(headerStyle);
            }
            int r = 1;
            for (Donation d : donations) {
                Row row = donSheet.createRow(r++);
                row.createCell(0).setCellValue(d.getReceiptNumber());
                row.createCell(1).setCellValue(d.getDonorName());
                row.createCell(2).setCellValue(d.getPhoneNumber());
                row.createCell(3).setCellValue(d.getAmount().doubleValue());
                row.createCell(4).setCellValue(d.getPaymentMode().toString());
                row.createCell(5).setCellValue(d.getDonationDate().toString());
            }
            for (int i = 0; i < donCols.length; i++) donSheet.autoSizeColumn(i);

            // Expenses sheet
            Sheet expSheet = workbook.createSheet("Expenses");
            Row expHeader = expSheet.createRow(0);
            String[] expCols = {"Description", "Category", "Amount", "Paid To", "Payment Mode", "Date"};
            for (int i = 0; i < expCols.length; i++) {
                org.apache.poi.ss.usermodel.Cell c = expHeader.createCell(i);
                c.setCellValue(expCols[i]);
                c.setCellStyle(headerStyle);
            }
            r = 1;
            for (Expense e : expenses) {
                Row row = expSheet.createRow(r++);
                row.createCell(0).setCellValue(e.getDescription());
                row.createCell(1).setCellValue(e.getCategory().getLabel());
                row.createCell(2).setCellValue(e.getAmount().doubleValue());
                row.createCell(3).setCellValue(e.getPaidTo());
                row.createCell(4).setCellValue(e.getPaymentMode().toString());
                row.createCell(5).setCellValue(e.getExpenseDate().toString());
            }
            for (int i = 0; i < expCols.length; i++) expSheet.autoSizeColumn(i);

            // Summary sheet
            Sheet summarySheet = workbook.createSheet("Summary");
            BigDecimal totalCollection = donations.stream().map(Donation::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalExpenses = expenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            String[][] summaryData = {
                    {"Total Collection", totalCollection.toString()},
                    {"Total Expenses", totalExpenses.toString()},
                    {"Balance Remaining", totalCollection.subtract(totalExpenses).toString()}
            };
            for (int i = 0; i < summaryData.length; i++) {
                Row row = summarySheet.createRow(i);
                row.createCell(0).setCellValue(summaryData[i][0]);
                row.createCell(1).setCellValue(Double.parseDouble(summaryData[i][1]));
            }
            summarySheet.autoSizeColumn(0);

            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    private List<Donation> filterDonations(LocalDate start, LocalDate end) {
        if (start == null || end == null) return donationRepository.findAll();
        return donationRepository.findByDonationDateBetween(start, end);
    }

    private List<Expense> filterExpenses(LocalDate start, LocalDate end) {
        if (start == null || end == null) return expenseRepository.findAll();
        return expenseRepository.findByExpenseDateBetween(start, end);
    }

    private Cell cell(String text) {
        return new Cell().add(new Paragraph(text == null ? "" : text));
    }

    private Cell headerCell(String text) {
        return new Cell().add(new Paragraph(text).setBold())
                .setBackgroundColor(new DeviceRgb(255, 204, 128));
    }
}
