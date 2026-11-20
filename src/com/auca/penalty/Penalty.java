package com.auca.penalty;

import com.auca.exceptions.InvalidTaxCalculationException;
import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.main.ValidationUtil;
import com.auca.taxobligation.TaxObligation;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Penalty implements Identifiable, Validatable {

    public static final String TYPE_LATE_FILING = "LATE_FILING";
    public static final String TYPE_LATE_PAYMENT = "LATE_PAYMENT";
    public static final String[] TYPE_CHOICES = {TYPE_LATE_FILING, TYPE_LATE_PAYMENT};

    public static final BigDecimal DEFAULT_RATE = new BigDecimal("5");

    private String penaltyId;
    private TaxObligation obligation;
    private String penaltyType;
    private BigDecimal baseAmount;
    private BigDecimal penaltyRate;
    private BigDecimal penaltyAmount;

    public Penalty(String penaltyId, TaxObligation obligation, String penaltyType,
                   BigDecimal baseAmount, BigDecimal penaltyRate, BigDecimal penaltyAmount) {
        this.penaltyId = ValidationUtil.trim(penaltyId);
        this.obligation = obligation;
        this.penaltyType = ValidationUtil.trim(penaltyType);
        this.baseAmount = baseAmount;
        this.penaltyRate = penaltyRate;
        this.penaltyAmount = penaltyAmount;
    }


    public Penalty(String penaltyId, TaxObligation obligation, String penaltyType,
                   BigDecimal baseAmount) {
        this(penaltyId, obligation, penaltyType, baseAmount, DEFAULT_RATE,
                calculateAmount(baseAmount, DEFAULT_RATE));
    }

    public static BigDecimal calculateAmount(BigDecimal baseAmount, BigDecimal rate) {
        if (baseAmount == null || rate == null) {
            return BigDecimal.ZERO;
        }
        return baseAmount.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    public boolean sameKey(Penalty other) {
        if (other == null || other == this) {
            return false;
        }
        return obligation == other.obligation && penaltyType != null
                && penaltyType.equals(other.penaltyType);
    }

    @Override
    public String getId() {
        return penaltyId;
    }

    @Override
    public String getDisplayName() {
        return "Penalty " + penaltyType + " " + penaltyId;
    }

    @Override
    public boolean isActive() {
        return obligation != null;
    }

    @Override
    public void validate() throws ValidationException {
        ValidationUtil.requireText(penaltyId, "Penalty ID", 4, 30);
        if (obligation == null) {
            throw new PrerequisiteMissingException("A tax obligation is required.");
        }
        ValidationUtil.requireChoice(penaltyType, "Penalty type", TYPE_CHOICES);
        ValidationUtil.requireMoney(baseAmount, "Base amount");
        ValidationUtil.requireRate(penaltyRate, "Penalty rate");
        ValidationUtil.requireMoney(penaltyAmount, "Penalty amount");
        BigDecimal expected = calculateAmount(baseAmount, penaltyRate);
        if (penaltyAmount.compareTo(expected) != 0) {
            throw new InvalidTaxCalculationException("Penalty amount " + penaltyAmount
                    + " does not match the expected amount " + expected + ".");
        }
    }

    @Override
    public void printDetails() {
        ValidationUtil.printField("Penalty ID", penaltyId);
        ValidationUtil.printField("Obligation", ValidationUtil.describe(obligation));
        ValidationUtil.printField("Penalty type", penaltyType);
        ValidationUtil.printField("Base amount", baseAmount);
        ValidationUtil.printField("Penalty rate (%)", penaltyRate);
        ValidationUtil.printField("Penalty amount", penaltyAmount);
    }

    @Override
    public String getSummary() {
        return "Penalty | " + penaltyId + " | " + penaltyType + " | " + penaltyAmount;
    }

    public String getPenaltyId() { return penaltyId; }
    public void setPenaltyId(String penaltyId) { this.penaltyId = ValidationUtil.trim(penaltyId); }

    public TaxObligation getObligation() { return obligation; }
    public void setObligation(TaxObligation obligation) { this.obligation = obligation; }

    public String getPenaltyType() { return penaltyType; }
    public void setPenaltyType(String penaltyType) { this.penaltyType = ValidationUtil.trim(penaltyType); }

    public BigDecimal getBaseAmount() { return baseAmount; }
    public void setBaseAmount(BigDecimal baseAmount) { this.baseAmount = baseAmount; }

    public BigDecimal getPenaltyRate() { return penaltyRate; }
    public void setPenaltyRate(BigDecimal penaltyRate) { this.penaltyRate = penaltyRate; }

    public BigDecimal getPenaltyAmount() { return penaltyAmount; }
    public void setPenaltyAmount(BigDecimal penaltyAmount) { this.penaltyAmount = penaltyAmount; }
}