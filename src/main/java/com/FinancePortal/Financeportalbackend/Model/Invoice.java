package com.FinancePortal.Financeportalbackend.Model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoicedata")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_reference_number", nullable = false, unique = true)
    private String invoiceReferenceNumber;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "due_type", nullable = false)
    private DueType dueType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvoiceStatus status;

    public enum DueType {
        LIBRARY_FINE,
        TUITION_FEE
    }

    public enum InvoiceStatus {
        OUTSTANDING,
        PAID
    }

    public Invoice() {
    }

    public Invoice(String invoiceReferenceNumber) {
        this.invoiceReferenceNumber = invoiceReferenceNumber;
    }

    public String getInvoiceReferenceNumber() {
        return invoiceReferenceNumber;
    }

    public void setInvoiceReferenceNumber(String invoiceReferenceNumber) {
        this.invoiceReferenceNumber = invoiceReferenceNumber;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }



    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public DueType getDueType() {
        return dueType;
    }

    public void setDueType(DueType dueType) {
        this.dueType = dueType;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    // Add getters, setters, toString, equals, and hashCode methods here...
}
