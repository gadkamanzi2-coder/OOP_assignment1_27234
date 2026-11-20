package com.auca.taxrefund;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.InvalidStatusTransitionException;
import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.UnauthorizedApprovalException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.main.ValidationUtil;
import com.auca.revenueofficer.RevenueOfficer;
import com.auca.taxassessment.TaxAssessment;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TaxRefund implements Identifiable, Validatable {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String[] STATUS_CHOICES = {STATUS_PENDING, STATUS_APPROVED, STATUS_REJECTED};

    private String refundId;
    private RegisteredTaxpayer taxpayer;
    private TaxAssessment assessment;
    private BigDecimal requestedAmount;
    private LocalDate requestDate;
    private String status;
    private RevenueOfficer approvedBy;

    public TaxRefund(String refundId, RegisteredTaxpayer taxpayer, TaxAssessment assessment,
                     BigDecimal requestedAmount, LocalDate requestDate, String status,
                     RevenueOfficer approvedBy) {
        this.refundId = ValidationUtil.trim(refundId);
        this.taxpayer = taxpayer;
        this.assessment = assessment;
        this.requestedAmount = requestedAmount;
        this.requestDate = requestDate;
        this.status = ValidationUtil.trim(status);
        this.approvedBy = approvedBy;
    }

    public TaxRefund(String refundId, RegisteredTaxpayer taxpayer, TaxAssessment assessment,
                     BigDecimal requestedAmount, LocalDate requestDate) {
        this(refundId, taxpayer, assessment, requestedAmount, requestDate, STATUS_PENDING, null);
    }

    public void approve(RevenueOfficer officer) throws ValidationException {
        if (!STATUS_PENDING.equals(status)) {
            throw new InvalidStatusTransitionException(
                    "Only a PENDING refund can be approved (current status: " + status + ").");
        }
        if (officer == null) {
            throw new UnauthorizedApprovalException("An approving officer is required.");
        }
        if (!officer.isActive()) {
            throw new UnauthorizedApprovalException("The officer is not active and cannot approve.");
        }
        if (!officer.canApprove(requestedAmount)) {
            throw new UnauthorizedApprovalException("The refund " + requestedAmount
                    + " is above the officer's approval limit " + officer.getApprovalLimit() + ".");
        }
        approvedBy = officer;
        status = STATUS_APPROVED;
    }

    public void reject() throws InvalidStatusTransitionException {
        if (!STATUS_PENDING.equals(status)) {
            throw new InvalidStatusTransitionException(
                    "Only a PENDING refund can be rejected (current status: " + status + ").");
        }
        status = STATUS_REJECTED;
    }

    @Override
    public String getId() {
        return refundId;
    }

    @Override
    public String getDisplayName() {
        return "Refund " + refundId;
    }

    @Override
    public boolean isActive() {
        return !STATUS_REJECTED.equals(status);
    }

    @Override
    public void validate() throws ValidationException {
        ValidationUtil.requireText(refundId, "Refund ID", 4, 30);
        if (taxpayer == null) {
            throw new PrerequisiteMissingException("A registered taxpayer is required.");
        }
        if (assessment == null) {
            throw new PrerequisiteMissingException("A supporting tax assessment is required.");
        }
        if (assessment.getTaxpayer() != taxpayer) {
            throw new ValidationException("The assessment does not belong to this taxpayer.");
        }
        ValidationUtil.requirePositiveMoney(requestedAmount, "Requested amount");
        ValidationUtil.requireNotFuture(requestDate, "Request date");
        ValidationUtil.requireChoice(status, "Refund status", STATUS_CHOICES);
        if (STATUS_APPROVED.equals(status) && approvedBy == null) {
            throw new UnauthorizedApprovalException("An approved refund must have an approving officer.");
        }
    }

    @Override
    public void printDetails() {
        ValidationUtil.printField("Refund ID", refundId);
        ValidationUtil.printField("Taxpayer", ValidationUtil.describe(taxpayer));
        ValidationUtil.printField("Assessment", ValidationUtil.describe(assessment));
        ValidationUtil.printField("Requested amount", requestedAmount);
        ValidationUtil.printField("Request date", requestDate);
        ValidationUtil.printField("Status", status);
        if (approvedBy != null) {
            ValidationUtil.printField("Approved by", ValidationUtil.describe(approvedBy));
        }
    }

    @Override
    public String getSummary() {
        return "Refund | " + refundId + " | " + status + " | " + requestedAmount;
    }

    public String getRefundId() { return refundId; }
    public void setRefundId(String refundId) { this.refundId = ValidationUtil.trim(refundId); }

    public RegisteredTaxpayer getTaxpayer() { return taxpayer; }
    public void setTaxpayer(RegisteredTaxpayer taxpayer) { this.taxpayer = taxpayer; }

    public TaxAssessment getAssessment() { return assessment; }
    public void setAssessment(TaxAssessment assessment) { this.assessment = assessment; }

    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }

    public LocalDate getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = ValidationUtil.trim(status); }

    public RevenueOfficer getApprovedBy() { return approvedBy; }
    public void setApprovedBy(RevenueOfficer approvedBy) { this.approvedBy = approvedBy; }
}