package com.FinancePortal.Financeportalbackend.Test;

import com.FinancePortal.Financeportalbackend.Model.Invoice;
import com.FinancePortal.Financeportalbackend.Repository.InvoiceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.BDDAssumptions.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class InvoiceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Test
    void testCreateInvoice_success() throws Exception {
        String studentId = "c0000000";
        BigDecimal totalAmount = new BigDecimal("100.00");

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("studentId", studentId);
        requestData.put("totalAmount", totalAmount);

        mockMvc.perform(post("/api/invoice/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestData)))
                .andExpect(status().isCreated());
    }

    @Test
    void testCreateInvoice_alreadyGenerated() throws Exception {
        String studentId = "c5812497";
        BigDecimal totalAmount = new BigDecimal("100.00");

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("studentId", studentId);
        requestData.put("totalAmount", totalAmount);

        // Create an invoice with the same studentId
        Invoice invoice = new Invoice();
        invoice.setStudentId(studentId);
        invoiceRepository.save(invoice);

        mockMvc.perform(post("/api/invoice/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestData)))
                .andExpect(status().isConflict());
    }





    @Test
    void testGetInvoiceByReferenceNumber_success() throws Exception {
        String invoiceReferenceNumber = "OUV238";
        Invoice invoice = new Invoice(invoiceReferenceNumber);
        invoiceRepository.save(invoice);

        mockMvc.perform(get("/api/invoice/searchbyreferenceNo/{referenceNumber}", invoiceReferenceNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.invoiceReferenceNumber").value(invoiceReferenceNumber));
    }

    @Test
    void testGetInvoiceByReferenceNumber_notFound() throws Exception {
        String invoiceReferenceNumber = "ABC123";

        mockMvc.perform(get("/api/invoice/searchbyreferenceNo/{referenceNumber}", invoiceReferenceNumber))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetInvoicesByStudentId_success() throws Exception {
        String studentId = "c2684835";
        Invoice invoice1 = new Invoice();
        invoice1.setStudentId(studentId);
        invoiceRepository.save(invoice1);

        Invoice invoice2 = new Invoice();
        invoice2.setStudentId(studentId);
        invoiceRepository.save(invoice2);

        mockMvc.perform(get("/api/invoice/searchbystudentId/{studentId}", studentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentId").value(studentId))
                .andExpect(jsonPath("$[1].studentId").value(studentId));
    }

    @Test
    void testGetInvoicesByStudentId_notFound() throws Exception {
        String studentId = "c123456";

        mockMvc.perform(get("/api/invoice/searchbystudentId/{studentId}", studentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateInvoiceStatus_success() throws Exception {
        String referenceNumber = "HSZ098";
        Invoice invoice = new Invoice();
        invoice.setInvoiceReferenceNumber(referenceNumber);
        invoice.setStudentId("c3922382");
        invoice.setTotalAmount(new BigDecimal("100.00"));
        invoice.setDueType(Invoice.DueType.TUITION_FEE);
        invoice.setDueDate(LocalDate.now().plusDays(30));
        invoice.setStatus(Invoice.InvoiceStatus.OUTSTANDING);

        Mockito.when(invoiceRepository.findByInvoiceReferenceNumber(referenceNumber)).thenReturn(Optional.of(invoice));
        Mockito.when(invoiceRepository.save(invoice)).thenReturn(invoice);

        mockMvc.perform(put("/api/invoice/updatestatus/{referenceNumber}", referenceNumber))
                .andExpect(status().isOk())
                .andExpect(content().string("Invoice status updated to PAID."));
        assertEquals(Invoice.InvoiceStatus.PAID, invoice.getStatus());
    }


    @Test
    void testUpdateInvoiceStatus_alreadyPaid() throws Exception {
        String referenceNumber = "PNL791";
        Invoice invoice = new Invoice();
        invoice.setInvoiceReferenceNumber(referenceNumber);
        invoice.setStudentId("c9143317");
        invoice.setTotalAmount(new BigDecimal("100.00"));
        invoice.setDueType(Invoice.DueType.TUITION_FEE);
        invoice.setDueDate(LocalDate.now().plusDays(30));
        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        Mockito.when(invoiceRepository.findByInvoiceReferenceNumber(referenceNumber)).thenReturn(Optional.of(invoice));

        MvcResult result = mockMvc.perform(put("/api/invoice/updatestatus/{referenceNumber}", referenceNumber)).andReturn();
        MockHttpServletResponse response = result.getResponse();
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals("Invoice status is already PAID.", response.getContentAsString());
    }




}


