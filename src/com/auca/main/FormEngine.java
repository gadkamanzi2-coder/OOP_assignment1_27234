package com.auca.main;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.exceptions.DuplicateRecordException;
import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.RecordNotFoundException;
import com.auca.exceptions.ValidationException;
import com.auca.taxassessment.TaxAssessment;
import com.auca.taxdeclaration.TaxDeclaration;
import com.auca.taxobligation.TaxObligation;
import com.auca.taxtype.TaxType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FormEngine {
    public static final int KIND_TEXT = 1;
    public static final int KIND_DIGITS = 2;
    public static final int KIND_CODE = 3;
    public static final int KIND_EMAIL = 4;
    public static final int KIND_PHONE = 5;
    public static final int KIND_DATE = 6;               // real date, not in the future
    public static final int KIND_MONEY = 7;
    public static final int KIND_RATE = 8;
    public static final int KIND_CHOICE = 9;
    public static final int KIND_YES_NO = 10;
    public static final int KIND_DATE_ANY = 11;          // real date, future allowed
    public static final int KIND_SIGNED_MONEY = 12;      // may be negative
    public static final int KIND_CALC = 13;              // calculated by the program
    public static final int KIND_REF_TAXPAYER = 14;
    public static final int KIND_REF_TAX_TYPE = 15;
    public static final int KIND_REF_OBLIGATION_FILING = 16;  // obligation with no active declaration
    public static final int KIND_REF_OBLIGATION = 17;
    public static final int KIND_REF_DECLARATION = 18;        // declaration that can be assessed
    public static final int KIND_REF_ASSESSMENT_PAYABLE = 19; // CONFIRMED assessment with a balance
    public static final int KIND_REF_ASSESSMENT = 20;         // any assessment that is not cancelled

    public static final int UNIQUE_NONE = 0;
    public static final int UNIQUE_PARTY_ID = 1;
    public static final int UNIQUE_TIN = 2;
    public static final int UNIQUE_NATIONAL_ID = 3;
    public static final int UNIQUE_COMPANY_NO = 4;
    public static final int UNIQUE_STAFF_NO = 5;
    public static final int UNIQUE_TAX_TYPE_ID = 6;
    public static final int UNIQUE_TAX_CODE = 7;
    public static final int UNIQUE_OBLIGATION_ID = 8;
    public static final int UNIQUE_DECLARATION_ID = 9;
    public static final int UNIQUE_ASSESSMENT_ID = 10;
    public static final int UNIQUE_PAYMENT_ID = 11;
    public static final int UNIQUE_PAYMENT_REF = 12;
    public static final int UNIQUE_PENALTY_ID = 13;
    public static final int UNIQUE_REFUND_ID = 14;
    public static final int UNIQUE_CLEARANCE_ID = 15;

    public static final int RESULT_CANCELLED = 0;
    public static final int RESULT_SAVED = 1;

    private static final int MOVE_NEXT = 1;
    private static final int MOVE_BACK = 2;
    private static final int MOVE_CANCEL = 3;
    private static final int MOVE_SAVED = 4;

    private static final int MAX_FIELDS = 20;
    private static final int ENTRY_STEPS = 3;   // steps 1-3. Step 4 is always the review.

    private final int formType;
    private final String title;
    private final DataStore store;


    private String[] labels = new String[MAX_FIELDS];
    private String[] hints = new String[MAX_FIELDS];
    private int[] kinds = new int[MAX_FIELDS];
    private int[] minimums = new int[MAX_FIELDS];
    private int[] maximums = new int[MAX_FIELDS];
    private String[][] allowedValues = new String[MAX_FIELDS][];
    private int[] uniqueRules = new int[MAX_FIELDS];
    private String[] values = new String[MAX_FIELDS];
    private int fieldCount = 0;


    private String[] stepTitles = new String[ENTRY_STEPS];
    private int[] stepFirstField = new int[ENTRY_STEPS];
    private int[] stepFieldTotal = new int[ENTRY_STEPS];
    private int stepCount = 0;
    private int fieldsAssigned = 0;

    public FormEngine(int formType, String title, DataStore store) {
        this.formType = formType;
        this.title = title;
        this.store = store;
    }


    private void register(String label, String hint, int kind, int min, int max,
                          String[] allowed, int uniqueRule) {
        labels[fieldCount] = label;
        hints[fieldCount] = hint;
        kinds[fieldCount] = kind;
        minimums[fieldCount] = min;
        maximums[fieldCount] = max;
        allowedValues[fieldCount] = allowed;
        uniqueRules[fieldCount] = uniqueRule;
        values[fieldCount] = null;
        fieldCount++;
    }

    public void addText(String label, int min, int max) {
        register(label, "String, " + min + "-" + max, KIND_TEXT, min, max, null, UNIQUE_NONE);
    }

    public void addUniqueText(String label, int min, int max, int uniqueRule) {
        register(label, "String, unique, " + min + "-" + max, KIND_TEXT, min, max, null, uniqueRule);
    }

    public void addDigits(String label, int exactLength, int uniqueRule) {
        String hint = "String, exactly " + exactLength + " digits";
        if (uniqueRule != UNIQUE_NONE) {
            hint = hint + ", unique";
        }
        register(label, hint, KIND_DIGITS, exactLength, exactLength, null, uniqueRule);
    }

    public void addCode(String label, int min, int max, int uniqueRule) {
        String hint = "String, A-Z 0-9 -, " + min + "-" + max;
        if (uniqueRule != UNIQUE_NONE) {
            hint = hint + ", unique";
        }
        register(label, hint, KIND_CODE, min, max, null, uniqueRule);
    }

    public void addEmail() {
        register("Email", "String, valid email, max 120", KIND_EMAIL, 0, 120, null, UNIQUE_NONE);
    }

    public void addPhone() {
        register("Phone", "String, 10-15 digits, optional +", KIND_PHONE, 10, 15, null, UNIQUE_NONE);
    }

    public void addDate(String label) {
        register(label, "LocalDate, YYYY-MM-DD, not in future", KIND_DATE, 0, 0, null, UNIQUE_NONE);
    }

    public void addAnyDate(String label) {
        register(label, "LocalDate, YYYY-MM-DD", KIND_DATE_ANY, 0, 0, null, UNIQUE_NONE);
    }

    public void addMoney(String label) {
        register(label, "BigDecimal, 0-999999999999.99, 2 decimals", KIND_MONEY, 0, 0, null, UNIQUE_NONE);
    }

    public void addSignedMoney(String label) {
        register(label, "BigDecimal, may be negative, 2 decimals", KIND_SIGNED_MONEY, 0, 0, null, UNIQUE_NONE);
    }

    public void addRate(String label) {
        register(label, "BigDecimal, 0-100", KIND_RATE, 0, 0, null, UNIQUE_NONE);
    }

    public void addChoice(String label, String[] allowed) {
        String list = "";
        for (int i = 0; i < allowed.length; i++) {
            list = list + allowed[i];
            if (i < allowed.length - 1) {
                list = list + "/";
            }
        }
        register(label, "String, " + list, KIND_CHOICE, 0, 0, allowed, UNIQUE_NONE);
    }

    public void addYesNo(String label) {
        register(label, "boolean, YES/NO", KIND_YES_NO, 0, 0, null, UNIQUE_NONE);
    }

    // A field the program fills in by itself (the user cannot edit it).
    public void addCalculated(String label, String hint) {
        register(label, hint, KIND_CALC, 0, 0, null, UNIQUE_NONE);
    }

    // A field that points to an existing record (typed as an ID, TIN or code).
    public void addRef(String label, int kind) {
        String hint;
        switch (kind) {
            case KIND_REF_TAXPAYER:
                hint = "RegisteredTaxpayer, existing ACTIVE, TIN or Party ID";
                break;
            case KIND_REF_TAX_TYPE:
                hint = "TaxType, existing active, ID or code";
                break;
            case KIND_REF_OBLIGATION_FILING:
                hint = "TaxObligation, existing, no active declaration";
                break;
            case KIND_REF_OBLIGATION:
                hint = "TaxObligation, existing, by ID";
                break;
            case KIND_REF_ASSESSMENT_PAYABLE:
                hint = "TaxAssessment, CONFIRMED, balance above 0";
                break;
            case KIND_REF_ASSESSMENT:
                hint = "TaxAssessment, existing, not cancelled";
                break;
            default:
                hint = "TaxDeclaration, existing, not rejected, not assessed";
                break;
        }
        register(label, hint, kind, 1, 60, null, UNIQUE_NONE);
    }


    public void addStep(String stepTitle, int fieldsInStep) {
        stepTitles[stepCount] = stepTitle;
        stepFirstField[stepCount] = fieldsAssigned;
        stepFieldTotal[stepCount] = fieldsInStep;
        fieldsAssigned = fieldsAssigned + fieldsInStep;
        stepCount++;
    }


    public String getText(int index) {
        return values[index];
    }

    public void setText(int index, String value) {
        values[index] = value;
    }

    public LocalDate getDate(int index) {
        return LocalDate.parse(values[index]);
    }

    public BigDecimal getDecimal(int index) {
        return new BigDecimal(values[index]);
    }

    public boolean getYesNo(int index) {
        return "YES".equals(values[index]);
    }

    private boolean isReference(int kind) {
        return kind >= KIND_REF_TAXPAYER && kind <= KIND_REF_ASSESSMENT;
    }


    public int run() {
        int step = 0;
        while (true) {
            int move;
            if (step < ENTRY_STEPS) {
                if (stepFieldTotal[step] > 0) {
                    move = runEntryStep(step);
                } else {
                    move = runValidationStep(step);
                }
            } else {
                move = runReviewStep();
            }

            if (move == MOVE_CANCEL) {
                System.out.println();
                System.out.println("Form cancelled. Nothing was saved.");
                return RESULT_CANCELLED;
            }
            if (move == MOVE_SAVED) {
                return RESULT_SAVED;
            }
            if (move == MOVE_NEXT) {
                step++;
            } else {
                if (step == 0) {
                    System.out.println("You are already at the first step.");
                } else {
                    step--;
                }
            }
        }
    }


    private int runEntryStep(int step) {
        int first = stepFirstField[step];
        int last = first + stepFieldTotal[step] - 1;

        for (int i = first; i <= last; i++) {
            if (values[i] == null) {
                printHeader(step);
                int move = askField(i);
                if (move == MOVE_BACK || move == MOVE_CANCEL) {
                    return move;
                }
            }
        }

        while (true) {
            printHeader(step);
            for (int i = first; i <= last; i++) {
                printFieldLine(i);
            }
            String command = InputHelper.readLine(
                    "NEXT / BACK / CANCEL or a field number to edit: ");
            if (InputHelper.isNext(command)) {
                return MOVE_NEXT;
            }
            if (InputHelper.isBack(command)) {
                return MOVE_BACK;
            }
            if (InputHelper.isCancel(command)) {
                return MOVE_CANCEL;
            }
            int number = InputHelper.parseMenuNumber(command);
            if (number >= first + 1 && number <= last + 1) {
                if (askField(number - 1) == MOVE_CANCEL) {
                    return MOVE_CANCEL;
                }
            } else {
                System.out.println("   ! Type NEXT, BACK, CANCEL or a field number from "
                        + (first + 1) + " to " + (last + 1) + ".");
            }
        }
    }

    private int runValidationStep(int step) {
        while (true) {
            printHeader(step);
            printAllFields();
            boolean passed = runCheck();
            String prompt;
            if (passed) {
                prompt = "NEXT / BACK / CANCEL or a field number to edit: ";
            } else {
                prompt = "BACK / CANCEL or a field number to edit: ";
            }
            String command = InputHelper.readLine(prompt);
            if (InputHelper.isCancel(command)) {
                return MOVE_CANCEL;
            }
            if (InputHelper.isBack(command)) {
                return MOVE_BACK;
            }
            if (InputHelper.isNext(command)) {
                if (passed) {
                    return MOVE_NEXT;
                }
                System.out.println("   ! Fix the problem first: edit a field number or go BACK.");
            } else {
                int number = InputHelper.parseMenuNumber(command);
                if (number >= 1 && number <= fieldCount) {
                    if (askField(number - 1) == MOVE_CANCEL) {
                        return MOVE_CANCEL;
                    }
                } else {
                    System.out.println("   ! Type NEXT, BACK, CANCEL or a field number from 1 to "
                            + fieldCount + ".");
                }
            }
        }
    }

    // ---------- Step 4: review, edit, save ----------
    private int runReviewStep() {
        while (true) {
            System.out.println();
            System.out.println("+------------------ " + title + " ------------------+");
            System.out.println("| STEP 4 OF 4 - REVIEW ALL " + fieldCount + " FIELDS");
            printAllFields();
            runCheck();
            System.out.println("|");
            System.out.println("| [1] SAVE   [2] EDIT FIELD   [0] CANCEL   (or BACK)");
            System.out.println("+--------------------------------------------------------------+");
            String choice = InputHelper.readLine("Select: ");

            if (InputHelper.isBack(choice)) {
                return MOVE_BACK;
            }
            if (InputHelper.isCancel(choice)) {
                return MOVE_CANCEL;
            }
            int option = InputHelper.parseMenuNumber(choice);
            if (option == 1) {
                try {
                    FormActions.save(formType, this, store);
                    return MOVE_SAVED;
                } catch (ValidationException e) {
                    System.out.println("   ! Save failed: " + e.getMessage());
                    System.out.println("   Nothing was saved. Use [2] EDIT FIELD to correct it.");
                }
            } else if (option == 2) {
                String text = InputHelper.readLine("Field number to edit (1-" + fieldCount + "): ");
                int number = InputHelper.parseMenuNumber(text);
                if (number >= 1 && number <= fieldCount) {
                    if (askField(number - 1) == MOVE_CANCEL) {
                        return MOVE_CANCEL;
                    }
                } else {
                    System.out.println("   ! Please enter a number from 1 to " + fieldCount + ".");
                }
            } else if (option == 0) {
                return MOVE_CANCEL;
            } else {
                System.out.println("   ! Unknown option. Type 1, 2, 0 or BACK.");
            }
        }
    }

    private int askField(int index) {
        if (kinds[index] == KIND_CALC) {
            System.out.println("   ! Field " + (index + 1) + " (" + labels[index]
                    + ") is calculated automatically and cannot be edited.");
            return MOVE_NEXT;
        }
        while (true) {
            String current = values[index];
            String prompt = String.format("%02d %s [%s]", index + 1, labels[index], hints[index]);
            if (current != null) {
                prompt = prompt + " (current: " + current + ", Enter keeps it)";
            }
            String text = InputHelper.readLine(prompt + " : ");

            if (InputHelper.isCancel(text)) {
                return MOVE_CANCEL;
            }
            if (InputHelper.isBack(text)) {
                return MOVE_BACK;
            }
            if (current != null && (text.isEmpty() || InputHelper.isNext(text))) {
                return MOVE_NEXT;
            }
            try {
                // The old value is replaced only when the new one passes every check.
                values[index] = checkField(index, text);
                return MOVE_NEXT;
            } catch (RecordNotFoundException e) {
                System.out.println("   ! " + e.getMessage());
                if (isReference(kinds[index])) {
                    if (offerPrerequisite(index) == MOVE_CANCEL) {
                        return MOVE_CANCEL;
                    }
                }
            } catch (ValidationException e) {
                System.out.println("   ! " + e.getMessage());
            }
        }
    }

    private int offerPrerequisite(int index) {
        String name = prerequisiteName(kinds[index]);
        while (true) {
            System.out.println("   [1] Register " + name + " now   [2] Type the value again"
                    + "   [0] Cancel this form");
            String choice = InputHelper.readLine("   Select: ");
            int option = InputHelper.parseMenuNumber(choice);
            if (option == 1) {
                registerPrerequisite(kinds[index]);
                System.out.println();
                System.out.println("   Back to the original form. Your typed values were kept.");
                return MOVE_NEXT;
            }
            if (option == 2) {
                return MOVE_NEXT;
            }
            if (option == 0 || InputHelper.isCancel(choice)) {
                return MOVE_CANCEL;
            }
            System.out.println("   ! Type 1, 2 or 0.");
        }
    }

    private String prerequisiteName(int kind) {
        switch (kind) {
            case KIND_REF_TAXPAYER:
                return "taxpayer";
            case KIND_REF_TAX_TYPE:
                return "tax type";
            case KIND_REF_DECLARATION:
                return "declaration";
            case KIND_REF_ASSESSMENT_PAYABLE:
            case KIND_REF_ASSESSMENT:
                return "assessment";
            default:
                return "obligation";
        }
    }

    private void registerPrerequisite(int kind) {
        switch (kind) {
            case KIND_REF_TAXPAYER: {
                String answer = InputHelper.readLine("   Taxpayer type: [1] Individual  [2] Corporate : ");
                int type = InputHelper.parseMenuNumber(answer);
                if (type == 1) {
                    FormActions.registerIndividual(store);
                } else if (type == 2) {
                    FormActions.registerCorporate(store);
                } else {
                    System.out.println("   No prerequisite was registered.");
                }
                break;
            }
            case KIND_REF_TAX_TYPE:
                FormActions.registerTaxType(store);
                break;
            case KIND_REF_DECLARATION:
                FormActions.fileDeclaration(store);
                break;
            case KIND_REF_ASSESSMENT_PAYABLE:
            case KIND_REF_ASSESSMENT:
                FormActions.createAssessment(store);
                break;
            default:
                FormActions.createObligation(store);
                break;
        }
    }

    private String checkField(int index, String text) throws ValidationException {
        String label = labels[index];
        String clean = text.trim();

        if (isReference(kinds[index])) {
            ValidationUtil.requireText(clean, label, 1, 60);
        }

        switch (kinds[index]) {
            case KIND_TEXT:
                ValidationUtil.requireText(clean, label, minimums[index], maximums[index]);
                break;
            case KIND_DIGITS:
                ValidationUtil.requireDigits(clean, label, maximums[index]);
                break;
            case KIND_CODE:
                ValidationUtil.requireCode(clean, label, minimums[index], maximums[index]);
                break;
            case KIND_EMAIL:
                ValidationUtil.requireEmail(clean);
                break;
            case KIND_PHONE:
                ValidationUtil.requirePhone(clean);
                break;
            case KIND_DATE: {
                LocalDate date = InputHelper.parseDate(clean, label);
                ValidationUtil.requireNotFuture(date, label);
                clean = date.toString();
                break;
            }
            case KIND_DATE_ANY: {
                LocalDate date = InputHelper.parseDate(clean, label);
                clean = date.toString();
                break;
            }
            case KIND_MONEY: {
                BigDecimal money = InputHelper.parseMoney(clean, label);
                clean = money.toPlainString();
                break;
            }
            case KIND_SIGNED_MONEY: {
                BigDecimal money = InputHelper.parseSignedMoney(clean, label);
                clean = money.toPlainString();
                break;
            }
            case KIND_RATE: {
                BigDecimal rate = InputHelper.parseRate(clean, label);
                clean = rate.toPlainString();
                break;
            }
            case KIND_CHOICE:
                clean = clean.toUpperCase();
                ValidationUtil.requireChoice(clean, label, allowedValues[index]);
                break;
            case KIND_YES_NO: {
                boolean yes = InputHelper.parseYesNo(clean, label);
                if (yes) {
                    clean = "YES";
                } else {
                    clean = "NO";
                }
                break;
            }
            case KIND_REF_TAXPAYER: {
                RegisteredTaxpayer taxpayer = store.requireTaxpayer(clean);
                if (!taxpayer.isActive()) {
                    throw new ValidationException("Taxpayer " + taxpayer.getPartyId()
                            + " is not ACTIVE (status " + taxpayer.getTaxpayerStatus() + ").");
                }
                System.out.println("   found: " + ValidationUtil.describe(taxpayer));
                clean = taxpayer.getPartyId();
                break;
            }
            case KIND_REF_TAX_TYPE: {
                TaxType taxType = store.requireTaxType(clean);
                if (!taxType.isActive()) {
                    throw new ValidationException("Tax type " + taxType.getTaxTypeId()
                            + " is not active.");
                }
                System.out.println("   found: " + ValidationUtil.describe(taxType));
                clean = taxType.getTaxTypeId();
                break;
            }
            case KIND_REF_OBLIGATION_FILING: {
                TaxObligation obligation = store.requireObligation(clean);
                TaxDeclaration existing = store.findActiveDeclarationForObligation(obligation);
                if (existing != null) {
                    throw new DuplicateRecordException("Obligation " + obligation.getObligationId()
                            + " already has an active declaration (" + existing.getDeclarationId()
                            + ").");
                }
                System.out.println("   found: " + ValidationUtil.describe(obligation)
                        + " | due " + obligation.getDueDate());
                clean = obligation.getObligationId();
                break;
            }
            case KIND_REF_OBLIGATION: {
                TaxObligation obligation = store.requireObligation(clean);
                System.out.println("   found: " + ValidationUtil.describe(obligation));
                clean = obligation.getObligationId();
                break;
            }
            case KIND_REF_DECLARATION: {
                TaxDeclaration declaration = store.requireDeclaration(clean);
                if (TaxDeclaration.STATUS_REJECTED.equals(declaration.getStatus())) {
                    throw new ValidationException("A rejected declaration cannot be assessed.");
                }
                TaxAssessment existing = store.findActiveAssessmentForDeclaration(declaration);
                if (existing != null) {
                    throw new DuplicateRecordException("Declaration " + declaration.getDeclarationId()
                            + " already has an assessment (" + existing.getAssessmentId() + ").");
                }
                System.out.println("   found: " + declaration.getSummary());
                clean = declaration.getDeclarationId();
                break;
            }
            case KIND_REF_ASSESSMENT_PAYABLE: {
                TaxAssessment assessment = store.requireAssessment(clean);
                if (!TaxAssessment.STATUS_CONFIRMED.equals(assessment.getStatus())) {
                    throw new PrerequisiteMissingException(
                            "Payments require a CONFIRMED assessment (this one is "
                                    + assessment.getStatus() + "). Use menu option 8 first.");
                }
                BigDecimal outstanding = store.getOutstandingBalance(assessment);
                if (outstanding.signum() <= 0) {
                    throw new ValidationException("Assessment " + assessment.getAssessmentId()
                            + " is already fully paid.");
                }
                System.out.println("   found: " + assessment.getSummary()
                        + " | outstanding " + outstanding);
                clean = assessment.getAssessmentId();
                break;
            }
            case KIND_REF_ASSESSMENT: {
                TaxAssessment assessment = store.requireAssessment(clean);
                if (TaxAssessment.STATUS_CANCELLED.equals(assessment.getStatus())) {
                    throw new ValidationException("Assessment " + assessment.getAssessmentId()
                            + " is CANCELLED.");
                }
                System.out.println("   found: " + assessment.getSummary());
                clean = assessment.getAssessmentId();
                break;
            }
            default:
                break;
        }

        checkUnique(uniqueRules[index], clean);
        return clean;
    }

    private void checkUnique(int rule, String value) throws ValidationException {
        boolean exists = false;
        String what = "";
        switch (rule) {
            case UNIQUE_PARTY_ID:
                exists = store.findPartyById(value) != null;
                what = "Party ID";
                break;
            case UNIQUE_TIN:
                exists = store.findTaxpayerByTin(value) != null;
                what = "TIN";
                break;
            case UNIQUE_NATIONAL_ID:
                exists = store.findIndividualByNationalId(value) != null;
                what = "National ID";
                break;
            case UNIQUE_COMPANY_NO:
                exists = store.findCorporateByCompanyNo(value) != null;
                what = "Company registration number";
                break;
            case UNIQUE_STAFF_NO:
                exists = store.findOfficerByStaffNumber(value) != null;
                what = "Staff number";
                break;
            case UNIQUE_TAX_TYPE_ID:
                exists = store.findTaxTypeById(value) != null;
                what = "Tax type ID";
                break;
            case UNIQUE_TAX_CODE:
                exists = store.findTaxTypeByCode(value) != null;
                what = "Tax code";
                break;
            case UNIQUE_OBLIGATION_ID:
                exists = store.findObligationById(value) != null;
                what = "Obligation ID";
                break;
            case UNIQUE_DECLARATION_ID:
                exists = store.findDeclarationById(value) != null;
                what = "Declaration ID";
                break;
            case UNIQUE_ASSESSMENT_ID:
                exists = store.findAssessmentById(value) != null;
                what = "Assessment ID";
                break;
            case UNIQUE_PAYMENT_ID:
                exists = store.findPaymentById(value) != null;
                what = "Payment ID";
                break;
            case UNIQUE_PAYMENT_REF:
                exists = store.findPaymentByReference(value) != null;
                what = "Payment reference";
                break;
            case UNIQUE_PENALTY_ID:
                exists = store.findPenaltyById(value) != null;
                what = "Penalty ID";
                break;
            case UNIQUE_REFUND_ID:
                exists = store.findRefundById(value) != null;
                what = "Refund ID";
                break;
            case UNIQUE_CLEARANCE_ID:
                exists = store.findClearanceById(value) != null;
                what = "Clearance ID";
                break;
            default:
                break;
        }
        if (exists) {
            throw new DuplicateRecordException(what + " " + value + " already exists.");
        }
    }

    // Builds the object from the typed values and validates it (nothing is saved).
    private boolean runCheck() {
        try {
            FormActions.check(formType, this, store);
            System.out.println("| VALIDATION PASSED.");
            return true;
        } catch (ValidationException e) {
            System.out.println("| VALIDATION FAILED: " + e.getMessage());
            return false;
        }
    }

    private void printHeader(int step) {
        System.out.println();
        System.out.println("+------------------ " + title + " ------------------+");
        System.out.println("| STEP " + (step + 1) + " OF 4 - " + stepTitles[step]);
    }

    private void printFieldLine(int index) {
        String shown = values[index];
        if (shown == null) {
            shown = "_";
        }
        System.out.println(String.format("| %02d %-26s [%s] : %s",
                index + 1, labels[index], hints[index], shown));
    }

    private void printAllFields() {
        FormActions.refresh(formType, this, store);   // fills calculated fields first
        for (int i = 0; i < fieldCount; i++) {
            printFieldLine(i);
        }
    }
}