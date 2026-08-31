package com.ganeshutsav.backend.service;

// Project Entities & Repositories
import com.ganeshutsav.backend.dto.FestivalAuditReportDTO;
import com.ganeshutsav.backend.entity.AnnadanamSponsor;
import com.ganeshutsav.backend.entity.AuctionItem;
import com.ganeshutsav.backend.entity.Donation;
import com.ganeshutsav.backend.entity.Expense;
import com.ganeshutsav.backend.entity.ExpenseCategory;
import com.ganeshutsav.backend.entity.FestivalYear;
import com.ganeshutsav.backend.entity.GeneralSponsor;
import com.ganeshutsav.backend.repository.AnnadanamSponsorRepository;
import com.ganeshutsav.backend.repository.AuctionItemRepository;
import com.ganeshutsav.backend.repository.DonationRepository;
import com.ganeshutsav.backend.repository.ExpenseRepository;
import com.ganeshutsav.backend.repository.GeneralSponsorRepository;
import com.ganeshutsav.backend.security.TenantContext;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final DonationRepository donationRepository;
    private final ExpenseRepository expenseRepository;
    private final AuctionItemRepository auctionItemRepository;
    private final GeneralSponsorRepository generalSponsorRepository;
    private final AnnadanamSponsorRepository annadanamSponsorRepository;
    private final FestivalYearGuard festivalYearGuard;
    private final TenantContext tenantContext;

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

    // MULTI-TENANT SAFETY: always scoped to the caller's own committee via
    // TenantContext - never returns another committee's donations, whether
    // or not a date range was given (the old findAll() fallback here was a
    // real cross-tenant data leak before this fix).
    private List<Donation> filterDonations(LocalDate start, LocalDate end) {
        Long committeeId = tenantContext.requireCommitteeId();
        if (start == null || end == null) return donationRepository.findByCommitteeIdOrderByDonationDateDesc(committeeId);
        return donationRepository.findByCommitteeIdAndDonationDateBetween(committeeId, start, end);
    }

    private List<Expense> filterExpenses(LocalDate start, LocalDate end) {
        Long committeeId = tenantContext.requireCommitteeId();
        if (start == null || end == null) return expenseRepository.findByCommitteeIdOrderByExpenseDateDesc(committeeId);
        return expenseRepository.findByCommitteeIdAndExpenseDateBetween(committeeId, start, end);
    }

    private Cell cell(String text) {
        return new Cell().add(new Paragraph(text == null ? "" : text));
    }

    private Cell headerCell(String text) {
        return new Cell().add(new Paragraph(text).setBold())
                .setBackgroundColor(new DeviceRgb(255, 204, 128));
    }

    // ============================================================
    // Festival Archives - detailed audit report for ONE festival
    // year (active or archived). Read-only: does NOT go through
    // FestivalYearGuard's active-only checks, since browsing a past
    // year's full ledger is exactly the point - only ownership
    // (loadOwned) is verified, via the same guard every other
    // festival-year-scoped service already uses.
    // ============================================================
    public FestivalAuditReportDTO.Response generateFestivalAuditReport(Long festivalYearId) {
        FestivalYear year = festivalYearGuard.loadOwned(festivalYearId);
        Long yearId = year.getId();

        List<Donation> donations = donationRepository.findByFestivalYearIdOrderByDonationDateDesc(yearId);
        List<Expense> expenses = expenseRepository.findByFestivalYearIdOrderByExpenseDateDesc(yearId);
        List<AuctionItem> auctionItems = auctionItemRepository.findByFestivalYearIdOrderByDayNumberAsc(yearId);
        List<GeneralSponsor> generalSponsors = generalSponsorRepository.findByFestivalYearIdOrderByCreatedAtDesc(yearId);
        List<AnnadanamSponsor> annadanamSponsors = annadanamSponsorRepository.findByFestivalYearIdOrderByDayNumberAsc(yearId);

        BigDecimal totalCollections = donationRepository.getTotalCollectionByFestivalYear(yearId);
        BigDecimal totalExpenses = expenseRepository.getTotalExpensesByFestivalYear(yearId);
        BigDecimal totalAuctionEarnings = auctionItemRepository.getTotalAuctionAmount(yearId);
        BigDecimal generalSponsorshipTotal = generalSponsorRepository.getTotalContributionByFestivalYear(yearId);
        BigDecimal annadanamSponsorshipTotal = annadanamSponsorRepository.getTotalContributionByFestivalYear(yearId);
        BigDecimal totalSponsorships = generalSponsorshipTotal.add(annadanamSponsorshipTotal);
        BigDecimal carryForward = year.getCarryForwardBalance() != null ? year.getCarryForwardBalance() : BigDecimal.ZERO;

        // carryForward + collections + sponsorships + auction earnings - expenses
        BigDecimal netSurplusOrDeficit = carryForward
                .add(totalCollections)
                .add(totalSponsorships)
                .add(totalAuctionEarnings)
                .subtract(totalExpenses);

        Map<String, BigDecimal> expenseByCategory = new LinkedHashMap<>();
        for (ExpenseRepository.CategoryTotal ct : expenseRepository.getCategoryWiseTotalsByFestivalYear(yearId)) {
            ExpenseCategory category = ct.getCategory();
            expenseByCategory.put(category != null ? category.getLabel() : "Uncategorized", ct.getTotal());
        }

        return FestivalAuditReportDTO.Response.builder()
                .festivalYearId(year.getId())
                .label(year.getLabel())
                .year(year.getYear())
                .startDate(year.getStartDate())
                .durationDays(year.getDurationDays())
                .active(year.isActive())
                .carryForwardBalance(carryForward)
                .totalCollections(totalCollections)
                .totalExpenses(totalExpenses)
                .totalSponsorships(totalSponsorships)
                .totalAuctionEarnings(totalAuctionEarnings)
                .netSurplusOrDeficit(netSurplusOrDeficit)
                .expenseByCategory(expenseByCategory)
                .generalSponsorshipTotal(generalSponsorshipTotal)
                .annadanamSponsorshipTotal(annadanamSponsorshipTotal)
                .donations(donations.stream().map(d -> FestivalAuditReportDTO.LedgerDonationDTO.builder()
                        .receiptNumber(d.getReceiptNumber())
                        .donorName(d.getDonorName())
                        .phoneNumber(d.getPhoneNumber())
                        .amount(d.getAmount())
                        .paymentMode(d.getPaymentMode() != null ? d.getPaymentMode().name() : null)
                        .donationDate(d.getDonationDate())
                        .recordedByName(d.getRecordedBy() != null ? d.getRecordedBy().getName() : null)
                        .build()).collect(Collectors.toList()))
                .expenses(expenses.stream().map(e -> FestivalAuditReportDTO.LedgerExpenseDTO.builder()
                        .description(e.getDescription())
                        .category(e.getCategory() != null ? e.getCategory().getLabel() : null)
                        .amount(e.getAmount())
                        .paidTo(e.getPaidTo())
                        .expenseDate(e.getExpenseDate())
                        .dayNumber(e.getDayNumber())
                        .recordedByName(e.getRecordedBy() != null ? e.getRecordedBy().getName() : null)
                        .build()).collect(Collectors.toList()))
                .auctionItems(auctionItems.stream().map(a -> FestivalAuditReportDTO.LedgerAuctionItemDTO.builder()
                        .dayNumber(a.getDayNumber())
                        .itemName(a.getItemName())
                        .winnerName(a.getWinnerName())
                        .bidAmount(a.getBidAmount())
                        .paymentStatus(a.getPaymentStatus() != null ? a.getPaymentStatus().name() : null)
                        .recordedByName(a.getRecordedBy() != null ? a.getRecordedBy().getName() : null)
                        .build()).collect(Collectors.toList()))
                .generalSponsors(generalSponsors.stream().map(s -> FestivalAuditReportDTO.LedgerGeneralSponsorDTO.builder()
                        .sponsorName(s.getSponsorName())
                        .categoryName(s.getCategory() != null ? s.getCategory().getName() : null)
                        .contributionAmount(s.getContributionAmount())
                        .contactInfo(s.getContactInfo())
                        .build()).collect(Collectors.toList()))
                .annadanamSponsors(annadanamSponsors.stream().map(s -> FestivalAuditReportDTO.LedgerAnnadanamSponsorDTO.builder()
                        .sponsorName(s.getSponsorName())
                        .dayNumber(s.getDayNumber())
                        .mealSlot(s.getMealSlot())
                        .contributionAmount(s.getContributionAmount())
                        .contactInfo(s.getContactInfo())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
