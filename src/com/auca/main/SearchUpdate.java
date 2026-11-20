package com.auca.main;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.corporatetaxpayer.CorporateTaxpayer;
import com.auca.exceptions.InvalidStatusTransitionException;
import com.auca.exceptions.RecordNotFoundException;
import com.auca.exceptions.ValidationException;
import com.auca.individualtaxpayer.IndividualTaxpayer;
import com.auca.interfaces.Validatable;
import com.auca.revenueofficer.RevenueOfficer;
import com.auca.taxassessment.TaxAssessment;
import com.auca.taxclearance.TaxClearance;
import com.auca.taxpayment.TaxPayment;
import com.auca.taxrefund.TaxRefund;
import com.auca.taxtype.TaxType;

import java.math.BigDecimal;
import java.time.LocalDate;


public class SearchUpdate {

    public static void search(DataStore store) {
        String key = InputHelper.readLine("Search for an ID, TIN, national ID, company no, "
                + "staff no, tax code or payment reference (or CANCEL): ");
        if (key.isEmpty() || InputHelper.isCancel(key)) {
            System.out.println("Cancelled.");
            return;
        }
        try {
            int found = searchAll(store, key);
            System.out.println();
            System.out.println(found + " record(s) found.");
        } catch (RecordNotFoundException e) {
            System.out.println("   ! " + e.getMessage());
        }
    }

    private static boolean same(String typed, String stored) {
        return stored != null && typed.equalsIgnoreCase(stored);
    }

    private static void show(Validatable item) {
        System.out.println("--------------------------------------------------");
        System.out.println(item.getSummary());
        item.printDetails();
    }

    private static int searchAll(DataStore store, String key) throws RecordNotFoundException {
        int found = 0;
        for (int i = 0; i < store.getIndividualCount(); i++) {
            IndividualTaxpayer item = store.getIndividual(i);
            if (same(key, item.getPartyId()) || same(key, item.getTin())
                    || same(key, item.getNationalId())) {
                show(item);
                found++;
            }
        }
        for (int i = 0; i < store.getCorporateCount(); i++) {
            CorporateTaxpayer item = store.getCorporate(i);
            if (same(key, item.getPartyId()) || same(key, item.getTin())
                    || same(key, item.getCompanyRegistrationNo())) {
                show(item);
                found++;
            }
        }
        for (int i = 0; i < store.getOfficerCount(); i++) {
            RevenueOfficer item = store.getOfficer(i);
            if (same(key, item.getPartyId()) || same(key, item.getStaffNumber())) {
                show(item);
                found++;
            }
        }
        for (int i = 0; i < store.getTaxTypeCount(); i++) {
            TaxType item = store.getTaxType(i);
            if (same(key, item.getTaxTypeId()) || same(key, item.getTaxCode())) {
                show(item);
                found++;
            }
        }
        for (int i = 0; i < store.getObligationCount(); i++) {
            if (same(key, store.getObligation(i).getObligationId())) {
                show(store.getObligation(i));
                found++;
            }
        }
        for (int i = 0; i < store.getDeclarationCount(); i++) {
            if (same(key, store.getDeclaration(i).getDeclarationId())) {
                show(store.getDeclaration(i));
                found++;
            }
        }
        for (int i = 0; i < store.getAssessmentCount(); i++) {
            if (same(key, store.getAssessment(i).getAssessmentId())) {
                show(store.getAssessment(i));
                found++;
            }
        }
        for (int i = 0; i < store.getPaymentCount(); i++) {
            TaxPayment item = store.getPayment(i);
            if (same(key, item.getPaymentId()) || same(key, item.getPaymentReference())) {
                show(item);
                found++;
            }
        }
        for (int i = 0; i < store.getPenaltyCount(); i++) {
            if (same(key, store.getPenalty(i).getPenaltyId())) {
                show(store.getPenalty(i));
                found++;
            }
        }
        for (int i = 0; i < store.getRefundCount(); i++) {
            if (same(key, store.getRefund(i).getRefundId())) {
                show(store.getRefund(i));
                found++;
            }
        }
        for (int i = 0; i < store.getClearanceCount(); i++) {
            if (same(key, store.getClearance(i).getClearanceId())) {
                show(store.getClearance(i));
                found++;
            }
        }
        if (found == 0) {
            throw new RecordNotFoundException("No record found for '" + key + "'.");
        }
        return found;
    }


    public static void updateMenu(DataStore store) {
        System.out.println();
        System.out.println("---------------- UPDATE ELIGIBLE RECORD ----------------");
        System.out.println(" [1] Taxpayer (contact, status, office, turnover)");
        System.out.println(" [2] Revenue officer (contact, role, limit, active)");
        System.out.println(" [3] Tax type (name, rate, frequency, active)");
        System.out.println(" [4] Assessment (adjustment, or cancel a DRAFT)");
        System.out.println(" [5] Pending refund (requested amount)");
        System.out.println(" [6] Pending clearance (expiry date)");
        System.out.println(" [0] Back to main menu");
        int option = InputHelper.parseMenuNumber(InputHelper.readLine("Select: "));
        try {
            switch (option) {
                case 1:
                    updateTaxpayer(store);
                    break;
                case 2:
                    updateOfficer(store);
                    break;
                case 3:
                    updateTaxType(store);
                    break;
                case 4:
                    updateAssessment(store);
                    break;
                case 5:
                    updateRefund(store);
                    break;
                case 6:
                    updateClearance(store);
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Unknown option.");
                    break;
            }
        } catch (ValidationException e) {
            System.out.println("   ! " + e.getMessage());
            System.out.println("   No change was made.");
        }
    }

    private static void cancelled() {
        System.out.println("Cancelled. No change was made.");
    }

    private static void setTurnover(RegisteredTaxpayer taxpayer, BigDecimal value) {
        if (taxpayer instanceof IndividualTaxpayer) {
            ((IndividualTaxpayer) taxpayer).setAnnualTurnover(value);
        } else if (taxpayer instanceof CorporateTaxpayer) {
            ((CorporateTaxpayer) taxpayer).setAnnualTurnover(value);
        }
    }

    private static void updateTaxpayer(DataStore store) throws ValidationException {
        String key = InputHelper.readLine("Taxpayer TIN or Party ID (or CANCEL): ");
        if (key.isEmpty() || InputHelper.isCancel(key)) {
            cancelled();
            return;
        }
        RegisteredTaxpayer taxpayer = store.requireTaxpayer(key);
        System.out.println();
        taxpayer.printDetails();
        System.out.println("Fields you can change: 1 Email  2 Phone  3 Status  4 District"
                + "  5 Sector  6 Tax office  7 Annual turnover");
        int field = InputHelper.parseMenuNumber(InputHelper.readLine("Field number (0 to cancel): "));
        if (field == 0) {
            cancelled();
            return;
        }
        if (field < 1 || field > 7) {
            System.out.println("   ! Please choose a number from 1 to 7.");
            return;
        }
        String text = InputHelper.readLine("New value (or CANCEL): ");
        if (InputHelper.isCancel(text)) {
            cancelled();
            return;
        }

        // Remember the old values so we can go back if the new value is not valid.
        String oldEmail = taxpayer.getEmail();
        String oldPhone = taxpayer.getPhoneNumber();
        String oldStatus = taxpayer.getTaxpayerStatus();
        String oldDistrict = taxpayer.getDistrict();
        String oldSector = taxpayer.getSector();
        String oldOffice = taxpayer.getTaxOffice();
        BigDecimal oldTurnover = taxpayer.getAnnualTurnover();

        try {
            switch (field) {
                case 1:
                    ValidationUtil.requireEmail(text);
                    taxpayer.setEmail(text);
                    break;
                case 2:
                    ValidationUtil.requirePhone(text);
                    taxpayer.setPhoneNumber(text);
                    break;
                case 3: {
                    String status = text.toUpperCase();
                    ValidationUtil.requireChoice(status, "Taxpayer status",
                            RegisteredTaxpayer.STATUS_CHOICES);
                    if (status.equals(oldStatus)) {
                        throw new InvalidStatusTransitionException(
                                "The taxpayer is already " + status + ".");
                    }
                    if (RegisteredTaxpayer.STATUS_CLOSED.equals(oldStatus)) {
                        throw new InvalidStatusTransitionException(
                                "A CLOSED taxpayer cannot be reopened.");
                    }
                    taxpayer.setTaxpayerStatus(status);
                    break;
                }
                case 4:
                    ValidationUtil.requireText(text, "District", 2, 60);
                    taxpayer.setDistrict(text);
                    break;
                case 5:
                    ValidationUtil.requireText(text, "Sector", 2, 60);
                    taxpayer.setSector(text);
                    break;
                case 6:
                    ValidationUtil.requireText(text, "Tax office", 2, 60);
                    taxpayer.setTaxOffice(text);
                    break;
                default:
                    setTurnover(taxpayer, InputHelper.parseMoney(text, "Annual turnover"));
                    break;
            }
            taxpayer.validate();
            System.out.println("Updated: " + taxpayer.getSummary());
        } catch (ValidationException e) {
            taxpayer.setEmail(oldEmail);
            taxpayer.setPhoneNumber(oldPhone);
            taxpayer.setTaxpayerStatus(oldStatus);
            taxpayer.setDistrict(oldDistrict);
            taxpayer.setSector(oldSector);
            taxpayer.setTaxOffice(oldOffice);
            setTurnover(taxpayer, oldTurnover);
            System.out.println("   ! " + e.getMessage());
            System.out.println("   The old values were restored. No change was made.");
        }
    }

    private static void updateOfficer(DataStore store) throws ValidationException {
        String key = InputHelper.readLine("Officer Party ID or staff number (or CANCEL): ");
        if (key.isEmpty() || InputHelper.isCancel(key)) {
            cancelled();
            return;
        }
        RevenueOfficer officer = store.requireOfficer(key);
        System.out.println();
        officer.printDetails();
        System.out.println("Fields you can change: 1 Email  2 Phone  3 Department  4 Role"
                + "  5 Approval limit  6 Active");
        int field = InputHelper.parseMenuNumber(InputHelper.readLine("Field number (0 to cancel): "));
        if (field == 0) {
            cancelled();
            return;
        }
        if (field < 1 || field > 6) {
            System.out.println("   ! Please choose a number from 1 to 6.");
            return;
        }
        String text = InputHelper.readLine("New value (or CANCEL): ");
        if (InputHelper.isCancel(text)) {
            cancelled();
            return;
        }

        String oldEmail = officer.getEmail();
        String oldPhone = officer.getPhoneNumber();
        String oldDepartment = officer.getDepartment();
        String oldRole = officer.getRole();
        BigDecimal oldLimit = officer.getApprovalLimit();
        boolean oldActive = officer.isActive();

        try {
            switch (field) {
                case 1:
                    ValidationUtil.requireEmail(text);
                    officer.setEmail(text);
                    break;
                case 2:
                    ValidationUtil.requirePhone(text);
                    officer.setPhoneNumber(text);
                    break;
                case 3:
                    ValidationUtil.requireText(text, "Department", 2, 60);
                    officer.setDepartment(text);
                    break;
                case 4:
                    ValidationUtil.requireText(text, "Role", 2, 60);
                    officer.setRole(text);
                    break;
                case 5:
                    officer.setApprovalLimit(InputHelper.parseMoney(text, "Approval limit"));
                    break;
                default:
                    officer.setActive(InputHelper.parseYesNo(text, "Active"));
                    break;
            }
            officer.validate();
            System.out.println("Updated: " + officer.getSummary()
                    + " | limit " + officer.getApprovalLimit() + " | active " + officer.isActive());
        } catch (ValidationException e) {
            officer.setEmail(oldEmail);
            officer.setPhoneNumber(oldPhone);
            officer.setDepartment(oldDepartment);
            officer.setRole(oldRole);
            officer.setApprovalLimit(oldLimit);
            officer.setActive(oldActive);
            System.out.println("   ! " + e.getMessage());
            System.out.println("   The old values were restored. No change was made.");
        }
    }

    private static int countObligationsUsing(DataStore store, TaxType taxType) {
        int count = 0;
        for (int i = 0; i < store.getObligationCount(); i++) {
            if (store.getObligation(i).getTaxType() == taxType) {
                count++;
            }
        }
        return count;
    }

    private static void updateTaxType(DataStore store) throws ValidationException {
        String key = InputHelper.readLine("Tax type ID or code (or CANCEL): ");
        if (key.isEmpty() || InputHelper.isCancel(key)) {
            cancelled();
            return;
        }
        TaxType taxType = store.requireTaxType(key);
        System.out.println();
        taxType.printDetails();
        System.out.println("Fields you can change: 1 Name  2 Rate  3 Filing frequency  4 Active");
        int field = InputHelper.parseMenuNumber(InputHelper.readLine("Field number (0 to cancel): "));
        if (field == 0) {
            cancelled();
            return;
        }
        if (field < 1 || field > 4) {
            System.out.println("   ! Please choose a number from 1 to 4.");
            return;
        }
        String text = InputHelper.readLine("New value (or CANCEL): ");
        if (InputHelper.isCancel(text)) {
            cancelled();
            return;
        }

        String oldName = taxType.getTaxName();
        BigDecimal oldRate = taxType.getRate();
        String oldFrequency = taxType.getFilingFrequency();
        boolean oldActive = taxType.isActive();

        try {
            int used = countObligationsUsing(store, taxType);
            if ((field == 2 || field == 3) && used > 0) {
                throw new ValidationException("The rate and filing frequency are locked: "
                        + used + " obligation(s) already use this tax type.");
            }
            switch (field) {
                case 1:
                    ValidationUtil.requireText(text, "Tax name", 2, 60);
                    taxType.setTaxName(text);
                    break;
                case 2:
                    taxType.setRate(InputHelper.parseRate(text, "Tax rate"));
                    break;
                case 3:
                    taxType.setFilingFrequency(text.toUpperCase());
                    break;
                default:
                    taxType.setActive(InputHelper.parseYesNo(text, "Active"));
                    break;
            }
            taxType.validate();
            System.out.println("Updated: " + taxType.getSummary());
        } catch (ValidationException e) {
            taxType.setTaxName(oldName);
            taxType.setRate(oldRate);
            taxType.setFilingFrequency(oldFrequency);
            taxType.setActive(oldActive);
            System.out.println("   ! " + e.getMessage());
            System.out.println("   The old values were restored. No change was made.");
        }
    }

    private static void updateAssessment(DataStore store) throws ValidationException {
        String id = InputHelper.readLine("Assessment ID (or CANCEL): ");
        if (id.isEmpty() || InputHelper.isCancel(id)) {
            cancelled();
            return;
        }
        TaxAssessment assessment = store.requireAssessment(id);
        System.out.println();
        assessment.printDetails();
        ValidationUtil.printField("Total", assessment.getTotal());
        if (TaxAssessment.STATUS_CANCELLED.equals(assessment.getStatus())) {
            throw new InvalidStatusTransitionException("A CANCELLED assessment cannot be changed.");
        }
        System.out.println(" [1] Change the adjustment amount (negative allowed)");
        System.out.println(" [2] Cancel this assessment (DRAFT only)");
        System.out.println(" [0] Back");
        int option = InputHelper.parseMenuNumber(InputHelper.readLine("Select: "));

        if (option == 1) {
            if (store.getApprovedRefunds(assessment).signum() > 0) {
                throw new ValidationException(
                        "The adjustment is locked: a refund was already approved for this assessment.");
            }
            String text = InputHelper.readLine("New adjustment amount (or CANCEL): ");
            if (InputHelper.isCancel(text)) {
                cancelled();
                return;
            }
            BigDecimal oldAdjustment = assessment.getAdjustmentAmount();
            try {
                assessment.setAdjustmentAmount(
                        InputHelper.parseSignedMoney(text, "Adjustment amount"));
                assessment.validate();
                System.out.println("Updated. New total          : " + assessment.getTotal());
                System.out.println("Outstanding balance        : "
                        + store.getOutstandingBalance(assessment));
                System.out.println("Overpayment (not refunded) : "
                        + store.getAvailableOverpayment(assessment));
            } catch (ValidationException e) {
                assessment.setAdjustmentAmount(oldAdjustment);
                System.out.println("   ! " + e.getMessage());
                System.out.println("   The old adjustment was restored. No change was made.");
            }
        } else if (option == 2) {
            String answer = InputHelper.readLine("Type YES to cancel this assessment: ");
            if (!answer.equalsIgnoreCase("YES")) {
                cancelled();
                return;
            }
            assessment.cancel();   // throws InvalidStatusTransitionException unless DRAFT
            System.out.println("Assessment " + assessment.getAssessmentId() + " is now CANCELLED.");
        } else if (option == 0) {
            cancelled();
        } else {
            System.out.println("Unknown option.");
        }
    }

    private static void updateRefund(DataStore store) throws ValidationException {
        String id = InputHelper.readLine("Refund ID (or CANCEL): ");
        if (id.isEmpty() || InputHelper.isCancel(id)) {
            cancelled();
            return;
        }
        TaxRefund oldRefund = store.requireRefund(id);
        System.out.println();
        oldRefund.printDetails();
        if (!TaxRefund.STATUS_PENDING.equals(oldRefund.getStatus())) {
            throw new InvalidStatusTransitionException(
                    "Only a PENDING refund can be changed (current status: "
                            + oldRefund.getStatus() + ").");
        }
        System.out.println("Verified overpayment available: "
                + store.getAvailableOverpayment(oldRefund.getAssessment()));
        String text = InputHelper.readLine("New requested amount (or CANCEL): ");
        if (InputHelper.isCancel(text)) {
            cancelled();
            return;
        }
        BigDecimal newAmount = InputHelper.parseMoney(text, "Requested amount");
        TaxRefund newRefund = new TaxRefund(oldRefund.getRefundId(), oldRefund.getTaxpayer(),
                oldRefund.getAssessment(), newAmount, oldRefund.getRequestDate());
        store.replaceRefund(oldRefund, newRefund);
        System.out.println("Refund replaced: " + newRefund.getSummary());
    }


    private static void updateClearance(DataStore store) throws ValidationException {
        String id = InputHelper.readLine("Clearance ID (or CANCEL): ");
        if (id.isEmpty() || InputHelper.isCancel(id)) {
            cancelled();
            return;
        }
        TaxClearance oldClearance = store.requireClearance(id);
        System.out.println();
        oldClearance.printDetails();
        if (!TaxClearance.STATUS_PENDING.equals(oldClearance.getStatus())) {
            throw new InvalidStatusTransitionException(
                    "Only a PENDING clearance can be changed (current status: "
                            + oldClearance.getStatus() + ").");
        }
        String text = InputHelper.readLine("New expiry date YYYY-MM-DD (or CANCEL): ");
        if (InputHelper.isCancel(text)) {
            cancelled();
            return;
        }
        LocalDate newExpiry = InputHelper.parseDate(text, "Expiry date");
        TaxClearance newClearance = new TaxClearance(oldClearance.getClearanceId(),
                oldClearance.getTaxpayer(), oldClearance.getRequestDate(), newExpiry);
        store.replaceClearance(oldClearance, newClearance);
        System.out.println("Clearance replaced: " + newClearance.getSummary());
    }
}