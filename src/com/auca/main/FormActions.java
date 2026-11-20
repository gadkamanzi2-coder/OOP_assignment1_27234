package com.auca.main;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.corporatetaxpayer.CorporateTaxpayer;
import com.auca.exceptions.DuplicateRecordException;
import com.auca.exceptions.InvalidTaxPeriodException;
import com.auca.exceptions.ValidationException;
import com.auca.individualtaxpayer.IndividualTaxpayer;
import com.auca.revenueofficer.RevenueOfficer;
import com.auca.taxassessment.TaxAssessment;
import com.auca.taxdeclaration.TaxDeclaration;
import com.auca.taxobligation.TaxObligation;
import com.auca.taxtype.TaxType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FormActions {

    public static final int FORM_INDIVIDUAL = 1;
    public static final int FORM_CORPORATE = 2;
    public static final int FORM_OFFICER = 3;
    public static final int FORM_TAX_TYPE = 4;
    public static final int FORM_OBLIGATION = 5;
    public static final int FORM_DECLARATION = 6;
    public static final int FORM_ASSESSMENT = 7;

    public static void refresh(int formType, FormEngine form, DataStore store) {
        if (formType == FORM_DECLARATION) {
            if (form.getText(1) != null && form.getText(2) != null) {
                TaxObligation obligation = store.findObligationById(form.getText(1));
                if (obligation != null && obligation.getDueDate() != null) {
                    if (form.getDate(2).isAfter(obligation.getDueDate())) {
                        form.setText(5, TaxDeclaration.STATUS_LATE);
                    } else {
                        form.setText(5, TaxDeclaration.STATUS_SUBMITTED);
                    }
                }
            }
        } else if (formType == FORM_ASSESSMENT) {
            form.setText(5, TaxAssessment.STATUS_DRAFT);
        } else {
            FinanceForms.refresh(formType, form, store);
        }
    }

    public static void check(int formType, FormEngine form, DataStore store)
            throws ValidationException {
        refresh(formType, form, store);
        switch (formType) {
            case FORM_INDIVIDUAL:
                buildIndividual(form).validate();
                break;
            case FORM_CORPORATE:
                buildCorporate(form).validate();
                break;
            case FORM_OFFICER:
                RevenueOfficer officer = buildOfficer(form);
                officer.validate();
                System.out.println("| This officer can approve refunds up to "
                        + officer.getApprovalLimit() + " (active: " + officer.isActive() + ").");
                break;
            case FORM_TAX_TYPE:
                TaxType type = buildTaxType(form);
                type.validate();
                BigDecimal sample = new BigDecimal("1000000.00");
                System.out.println("| Example calculation: tax on " + sample + " at "
                        + type.getRate() + "% = " + type.calculateTax(sample));
                break;
            case FORM_OBLIGATION: {
                TaxObligation obligation = buildObligation(form, store);
                System.out.println("| Taxpayer: " + ValidationUtil.describe(obligation.getTaxpayer()));
                System.out.println("| Tax type: " + ValidationUtil.describe(obligation.getTaxType()));
                obligation.validate();
                for (int i = 0; i < store.getObligationCount(); i++) {
                    if (store.getObligation(i).overlaps(obligation)) {
                        throw new InvalidTaxPeriodException("This period overlaps existing obligation "
                                + store.getObligation(i).getObligationId()
                                + " for the same taxpayer and tax type.");
                    }
                }
                System.out.println("| Period is valid and does not overlap any existing obligation.");
                break;
            }
            case FORM_DECLARATION: {
                TaxDeclaration declaration = buildDeclaration(form, store);
                System.out.println("| Due date: " + declaration.getObligation().getDueDate()
                        + " | Filing date: " + declaration.getFilingDate()
                        + " | Late: " + (declaration.isLate() ? "YES" : "NO"));
                System.out.println("| Expected tax = " + declaration.getTaxableAmount() + " x "
                        + declaration.getObligation().getTaxType().getRate() + " / 100 = "
                        + declaration.getExpectedTax());
                declaration.validate();
                TaxDeclaration existing =
                        store.findActiveDeclarationForObligation(declaration.getObligation());
                if (existing != null) {
                    throw new DuplicateRecordException("Obligation "
                            + declaration.getObligation().getObligationId()
                            + " already has an active declaration (" + existing.getDeclarationId() + ").");
                }
                System.out.println("| Declared tax matches the expected tax. Status will be "
                        + declaration.getStatus() + ".");
                break;
            }
            case FORM_ASSESSMENT: {
                TaxAssessment assessment = buildAssessment(form, store);
                System.out.println("| Declared tax on the declaration: "
                        + assessment.getDeclaration().getDeclaredTax());
                System.out.println("| Total = assessed tax + adjustment = " + assessment.getTotal());
                assessment.validate();
                TaxAssessment existing =
                        store.findActiveAssessmentForDeclaration(assessment.getDeclaration());
                if (existing != null) {
                    throw new DuplicateRecordException("Declaration "
                            + assessment.getDeclaration().getDeclarationId()
                            + " already has an assessment (" + existing.getAssessmentId() + ").");
                }
                break;
            }
            default:
                FinanceForms.check(formType, form, store);
                break;
        }
    }

    public static void save(int formType, FormEngine form, DataStore store)
            throws ValidationException {
        refresh(formType, form, store);
        switch (formType) {
            case FORM_INDIVIDUAL:
                IndividualTaxpayer individual = buildIndividual(form);
                store.addIndividual(individual);
                System.out.println();
                System.out.println("SAVED: " + individual.getSummary());
                System.out.println("Individual taxpayers stored: " + store.getIndividualCount());
                break;
            case FORM_CORPORATE:
                CorporateTaxpayer corporate = buildCorporate(form);
                store.addCorporate(corporate);
                System.out.println();
                System.out.println("SAVED: " + corporate.getSummary());
                System.out.println("Corporate taxpayers stored: " + store.getCorporateCount());
                break;
            case FORM_OFFICER:
                RevenueOfficer officer = buildOfficer(form);
                store.addOfficer(officer);
                System.out.println();
                System.out.println("SAVED: " + officer.getSummary());
                System.out.println("Revenue officers stored: " + store.getOfficerCount());
                break;
            case FORM_TAX_TYPE:
                TaxType type = buildTaxType(form);
                store.addTaxType(type);
                System.out.println();
                System.out.println("SAVED: " + type.getSummary());
                System.out.println("Tax types stored: " + store.getTaxTypeCount());
                break;
            case FORM_OBLIGATION: {
                TaxObligation obligation = buildObligation(form, store);
                store.addObligation(obligation);
                System.out.println();
                System.out.println("SAVED: " + obligation.getSummary());
                System.out.println("Obligations stored: " + store.getObligationCount());
                break;
            }
            case FORM_DECLARATION: {
                TaxDeclaration declaration = buildDeclaration(form, store);
                store.addDeclaration(declaration);
                System.out.println();
                System.out.println("SAVED: " + declaration.getSummary());
                System.out.println("Declarations stored: " + store.getDeclarationCount());
                break;
            }
            case FORM_ASSESSMENT: {
                TaxAssessment assessment = buildAssessment(form, store);
                store.addAssessment(assessment);
                System.out.println();
                System.out.println("SAVED: " + assessment.getSummary());
                System.out.println("Declaration " + assessment.getDeclaration().getDeclarationId()
                        + " is now " + assessment.getDeclaration().getStatus() + ".");
                System.out.println("The assessment is DRAFT. Use menu option 8 to confirm it.");
                break;
            }
            default:
                FinanceForms.save(formType, form, store);
                break;
        }
    }

    private static void addPersonFields(FormEngine form) {
        form.addUniqueText("Party ID", 4, 30, FormEngine.UNIQUE_PARTY_ID);
        form.addText("First name", 2, 60);
        form.addText("Last name", 2, 60);
        form.addEmail();
        form.addPhone();
        form.addDate("Date of birth");
    }

    private static void addRegistrationFields(FormEngine form) {
        form.addDigits("TIN", 9, FormEngine.UNIQUE_TIN);
        form.addDate("Registration date");
        form.addChoice("Taxpayer status", RegisteredTaxpayer.STATUS_CHOICES);
        form.addText("District", 2, 60);
        form.addText("Sector", 2, 60);
        form.addText("Tax office", 2, 60);
    }

    public static void registerIndividual(DataStore store) {
        FormEngine form = new FormEngine(FORM_INDIVIDUAL, "REGISTER INDIVIDUAL TAXPAYER", store);
        addPersonFields(form);                                               // fields 1-6
        addRegistrationFields(form);                                         // fields 7-12
        form.addDigits("National ID", 16, FormEngine.UNIQUE_NATIONAL_ID);   // 13
        form.addText("Occupation", 2, 60);                                   // 14
        form.addText("Business name", 2, 60);                                // 15
        form.addDate("Business start date");                                 // 16
        form.addMoney("Annual turnover");                                    // 17
        form.addYesNo("Resident");                                           // 18
        form.addStep("PERSONAL DETAILS", 6);
        form.addStep("TAXPAYER REGISTRATION (TIN, STATUS, OFFICE)", 6);
        form.addStep("INDIVIDUAL BUSINESS DETAILS", 6);
        form.run();
    }

    public static void registerCorporate(DataStore store) {
        FormEngine form = new FormEngine(FORM_CORPORATE, "REGISTER CORPORATE TAXPAYER", store);
        addPersonFields(form);                                               // fields 1-6 (contact person)
        addRegistrationFields(form);                                         // fields 7-12
        form.addCode("Company registration no", 5, 30, FormEngine.UNIQUE_COMPANY_NO);  // 13
        form.addText("Legal name", 3, 120);                                  // 14
        form.addDate("Incorporation date");                                  // 15
        form.addText("Business sector", 2, 60);                              // 16
        form.addText("Authorized representative", 2, 60);                    // 17
        form.addMoney("Annual turnover");                                    // 18
        form.addStep("CONTACT PERSON DETAILS", 6);
        form.addStep("TAXPAYER REGISTRATION (TIN, STATUS, OFFICE)", 6);
        form.addStep("COMPANY DETAILS", 6);
        form.run();
    }

    public static void registerOfficer(DataStore store) {
        FormEngine form = new FormEngine(FORM_OFFICER, "REGISTER REVENUE OFFICER", store);
        addPersonFields(form);                                               // fields 1-6
        form.addCode("Staff number", 3, 20, FormEngine.UNIQUE_STAFF_NO);    // 7
        form.addText("Department", 2, 60);                                   // 8
        form.addText("Role", 2, 60);                                         // 9
        form.addDate("Hire date");                                           // 10
        form.addMoney("Approval limit");                                     // 11
        form.addYesNo("Active");                                             // 12
        form.addStep("PERSONAL DETAILS", 6);
        form.addStep("STAFF DETAILS", 6);
        form.addStep("VALIDATION / CALCULATIONS", 0);
        form.run();
    }

    public static void registerTaxType(DataStore store) {
        FormEngine form = new FormEngine(FORM_TAX_TYPE, "REGISTER TAX TYPE", store);
        form.addUniqueText("Tax type ID", 4, 30, FormEngine.UNIQUE_TAX_TYPE_ID);   // 1
        form.addCode("Tax code", 2, 20, FormEngine.UNIQUE_TAX_CODE);               // 2
        form.addText("Tax name", 2, 60);                                           // 3
        form.addRate("Rate (%)");                                                  // 4
        form.addChoice("Filing frequency", TaxType.FREQUENCY_CHOICES);             // 5
        form.addYesNo("Active");                                                   // 6
        form.addStep("IDENTIFICATION", 2);
        form.addStep("TAX DETAILS", 4);
        form.addStep("VALIDATION / CALCULATIONS", 0);
        form.run();
    }

    public static void createObligation(DataStore store) {
        FormEngine form = new FormEngine(FORM_OBLIGATION, "CREATE TAX OBLIGATION", store);
        form.addUniqueText("Obligation ID", 4, 30, FormEngine.UNIQUE_OBLIGATION_ID);  // 1
        form.addRef("Taxpayer (TIN or Party ID)", FormEngine.KIND_REF_TAXPAYER);       // 2
        form.addRef("Tax type (ID or code)", FormEngine.KIND_REF_TAX_TYPE);            // 3
        form.addAnyDate("Period start");                                               // 4
        form.addAnyDate("Period end");                                                 // 5
        form.addAnyDate("Due date");                                                   // 6
        form.addStep("IDENTIFICATION / PREREQUISITES", 3);
        form.addStep("OBLIGATION PERIOD DETAILS", 3);
        form.addStep("VALIDATION / CALCULATIONS", 0);
        form.run();
    }

    public static void fileDeclaration(DataStore store) {
        FormEngine form = new FormEngine(FORM_DECLARATION, "FILE TAX DECLARATION", store);
        form.addUniqueText("Declaration ID", 4, 30, FormEngine.UNIQUE_DECLARATION_ID);  // 1
        form.addRef("Obligation ID", FormEngine.KIND_REF_OBLIGATION_FILING);             // 2
        form.addDate("Filing date");                                                     // 3
        form.addMoney("Taxable amount");                                                 // 4
        form.addMoney("Declared tax");                                                   // 5
        form.addCalculated("Status", "String, SUBMITTED/LATE, calculated");              // 6
        form.addStep("IDENTIFICATION / PREREQUISITE", 2);
        form.addStep("DECLARATION DETAILS", 3);
        form.addStep("VALIDATION / CALCULATIONS", 0);
        form.run();
    }

    public static void createAssessment(DataStore store) {
        FormEngine form = new FormEngine(FORM_ASSESSMENT, "ASSESS DECLARATION", store);
        form.addUniqueText("Assessment ID", 4, 30, FormEngine.UNIQUE_ASSESSMENT_ID);    // 1
        form.addRef("Declaration ID", FormEngine.KIND_REF_DECLARATION);                  // 2
        form.addMoney("Assessed tax");                                                   // 3
        form.addSignedMoney("Adjustment amount");                                        // 4
        form.addDate("Assessment date");                                                 // 5
        form.addCalculated("Status", "String, DRAFT, calculated");                       // 6
        form.addStep("IDENTIFICATION / PREREQUISITE", 2);
        form.addStep("ASSESSMENT DETAILS", 3);
        form.addStep("VALIDATION / CALCULATIONS", 0);
        form.run();
    }

    private static IndividualTaxpayer buildIndividual(FormEngine f) {
        return new IndividualTaxpayer(
                f.getText(0), f.getText(1), f.getText(2), f.getText(3), f.getText(4),
                f.getDate(5),
                f.getText(6), f.getDate(7), f.getText(8), f.getText(9), f.getText(10),
                f.getText(11),
                f.getText(12), f.getText(13), f.getText(14), f.getDate(15),
                f.getDecimal(16), f.getYesNo(17));
    }

    private static CorporateTaxpayer buildCorporate(FormEngine f) {
        return new CorporateTaxpayer(
                f.getText(0), f.getText(1), f.getText(2), f.getText(3), f.getText(4),
                f.getDate(5),
                f.getText(6), f.getDate(7), f.getText(8), f.getText(9), f.getText(10),
                f.getText(11),
                f.getText(12), f.getText(13), f.getDate(14), f.getText(15), f.getText(16),
                f.getDecimal(17));
    }

    private static RevenueOfficer buildOfficer(FormEngine f) {
        return new RevenueOfficer(
                f.getText(0), f.getText(1), f.getText(2), f.getText(3), f.getText(4),
                f.getDate(5),
                f.getText(6), f.getText(7), f.getText(8), f.getDate(9),
                f.getDecimal(10), f.getYesNo(11));
    }

    private static TaxType buildTaxType(FormEngine f) {
        return new TaxType(
                f.getText(0), f.getText(1), f.getText(2), f.getDecimal(3),
                f.getText(4), f.getYesNo(5));
    }

    private static TaxObligation buildObligation(FormEngine f, DataStore store) {
        RegisteredTaxpayer taxpayer = store.findTaxpayerByPartyId(f.getText(1));
        TaxType taxType = store.findTaxTypeById(f.getText(2));
        return new TaxObligation(f.getText(0), taxpayer, taxType,
                f.getDate(3), f.getDate(4), f.getDate(5));
    }

    private static TaxDeclaration buildDeclaration(FormEngine f, DataStore store) {
        TaxObligation obligation = store.findObligationById(f.getText(1));
        return new TaxDeclaration(f.getText(0), obligation, f.getDate(2),
                f.getDecimal(3), f.getDecimal(4), f.getText(5));
    }

    private static TaxAssessment buildAssessment(FormEngine f, DataStore store) {
        TaxDeclaration declaration = store.findDeclarationById(f.getText(1));
        return new TaxAssessment(f.getText(0), declaration, f.getDecimal(2),
                f.getDecimal(3), f.getDate(4), f.getText(5));
    }
}