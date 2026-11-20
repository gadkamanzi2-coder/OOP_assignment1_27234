package com.auca.taxtype;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.main.ValidationUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TaxType implements Identifiable, Validatable {

    public static final String FREQ_MONTHLY = "MONTHLY";
    public static final String FREQ_QUARTERLY = "QUARTERLY";
    public static final String FREQ_ANNUAL = "ANNUAL";
    public static final String[] FREQUENCY_CHOICES = {FREQ_MONTHLY, FREQ_QUARTERLY, FREQ_ANNUAL};

    private String taxTypeId;
    private String taxCode;
    private String taxName;
    private BigDecimal rate;
    private String filingFrequency;
    private boolean active;

    public TaxType(String taxTypeId, String taxCode, String taxName, BigDecimal rate,
                   String filingFrequency, boolean active) {
        this.taxTypeId = ValidationUtil.trim(taxTypeId);
        this.taxCode = ValidationUtil.trim(taxCode);
        this.taxName = ValidationUtil.trim(taxName);
        this.rate = rate;
        this.filingFrequency = ValidationUtil.trim(filingFrequency);
        this.active = active;
    }

    public TaxType(String taxTypeId, String taxCode, String taxName, BigDecimal rate,
                   String filingFrequency) {
        this(taxTypeId, taxCode, taxName, rate, filingFrequency, true);
    }

    public BigDecimal calculateTax(BigDecimal taxableAmount) {
        if (taxableAmount == null || rate == null) {
            return BigDecimal.ZERO;
        }
        return taxableAmount.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    @Override
    public String getId() {
        return taxTypeId;
    }

    @Override
    public String getDisplayName() {
        return taxName + " (" + taxCode + ")";
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void validate() throws ValidationException {
        ValidationUtil.requireText(taxTypeId, "Tax type ID", 4, 30);
        ValidationUtil.requireCode(taxCode, "Tax code", 2, 20);
        ValidationUtil.requireText(taxName, "Tax name", 2, 60);
        ValidationUtil.requireRate(rate, "Tax rate");
        ValidationUtil.requireChoice(filingFrequency, "Filing frequency", FREQUENCY_CHOICES);
    }

    @Override
    public void printDetails() {
        ValidationUtil.printField("Tax type ID", taxTypeId);
        ValidationUtil.printField("Tax code", taxCode);
        ValidationUtil.printField("Tax name", taxName);
        ValidationUtil.printField("Rate (%)", rate);
        ValidationUtil.printField("Filing frequency", filingFrequency);
        ValidationUtil.printField("Active", active ? "YES" : "NO");
    }

    @Override
    public String getSummary() {
        return "Tax Type | " + taxTypeId + " | " + getDisplayName() + " | "
                + rate + "% | " + filingFrequency;
    }

    public String getTaxTypeId() { return taxTypeId; }
    public void setTaxTypeId(String taxTypeId) { this.taxTypeId = ValidationUtil.trim(taxTypeId); }

    public String getTaxCode() { return taxCode; }
    public void setTaxCode(String taxCode) { this.taxCode = ValidationUtil.trim(taxCode); }

    public String getTaxName() { return taxName; }
    public void setTaxName(String taxName) { this.taxName = ValidationUtil.trim(taxName); }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public String getFilingFrequency() { return filingFrequency; }
    public void setFilingFrequency(String filingFrequency) { this.filingFrequency = ValidationUtil.trim(filingFrequency); }

    public void setActive(boolean active) { this.active = active; }
}