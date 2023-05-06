package com.FinancePortal.Financeportalbackend.Repository;

import com.FinancePortal.Financeportalbackend.Model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceReferenceNumber(String referenceNumber);

    List<Invoice> findByStudentId(String studentId);
    List<Invoice> findByStudentIdAndDueType(String studentId, Invoice.DueType dueType);

}
