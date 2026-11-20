package com.auca.taxobligation;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.InvalidTaxPeriodException;
import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.main.ValidationUtil;
import com.auca.taxtype.TaxType;

import java.time.LocalDate;

public class TaxObligation implements Identifiable, Validatable {

    private String obligationId;
    private RegisteredTaxpayer taxpayer;
    private TaxType taxType;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate dueDate;

    public TaxObligation(String obligationId, RegisteredTaxpayer taxpayer, TaxType taxType,
                         LocalDate periodStart, LocalDate periodEnd, LocalDate dueDate) {
        this.obligationId = ValidationUtil.trim(obligationId);
        this.taxpayer = taxpayer;
        this.taxType = taxType;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.dueDate = dueDate;
    }


    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now());
    }

    public boolean overlaps(TaxObligation other) {
        if (other == null || other == this) {
            return false;
        }
        if (taxpayer != other.taxpayer || taxType != other.taxType) {
            return false;
        }
        return !periodStart.isAfter(other.periodEnd) && !other.periodStart.isAfter(periodEnd);
    }

    @Override
    public String getId() {
        return obligationId;
    }

    @Override
    public String getDisplayName() {
        String typeName = (taxType == null) ? "?" : taxType.getTaxName();
        return typeName + " " + periodStart + " to " + periodEnd;
    }

    @Override
    public boolean isActive() {
        return taxpayer != null && taxType != null;
    }

    @Override
    public void validate() throws ValidationException {
        ValidationUtil.requireText(obligationId, "Obligation ID", 4, 30);
        if (taxpayer == null) {
            throw new PrerequisiteMissingException("A registered taxpayer is required.");
        }
        if (!taxpayer.isActive()) {
            throw new ValidationException("The taxpayer must be ACTIVE to have an obligation.");
        }
        if (taxType == null) {
            throw new PrerequisiteMissingException("A tax type is required.");
        }
        if (!taxType.isActive()) {
            throw new ValidationException("The tax type must be active.");
        }
        ValidationUtil.requireDate(periodStart, "Period start");
        ValidationUtil.requireDate(periodEnd, "Period end");
        ValidationUtil.requireDate(dueDate, "Due date");
        if (periodStart.isAfter(periodEnd)) {
            throw new InvalidTaxPeriodException("Period start must be on or before period end.");
        }
        if (dueDate.isBefore(periodEnd)) {
            throw new InvalidTaxPeriodException("Due date must not be before the period end.");
        }
    }

    @Override
    public void printDetails() {
        ValidationUtil.printField("Obligation ID", obligationId);
        ValidationUtil.printField("Taxpayer", ValidationUtil.describe(taxpayer));
        ValidationUtil.printField("Tax type", ValidationUtil.describe(taxType));
        ValidationUtil.printField("Period start", periodStart);
        ValidationUtil.printField("Period end", periodEnd);
        ValidationUtil.printField("Due date", dueDate);
    }

    @Override
    public String getSummary() {
        String owner = (taxpayer == null) ? "?" : taxpayer.getDisplayName();
        return "Obligation | " + obligationId + " | " + owner + " | " + getDisplayName()
                + " | due " + dueDate;
    }

    public String getObligationId() { return obligationId; }
    public void setObligationId(String obligationId) { this.obligationId = ValidationUtil.trim(obligationId); }

    public RegisteredTaxpayer getTaxpayer() { return taxpayer; }
    public void setTaxpayer(RegisteredTaxpayer taxpayer) { this.taxpayer = taxpayer; }

    public TaxType getTaxType() { return taxType; }
    public void setTaxType(TaxType taxType) { this.taxType = taxType; }

    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}