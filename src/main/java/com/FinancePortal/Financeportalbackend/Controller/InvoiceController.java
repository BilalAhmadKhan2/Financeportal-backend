package com.FinancePortal.Financeportalbackend.Controller;

import com.FinancePortal.Financeportalbackend.Model.Invoice;
import com.FinancePortal.Financeportalbackend.Repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;


@RestController
@RequestMapping("/api/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceRepository invoiceRepository;

    private static final SecureRandom RANDOM = new SecureRandom();
    @CrossOrigin(origins = "http://localhost:8080")
    @PostMapping("/create")
    public ResponseEntity<String> createInvoice(@RequestBody Map<String, Object> requestData) {
        String studentId = requestData.get("studentId").toString();
        BigDecimal totalAmount = new BigDecimal(requestData.get("totalAmount").toString());

        // Check if the student already has a TUITION_FEE due type saved
        List<Invoice> existingInvoices = invoiceRepository.findByStudentIdAndDueType(studentId, Invoice.DueType.TUITION_FEE);
        if (!existingInvoices.isEmpty()) {
            return new ResponseEntity<>("Already generated", HttpStatus.CONFLICT);
        }

        String invoiceReferenceNumber = generateUniqueInvoiceReferenceNumber();

        Invoice invoice = new Invoice(invoiceReferenceNumber);
        invoice.setStudentId(studentId);
        invoice.setTotalAmount(totalAmount);
        invoice.setDueType(Invoice.DueType.TUITION_FEE);
        invoice.setDueDate(LocalDate.now().plusDays(30)); // Set due date 30 days from now
        invoice.setStatus(Invoice.InvoiceStatus.OUTSTANDING);

        Invoice savedInvoice = invoiceRepository.save(invoice);
        return new ResponseEntity<>(savedInvoice.getInvoiceReferenceNumber(), HttpStatus.CREATED);
    }
    @CrossOrigin(origins = "http://localhost:3001")
    @GetMapping("/searchbyreferenceNo/{referenceNumber}")
    public ResponseEntity<Invoice> getInvoiceByReferenceNumber(@PathVariable String referenceNumber) {
        Optional<Invoice> invoiceOptional = invoiceRepository.findByInvoiceReferenceNumber(referenceNumber);
        if (invoiceOptional.isPresent()) {
            Invoice invoice = invoiceOptional.get();
            return ResponseEntity.ok().body(invoice);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @CrossOrigin(origins = "http://localhost:3001")
    @GetMapping("/searchbystudentId/{studentId}")
    public ResponseEntity<?> getInvoicesByStudentId(@PathVariable String studentId) {
        List<Invoice> invoices = invoiceRepository.findByStudentId(studentId);
        if (invoices.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(invoices);
    }
    @CrossOrigin(origins = "http://localhost:3001")
    @PutMapping("/updatestatus/{referenceNumber}")
    public ResponseEntity<String> updateInvoiceStatus(@PathVariable String referenceNumber) {
        Optional<Invoice> invoiceOptional = invoiceRepository.findByInvoiceReferenceNumber(referenceNumber);
        if (invoiceOptional.isPresent()) {
            Invoice invoice = invoiceOptional.get();
            if (invoice.getStatus() == Invoice.InvoiceStatus.OUTSTANDING) {
                invoice.setStatus(Invoice.InvoiceStatus.PAID);
                invoiceRepository.save(invoice);
                return ResponseEntity.ok("Invoice status updated to PAID.");
            } else {
                return ResponseEntity.badRequest().body("Invoice status is already PAID.");
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @CrossOrigin(origins = "http://localhost:8080")
    @GetMapping("/check-clearance/{studentId}")
    public ResponseEntity<String> checkClearanceStatus(@PathVariable("studentId") String studentId) {
        List<Invoice> invoices = invoiceRepository.findByStudentId(studentId);

        boolean allPaid = invoices.stream().allMatch(invoice -> invoice.getStatus() == Invoice.InvoiceStatus.PAID);

        if (allPaid) {
            return new ResponseEntity<>("clear", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("not_clear", HttpStatus.OK);
        }
    }

    @CrossOrigin(origins = "http://localhost:8082")
    @PostMapping("/create-library-fine")
    public ResponseEntity<Map<String, Object>> createLibraryFineInvoice(@RequestBody Map<String, Object> requestData) {
        String studentId = requestData.containsKey("account") ? ((Map<String, String>) requestData.get("account")).get("studentId") : null;
        BigDecimal fineAmount = requestData.containsKey("amount") ? new BigDecimal(requestData.get("amount").toString()) : null;

        if (studentId == null || fineAmount == null) {
            return new ResponseEntity<>(Collections.singletonMap("error", "Missing required parameters"), HttpStatus.BAD_REQUEST);
        }

        String invoiceReferenceNumber = generateUniqueInvoiceReferenceNumber();

        Invoice invoice = new Invoice(invoiceReferenceNumber);
        invoice.setStudentId(studentId);
        invoice.setTotalAmount(fineAmount);
        invoice.setDueType(Invoice.DueType.LIBRARY_FINE);
        invoice.setDueDate(LocalDate.now().plusDays(30));
        invoice.setStatus(Invoice.InvoiceStatus.OUTSTANDING);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("reference", savedInvoice.getInvoiceReferenceNumber());

        return ResponseEntity.ok().body(responseMap);
    }








    //unique invoice reference number generation methods
    private String generateUniqueInvoiceReferenceNumber() {
        String referenceNumber;
        do {
            String prefix = generateRandomAlphabetic(3);
            String suffix = generateRandomNumeric(3);
            referenceNumber = prefix + suffix;
        } while (invoiceRepository.findByInvoiceReferenceNumber(referenceNumber).isPresent());

        return referenceNumber;
    }

    private String generateRandomAlphabetic(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char randomChar = (char) ('A' + RANDOM.nextInt(26));
            sb.append(randomChar);
        }
        return sb.toString();
    }

    private String generateRandomNumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomDigit = RANDOM.nextInt(10);
            sb.append(randomDigit);
        }
        return sb.toString();
    }
}
