package com.auca.abstracts;

import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.main.ValidationUtil;

import java.time.LocalDate;

public abstract class TaxParty implements Identifiable, Validatable {

    private String partyId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;

    public TaxParty(String partyId, String firstName, String lastName, String email,
                    String phoneNumber, LocalDate dateOfBirth) {
        this.partyId = ValidationUtil.trim(partyId);
        this.firstName = ValidationUtil.trim(firstName);
        this.lastName = ValidationUtil.trim(lastName);
        this.email = ValidationUtil.trim(email);
        this.phoneNumber = ValidationUtil.trim(phoneNumber);
        this.dateOfBirth = dateOfBirth;
    }

    public abstract String getRoleName();

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String getId() {
        return partyId;
    }

    @Override
    public String getDisplayName() {
        return getFullName();
    }

    @Override
    public void validate() throws ValidationException {
        ValidationUtil.requireText(partyId, "Party ID", 4, 30);
        ValidationUtil.requireText(firstName, "First name", 2, 60);
        ValidationUtil.requireText(lastName, "Last name", 2, 60);
        ValidationUtil.requireEmail(email);
        ValidationUtil.requirePhone(phoneNumber);
        ValidationUtil.requireNotFuture(dateOfBirth, "Date of birth");
    }

    @Override
    public void printDetails() {
        ValidationUtil.printField("Party ID", partyId);
        ValidationUtil.printField("First name", firstName);
        ValidationUtil.printField("Last name", lastName);
        ValidationUtil.printField("Email", email);
        ValidationUtil.printField("Phone", phoneNumber);
        ValidationUtil.printField("Date of birth", dateOfBirth);
    }

    @Override
    public String getSummary() {
        return getRoleName() + " | " + partyId + " | " + getDisplayName();
    }

    // ---------- Getters and setters ----------
    public String getPartyId() { return partyId; }
    public void setPartyId(String partyId) { this.partyId = ValidationUtil.trim(partyId); }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = ValidationUtil.trim(firstName); }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = ValidationUtil.trim(lastName); }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = ValidationUtil.trim(email); }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = ValidationUtil.trim(phoneNumber); }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
}