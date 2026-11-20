package com.auca.main;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.abstracts.TaxParty;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.penalty.Penalty;
import com.auca.revenueofficer.RevenueOfficer;
import com.auca.taxassessment.TaxAssessment;
import com.auca.taxclearance.TaxClearance;
import com.auca.taxdeclaration.TaxDeclaration;
import com.auca.taxobligation.TaxObligation;
import com.auca.taxpayment.TaxPayment;
import com.auca.taxrefund.TaxRefund;
import com.auca.taxtype.TaxType;

import java.math.BigDecimal;

public class Views {

    private static void title(String name, int count) {
        System.out.println();
        System.out.println("=== " + name + " (" + count + ") ===");
        if (count == 0) {
            System.out.println("  (none)");
        }
    }

    // Works for every record type because they all implement Validatable.
    private static void show(Validatable item, boolean details) {
        System.out.println("  " + item.getSummary());
        if (details) {
            item.printDetails();
            System.out.println();
        }
    }

    public static void viewAllRecords(DataStore store) {
        System.out.println();
        System.out.println("---------------- VIEW ALL RECORDS ----------------");
        System.out.println(" [1] Short list   [2] Full details   [0] Back");
        int mode = InputHelper.parseMenuNumber(InputHelper.readLine("Select: "));
        if (mode != 1 && mode != 2) {
            if (mode != 0) {
                System.out.println("Unknown option.");
            }
            return;
        }
        boolean details = (mode == 2);

        title("INDIVIDUAL TAXPAYERS", store.getIndividualCount());
        for (int i = 0; i < store.getIndividualCount(); i++) {
            show(store.getIndividual(i), details);
        }
        title("CORPORATE TAXPAYERS", store.getCorporateCount());
        for (int i = 0; i < store.getCorporateCount(); i++) {
            show(store.getCorporate(i), details);
        }
        title("REVENUE OFFICERS", store.getOfficerCount());
        for (int i = 0; i < store.getOfficerCount(); i++) {
            show(store.getOfficer(i), details);
        }
        title("TAX TYPES", store.getTaxTypeCount());
        for (int i = 0; i < store.getTaxTypeCount(); i++) {
            show(store.getTaxType(i), details);
        }
        title("TAX OBLIGATIONS", store.getObligationCount());
        for (int i = 0; i < store.getObligationCount(); i++) {
            show(store.getObligation(i), details);
        }
        title("TAX DECLARATIONS", store.getDeclarationCount());
        for (int i = 0; i < store.getDeclarationCount(); i++) {
            show(store.getDeclaration(i), details);
        }
        title("TAX ASSESSMENTS", store.getAssessmentCount());
        for (int i = 0; i < store.getAssessmentCount(); i++) {
            show(store.getAssessment(i), details);
        }
        title("TAX PAYMENTS", store.getPaymentCount());
        for (int i = 0; i < store.getPaymentCount(); i++) {
            show(store.getPayment(i), details);
        }
        title("PENALTIES", store.getPenaltyCount());
        for (int i = 0; i < store.getPenaltyCount(); i++) {
            show(store.getPenalty(i), details);
        }
        title("TAX REFUNDS", store.getRefundCount());
        for (int i = 0; i < store.getRefundCount(); i++) {
            show(store.getRefund(i), details);
        }
        title("TAX CLEARANCES", store.getClearanceCount());
        for (int i = 0; i < store.getClearanceCount(); i++) {
            show(store.getClearance(i), details);
        }
    }

    private static RegisteredTaxpayer askTaxpayerFilter(DataStore store) throws ValidationException {
        String key = InputHelper.readLine(
                "Taxpayer TIN or Party ID (ENTER for everyone, CANCEL to go back): ");
        if (InputHelper.isCancel(key)) {
            throw new ValidationException("Cancelled.");
        }
        if (key.isEmpty()) {
            return null;
        }
        return store.requireTaxpayer(key);
    }

    public static void declarationHistory(DataStore store) {
        System.out.println();
        System.out.println("---------------- DECLARATION HISTORY ----------------");
        try {
            RegisteredTaxpayer filter = askTaxpayerFilter(store);
            int shown = 0;
            for (int i = 0; i < store.getDeclarationCount(); i++) {
                TaxDeclaration d = store.getDeclaration(i);
                if (filter == null || d.getTaxpayer() == filter) {
                    String assessmentText = "not assessed";
                    TaxAssessment a = store.findActiveAssessmentForDeclaration(d);
                    if (a != null) {
                        assessmentText = a.getAssessmentId() + " (" + a.getStatus() + ")";
                    }
                    System.out.println(d.getDeclarationId() + " | " + d.getObligation().getObligationId()
                            + " | " + d.getTaxpayer().getDisplayName()
                            + " | filed " + d.getFilingDate()
                            + " | due " + d.getObligation().getDueDate()
                            + " | " + d.getStatus()
                            + " | tax " + d.getDeclaredTax()
                            + " | " + assessmentText);
                    shown++;
                }
            }
            if (shown == 0) {
                System.out.println("(no declarations found)");
            } else {
                System.out.println(shown + " declaration(s).");
            }
        } catch (ValidationException e) {
            System.out.println("   ! " + e.getMessage());
        }
    }

    public static void assessmentHistory(DataStore store) {
        System.out.println();
        System.out.println("---------------- ASSESSMENT HISTORY ----------------");
        try {
            RegisteredTaxpayer filter = askTaxpayerFilter(store);
            int shown = 0;
            for (int i = 0; i < store.getAssessmentCount(); i++) {
                TaxAssessment a = store.getAssessment(i);
                if (filter == null || a.getTaxpayer() == filter) {
                    System.out.println(a.getAssessmentId() + " | " + a.getDeclaration().getDeclarationId()
                            + " | " + a.getTaxpayer().getDisplayName()
                            + " | " + a.getStatus() + " | " + a.getAssessmentDate());
                    System.out.println("     assessed " + a.getAssessedTax()
                            + " + adjustment " + a.getAdjustmentAmount()
                            + " = total " + a.getTotal()
                            + " | penalties " + store.getTotalPenalties(a.getObligation())
                            + " | paid " + store.getTotalPaid(a)
                            + " | outstanding " + store.getOutstandingBalance(a));
                    shown++;
                }
            }
            if (shown == 0) {
                System.out.println("(no assessments found)");
            } else {
                System.out.println(shown + " assessment(s).");
            }
        } catch (ValidationException e) {
            System.out.println("   ! " + e.getMessage());
        }
    }

    public static void taxpayerStatement(DataStore store) {
        String key = InputHelper.readLine("Taxpayer TIN or Party ID (or CANCEL): ");
        if (key.isEmpty() || InputHelper.isCancel(key)) {
            System.out.println("Cancelled.");
            return;
        }
        try {
            RegisteredTaxpayer taxpayer = store.requireTaxpayer(key);
            System.out.println();
            System.out.println("================ TAXPAYER STATEMENT ================");
            taxpayer.printDetails();

            BigDecimal totalAssessed = BigDecimal.ZERO;
            BigDecimal totalPenalties = BigDecimal.ZERO;
            BigDecimal totalPaid = BigDecimal.ZERO;
            BigDecimal totalOutstanding = BigDecimal.ZERO;
            BigDecimal totalOverpaid = BigDecimal.ZERO;
            int obligationsFound = 0;

            for (int i = 0; i < store.getObligationCount(); i++) {
                TaxObligation obligation = store.getObligation(i);
                if (obligation.getTaxpayer() == taxpayer) {
                    obligationsFound++;
                    System.out.println();
                    System.out.println("Obligation : " + obligation.getSummary());

                    TaxDeclaration declaration = store.findActiveDeclarationForObligation(obligation);
                    TaxAssessment assessment = null;
                    if (declaration == null) {
                        System.out.println("  Declaration : none filed");
                    } else {
                        System.out.println("  Declaration : " + declaration.getSummary());
                        assessment = store.findActiveAssessmentForDeclaration(declaration);
                    }
                    if (assessment == null) {
                        System.out.println("  Assessment  : none");
                    } else {
                        System.out.println("  Assessment  : " + assessment.getSummary());
                        totalAssessed = totalAssessed.add(assessment.getTotal());
                        for (int j = 0; j < store.getPaymentCount(); j++) {
                            TaxPayment payment = store.getPayment(j);
                            if (payment.getAssessment() == assessment) {
                                System.out.println("  Payment     : " + payment.getSummary());
                            }
                        }
                        totalPaid = totalPaid.add(store.getTotalPaid(assessment));
                        totalOutstanding = totalOutstanding.add(store.getOutstandingBalance(assessment));
                        totalOverpaid = totalOverpaid.add(store.getOverpayment(assessment));
                    }
                    for (int j = 0; j < store.getPenaltyCount(); j++) {
                        Penalty penalty = store.getPenalty(j);
                        if (penalty.getObligation() == obligation) {
                            System.out.println("  Penalty     : " + penalty.getSummary());
                        }
                    }
                    totalPenalties = totalPenalties.add(store.getTotalPenalties(obligation));
                }
            }
            if (obligationsFound == 0) {
                System.out.println();
                System.out.println("This taxpayer has no obligations yet.");
            }

            System.out.println();
            for (int i = 0; i < store.getRefundCount(); i++) {
                if (store.getRefund(i).getTaxpayer() == taxpayer) {
                    System.out.println("Refund     : " + store.getRefund(i).getSummary());
                }
            }
            for (int i = 0; i < store.getClearanceCount(); i++) {
                if (store.getClearance(i).getTaxpayer() == taxpayer) {
                    System.out.println("Clearance  : " + store.getClearance(i).getSummary());
                }
            }

            System.out.println();
            System.out.println("---------------- TOTALS ----------------");
            ValidationUtil.printField("Total assessed", totalAssessed);
            ValidationUtil.printField("Total penalties", totalPenalties);
            ValidationUtil.printField("Total paid", totalPaid);
            ValidationUtil.printField("Outstanding balance", totalOutstanding);
            ValidationUtil.printField("Overpaid (not refunded)", totalOverpaid);
        } catch (ValidationException e) {
            System.out.println("   ! " + e.getMessage());
        }
    }

    public static void outstandingBalances(DataStore store) {
        System.out.println();
        System.out.println("---------------- OUTSTANDING BALANCES ----------------");
        System.out.println(String.format("%-10s %-22s %12s %10s %12s %12s  %s",
                "ASSESSMENT", "TAXPAYER", "TOTAL", "PENALTIES", "PAID", "OUTSTANDING", "NOTE"));
        BigDecimal grandTotal = BigDecimal.ZERO;
        int rows = 0;
        for (int i = 0; i < store.getAssessmentCount(); i++) {
            TaxAssessment a = store.getAssessment(i);
            if (TaxAssessment.STATUS_CONFIRMED.equals(a.getStatus())) {
                BigDecimal outstanding = store.getOutstandingBalance(a);
                if (outstanding.signum() > 0) {
                    String note = "";
                    if (a.getObligation().isOverdue()) {
                        note = "OVERDUE";
                    }
                    System.out.println(String.format("%-10s %-22.22s %12s %10s %12s %12s  %s",
                            a.getAssessmentId(), a.getTaxpayer().getDisplayName(), a.getTotal(),
                            store.getTotalPenalties(a.getObligation()), store.getTotalPaid(a),
                            outstanding, note));
                    grandTotal = grandTotal.add(outstanding);
                    rows++;
                }
            }
        }
        if (rows == 0) {
            System.out.println("(nothing is outstanding)");
        }
        System.out.println();
        System.out.println("Assessments with a balance: " + rows);
        System.out.println("Grand total outstanding   : " + grandTotal);
    }


    public static void reportsMenu(DataStore store) {
        System.out.println();
        System.out.println("---------------- TAX REPORTS ----------------");
        System.out.println(" [1] Collection by tax type");
        System.out.println(" [2] Status summary");
        System.out.println(" [3] Payments by method");
        System.out.println(" [4] Storage usage");
        System.out.println(" [0] Back");
        int option = InputHelper.parseMenuNumber(InputHelper.readLine("Select: "));
        switch (option) {
            case 1:
                collectionReport(store);
                break;
            case 2:
                statusReport(store);
                break;
            case 3:
                paymentMethodReport(store);
                break;
            case 4:
                storageReport(store);
                break;
            case 0:
                break;
            default:
                System.out.println("Unknown option.");
                break;
        }
    }

    private static void collectionReport(DataStore store) {
        System.out.println();
        System.out.println(String.format("%-24s %5s %14s %12s %14s %14s",
                "TAX TYPE", "COUNT", "ASSESSED", "PENALTIES", "PAID", "BALANCE"));
        if (store.getTaxTypeCount() == 0) {
            System.out.println("(no tax types)");
        }
        for (int t = 0; t < store.getTaxTypeCount(); t++) {
            TaxType type = store.getTaxType(t);
            BigDecimal assessed = BigDecimal.ZERO;
            BigDecimal penalties = BigDecimal.ZERO;
            BigDecimal paid = BigDecimal.ZERO;
            int count = 0;
            for (int i = 0; i < store.getAssessmentCount(); i++) {
                TaxAssessment a = store.getAssessment(i);
                if (TaxAssessment.STATUS_CONFIRMED.equals(a.getStatus())
                        && a.getObligation().getTaxType() == type) {
                    assessed = assessed.add(a.getTotal());
                    penalties = penalties.add(store.getTotalPenalties(a.getObligation()));
                    paid = paid.add(store.getTotalPaid(a));
                    count++;
                }
            }
            BigDecimal balance = assessed.add(penalties).subtract(paid);
            System.out.println(String.format("%-24.24s %5d %14s %12s %14s %14s",
                    type.getDisplayName(), count, assessed, penalties, paid, balance));
        }
        System.out.println("(only CONFIRMED assessments are counted)");
    }

    private static void statusReport(DataStore store) {
        System.out.println();
        System.out.println("Declarations:");
        for (int s = 0; s < TaxDeclaration.STATUS_CHOICES.length; s++) {
            int count = 0;
            for (int i = 0; i < store.getDeclarationCount(); i++) {
                if (TaxDeclaration.STATUS_CHOICES[s].equals(store.getDeclaration(i).getStatus())) {
                    count++;
                }
            }
            System.out.println(String.format("  %-12s %d", TaxDeclaration.STATUS_CHOICES[s], count));
        }
        System.out.println("Assessments:");
        for (int s = 0; s < TaxAssessment.STATUS_CHOICES.length; s++) {
            int count = 0;
            for (int i = 0; i < store.getAssessmentCount(); i++) {
                if (TaxAssessment.STATUS_CHOICES[s].equals(store.getAssessment(i).getStatus())) {
                    count++;
                }
            }
            System.out.println(String.format("  %-12s %d", TaxAssessment.STATUS_CHOICES[s], count));
        }
        System.out.println("Refunds:");
        for (int s = 0; s < TaxRefund.STATUS_CHOICES.length; s++) {
            int count = 0;
            for (int i = 0; i < store.getRefundCount(); i++) {
                if (TaxRefund.STATUS_CHOICES[s].equals(store.getRefund(i).getStatus())) {
                    count++;
                }
            }
            System.out.println(String.format("  %-12s %d", TaxRefund.STATUS_CHOICES[s], count));
        }
        System.out.println("Clearances:");
        for (int s = 0; s < TaxClearance.STATUS_CHOICES.length; s++) {
            int count = 0;
            for (int i = 0; i < store.getClearanceCount(); i++) {
                if (TaxClearance.STATUS_CHOICES[s].equals(store.getClearance(i).getStatus())) {
                    count++;
                }
            }
            System.out.println(String.format("  %-12s %d", TaxClearance.STATUS_CHOICES[s], count));
        }
    }

    private static void paymentMethodReport(DataStore store) {
        System.out.println();
        System.out.println(String.format("%-16s %6s %16s", "METHOD", "COUNT", "TOTAL"));
        BigDecimal grand = BigDecimal.ZERO;
        for (int m = 0; m < TaxPayment.METHOD_CHOICES.length; m++) {
            int count = 0;
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < store.getPaymentCount(); i++) {
                TaxPayment payment = store.getPayment(i);
                if (TaxPayment.METHOD_CHOICES[m].equals(payment.getPaymentMethod())) {
                    count++;
                    total = total.add(payment.getAmount());
                }
            }
            grand = grand.add(total);
            System.out.println(String.format("%-16s %6d %16s",
                    TaxPayment.METHOD_CHOICES[m], count, total));
        }
        System.out.println("All payments total: " + grand);
    }

    private static void usage(String name, int count, int max) {
        System.out.println(String.format("  %-22s %4d / %d", name, count, max));
    }

    private static void storageReport(DataStore store) {
        System.out.println();
        System.out.println("Storage usage (records stored / capacity):");
        usage("Individual taxpayers", store.getIndividualCount(), DataStore.MAX_INDIVIDUALS);
        usage("Corporate taxpayers", store.getCorporateCount(), DataStore.MAX_CORPORATES);
        usage("Revenue officers", store.getOfficerCount(), DataStore.MAX_OFFICERS);
        usage("Tax types", store.getTaxTypeCount(), DataStore.MAX_TAX_TYPES);
        usage("Obligations", store.getObligationCount(), DataStore.MAX_OBLIGATIONS);
        usage("Declarations", store.getDeclarationCount(), DataStore.MAX_DECLARATIONS);
        usage("Assessments", store.getAssessmentCount(), DataStore.MAX_ASSESSMENTS);
        usage("Payments", store.getPaymentCount(), DataStore.MAX_PAYMENTS);
        usage("Penalties", store.getPenaltyCount(), DataStore.MAX_PENALTIES);
        usage("Refunds", store.getRefundCount(), DataStore.MAX_REFUNDS);
        usage("Clearances", store.getClearanceCount(), DataStore.MAX_CLEARANCES);
    }

    public static void demonstratePolymorphism(DataStore store) {
        int partyTotal = store.getIndividualCount() + store.getCorporateCount()
                + store.getOfficerCount();
        if (partyTotal == 0) {
            System.out.println("No taxpayers or officers are stored yet. "
                    + "Register some first (options 1 to 3), then run this again.");
            return;
        }

        // ---------- Part 1: TaxParty[] ----------
        TaxParty[] parties = new TaxParty[partyTotal];
        int n = 0;
        for (int i = 0; i < store.getIndividualCount(); i++) {
            parties[n] = store.getIndividual(i);
            n++;
        }
        for (int i = 0; i < store.getCorporateCount(); i++) {
            parties[n] = store.getCorporate(i);
            n++;
        }
        for (int i = 0; i < store.getOfficerCount(); i++) {
            parties[n] = store.getOfficer(i);
            n++;
        }

        System.out.println();
        System.out.println("===== PART 1: TaxParty[] with " + parties.length + " element(s) =====");
        System.out.println("Every variable has the type TaxParty. Java picks the method of the");
        System.out.println("REAL object while the program runs (runtime polymorphism).");
        for (int i = 0; i < parties.length; i++) {
            TaxParty party = parties[i];
            System.out.println();
            System.out.println((i + 1) + ") real object   : " + party.getClass().getSimpleName());
            System.out.println("   getRoleName()    : " + party.getRoleName());
            System.out.println("   getDisplayName() : " + party.getDisplayName());
            System.out.println("   getSummary()     : " + party.getSummary());
            System.out.println("   isActive()       : " + party.isActive());
            if (party instanceof RevenueOfficer) {
                RevenueOfficer officer = (RevenueOfficer) party;
                System.out.println("   officer only     : approval limit " + officer.getApprovalLimit()
                        + ", can approve 1000.00? " + officer.canApprove(new BigDecimal("1000.00")));
            } else if (party instanceof RegisteredTaxpayer) {
                RegisteredTaxpayer taxpayer = (RegisteredTaxpayer) party;
                System.out.println("   taxpayer only    : TIN " + taxpayer.getTin()
                        + ", annual turnover " + taxpayer.getAnnualTurnover());
            }
        }

        // ---------- Part 2: Identifiable[] ----------
        int total = partyTotal + store.getTaxTypeCount() + store.getObligationCount()
                + store.getDeclarationCount() + store.getAssessmentCount()
                + store.getPaymentCount() + store.getPenaltyCount()
                + store.getRefundCount() + store.getClearanceCount();
        Identifiable[] items = new Identifiable[total];
        int k = 0;
        for (int i = 0; i < parties.length; i++) {
            items[k] = parties[i];
            k++;
        }
        for (int i = 0; i < store.getTaxTypeCount(); i++) {
            items[k] = store.getTaxType(i);
            k++;
        }
        for (int i = 0; i < store.getObligationCount(); i++) {
            items[k] = store.getObligation(i);
            k++;
        }
        for (int i = 0; i < store.getDeclarationCount(); i++) {
            items[k] = store.getDeclaration(i);
            k++;
        }
        for (int i = 0; i < store.getAssessmentCount(); i++) {
            items[k] = store.getAssessment(i);
            k++;
        }
        for (int i = 0; i < store.getPaymentCount(); i++) {
            items[k] = store.getPayment(i);
            k++;
        }
        for (int i = 0; i < store.getPenaltyCount(); i++) {
            items[k] = store.getPenalty(i);
            k++;
        }
        for (int i = 0; i < store.getRefundCount(); i++) {
            items[k] = store.getRefund(i);
            k++;
        }
        for (int i = 0; i < store.getClearanceCount(); i++) {
            items[k] = store.getClearance(i);
            k++;
        }

        System.out.println();
        System.out.println("===== PART 2: Identifiable[] with " + items.length + " element(s) =====");
        System.out.println("Different classes, one interface type.");
        for (int i = 0; i < items.length; i++) {
            System.out.println(String.format("%-20s | %-10s | %-40.40s | active=%s",
                    items[i].getClass().getSimpleName(), items[i].getId(),
                    items[i].getDisplayName(), items[i].isActive()));
        }
        System.out.println();
        System.out.println("Note: 'new TaxParty(...)' or 'new RegisteredTaxpayer(...)' does not compile,");
        System.out.println("because both are abstract. Only the concrete subclasses are created.");
    }
}