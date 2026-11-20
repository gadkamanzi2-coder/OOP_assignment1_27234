package com.auca.main;

import com.auca.exceptions.InvalidStatusTransitionException;
import com.auca.exceptions.ValidationException;
import com.auca.taxassessment.TaxAssessment;
import com.auca.taxdeclaration.TaxDeclaration;


public class Workflows {

    public static void assessDeclaration(DataStore store) {
        System.out.println();
        System.out.println("---------------- ASSESS DECLARATION ----------------");
        System.out.println(" [1] Create an assessment for a declaration");
        System.out.println(" [2] Reject a declaration");
        System.out.println(" [0] Back to main menu");
        int option = InputHelper.parseMenuNumber(InputHelper.readLine("Select: "));
        if (option == 1) {
            FormActions.createAssessment(store);
        } else if (option == 2) {
            rejectDeclaration(store);
        } else if (option != 0) {
            System.out.println("Unknown option.");
        }
    }

    private static void rejectDeclaration(DataStore store) {
        String id = InputHelper.readLine("Declaration ID to reject (or CANCEL): ");
        if (id.isEmpty() || InputHelper.isCancel(id)) {
            System.out.println("Cancelled. No change was made.");
            return;
        }
        try {
            TaxDeclaration declaration = store.requireDeclaration(id);
            if (store.findActiveAssessmentForDeclaration(declaration) != null) {
                throw new InvalidStatusTransitionException(
                        "This declaration already has an assessment and cannot be rejected.");
            }
            declaration.printDetails();
            String answer = InputHelper.readLine("Type YES to reject it, anything else cancels: ");
            if (!answer.equalsIgnoreCase("YES")) {
                System.out.println("Cancelled. No change was made.");
                return;
            }
            declaration.reject();
            System.out.println("Declaration " + declaration.getDeclarationId() + " is now REJECTED.");
        } catch (ValidationException e) {
            System.out.println("   ! " + e.getMessage());
        }
    }

    // Menu option 8
    public static void confirmAssessment(DataStore store) {
        System.out.println();
        System.out.println("---------------- CONFIRM ASSESSMENT ----------------");
        String id = InputHelper.readLine("Assessment ID to confirm (or CANCEL): ");
        if (id.isEmpty() || InputHelper.isCancel(id)) {
            System.out.println("Cancelled. No change was made.");
            return;
        }
        try {
            TaxAssessment assessment = store.requireAssessment(id);
            assessment.printDetails();
            ValidationUtil.printField("Total", assessment.getTotal());
            if (!TaxAssessment.STATUS_DRAFT.equals(assessment.getStatus())) {
                throw new InvalidStatusTransitionException(
                        "Only a DRAFT assessment can be confirmed (current status: "
                                + assessment.getStatus() + ").");
            }
            assessment.validate();   // revalidate before the status changes
            int option = InputHelper.parseMenuNumber(
                    InputHelper.readLine("[1] CONFIRM   [0] CANCEL : "));
            if (option == 1) {
                assessment.confirm();
                System.out.println("Assessment " + assessment.getAssessmentId() + " is now CONFIRMED.");
                System.out.println("Outstanding balance: " + store.getOutstandingBalance(assessment));
            } else {
                System.out.println("Cancelled. The assessment is still " + assessment.getStatus() + ".");
            }
        } catch (ValidationException e) {
            System.out.println("   ! " + e.getMessage());
        }
    }
}