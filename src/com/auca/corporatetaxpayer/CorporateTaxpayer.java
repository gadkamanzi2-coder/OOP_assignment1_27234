package com.auca.corporatetaxpayer;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.ValidationException;
import com.auca.main.ValidationUtil;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CorporateTaxpayer extends RegisteredTaxpayer {

    private String companyRegistrationNo;
    private String legalName;
    private LocalDate incorporationDate;
    private String businessSector;
    private String authorizedRepresentative;
    private BigDecimal annualTurnover;

    public CorporateTaxpayer(String partyId, String firstName, String lastName, String email,
                             String phoneNumber, LocalDate dateOfBirth,
                             String tin, LocalDate registrationDate, String taxpayerStatus,
                             String district, String sector, String taxOffice,
                             String companyRegistrationNo, String legalName,
                             LocalDate incorporationDate, String businessSector,
                             String authorizedRepresentative, BigDecimal annualTurnover) {
        super(partyId, firstName, lastName, email, phoneNumber, dateOfBirth,
                tin, registrationDate, taxpayerStatus, district, sector, taxOffice);
        this.companyRegistrationNo = ValidationUtil.trim(companyRegistrationNo);
        this.legalName = ValidationUtil.trim(legalName);
        this.incorporationDate = incorporationDate;
        this.businessSector = ValidationUtil.trim(businessSector);
        this.authorizedRepresentative = ValidationUtil.trim(authorizedRepresentative);
        this.annualTurnover = annualTurnover;
    }

    public CorporateTaxpayer(String partyId, String firstName, String lastName, String email,
                             String phoneNumber, LocalDate dateOfBirth,
                             String tin, LocalDate registrationDate, String taxpayerStatus,
                             String district, String sector, String taxOffice,
                             String companyRegistrationNo, String legalName,
                             LocalDate incorporationDate, String businessSector,
                             String authorizedRepresentative) {
        this(partyId, firstName, lastName, email, phoneNumber, dateOfBirth,
                tin, registrationDate, taxpayerStatus, district, sector, taxOffice,
                companyRegistrationNo, legalName, incorporationDate, businessSector,
                authorizedRepresentative, BigDecimal.ZERO);
    }

    @Override
    public String getRoleName() {
        return "Corporate Taxpayer";
    }

    @Override
    public String getDisplayName() {
        return legalName;
    }

    @Override
    public BigDecimal getAnnualTurnover() {
        return annualTurnover;
    }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        ValidationUtil.requireCode(companyRegistrationNo, "Company registration number", 5, 30);
        ValidationUtil.requireText(legalName, "Legal name", 3, 120);
        ValidationUtil.requireNotFuture(incorporationDate, "Incorporation date");
        ValidationUtil.requireBefore(incorporationDate, "Incorporation date",
                getRegistrationDate(), "Registration date");
        ValidationUtil.requireText(businessSector, "Business sector", 2, 60);
        ValidationUtil.requireText(authorizedRepresentative, "Authorized representative", 2, 60);
        ValidationUtil.requireMoney(annualTurnover, "Annual turnover");
    }

    @Override
    public void printDetails() {
        super.printDetails();
        ValidationUtil.printField("Company registration no", companyRegistrationNo);
        ValidationUtil.printField("Legal name", legalName);
        ValidationUtil.printField("Incorporation date", incorporationDate);
        ValidationUtil.printField("Business sector", businessSector);
        ValidationUtil.printField("Authorized representative", authorizedRepresentative);
        ValidationUtil.printField("Annual turnover", annualTurnover);
    }

    @Override
    public String getSummary() {
        return super.getSummary() + " | " + businessSector;
    }

    public String getCompanyRegistrationNo() { return companyRegistrationNo; }
    public void setCompanyRegistrationNo(String companyRegistrationNo) { this.companyRegistrationNo = ValidationUtil.trim(companyRegistrationNo); }

    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = ValidationUtil.trim(legalName); }

    public LocalDate getIncorporationDate() { return incorporationDate; }
    public void setIncorporationDate(LocalDate incorporationDate) { this.incorporationDate = incorporationDate; }

    public String getBusinessSector() { return businessSector; }
    public void setBusinessSector(String businessSector) { this.businessSector = ValidationUtil.trim(businessSector); }

    public String getAuthorizedRepresentative() { return authorizedRepresentative; }
    public void setAuthorizedRepresentative(String authorizedRepresentative) { this.authorizedRepresentative = ValidationUtil.trim(authorizedRepresentative); }

    public void setAnnualTurnover(BigDecimal annualTurnover) { this.annualTurnover = annualTurnover; }
}