package com.auca.main;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.DuplicateRecordException;
import com.auca.exceptions.OverpaymentException;
import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.ValidationException;
import com.auca.penalty.Penalty;
import com.auca.taxassessment.TaxAssessment;
import com.auca.taxclearance.TaxClearance;
import com.auca.taxdeclaration.TaxDeclaration;
import com.auca.taxobligation.TaxObligation;
import com.auca.taxpayment.TaxPayment;
import com.auca.taxrefund.TaxRefund;

import java.math.BigDecimal;

public class FinanceForms {

    public static final int FORM_PAYMENT = 8;
    public static final int FORM_PENALTY = 9;
    public static final int FORM_REFUND = 10;
    public static final int FORM_CLEARANCE = 11;

    private static final String NO_OFFICER_YET = "(none yet)";

    public static void refresh(int formType, FormEngine form, DataStore store) {
        if (formType == FORM_PENALTY) {
            refreshPenalty(form, store);
        } else if (formType == FORM_REFUND) {
            form.setText(5, TaxRefund.STATUS_PENDING);
        } else if (formType == FORM_CLEARANCE) {
            form.setText(4, TaxClearance.STATUS_PENDING);
            form.setText(5, NO_OFFICER_YET);
        }
    }

    public static void check(int formType, FormEngine form, DataStore store)
            throws ValidationException {
        switch (formType) {
            case FORM_PAYMENT: {
                TaxPayment payment = buildPayment(form, store);
                TaxAssessment assessment = payment.getAssessment();
                printBalance(assessment, store);
                payment.validate();
                BigDecimal outstanding = store.getOutstandingBalance(assessment);
                if (payment.getAmount().compareTo(outstanding) > 0) {
                    throw new OverpaymentException("Payment amount " + payment.getAmount()
                            + " exceeds the outstanding balance " + outstanding + ".");
                }
                System.out.println("| Balance after this payment: "
                        + outstanding.subtract(payment.getAmount()));
                break;
            }
            case FORM_PENALTY: {
                requirePenaltyInputs(form);
                Penalty penalty = buildPenalty(form, store);
                System.out.println("| Base amount: " + penalty.getBaseAmount()
                        + " (assessment total, or declared tax if not assessed yet)");
                System.out.println("| Penalty = " + penalty.getBaseAmount() + " x "
                        + penalty.getPenaltyRate() + " / 100 = " + penalty.getPenaltyAmount());
                penalty.validate();
                precheckPenalty(penalty, store);
                System.out.println("| Penalty rules are satisfied.");
                break;
            }
            case FORM_REFUND: {
                TaxRefund refund = buildRefund(form, store);
                TaxAssessment assessment = refund.getAssessment();
                printBalance(assessment, store);
                System.out.println("| Verified overpayment available: "
                        + store.getAvailableOverpayment(assessment));
                refund.validate();
                precheckRefund(refund, store);
                break;
            }
            case FORM_CLEARANCE: {
                TaxClearance clearance = buildClearance(form, store);
                clearance.validate();
                try {
                    store.checkClearanceEligibility(clearance.getTaxpayer());
                    System.out.println("| Eligibility now: ELIGIBLE.");
                } catch (ValidationException e) {
                    System.out.println("| Eligibility now: NOT ELIGIBLE - " + e.getMessage());
                    System.out.println("| The request can be saved as PENDING, "
                            + "but it cannot be approved until this is fixed.");
                }
                break;
            }
            default:
                throw new ValidationException("Unknown form type.");
        }
    }

    public static void save(int formType, FormEngine form, DataStore store)
            throws ValidationException {
        switch (formType) {
            case FORM_PAYMENT: {
                TaxPayment payment = buildPayment(form, store);
                store.addPayment(payment);
                System.out.println();
                System.out.println("SAVED: " + payment.getSummary());
                System.out.println("Outstanding balance of "
                        + payment.getAssessment().getAssessmentId() + " is now "
                        + store.getOutstandingBalance(payment.getAssessment()));
                break;
            }
            case FORM_PENALTY: {
                requirePenaltyInputs(form);
                Penalty penalty = buildPenalty(form, store);
                store.addPenalty(penalty);
                System.out.println();
                System.out.println("SAVED: " + penalty.getSummary());
                TaxDeclaration declaration =
                        store.findActiveDeclarationForObligation(penalty.getObligation());
                if (declaration != null) {
                    TaxAssessment assessment = store.findActiveAssessmentForDeclaration(declaration);
                    if (assessment != null) {
                        System.out.println("Outstanding balance of " + assessment.getAssessmentId()
                                + " is now " + store.getOutstandingBalance(assessment));
                    }
                }
                break;
            }
            case FORM_REFUND: {
                TaxRefund refund = buildRefund(form, store);
                store.addRefund(refund);
                System.out.println();
                System.out.println("SAVED: " + refund.getSummary());
                System.out.println("The refund is PENDING. Use menu 14, option 2 to approve or reject it.");
                break;
            }
            case FORM_CLEARANCE: {
                TaxClearance clearance = buildClearance(form, store);
                store.addClearance(clearance);
                System.out.println();
                System.out.println("SAVED: " + clearance.getSummary());
                System.out.println("The clearance is PENDING. Use menu 15, option 2 to decide it.");
                break;
            }
            default:
                throw new ValidationException("Unknown form type.");
        }
    }

    // =====================================================================
    // Menu actions: define the fields and steps, then run the form
    // =====================================================================

    public static void recordPayment(DataStore store) {
        FormEngine form = new FormEngine(FORM_PAYMENT, "RECORD TAX PAYMENT", store);
        form.addUniqueText("Payment ID", 4, 30, FormEngine.UNIQUE_PAYMENT_ID);               // 1
        form.addRef("Assessment ID", FormEngine.KIND_REF_ASSESSMENT_PAYABLE);                 // 2
        form.addCode("Payment reference", 6, 30, FormEngine.UNIQUE_PAYMENT_REF);             // 3
        form.addMoney("Amount");                                                              // 4
        form.addDate("Payment date");                                                         // 5
        form.addChoice("Payment method", TaxPayment.METHOD_CHOICES);                          // 6
        form.addStep("IDENTIFICATION / PREREQUISITE", 2);
        form.addStep("PAYMENT DETAILS", 4);
        form.addStep("VALIDATION / CALCULATIONS", 0);
        form.run();
    }

    public static void recordPenalty(DataStore store) {
        FormEngine form = new FormEngine(FORM_PENALTY, "CALCULATE / RECORD PENALTY", store);
        form.addUniqueText("Penalty ID", 4, 30, FormEngine.UNIQUE_PENALTY_ID);               // 1
        form.addRef("Obligation ID", FormEngine.KIND_REF_OBLIGATION);                         // 2
        form.addChoice("Penalty type", Penalty.TYPE_CHOICES);                                 // 3
        form.addRate("Penalty rate (%)");                                                     // 4
        form.addCalculated("Base amount", "BigDecimal, calculated");                          // 5
        form.addCalculated("Penalty amount", "BigDecimal, calculated");                       // 6
        form.addStep("IDENTIFICATION / PREREQUISITE", 2);
        form.addStep("PENALTY DETAILS", 2);
        form.addStep("VALIDATION / CALCULATIONS", 0);
        form.run();
    }

    public static void requestRefund(DataStore store) {
        FormEngine form = new FormEngine(FORM_REFUND, "REQUEST TAX REFUND", store);
        form.addUniqueText("Refund ID", 4, 30, FormEngine.UNIQUE_REFUND_ID);                 // 1
        form.addRef("Taxpayer (TIN or Party ID)", FormEngine.KIND_REF_TAXPAYER);              // 2
        form.addRef("Assessment ID", FormEngine.KIND_REF_ASSESSMENT);                         // 3
        form.addMoney("Requested amount");                                                    // 4
        form.addDate("Request date");                                                         // 5
        form.addCalculated("Status", "String, PENDING, calculated");                          // 6
        form.addStep("IDENTIFICATION / PREREQUISITES", 3);
        form.addStep("REFUND DETAILS", 2);
        form.addStep("VALIDATION / CALCULATIONS", 0);
        form.run();
    }

    public static void requestClearance(DataStore store) {
        FormEngine form = new FormEngine(FORM_CLEARANCE, "REQUEST TAX CLEARANCE", store);
        form.addUniqueText("Clearance ID", 4, 30, FormEngine.UNIQUE_CLEARANCE_ID);           // 1
        form.addRef("Taxpayer (TIN or Party ID)", FormEngine.KIND_REF_TAXPAYER);              // 2
        form.addDate("Request date");                                                         // 3
        form.addAnyDate("Expiry date");                                                       // 4
        form.addCalculated("Status", "String, PENDING, calculated");                          // 5
        form.addCalculated("Approved by", "RevenueOfficer, set on approval");                 // 6
        form.addStep("IDENTIFICATION / PREREQUISITE", 2);
        form.addStep("CLEARANCE DETAILS", 2);
        form.addStep("VALIDATION / CALCULATIONS", 0);
        form.run();
    }

    private static void printBalance(TaxAssessment assessment, DataStore store) {
        System.out.println("| Assessment total   : " + assessment.getTotal());
        System.out.println("| Penalties          : " + store.getTotalPenalties(assessment.getObligation()));
        System.out.println("| Confirmed payments : " + store.getTotalPaid(assessment));
        System.out.println("| Outstanding balance: " + store.getOutstandingBalance(assessment));
    }

    // Base = active assessment total; if not assessed yet, the declared tax; null if no declaration.
    private static BigDecimal findPenaltyBase(TaxObligation obligation, DataStore store) {
        TaxDeclaration declaration = store.findActiveDeclarationForObligation(obligation);
        if (declaration == null) {
            return null;
        }
        TaxAssessment assessment = store.findActiveAssessmentForDeclaration(declaration);
        if (assessment != null) {
            return assessment.getTotal();
        }
        return declaration.getDeclaredTax();
    }

    private static void refreshPenalty(FormEngine form, DataStore store) {
        form.setText(4, null);
        form.setText(5, null);
        if (form.getText(1) == null) {
            return;
        }
        TaxObligation obligation = store.findObligationById(form.getText(1));
        if (obligation == null) {
            return;
        }
        BigDecimal base = findPenaltyBase(obligation, store);
        if (base == null) {
            return;
        }
        form.setText(4, base.toPlainString());
        if (form.getText(3) != null) {
            BigDecimal rate = form.getDecimal(3);
            form.setText(5, Penalty.calculateAmount(base, rate).toPlainString());
        }
    }

    private static void requirePenaltyInputs(FormEngine form) throws PrerequisiteMissingException {
        if (form.getText(4) == null || form.getText(5) == null) {
            throw new PrerequisiteMissingException("No base amount is available. "
                    + "The obligation needs a filed declaration first (menu option 6).");
        }
    }

    private static void precheckPenalty(Penalty penalty, DataStore store) throws ValidationException {
        for (int i = 0; i < store.getPenaltyCount(); i++) {
            if (store.getPenalty(i).sameKey(penalty)) {
                throw new DuplicateRecordException("Obligation "
                        + penalty.getObligation().getObligationId() + " already has a "
                        + penalty.getPenaltyType() + " penalty.");
            }
        }
        if (Penalty.TYPE_LATE_FILING.equals(penalty.getPenaltyType())) {
            TaxDeclaration declaration = store.findActiveDeclarationForObligation(penalty.getObligation());
            if (declaration == null) {
                throw new PrerequisiteMissingException(
                        "A late-filing penalty needs a filed declaration for the obligation.");
            }
            if (!declaration.isLate()) {
                throw new ValidationException("The declaration was filed on time, "
                        + "so no late-filing penalty applies.");
            }
        }
        if (Penalty.TYPE_LATE_PAYMENT.equals(penalty.getPenaltyType())
                && !penalty.getObligation().isOverdue()) {
            throw new ValidationException("The due date has not passed, "
                    + "so no late-payment penalty applies.");
        }
    }

    private static void precheckRefund(TaxRefund refund, DataStore store) throws ValidationException {
        for (int i = 0; i < store.getRefundCount(); i++) {
            TaxRefund other = store.getRefund(i);
            if (other.getAssessment() == refund.getAssessment()
                    && TaxRefund.STATUS_PENDING.equals(other.getStatus())) {
                throw new DuplicateRecordException("Assessment "
                        + refund.getAssessment().getAssessmentId()
                        + " already has a pending refund (" + other.getRefundId() + ").");
            }
        }
        BigDecimal available = store.getAvailableOverpayment(refund.getAssessment());
        if (available.signum() <= 0) {
            throw new OverpaymentException("No verified overpayment exists for assessment "
                    + refund.getAssessment().getAssessmentId() + ".");
        }
        if (refund.getRequestedAmount().compareTo(available) > 0) {
            throw new OverpaymentException("Requested refund " + refund.getRequestedAmount()
                    + " exceeds the verified overpayment " + available + ".");
        }
    }

    private static TaxPayment buildPayment(FormEngine f, DataStore store) {
        TaxAssessment assessment = store.findAssessmentById(f.getText(1));
        return new TaxPayment(f.getText(0), assessment, f.getText(2),
                f.getDecimal(3), f.getDate(4), f.getText(5));
    }

    private static Penalty buildPenalty(FormEngine f, DataStore store) {
        TaxObligation obligation = store.findObligationById(f.getText(1));
        // constructor order: id, obligation, type, base, rate, amount
        return new Penalty(f.getText(0), obligation, f.getText(2),
                f.getDecimal(4), f.getDecimal(3), f.getDecimal(5));
    }

    private static TaxRefund buildRefund(FormEngine f, DataStore store) {
        RegisteredTaxpayer taxpayer = store.findTaxpayerByPartyId(f.getText(1));
        TaxAssessment assessment = store.findAssessmentById(f.getText(2));
        return new TaxRefund(f.getText(0), taxpayer, assessment, f.getDecimal(3), f.getDate(4));
    }

    private static TaxClearance buildClearance(FormEngine f, DataStore store) {
        RegisteredTaxpayer taxpayer = store.findTaxpayerByPartyId(f.getText(1));
        return new TaxClearance(f.getText(0), taxpayer, f.getDate(2), f.getDate(3));
    }
}