package com.FinancePortal.Financeportalbackend.Test;


import com.FinancePortal.Financeportalbackend.Controller.InvoiceController;
import com.FinancePortal.Financeportalbackend.Model.Invoice;
import com.FinancePortal.Financeportalbackend.Repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InvoiceControllerTest {

    @InjectMocks
    private InvoiceController invoiceController;

    @Mock
    private InvoiceRepository invoiceRepository;

    private Invoice sampleInvoice;

    @BeforeEach
    public void setUp() {
        sampleInvoice = new Invoice("ABC123");
        sampleInvoice.setStudentId("studentId");
        sampleInvoice.setTotalAmount(BigDecimal.valueOf(1000));
        sampleInvoice.setStatus(Invoice.InvoiceStatus.OUTSTANDING);
    }

    @Test
    public void testCreateInvoice() {
        // Prepare request data
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("studentId", "studentId");
        requestData.put("totalAmount", "1000");

        when(invoiceRepository.findByStudentIdAndDueType(anyString(), any(Invoice.DueType.class))).thenReturn(Collections.emptyList());
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(sampleInvoice);

        ResponseEntity<String> response = invoiceController.createInvoice(requestData);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(sampleInvoice.getInvoiceReferenceNumber(), response.getBody());

        verify(invoiceRepository, times(1)).findByStudentIdAndDueType(anyString(), any(Invoice.DueType.class));
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    public void testGetInvoiceByReferenceNumber() {
        when(invoiceRepository.findByInvoiceReferenceNumber(anyString())).thenReturn(Optional.of(sampleInvoice));

        ResponseEntity<Invoice> response = invoiceController.getInvoiceByReferenceNumber("ABC123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleInvoice, response.getBody());

        verify(invoiceRepository, times(1)).findByInvoiceReferenceNumber(anyString());
    }

    @Test
    public void testGetInvoiceByReferenceNumber_notFound() {
        when(invoiceRepository.findByInvoiceReferenceNumber(anyString())).thenReturn(Optional.empty());

        ResponseEntity<Invoice> response = invoiceController.getInvoiceByReferenceNumber("ABC123");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(invoiceRepository, times(1)).findByInvoiceReferenceNumber(anyString());
    }

    @Test
    public void testGetInvoicesByStudentId() {
        List<Invoice> invoices = Arrays.asList(sampleInvoice);
        when(invoiceRepository.findByStudentId(anyString())).thenReturn(invoices);

        ResponseEntity<?> response = invoiceController.getInvoicesByStudentId("studentId");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(invoices, response.getBody());

        verify(invoiceRepository, times(1)).findByStudentId(anyString());
    }

    @Test
    public void testGetInvoicesByStudentId_notFound() {
        when(invoiceRepository.findByStudentId(anyString())).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = invoiceController.getInvoicesByStudentId("studentId");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(invoiceRepository, times(1)).findByStudentId(anyString());
    }

    @Test
    public void testUpdateInvoiceStatus() {
        when(invoiceRepository.findByInvoiceReferenceNumber(anyString())).thenReturn(Optional.of(sampleInvoice));

        ResponseEntity<String> response = invoiceController.updateInvoiceStatus("ABC123");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Invoice status updated to PAID.", response.getBody());

        verify(invoiceRepository, times(1)).findByInvoiceReferenceNumber(anyString());
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    public void testUpdateInvoiceStatus_notFound() {
        when(invoiceRepository.findByInvoiceReferenceNumber(anyString())).thenReturn(Optional.empty());

        ResponseEntity<String> response = invoiceController.updateInvoiceStatus("ABC123");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(invoiceRepository, times(1)).findByInvoiceReferenceNumber(anyString());
    }

    @Test
    public void testUpdateInvoiceStatus_alreadyPaid() {
        sampleInvoice.setStatus(Invoice.InvoiceStatus.PAID);
        when(invoiceRepository.findByInvoiceReferenceNumber(anyString())).thenReturn(Optional.of(sampleInvoice));

        ResponseEntity<String> response = invoiceController.updateInvoiceStatus("ABC123");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invoice status is already PAID.", response.getBody());

        verify(invoiceRepository, times(1)).findByInvoiceReferenceNumber(anyString());
    }

    @Test
    public void testCheckClearanceStatus() {
        List<Invoice> invoices = Arrays.asList(sampleInvoice);
        when(invoiceRepository.findByStudentId(anyString())).thenReturn(invoices);

        ResponseEntity<String> response = invoiceController.checkClearanceStatus("studentId");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("not_clear", response.getBody());

        verify(invoiceRepository, times(1)).findByStudentId(anyString());
    }

    @Test
    public void testCreateLibraryFineInvoice() {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("account", Collections.singletonMap("studentId", "studentId"));
        requestData.put("amount", "1000");

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(sampleInvoice);

        ResponseEntity<Map<String, Object>> response = invoiceController.createLibraryFineInvoice(requestData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Collections.singletonMap("reference", "ABC123"), response.getBody());

        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }
}
