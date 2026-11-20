package com.auca.abstracts;

import com.auca.exceptions.ValidationException;
import com.auca.main.ValidationUtil;

import java.math.BigDecimal;
import java.time.LocalDate;

public abstract class RegisteredTaxpayer extends TaxParty {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_SUSPENDED = "SUSPENDED";
    public static final String STATUS_CLOSED = "CLOSED";
    public static final String[] STATUS_CHOICES = {STATUS_ACTIVE, STATUS_SUSPENDED, STATUS_CLOSED};

    private String tin;
    private LocalDate registrationDate;
    private String taxpayerStatus;
    private String district;
    private String sector;
    private String taxOffice;

    public RegisteredTaxpayer(String partyId, String firstName, String lastName, String email,
                              String phoneNumber, LocalDate dateOfBirth,
                              String tin, LocalDate registrationDate, String taxpayerStatus,
                              String district, String sector, String taxOffice) {
        super(partyId, firstName, lastName, email, phoneNumber, dateOfBirth);
        this.tin = ValidationUtil.trim(tin);
        this.registrationDate = registrationDate;
        this.taxpayerStatus = ValidationUtil.trim(taxpayerStatus);
        this.district = ValidationUtil.trim(district);
        this.sector = ValidationUtil.trim(sector);
        this.taxOffice = ValidationUtil.trim(taxOffice);
    }


    public abstract BigDecimal getAnnualTurnover();

    @Override
    public boolean isActive() {
        return STATUS_ACTIVE.equals(taxpayerStatus);
    }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        ValidationUtil.requireDigits(tin, "TIN", 9);
        ValidationUtil.requireNotFuture(registrationDate, "Registration date");
        ValidationUtil.requireBefore(getDateOfBirth(), "Date of birth",
                registrationDate, "Registration date");
        ValidationUtil.requireChoice(taxpayerStatus, "Taxpayer status", STATUS_CHOICES);
        ValidationUtil.requireText(district, "District", 2, 60);
        ValidationUtil.requireText(sector, "Sector", 2, 60);
        ValidationUtil.requireText(taxOffice, "Tax office", 2, 60);
    }

    @Override
    public void printDetails() {
        super.printDetails();
        ValidationUtil.printField("TIN", tin);
        ValidationUtil.printField("Registration date", registrationDate);
        ValidationUtil.printField("Taxpayer status", taxpayerStatus);
        ValidationUtil.printField("District", district);
        ValidationUtil.printField("Sector", sector);
        ValidationUtil.printField("Tax office", taxOffice);
    }

    @Override
    public String getSummary() {
        return super.getSummary() + " | TIN " + tin + " | " + taxpayerStatus;
    }

    public String getTin() { return tin; }
    public void setTin(String tin) { this.tin = ValidationUtil.trim(tin); }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }

    public String getTaxpayerStatus() { return taxpayerStatus; }
    public void setTaxpayerStatus(String taxpayerStatus) { this.taxpayerStatus = ValidationUtil.trim(taxpayerStatus); }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = ValidationUtil.trim(district); }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = ValidationUtil.trim(sector); }

    public String getTaxOffice() { return taxOffice; }
    public void setTaxOffice(String taxOffice) { this.taxOffice = ValidationUtil.trim(taxOffice); }
}