package com.auca.main;

public class Main {

    public static void main(String[] args) {
        DataStore store = new DataStore();
        boolean running = true;

        System.out.println("Tax Administration Management System (teaching simulation).");
        System.out.println("All rates, deadlines and amounts are fictional, not official tax law.");

        while (running) {
            printMenu();
            String text = InputHelper.readLine("Select option: ");
            int option = InputHelper.parseMenuNumber(text);
            try {
                running = handleOption(option, store);
            } catch (RuntimeException e) {

                System.out.println("An unexpected problem occurred (" + e.getClass().getSimpleName()
                        + "). Returning to the main menu.");
            }
        }
    }


    private static boolean handleOption(int option, DataStore store) {
        switch (option) {
            case 0:
                System.out.println("Goodbye.");
                return false;
            case 1:
                FormActions.registerIndividual(store);
                break;
            case 2:
                FormActions.registerCorporate(store);
                break;
            case 3:
                FormActions.registerOfficer(store);
                break;
            case 4:
                FormActions.registerTaxType(store);
                break;
            case 5:
                FormActions.createObligation(store);
                break;
            case 6:
                FormActions.fileDeclaration(store);
                break;
            case 7:
                Workflows.assessDeclaration(store);
                break;
            case 8:
                Workflows.confirmAssessment(store);
                break;
            case 9:
                Views.viewAllRecords(store);
                break;
            case 10:
                Views.declarationHistory(store);
                break;
            case 11:
                Views.assessmentHistory(store);
                break;
            case 12:
                FinanceForms.recordPayment(store);
                break;
            case 13:
                FinanceForms.recordPenalty(store);
                break;
            case 14:
                ApprovalWorkflows.refundMenu(store);
                break;
            case 15:
                ApprovalWorkflows.clearanceMenu(store);
                break;
            case 16:
                SearchUpdate.search(store);
                break;
            case 17:
                SearchUpdate.updateMenu(store);
                break;
            case 18:
                Views.taxpayerStatement(store);
                break;
            case 19:
                Views.outstandingBalances(store);
                break;
            case 20:
                Views.reportsMenu(store);
                break;
            case 21:
                Views.demonstratePolymorphism(store);
                break;
            default:
                System.out.println("Unknown option. Please enter a number from 0 to 21.");
                break;
        }
        return true;
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("================ TAX ADMINISTRATION MANAGEMENT ================");
        System.out.println(" 1 Register Individual Taxpayer     12 Record Tax Payment");
        System.out.println(" 2 Register Corporate Taxpayer      13 Calculate / Record Penalty");
        System.out.println(" 3 Register Revenue Officer         14 Request / Approve Refund");
        System.out.println(" 4 Register Tax Type                15 Request / Issue Tax Clearance");
        System.out.println(" 5 Create Tax Obligation            16 Search Record by ID / TIN");
        System.out.println(" 6 File Tax Declaration             17 Update Eligible Record");
        System.out.println(" 7 Assess Declaration               18 View Taxpayer Statement");
        System.out.println(" 8 Confirm Assessment               19 View Outstanding Balances");
        System.out.println(" 9 View All Records                 20 View Tax Reports");
        System.out.println("10 View Declaration History         21 Demonstrate Polymorphism");
        System.out.println("11 View Assessment History           0 Exit");
    }
}