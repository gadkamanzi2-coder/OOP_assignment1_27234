package com.auca.individualtaxpayer;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.ValidationException;
import com.auca.main.ValidationUtil;

import java.math.BigDecimal;
import java.time.LocalDate;

public class IndividualTaxpayer extends RegisteredTaxpayer {

    private String nationalId;
    private String occupation;
    private String businessName;
    private LocalDate businessStartDate;
    private BigDecimal annualTurnover;
    private boolean resident;

    public IndividualTaxpayer(String partyId, String firstName, String lastName, String email,
                              String phoneNumber, LocalDate dateOfBirth,
                              String tin, LocalDate registrationDate, String taxpayerStatus,
                              String district, String sector, String taxOffice,
                              String nationalId, String occupation, String businessName,
                              LocalDate businessStartDate, BigDecimal annualTurnover,
                              boolean resident) {
        super(partyId, firstName, lastName, email, phoneNumber, dateOfBirth,
                tin, registrationDate, taxpayerStatus, district, sector, taxOffice);
        this.nationalId = ValidationUtil.trim(nationalId);
        this.occupation = ValidationUtil.trim(occupation);
        this.businessName = ValidationUtil.trim(businessName);
        this.businessStartDate = businessStartDate;
        this.annualTurnover = annualTurnover;
        this.resident = resident;
    }

    public IndividualTaxpayer(String partyId, String firstName, String lastName, String email,
                              String phoneNumber, LocalDate dateOfBirth,
                              String tin, LocalDate registrationDate, String taxpayerStatus,
                              String district, String sector, String taxOffice,
                              String nationalId, String occupation, String businessName,
                              LocalDate businessStartDate, BigDecimal annualTurnover) {
        this(partyId, firstName, lastName, email, phoneNumber, dateOfBirth,
                tin, registrationDate, taxpayerStatus, district, sector, taxOffice,
                nationalId, occupation, businessName, businessStartDate, annualTurnover, true);
    }

    @Override
    public String getRoleName() {
        return "Individual Taxpayer";
    }

    @Override
    public BigDecimal getAnnualTurnover() {
        return annualTurnover;
    }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        ValidationUtil.requireDigits(nationalId, "National ID", 16);
        ValidationUtil.requireText(occupation, "Occupation", 2, 60);
        ValidationUtil.requireText(businessName, "Business name", 2, 60);
        ValidationUtil.requireNotFuture(businessStartDate, "Business start date");
        ValidationUtil.requireBefore(businessStartDate, "Business start date",
                getRegistrationDate(), "Registration date");
        ValidationUtil.requireMoney(annualTurnover, "Annual turnover");
    }

    @Override
    public void printDetails() {
        super.printDetails();
        ValidationUtil.printField("National ID", nationalId);
        ValidationUtil.printField("Occupation", occupation);
        ValidationUtil.printField("Business name", businessName);
        ValidationUtil.printField("Business start date", businessStartDate);
        ValidationUtil.printField("Annual turnover", annualTurnover);
        ValidationUtil.printField("Resident", resident ? "YES" : "NO");
    }

    @Override
    public String getSummary() {
        return super.getSummary() + " | " + occupation;
    }

    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = ValidationUtil.trim(nationalId); }

    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = ValidationUtil.trim(occupation); }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = ValidationUtil.trim(businessName); }

    public LocalDate getBusinessStartDate() { return businessStartDate; }
    public void setBusinessStartDate(LocalDate businessStartDate) { this.businessStartDate = businessStartDate; }

    public void setAnnualTurnover(BigDecimal annualTurnover) { this.annualTurnover = annualTurnover; }

    public boolean isResident() { return resident; }
    public void setResident(boolean resident) { this.resident = resident; }
}