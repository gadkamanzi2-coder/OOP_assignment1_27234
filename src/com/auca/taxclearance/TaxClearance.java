package com.auca.taxclearance;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.InvalidStatusTransitionException;
import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.UnauthorizedApprovalException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.main.ValidationUtil;
import com.auca.revenueofficer.RevenueOfficer;

import java.time.LocalDate;

public class TaxClearance implements Identifiable, Validatable {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String[] STATUS_CHOICES = {STATUS_PENDING, STATUS_APPROVED, STATUS_REJECTED};

    private String clearanceId;
    private RegisteredTaxpayer taxpayer;
    private LocalDate requestDate;
    private LocalDate expiryDate;
    private String status;
    private RevenueOfficer approvedBy;

    public TaxClearance(String clearanceId, RegisteredTaxpayer taxpayer, LocalDate requestDate,
                        LocalDate expiryDate, String status, RevenueOfficer approvedBy) {
        this.clearanceId = ValidationUtil.trim(clearanceId);
        this.taxpayer = taxpayer;
        this.requestDate = requestDate;
        this.expiryDate = expiryDate;
        this.status = ValidationUtil.trim(status);
        this.approvedBy = approvedBy;
    }

    public TaxClearance(String clearanceId, RegisteredTaxpayer taxpayer, LocalDate requestDate,
                        LocalDate expiryDate) {
        this(clearanceId, taxpayer, requestDate, expiryDate, STATUS_PENDING, null);
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public void approve(RevenueOfficer officer) throws ValidationException {
        if (!STATUS_PENDING.equals(status)) {
            throw new InvalidStatusTransitionException(
                    "Only a PENDING clearance can be approved (current status: " + status + ").");
        }
        if (officer == null) {
            throw new UnauthorizedApprovalException("An approving officer is required.");
        }
        if (!officer.isActive()) {
            throw new UnauthorizedApprovalException("The officer is not active and cannot approve.");
        }
        approvedBy = officer;
        status = STATUS_APPROVED;
    }

    public void reject() throws InvalidStatusTransitionException {
        if (!STATUS_PENDING.equals(status)) {
            throw new InvalidStatusTransitionException(
                    "Only a PENDING clearance can be rejected (current status: " + status + ").");
        }
        status = STATUS_REJECTED;
    }

    @Override
    public String getId() {
        return clearanceId;
    }

    @Override
    public String getDisplayName() {
        return "Clearance " + clearanceId;
    }

    @Override
    public boolean isActive() {
        return !STATUS_REJECTED.equals(status);
    }

    @Override
    public void validate() throws ValidationException {
        ValidationUtil.requireText(clearanceId, "Clearance ID", 4, 30);
        if (taxpayer == null) {
            throw new PrerequisiteMissingException("A registered taxpayer is required.");
        }
        ValidationUtil.requireNotFuture(requestDate, "Request date");
        ValidationUtil.requireBefore(requestDate, "Request date", expiryDate, "Expiry date");
        ValidationUtil.requireChoice(status, "Clearance status", STATUS_CHOICES);
        if (STATUS_APPROVED.equals(status) && approvedBy == null) {
            throw new UnauthorizedApprovalException(
                    "An approved clearance must have an approving officer.");
        }
    }

    @Override
    public void printDetails() {
        ValidationUtil.printField("Clearance ID", clearanceId);
        ValidationUtil.printField("Taxpayer", ValidationUtil.describe(taxpayer));
        ValidationUtil.printField("Request date", requestDate);
        ValidationUtil.printField("Expiry date", expiryDate);
        ValidationUtil.printField("Status", status);
        ValidationUtil.printField("Approved by", ValidationUtil.describe(approvedBy));
    }

    @Override
    public String getSummary() {
        return "Clearance | " + clearanceId + " | " + status + " | expires " + expiryDate;
    }

    public String getClearanceId() { return clearanceId; }
    public void setClearanceId(String clearanceId) { this.clearanceId = ValidationUtil.trim(clearanceId); }

    public RegisteredTaxpayer getTaxpayer() { return taxpayer; }
    public void setTaxpayer(RegisteredTaxpayer taxpayer) { this.taxpayer = taxpayer; }

    public LocalDate getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = ValidationUtil.trim(status); }

    public RevenueOfficer getApprovedBy() { return approvedBy; }
    public void setApprovedBy(RevenueOfficer approvedBy) { this.approvedBy = approvedBy; }
}