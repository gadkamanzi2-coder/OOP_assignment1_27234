package com.auca.taxassessment;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.InvalidStatusTransitionException;
import com.auca.exceptions.InvalidTaxCalculationException;
import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.main.ValidationUtil;
import com.auca.taxdeclaration.TaxDeclaration;
import com.auca.taxobligation.TaxObligation;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TaxAssessment implements Identifiable, Validatable {

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String[] STATUS_CHOICES = {STATUS_DRAFT, STATUS_CONFIRMED, STATUS_CANCELLED};

    private String assessmentId;
    private TaxDeclaration declaration;
    private BigDecimal assessedTax;
    private BigDecimal adjustmentAmount;
    private LocalDate assessmentDate;
    private String status;

    public TaxAssessment(String assessmentId, TaxDeclaration declaration, BigDecimal assessedTax,
                         BigDecimal adjustmentAmount, LocalDate assessmentDate, String status) {
        this.assessmentId = ValidationUtil.trim(assessmentId);
        this.declaration = declaration;
        this.assessedTax = assessedTax;
        this.adjustmentAmount = adjustmentAmount;
        this.assessmentDate = assessmentDate;
        this.status = ValidationUtil.trim(status);
    }

    public TaxAssessment(String assessmentId, TaxDeclaration declaration, BigDecimal assessedTax,
                         BigDecimal adjustmentAmount, LocalDate assessmentDate) {
        this(assessmentId, declaration, assessedTax, adjustmentAmount, assessmentDate, STATUS_DRAFT);
    }

    public BigDecimal getTotal() {
        BigDecimal tax = (assessedTax == null) ? BigDecimal.ZERO : assessedTax;
        BigDecimal adjustment = (adjustmentAmount == null) ? BigDecimal.ZERO : adjustmentAmount;
        return tax.add(adjustment);
    }

    public TaxObligation getObligation() {
        if (declaration == null) {
            return null;
        }
        return declaration.getObligation();
    }

    public RegisteredTaxpayer getTaxpayer() {
        if (declaration == null) {
            return null;
        }
        return declaration.getTaxpayer();
    }

    public void confirm() throws InvalidStatusTransitionException {
        if (!STATUS_DRAFT.equals(status)) {
            throw new InvalidStatusTransitionException(
                    "Only a DRAFT assessment can be confirmed (current status: " + status + ").");
        }
        status = STATUS_CONFIRMED;
    }

    public void cancel() throws InvalidStatusTransitionException {
        if (!STATUS_DRAFT.equals(status)) {
            throw new InvalidStatusTransitionException(
                    "Only a DRAFT assessment can be cancelled (current status: " + status + ").");
        }
        status = STATUS_CANCELLED;
    }

    @Override
    public String getId() {
        return assessmentId;
    }

    @Override
    public String getDisplayName() {
        return "Assessment " + assessmentId;
    }

    @Override
    public boolean isActive() {
        return !STATUS_CANCELLED.equals(status);
    }

    @Override
    public void validate() throws ValidationException {
        ValidationUtil.requireText(assessmentId, "Assessment ID", 4, 30);
        if (declaration == null) {
            throw new PrerequisiteMissingException("A tax declaration is required.");
        }
        if (TaxDeclaration.STATUS_REJECTED.equals(declaration.getStatus())) {
            throw new ValidationException("A rejected declaration cannot be assessed.");
        }
        ValidationUtil.requireMoney(assessedTax, "Assessed tax");
        ValidationUtil.requireSignedMoney(adjustmentAmount, "Adjustment amount");
        ValidationUtil.requireNotFuture(assessmentDate, "Assessment date");
        if (declaration.getFilingDate() != null
                && assessmentDate.isBefore(declaration.getFilingDate())) {
            throw new ValidationException("Assessment date cannot be before the filing date.");
        }
        ValidationUtil.requireChoice(status, "Assessment status", STATUS_CHOICES);
        if (getTotal().signum() < 0) {
            throw new InvalidTaxCalculationException(
                    "Assessment total (assessed tax + adjustment) cannot be negative.");
        }
    }

    @Override
    public void printDetails() {
        ValidationUtil.printField("Assessment ID", assessmentId);
        ValidationUtil.printField("Declaration", ValidationUtil.describe(declaration));
        ValidationUtil.printField("Assessed tax", assessedTax);
        ValidationUtil.printField("Adjustment amount", adjustmentAmount);
        ValidationUtil.printField("Assessment date", assessmentDate);
        ValidationUtil.printField("Status", status);
    }

    @Override
    public String getSummary() {
        return "Assessment | " + assessmentId + " | " + status + " | total " + getTotal();
    }

    public String getAssessmentId() { return assessmentId; }
    public void setAssessmentId(String assessmentId) { this.assessmentId = ValidationUtil.trim(assessmentId); }

    public TaxDeclaration getDeclaration() { return declaration; }
    public void setDeclaration(TaxDeclaration declaration) { this.declaration = declaration; }

    public BigDecimal getAssessedTax() { return assessedTax; }
    public void setAssessedTax(BigDecimal assessedTax) { this.assessedTax = assessedTax; }

    public BigDecimal getAdjustmentAmount() { return adjustmentAmount; }
    public void setAdjustmentAmount(BigDecimal adjustmentAmount) { this.adjustmentAmount = adjustmentAmount; }

    public LocalDate getAssessmentDate() { return assessmentDate; }
    public void setAssessmentDate(LocalDate assessmentDate) { this.assessmentDate = assessmentDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = ValidationUtil.trim(status); }
}