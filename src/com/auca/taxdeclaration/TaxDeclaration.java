package com.auca.taxdeclaration;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.InvalidStatusTransitionException;
import com.auca.exceptions.InvalidTaxCalculationException;
import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.main.ValidationUtil;
import com.auca.taxobligation.TaxObligation;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TaxDeclaration implements Identifiable, Validatable {

    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_LATE = "LATE";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_ASSESSED = "ASSESSED";
    public static final String[] STATUS_CHOICES = {STATUS_SUBMITTED, STATUS_LATE,
            STATUS_REJECTED, STATUS_ASSESSED};

    private String declarationId;
    private TaxObligation obligation;
    private LocalDate filingDate;
    private BigDecimal taxableAmount;
    private BigDecimal declaredTax;
    private String status;

    public TaxDeclaration(String declarationId, TaxObligation obligation, LocalDate filingDate,
                          BigDecimal taxableAmount, BigDecimal declaredTax, String status) {
        this.declarationId = ValidationUtil.trim(declarationId);
        this.obligation = obligation;
        this.filingDate = filingDate;
        this.taxableAmount = taxableAmount;
        this.declaredTax = declaredTax;
        this.status = ValidationUtil.trim(status);
    }

    public TaxDeclaration(String declarationId, TaxObligation obligation, LocalDate filingDate,
                          BigDecimal taxableAmount, BigDecimal declaredTax) {
        this(declarationId, obligation, filingDate, taxableAmount, declaredTax,
                initialStatus(obligation, filingDate));
    }

    private static String initialStatus(TaxObligation obligation, LocalDate filingDate) {
        if (obligation != null && obligation.getDueDate() != null && filingDate != null
                && filingDate.isAfter(obligation.getDueDate())) {
            return STATUS_LATE;
        }
        return STATUS_SUBMITTED;
    }

    public boolean isLate() {
        return obligation != null && obligation.getDueDate() != null && filingDate != null
                && filingDate.isAfter(obligation.getDueDate());
    }

    public BigDecimal getExpectedTax() {
        if (obligation == null || obligation.getTaxType() == null) {
            return BigDecimal.ZERO;
        }
        return obligation.getTaxType().calculateTax(taxableAmount);
    }

    public RegisteredTaxpayer getTaxpayer() {
        if (obligation == null) {
            return null;
        }
        return obligation.getTaxpayer();
    }

    public void reject() throws InvalidStatusTransitionException {
        if (STATUS_REJECTED.equals(status)) {
            throw new InvalidStatusTransitionException("The declaration is already REJECTED.");
        }
        if (STATUS_ASSESSED.equals(status)) {
            throw new InvalidStatusTransitionException("An ASSESSED declaration cannot be rejected.");
        }
        status = STATUS_REJECTED;
    }

    public void markAssessed() throws InvalidStatusTransitionException {
        if (STATUS_REJECTED.equals(status)) {
            throw new InvalidStatusTransitionException("A rejected declaration cannot be assessed.");
        }
        if (STATUS_ASSESSED.equals(status)) {
            throw new InvalidStatusTransitionException("The declaration is already ASSESSED.");
        }
        status = STATUS_ASSESSED;
    }

    @Override
    public String getId() {
        return declarationId;
    }

    @Override
    public String getDisplayName() {
        return "Declaration " + declarationId;
    }

    // A declaration is active unless it was rejected.
    @Override
    public boolean isActive() {
        return !STATUS_REJECTED.equals(status);
    }

    @Override
    public void validate() throws ValidationException {
        ValidationUtil.requireText(declarationId, "Declaration ID", 4, 30);
        if (obligation == null) {
            throw new PrerequisiteMissingException("A tax obligation is required.");
        }
        ValidationUtil.requireNotFuture(filingDate, "Filing date");
        ValidationUtil.requireMoney(taxableAmount, "Taxable amount");
        ValidationUtil.requireMoney(declaredTax, "Declared tax");
        ValidationUtil.requireChoice(status, "Declaration status", STATUS_CHOICES);

        if (isLate() && STATUS_SUBMITTED.equals(status)) {
            throw new ValidationException("A declaration filed after the due date must be LATE.");
        }
        if (!isLate() && STATUS_LATE.equals(status)) {
            throw new ValidationException("A declaration filed on time cannot be LATE.");
        }

        BigDecimal expected = getExpectedTax();
        BigDecimal difference = declaredTax.subtract(expected).abs();
        if (difference.compareTo(new BigDecimal("0.01")) > 0) {
            throw new InvalidTaxCalculationException("Declared tax " + declaredTax
                    + " does not match the expected tax " + expected + ".");
        }
    }

    @Override
    public void printDetails() {
        ValidationUtil.printField("Declaration ID", declarationId);
        ValidationUtil.printField("Obligation", ValidationUtil.describe(obligation));
        ValidationUtil.printField("Filing date", filingDate);
        ValidationUtil.printField("Taxable amount", taxableAmount);
        ValidationUtil.printField("Declared tax", declaredTax);
        ValidationUtil.printField("Status", status);
    }

    @Override
    public String getSummary() {
        return "Declaration | " + declarationId + " | " + status + " | filed " + filingDate
                + " | tax " + declaredTax;
    }

    public String getDeclarationId() { return declarationId; }
    public void setDeclarationId(String declarationId) { this.declarationId = ValidationUtil.trim(declarationId); }

    public TaxObligation getObligation() { return obligation; }
    public void setObligation(TaxObligation obligation) { this.obligation = obligation; }

    public LocalDate getFilingDate() { return filingDate; }
    public void setFilingDate(LocalDate filingDate) { this.filingDate = filingDate; }

    public BigDecimal getTaxableAmount() { return taxableAmount; }
    public void setTaxableAmount(BigDecimal taxableAmount) { this.taxableAmount = taxableAmount; }

    public BigDecimal getDeclaredTax() { return declaredTax; }
    public void setDeclaredTax(BigDecimal declaredTax) { this.declaredTax = declaredTax; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = ValidationUtil.trim(status); }
}