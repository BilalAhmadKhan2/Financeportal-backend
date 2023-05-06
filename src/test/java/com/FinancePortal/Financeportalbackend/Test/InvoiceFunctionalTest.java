package com.FinancePortal.Financeportalbackend.Test;

import com.FinancePortal.Financeportalbackend.Controller.InvoiceController;
import com.FinancePortal.Financeportalbackend.Model.Invoice;
import com.FinancePortal.Financeportalbackend.Repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(InvoiceController.class)
public class InvoiceFunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvoiceRepository invoiceRepository;

    private Invoice invoice;

    @BeforeEach
    void setUp() {
        invoice = new Invoice("ABC123");
        invoice.setStudentId("student123");
        invoice.setTotalAmount(new BigDecimal(1000));
        invoice.setDueType(Invoice.DueType.TUITION_FEE);
        invoice.setDueDate(LocalDate.now().plusDays(30));
        invoice.setStatus(Invoice.InvoiceStatus.OUTSTANDING);
    }

    @Test
    void createInvoice() throws Exception {
        when(invoiceRepository.findByStudentIdAndDueType("student123", Invoice.DueType.TUITION_FEE))
                .thenReturn(Collections.emptyList());
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        mockMvc.perform(post("/api/invoice/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\": \"student123\", \"totalAmount\": 1000}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").value("ABC123"));
    }

    @Test
    void getInvoiceByReferenceNumber() throws Exception {
        when(invoiceRepository.findByInvoiceReferenceNumber("ABC123")).thenReturn(Optional.of(invoice));

        mockMvc.perform(get("/api/invoice/searchbyreferenceNo/ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceReferenceNumber").value("ABC123"))
                .andExpect(jsonPath("$.studentId").value("student123"))
                .andExpect(jsonPath("$.totalAmount").value(1000));
    }

    @Test
    void getInvoicesByStudentId() throws Exception {
        when(invoiceRepository.findByStudentId("student123")).thenReturn(Arrays.asList(invoice));

        mockMvc.perform(get("/api/invoice/searchbystudentId/student123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].invoiceReferenceNumber").value("ABC123"))
                .andExpect(jsonPath("$[0].studentId").value("student123"))
                .andExpect(jsonPath("$[0].totalAmount").value(1000));
    }

    @Test
    void updateInvoiceStatus() throws Exception {
        when(invoiceRepository.findByInvoiceReferenceNumber("ABC123")).thenReturn(Optional.of(invoice));

        mockMvc.perform(put("/api/invoice/updatestatus/ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Invoice status updated to PAID."));
    }

    @Test
    void checkClearanceStatus() throws Exception {
        when(invoiceRepository.findByStudentId("student123")).thenReturn(Arrays.asList(invoice));

        mockMvc.perform(get("/api/invoice/check-clearance/student123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("not_clear"));

        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        when(invoiceRepository.findByStudentId("student123")).thenReturn(Arrays.asList(invoice));

        mockMvc.perform(get("/api/invoice/check-clearance/student123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("clear"));
    }

    @Test
    void createLibraryFineInvoice() throws Exception {
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);

        mockMvc.perform(post("/api/invoice/create-library-fine")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"account\": {\"studentId\": \"student123\"}, \"amount\": 500}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reference").value("ABC123"));
    }

    @Test
    void createLibraryFineInvoiceMissingParameters() throws Exception {
        mockMvc.perform(post("/api/invoice/create-library-fine")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"account\": {}, \"amount\": 500}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing required parameters"));
    }
    @Test
    void createInvoiceAlreadyExists() throws Exception {
        when(invoiceRepository.findByStudentIdAndDueType("student123", Invoice.DueType.TUITION_FEE))
                .thenReturn(Arrays.asList(invoice));

        mockMvc.perform(post("/api/invoice/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"studentId\": \"student123\", \"totalAmount\": 1000}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$").value("Already generated"));
    }

    @Test
    void getInvoiceByReferenceNumberNotFound() throws Exception {
        when(invoiceRepository.findByInvoiceReferenceNumber("ABC123")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/invoice/searchbyreferenceNo/ABC123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getInvoicesByStudentIdNotFound() throws Exception {
        when(invoiceRepository.findByStudentId("student123")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/invoice/searchbystudentId/student123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateInvoiceStatusAlreadyPaid() throws Exception {
        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        when(invoiceRepository.findByInvoiceReferenceNumber("ABC123")).thenReturn(Optional.of(invoice));

        mockMvc.perform(put("/api/invoice/updatestatus/ABC123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Invoice status is already PAID."));
    }

    @Test
    void updateInvoiceStatusNotFound() throws Exception {
        when(invoiceRepository.findByInvoiceReferenceNumber("ABC123")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/invoice/updatestatus/ABC123"))
                .andExpect(status().isNotFound());
    }
}

