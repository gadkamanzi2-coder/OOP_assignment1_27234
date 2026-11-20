package com.auca.main;

import com.auca.exceptions.InvalidStatusTransitionException;
import com.auca.exceptions.ValidationException;
import com.auca.revenueofficer.RevenueOfficer;
import com.auca.taxclearance.TaxClearance;
import com.auca.taxrefund.TaxRefund;


public class ApprovalWorkflows {

    // ---------------- Menu option 14 ----------------
    public static void refundMenu(DataStore store) {
        System.out.println();
        System.out.println("---------------- REQUEST / APPROVE REFUND ----------------");
        System.out.println(" [1] Request a refund");
        System.out.println(" [2] Approve or reject a pending refund");
        System.out.println(" [0] Back to main menu");
        int option = InputHelper.parseMenuNumber(InputHelper.readLine("Select: "));
        if (option == 1) {
            FinanceForms.requestRefund(store);
        } else if (option == 2) {
            decideRefund(store);
        } else if (option != 0) {
            System.out.println("Unknown option.");
        }
    }

    private static void decideRefund(DataStore store) {
        String id = InputHelper.readLine("Refund ID (or CANCEL): ");
        if (id.isEmpty() || InputHelper.isCancel(id)) {
            System.out.println("Cancelled. No change was made.");
            return;
        }
        TaxRefund refund = null;
        try {
            refund = store.requireRefund(id);
            refund.printDetails();
            ValidationUtil.printField("Verified overpayment left",
                    store.getAvailableOverpayment(refund.getAssessment()));
            if (!TaxRefund.STATUS_PENDING.equals(refund.getStatus())) {
                throw new InvalidStatusTransitionException(
                        "Only a PENDING refund can be decided (current status: "
                                + refund.getStatus() + ").");
            }
            RevenueOfficer officer = askOfficer(store);
            if (officer == null) {
                System.out.println("Cancelled. The refund is still " + refund.getStatus() + ".");
                return;
            }
            int option = InputHelper.parseMenuNumber(
                    InputHelper.readLine("[1] APPROVE   [2] REJECT   [0] CANCEL : "));
            if (option == 1) {
                store.approveRefund(refund, officer);
                System.out.println("Refund " + refund.getRefundId() + " is now APPROVED by "
                        + ValidationUtil.describe(refund.getApprovedBy()) + ".");
            } else if (option == 2) {
                refund.reject();
                System.out.println("Refund " + refund.getRefundId() + " is now REJECTED.");
            } else {
                System.out.println("Cancelled. The refund is still " + refund.getStatus() + ".");
            }
        } catch (ValidationException e) {
            System.out.println("   ! " + e.getMessage());
            if (refund != null) {
                System.out.println("   No change was made. The refund is still "
                        + refund.getStatus() + ".");
            }
        }
    }


    public static void clearanceMenu(DataStore store) {
        System.out.println();
        System.out.println("---------------- REQUEST / ISSUE TAX CLEARANCE ----------------");
        System.out.println(" [1] Request a clearance");
        System.out.println(" [2] Approve or reject a pending clearance");
        System.out.println(" [0] Back to main menu");
        int option = InputHelper.parseMenuNumber(InputHelper.readLine("Select: "));
        if (option == 1) {
            FinanceForms.requestClearance(store);
        } else if (option == 2) {
            decideClearance(store);
        } else if (option != 0) {
            System.out.println("Unknown option.");
        }
    }

    private static void decideClearance(DataStore store) {
        String id = InputHelper.readLine("Clearance ID (or CANCEL): ");
        if (id.isEmpty() || InputHelper.isCancel(id)) {
            System.out.println("Cancelled. No change was made.");
            return;
        }
        TaxClearance clearance = null;
        try {
            clearance = store.requireClearance(id);
            clearance.printDetails();
            try {
                store.checkClearanceEligibility(clearance.getTaxpayer());
                System.out.println("Eligibility check: ELIGIBLE.");
            } catch (ValidationException e) {
                System.out.println("Eligibility check: NOT ELIGIBLE - " + e.getMessage());
            }
            if (!TaxClearance.STATUS_PENDING.equals(clearance.getStatus())) {
                throw new InvalidStatusTransitionException(
                        "Only a PENDING clearance can be decided (current status: "
                                + clearance.getStatus() + ").");
            }
            RevenueOfficer officer = askOfficer(store);
            if (officer == null) {
                System.out.println("Cancelled. The clearance is still " + clearance.getStatus() + ".");
                return;
            }
            int option = InputHelper.parseMenuNumber(
                    InputHelper.readLine("[1] APPROVE   [2] REJECT   [0] CANCEL : "));
            if (option == 1) {
                store.approveClearance(clearance, officer);
                System.out.println("Clearance " + clearance.getClearanceId() + " is now APPROVED by "
                        + ValidationUtil.describe(clearance.getApprovedBy())
                        + ", valid until " + clearance.getExpiryDate() + ".");
            } else if (option == 2) {
                clearance.reject();
                System.out.println("Clearance " + clearance.getClearanceId() + " is now REJECTED.");
            } else {
                System.out.println("Cancelled. The clearance is still " + clearance.getStatus() + ".");
            }
        } catch (ValidationException e) {
            System.out.println("   ! " + e.getMessage());
            if (clearance != null) {
                System.out.println("   No change was made. The clearance is still "
                        + clearance.getStatus() + ".");
            }
        }
    }

    private static RevenueOfficer askOfficer(DataStore store) throws ValidationException {
        String text = InputHelper.readLine("Officer (Party ID or staff number, or CANCEL): ");
        if (text.isEmpty() || InputHelper.isCancel(text)) {
            return null;
        }
        RevenueOfficer officer = store.requireOfficer(text);
        System.out.println("Officer: " + ValidationUtil.describe(officer)
                + " | approval limit " + officer.getApprovalLimit()
                + " | active " + officer.isActive());
        return officer;
    }
}