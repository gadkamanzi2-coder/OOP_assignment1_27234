package com.auca.main;

import com.auca.abstracts.RegisteredTaxpayer;
import com.auca.abstracts.TaxParty;
import com.auca.corporatetaxpayer.CorporateTaxpayer;
import com.auca.exceptions.DuplicateRecordException;
import com.auca.exceptions.InvalidStatusTransitionException;
import com.auca.exceptions.InvalidTaxPeriodException;
import com.auca.exceptions.OverpaymentException;
import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.RecordNotFoundException;
import com.auca.exceptions.ValidationException;
import com.auca.individualtaxpayer.IndividualTaxpayer;
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

public class DataStore {

    public static final int MAX_INDIVIDUALS = 100;
    public static final int MAX_CORPORATES = 100;
    public static final int MAX_OFFICERS = 50;
    public static final int MAX_TAX_TYPES = 30;
    public static final int MAX_OBLIGATIONS = 300;
    public static final int MAX_DECLARATIONS = 300;
    public static final int MAX_ASSESSMENTS = 300;
    public static final int MAX_PAYMENTS = 300;
    public static final int MAX_PENALTIES = 100;
    public static final int MAX_REFUNDS = 100;
    public static final int MAX_CLEARANCES = 100;

    private IndividualTaxpayer[] individuals = new IndividualTaxpayer[MAX_INDIVIDUALS];
    private int individualCount = 0;

    private CorporateTaxpayer[] corporates = new CorporateTaxpayer[MAX_CORPORATES];
    private int corporateCount = 0;

    private RevenueOfficer[] officers = new RevenueOfficer[MAX_OFFICERS];
    private int officerCount = 0;

    private TaxType[] taxTypes = new TaxType[MAX_TAX_TYPES];
    private int taxTypeCount = 0;

    private TaxObligation[] obligations = new TaxObligation[MAX_OBLIGATIONS];
    private int obligationCount = 0;

    private TaxDeclaration[] declarations = new TaxDeclaration[MAX_DECLARATIONS];
    private int declarationCount = 0;

    private TaxAssessment[] assessments = new TaxAssessment[MAX_ASSESSMENTS];
    private int assessmentCount = 0;

    private TaxPayment[] payments = new TaxPayment[MAX_PAYMENTS];
    private int paymentCount = 0;

    private Penalty[] penalties = new Penalty[MAX_PENALTIES];
    private int penaltyCount = 0;

    private TaxRefund[] refunds = new TaxRefund[MAX_REFUNDS];
    private int refundCount = 0;

    private TaxClearance[] clearances = new TaxClearance[MAX_CLEARANCES];
    private int clearanceCount = 0;

    // =====================================================================
    // FIND methods: return the record, or null when it does not exist.
    // =====================================================================

    // Any person-like party: individual, corporate or officer.
    public TaxParty findPartyById(String id) {
        if (id == null) {
            return null;
        }
        for (int i = 0; i < individualCount; i++) {
            if (individuals[i].getPartyId().equals(id)) {
                return individuals[i];
            }
        }
        for (int i = 0; i < corporateCount; i++) {
            if (corporates[i].getPartyId().equals(id)) {
                return corporates[i];
            }
        }
        for (int i = 0; i < officerCount; i++) {
            if (officers[i].getPartyId().equals(id)) {
                return officers[i];
            }
        }
        return null;
    }

    public RegisteredTaxpayer findTaxpayerByPartyId(String id) {
        if (id == null) {
            return null;
        }
        for (int i = 0; i < individualCount; i++) {
            if (individuals[i].getPartyId().equals(id)) {
                return individuals[i];
            }
        }
        for (int i = 0; i < corporateCount; i++) {
            if (corporates[i].getPartyId().equals(id)) {
                return corporates[i];
            }
        }
        return null;
    }

    // The TIN must be unique across BOTH taxpayer types, so we search both arrays.
    public RegisteredTaxpayer findTaxpayerByTin(String tin) {
        if (tin == null) {
            return null;
        }
        for (int i = 0; i < individualCount; i++) {
            if (individuals[i].getTin().equals(tin)) {
                return individuals[i];
            }
        }
        for (int i = 0; i < corporateCount; i++) {
            if (corporates[i].getTin().equals(tin)) {
                return corporates[i];
            }
        }
        return null;
    }

    public IndividualTaxpayer findIndividualByNationalId(String nationalId) {
        if (nationalId == null) {
            return null;
        }
        for (int i = 0; i < individualCount; i++) {
            if (individuals[i].getNationalId().equals(nationalId)) {
                return individuals[i];
            }
        }
        return null;
    }

    public CorporateTaxpayer findCorporateByCompanyNo(String companyNo) {
        if (companyNo == null) {
            return null;
        }
        for (int i = 0; i < corporateCount; i++) {
            if (corporates[i].getCompanyRegistrationNo().equals(companyNo)) {
                return corporates[i];
            }
        }
        return null;
    }

    public RevenueOfficer findOfficerByPartyId(String id) {
        if (id == null) {
            return null;
        }
        for (int i = 0; i < officerCount; i++) {
            if (officers[i].getPartyId().equals(id)) {
                return officers[i];
            }
        }
        return null;
    }

    public RevenueOfficer findOfficerByStaffNumber(String staffNumber) {
        if (staffNumber == null) {
            return null;
        }
        for (int i = 0; i < officerCount; i++) {
            if (officers[i].getStaffNumber().equals(staffNumber)) {
                return officers[i];
            }
        }
        return null;
    }

    public TaxType findTaxTypeById(String id) {
        if (id == null) {
            return null;
        }
        for (int i = 0; i < taxTypeCount; i++) {
            if (taxTypes[i].getTaxTypeId().equals(id)) {
                return taxTypes[i];
            }
        }
        return null;
    }

    public TaxType findTaxTypeByCode(String code) {
        if (code == null) {
            return null;
        }
        for (int i = 0; i < taxTypeCount; i++) {
            if (taxTypes[i].getTaxCode().equals(code)) {
                return taxTypes[i];
            }
        }
        return null;
    }

    public TaxObligation findObligationById(String id) {
        if (id == null) {
            return null;
        }
        for (int i = 0; i < obligationCount; i++) {
            if (obligations[i].getObligationId().equals(id)) {
                return obligations[i];
            }
        }
        return null;
    }

    public TaxDeclaration findDeclarationById(String id) {
        if (id == null) {
            return null;
        }
        for (int i = 0; i < declarationCount; i++) {
            if (declarations[i].getDeclarationId().equals(id)) {
                return declarations[i];
            }
        }
        return null;
    }

    public TaxAssessment findAssessmentById(String id) {
        if (id == null) {
            return null;
        }
        for (int i = 0; i < assessmentCount; i++) {
            if (assessments[i].getAssessmentId().equals(id)) {
                return assessments[i];
            }
        }
        return null;
    }

    public TaxPayment findPaymentById(String id) {
        if (id == null) {
            return null;
        }
        for (int i = 0; i < paymentCount; i++) {
            if (payments[i].getPaymentId().equals(id)) {
                return payments[i];
            }
        }
        return null;
    }

    public TaxPayment findPaymentByReference(String reference) {
        if (reference == null) {
            return null;
        }
        for (int i = 0; i < paymentCount; i++) {
            if (payments[i].getPaymentReference().equals(reference)) {
                return payments[i];
            }
        }
        return null;
    }

    public Penalty findPenaltyById(String id) {
        if (id == null) {
            return null;
        }
        for (int i = 0; i < penaltyCount; i++) {
            if (penalties[i].getPenaltyId().equals(id)) {
                return penalties[i];
            }
        }
        return null;
    }

    public TaxRefund findRefundById(String id) {
        if (id == null) {
            return null;
        }
        for (int i = 0; i < refundCount; i++) {
            if (refunds[i].getRefundId().equals(id)) {
                return refunds[i];
            }
        }
        return null;
    }

    public TaxClearance findClearanceById(String id) {
        if (id == null) {
            return null;
        }
        for (int i = 0; i < clearanceCount; i++) {
            if (clearances[i].getClearanceId().equals(id)) {
                return clearances[i];
            }
        }
        return null;
    }

    public RegisteredTaxpayer requireTaxpayer(String tinOrId) throws RecordNotFoundException {
        RegisteredTaxpayer taxpayer = findTaxpayerByTin(tinOrId);
        if (taxpayer == null) {
            taxpayer = findTaxpayerByPartyId(tinOrId);
        }
        if (taxpayer == null) {
            throw new RecordNotFoundException("No taxpayer found with TIN or Party ID '"
                    + tinOrId + "'.");
        }
        return taxpayer;
    }

    public RevenueOfficer requireOfficer(String idOrStaffNo) throws RecordNotFoundException {
        RevenueOfficer officer = findOfficerByPartyId(idOrStaffNo);
        if (officer == null) {
            officer = findOfficerByStaffNumber(idOrStaffNo);
        }
        if (officer == null) {
            throw new RecordNotFoundException("No officer found with Party ID or staff number '"
                    + idOrStaffNo + "'.");
        }
        return officer;
    }

    public TaxType requireTaxType(String idOrCode) throws RecordNotFoundException {
        TaxType type = findTaxTypeById(idOrCode);
        if (type == null) {
            type = findTaxTypeByCode(idOrCode);
        }
        if (type == null) {
            throw new RecordNotFoundException("No tax type found with ID or code '"
                    + idOrCode + "'.");
        }
        return type;
    }

    public TaxObligation requireObligation(String id) throws RecordNotFoundException {
        TaxObligation item = findObligationById(id);
        if (item == null) {
            throw new RecordNotFoundException("No obligation found with ID '" + id + "'.");
        }
        return item;
    }

    public TaxDeclaration requireDeclaration(String id) throws RecordNotFoundException {
        TaxDeclaration item = findDeclarationById(id);
        if (item == null) {
            throw new RecordNotFoundException("No declaration found with ID '" + id + "'.");
        }
        return item;
    }

    public TaxAssessment requireAssessment(String id) throws RecordNotFoundException {
        TaxAssessment item = findAssessmentById(id);
        if (item == null) {
            throw new RecordNotFoundException("No assessment found with ID '" + id + "'.");
        }
        return item;
    }

    public TaxRefund requireRefund(String id) throws RecordNotFoundException {
        TaxRefund item = findRefundById(id);
        if (item == null) {
            throw new RecordNotFoundException("No refund found with ID '" + id + "'.");
        }
        return item;
    }

    public TaxClearance requireClearance(String id) throws RecordNotFoundException {
        TaxClearance item = findClearanceById(id);
        if (item == null) {
            throw new RecordNotFoundException("No clearance found with ID '" + id + "'.");
        }
        return item;
    }

    // =====================================================================
    // Private checks used by the add methods
    // =====================================================================

    private void checkRoom(int count, int max, String name) throws ValidationException {
        if (count >= max) {
            throw new ValidationException("Storage for " + name + " is full (maximum " + max
                    + "). Nothing was saved.");
        }
    }

    private void checkPartyAndTinUnique(RegisteredTaxpayer taxpayer) throws ValidationException {
        if (findPartyById(taxpayer.getPartyId()) != null) {
            throw new DuplicateRecordException("Party ID " + taxpayer.getPartyId()
                    + " already exists.");
        }
        if (findTaxpayerByTin(taxpayer.getTin()) != null) {
            throw new DuplicateRecordException("TIN " + taxpayer.getTin() + " already exists.");
        }
    }

    // The reference must point to the REAL stored object (same object, not a copy).
    private void requireStoredTaxpayer(RegisteredTaxpayer taxpayer) throws ValidationException {
        if (taxpayer == null) {
            throw new PrerequisiteMissingException("A registered taxpayer is required.");
        }
        if (findTaxpayerByPartyId(taxpayer.getPartyId()) != taxpayer) {
            throw new RecordNotFoundException("Taxpayer " + taxpayer.getPartyId()
                    + " is not registered in the system.");
        }
    }

    private void requireStoredOfficer(RevenueOfficer officer) throws ValidationException {
        if (officer == null) {
            throw new PrerequisiteMissingException("A revenue officer is required.");
        }
        if (findOfficerByPartyId(officer.getPartyId()) != officer) {
            throw new RecordNotFoundException("Officer " + officer.getPartyId()
                    + " is not registered in the system.");
        }
    }

    private void requireStoredTaxType(TaxType taxType) throws ValidationException {
        if (taxType == null) {
            throw new PrerequisiteMissingException("A tax type is required.");
        }
        if (findTaxTypeById(taxType.getTaxTypeId()) != taxType) {
            throw new RecordNotFoundException("Tax type " + taxType.getTaxTypeId()
                    + " is not registered in the system.");
        }
    }

    private void requireStoredObligation(TaxObligation obligation) throws ValidationException {
        if (obligation == null) {
            throw new PrerequisiteMissingException("A tax obligation is required.");
        }
        if (findObligationById(obligation.getObligationId()) != obligation) {
            throw new RecordNotFoundException("Obligation " + obligation.getObligationId()
                    + " does not exist in the system.");
        }
    }

    private void requireStoredDeclaration(TaxDeclaration declaration) throws ValidationException {
        if (declaration == null) {
            throw new PrerequisiteMissingException("A tax declaration is required.");
        }
        if (findDeclarationById(declaration.getDeclarationId()) != declaration) {
            throw new RecordNotFoundException("Declaration " + declaration.getDeclarationId()
                    + " does not exist in the system.");
        }
    }

    private void requireStoredAssessment(TaxAssessment assessment) throws ValidationException {
        if (assessment == null) {
            throw new PrerequisiteMissingException("A tax assessment is required.");
        }
        if (findAssessmentById(assessment.getAssessmentId()) != assessment) {
            throw new RecordNotFoundException("Assessment " + assessment.getAssessmentId()
                    + " does not exist in the system.");
        }
    }

    public void addIndividual(IndividualTaxpayer taxpayer) throws ValidationException {
        if (taxpayer == null) {
            throw new ValidationException("There is no taxpayer to save.");
        }
        taxpayer.validate();
        checkPartyAndTinUnique(taxpayer);
        if (findIndividualByNationalId(taxpayer.getNationalId()) != null) {
            throw new DuplicateRecordException("National ID " + taxpayer.getNationalId()
                    + " already exists.");
        }
        checkRoom(individualCount, MAX_INDIVIDUALS, "individual taxpayers");
        individuals[individualCount] = taxpayer;
        individualCount++;
    }

    public void addCorporate(CorporateTaxpayer taxpayer) throws ValidationException {
        if (taxpayer == null) {
            throw new ValidationException("There is no taxpayer to save.");
        }
        taxpayer.validate();
        checkPartyAndTinUnique(taxpayer);
        if (findCorporateByCompanyNo(taxpayer.getCompanyRegistrationNo()) != null) {
            throw new DuplicateRecordException("Company registration number "
                    + taxpayer.getCompanyRegistrationNo() + " already exists.");
        }
        checkRoom(corporateCount, MAX_CORPORATES, "corporate taxpayers");
        corporates[corporateCount] = taxpayer;
        corporateCount++;
    }

    public void addOfficer(RevenueOfficer officer) throws ValidationException {
        if (officer == null) {
            throw new ValidationException("There is no officer to save.");
        }
        officer.validate();
        if (findPartyById(officer.getPartyId()) != null) {
            throw new DuplicateRecordException("Party ID " + officer.getPartyId()
                    + " already exists.");
        }
        if (findOfficerByStaffNumber(officer.getStaffNumber()) != null) {
            throw new DuplicateRecordException("Staff number " + officer.getStaffNumber()
                    + " already exists.");
        }
        checkRoom(officerCount, MAX_OFFICERS, "revenue officers");
        officers[officerCount] = officer;
        officerCount++;
    }

    public void addTaxType(TaxType taxType) throws ValidationException {
        if (taxType == null) {
            throw new ValidationException("There is no tax type to save.");
        }
        taxType.validate();
        if (findTaxTypeById(taxType.getTaxTypeId()) != null) {
            throw new DuplicateRecordException("Tax type ID " + taxType.getTaxTypeId()
                    + " already exists.");
        }
        if (findTaxTypeByCode(taxType.getTaxCode()) != null) {
            throw new DuplicateRecordException("Tax code " + taxType.getTaxCode()
                    + " already exists.");
        }
        checkRoom(taxTypeCount, MAX_TAX_TYPES, "tax types");
        taxTypes[taxTypeCount] = taxType;
        taxTypeCount++;
    }

    public void addObligation(TaxObligation obligation) throws ValidationException {
        if (obligation == null) {
            throw new ValidationException("There is no obligation to save.");
        }
        obligation.validate();
        if (findObligationById(obligation.getObligationId()) != null) {
            throw new DuplicateRecordException("Obligation ID " + obligation.getObligationId()
                    + " already exists.");
        }
        requireStoredTaxpayer(obligation.getTaxpayer());
        requireStoredTaxType(obligation.getTaxType());
        for (int i = 0; i < obligationCount; i++) {
            if (obligations[i].overlaps(obligation)) {
                throw new InvalidTaxPeriodException("This period overlaps existing obligation "
                        + obligations[i].getObligationId()
                        + " for the same taxpayer and tax type.");
            }
        }
        checkRoom(obligationCount, MAX_OBLIGATIONS, "obligations");
        obligations[obligationCount] = obligation;
        obligationCount++;
    }

    public void addDeclaration(TaxDeclaration declaration) throws ValidationException {
        if (declaration == null) {
            throw new ValidationException("There is no declaration to save.");
        }
        declaration.validate();
        if (findDeclarationById(declaration.getDeclarationId()) != null) {
            throw new DuplicateRecordException("Declaration ID " + declaration.getDeclarationId()
                    + " already exists.");
        }
        requireStoredObligation(declaration.getObligation());
        if (declaration.isActive()) {
            TaxDeclaration existing = findActiveDeclarationForObligation(declaration.getObligation());
            if (existing != null) {
                throw new DuplicateRecordException("Obligation "
                        + declaration.getObligation().getObligationId()
                        + " already has an active declaration (" + existing.getDeclarationId() + ").");
            }
        }
        checkRoom(declarationCount, MAX_DECLARATIONS, "declarations");
        declarations[declarationCount] = declaration;
        declarationCount++;
    }

    public void addAssessment(TaxAssessment assessment) throws ValidationException {
        if (assessment == null) {
            throw new ValidationException("There is no assessment to save.");
        }
        assessment.validate();
        if (findAssessmentById(assessment.getAssessmentId()) != null) {
            throw new DuplicateRecordException("Assessment ID " + assessment.getAssessmentId()
                    + " already exists.");
        }
        requireStoredDeclaration(assessment.getDeclaration());
        TaxAssessment existing = findActiveAssessmentForDeclaration(assessment.getDeclaration());
        if (existing != null) {
            throw new DuplicateRecordException("Declaration "
                    + assessment.getDeclaration().getDeclarationId()
                    + " already has an assessment (" + existing.getAssessmentId() + ").");
        }
        checkRoom(assessmentCount, MAX_ASSESSMENTS, "assessments");

        if (!TaxDeclaration.STATUS_ASSESSED.equals(assessment.getDeclaration().getStatus())) {
            assessment.getDeclaration().markAssessed();
        }
        assessments[assessmentCount] = assessment;
        assessmentCount++;
    }

    public void addPayment(TaxPayment payment) throws ValidationException {
        if (payment == null) {
            throw new ValidationException("There is no payment to save.");
        }
        payment.validate();
        if (findPaymentById(payment.getPaymentId()) != null) {
            throw new DuplicateRecordException("Payment ID " + payment.getPaymentId()
                    + " already exists.");
        }
        if (findPaymentByReference(payment.getPaymentReference()) != null) {
            throw new DuplicateRecordException("Payment reference "
                    + payment.getPaymentReference() + " already exists.");
        }
        requireStoredAssessment(payment.getAssessment());
        BigDecimal outstanding = getOutstandingBalance(payment.getAssessment());
        if (payment.getAmount().compareTo(outstanding) > 0) {
            throw new OverpaymentException("Payment amount " + payment.getAmount()
                    + " exceeds the outstanding balance " + outstanding + ".");
        }
        checkRoom(paymentCount, MAX_PAYMENTS, "payments");
        payments[paymentCount] = payment;
        paymentCount++;
    }

    public void addPenalty(Penalty penalty) throws ValidationException {
        if (penalty == null) {
            throw new ValidationException("There is no penalty to save.");
        }
        penalty.validate();
        if (findPenaltyById(penalty.getPenaltyId()) != null) {
            throw new DuplicateRecordException("Penalty ID " + penalty.getPenaltyId()
                    + " already exists.");
        }
        requireStoredObligation(penalty.getObligation());
        for (int i = 0; i < penaltyCount; i++) {
            if (penalties[i].sameKey(penalty)) {
                throw new DuplicateRecordException("Obligation "
                        + penalty.getObligation().getObligationId() + " already has a "
                        + penalty.getPenaltyType() + " penalty.");
            }
        }
        if (Penalty.TYPE_LATE_FILING.equals(penalty.getPenaltyType())) {
            TaxDeclaration declaration = findActiveDeclarationForObligation(penalty.getObligation());
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
        checkRoom(penaltyCount, MAX_PENALTIES, "penalties");
        penalties[penaltyCount] = penalty;
        penaltyCount++;
    }

    public void addRefund(TaxRefund refund) throws ValidationException {
        if (refund == null) {
            throw new ValidationException("There is no refund to save.");
        }
        refund.validate();
        if (!TaxRefund.STATUS_PENDING.equals(refund.getStatus())) {
            throw new ValidationException("A new refund request must start as PENDING.");
        }
        if (findRefundById(refund.getRefundId()) != null) {
            throw new DuplicateRecordException("Refund ID " + refund.getRefundId()
                    + " already exists.");
        }
        requireStoredTaxpayer(refund.getTaxpayer());
        requireStoredAssessment(refund.getAssessment());
        for (int i = 0; i < refundCount; i++) {
            if (refunds[i].getAssessment() == refund.getAssessment()
                    && TaxRefund.STATUS_PENDING.equals(refunds[i].getStatus())) {
                throw new DuplicateRecordException("Assessment "
                        + refund.getAssessment().getAssessmentId()
                        + " already has a pending refund (" + refunds[i].getRefundId() + ").");
            }
        }
        BigDecimal available = getAvailableOverpayment(refund.getAssessment());
        if (available.signum() <= 0) {
            throw new OverpaymentException("No verified overpayment exists for assessment "
                    + refund.getAssessment().getAssessmentId() + ".");
        }
        if (refund.getRequestedAmount().compareTo(available) > 0) {
            throw new OverpaymentException("Requested refund " + refund.getRequestedAmount()
                    + " exceeds the verified overpayment " + available + ".");
        }
        checkRoom(refundCount, MAX_REFUNDS, "refunds");
        refunds[refundCount] = refund;
        refundCount++;
    }

    public void addClearance(TaxClearance clearance) throws ValidationException {
        if (clearance == null) {
            throw new ValidationException("There is no clearance to save.");
        }
        clearance.validate();
        if (!TaxClearance.STATUS_PENDING.equals(clearance.getStatus())) {
            throw new ValidationException("A new clearance request must start as PENDING.");
        }
        if (findClearanceById(clearance.getClearanceId()) != null) {
            throw new DuplicateRecordException("Clearance ID " + clearance.getClearanceId()
                    + " already exists.");
        }
        requireStoredTaxpayer(clearance.getTaxpayer());
        checkRoom(clearanceCount, MAX_CLEARANCES, "clearances");
        clearances[clearanceCount] = clearance;
        clearanceCount++;
    }


    public void approveRefund(TaxRefund refund, RevenueOfficer officer) throws ValidationException {
        if (refund == null || findRefundById(refund.getRefundId()) != refund) {
            throw new RecordNotFoundException("The refund is not in the system.");
        }
        if (!TaxRefund.STATUS_PENDING.equals(refund.getStatus())) {
            throw new InvalidStatusTransitionException(
                    "Only a PENDING refund can be approved (current status: "
                            + refund.getStatus() + ").");
        }
        if (officer != null) {
            requireStoredOfficer(officer);
        }
        BigDecimal available = getAvailableOverpayment(refund.getAssessment());
        if (refund.getRequestedAmount().compareTo(available) > 0) {
            throw new OverpaymentException("Requested refund " + refund.getRequestedAmount()
                    + " exceeds the verified overpayment " + available + ".");
        }
        refund.approve(officer);
    }

    public void approveClearance(TaxClearance clearance, RevenueOfficer officer)
            throws ValidationException {
        if (clearance == null || findClearanceById(clearance.getClearanceId()) != clearance) {
            throw new RecordNotFoundException("The clearance is not in the system.");
        }
        if (!TaxClearance.STATUS_PENDING.equals(clearance.getStatus())) {
            throw new InvalidStatusTransitionException(
                    "Only a PENDING clearance can be approved (current status: "
                            + clearance.getStatus() + ").");
        }
        if (officer != null) {
            requireStoredOfficer(officer);
        }
        checkClearanceEligibility(clearance.getTaxpayer());
        clearance.approve(officer);
    }

    public void replaceRefund(TaxRefund oldRefund, TaxRefund newRefund) throws ValidationException {
        int index = -1;
        for (int i = 0; i < refundCount; i++) {
            if (refunds[i] == oldRefund) {
                index = i;
            }
        }
        if (index < 0) {
            throw new RecordNotFoundException("The refund is not in the system.");
        }
        if (newRefund == null) {
            throw new ValidationException("There is no refund to save.");
        }
        if (!TaxRefund.STATUS_PENDING.equals(oldRefund.getStatus())) {
            throw new InvalidStatusTransitionException(
                    "Only a PENDING refund can be changed (current status: "
                            + oldRefund.getStatus() + ").");
        }
        newRefund.validate();
        if (!newRefund.getRefundId().equals(oldRefund.getRefundId())) {
            throw new ValidationException("The refund ID cannot change.");
        }
        if (!TaxRefund.STATUS_PENDING.equals(newRefund.getStatus())) {
            throw new ValidationException("A changed refund request must stay PENDING.");
        }
        requireStoredTaxpayer(newRefund.getTaxpayer());
        requireStoredAssessment(newRefund.getAssessment());
        BigDecimal available = getAvailableOverpayment(newRefund.getAssessment());
        if (newRefund.getRequestedAmount().compareTo(available) > 0) {
            throw new OverpaymentException("Requested refund " + newRefund.getRequestedAmount()
                    + " exceeds the verified overpayment " + available + ".");
        }
        refunds[index] = newRefund;
    }

    public void replaceClearance(TaxClearance oldClearance, TaxClearance newClearance)
            throws ValidationException {
        int index = -1;
        for (int i = 0; i < clearanceCount; i++) {
            if (clearances[i] == oldClearance) {
                index = i;
            }
        }
        if (index < 0) {
            throw new RecordNotFoundException("The clearance is not in the system.");
        }
        if (newClearance == null) {
            throw new ValidationException("There is no clearance to save.");
        }
        if (!TaxClearance.STATUS_PENDING.equals(oldClearance.getStatus())) {
            throw new InvalidStatusTransitionException(
                    "Only a PENDING clearance can be changed (current status: "
                            + oldClearance.getStatus() + ").");
        }
        newClearance.validate();
        if (!newClearance.getClearanceId().equals(oldClearance.getClearanceId())) {
            throw new ValidationException("The clearance ID cannot change.");
        }
        if (!TaxClearance.STATUS_PENDING.equals(newClearance.getStatus())) {
            throw new ValidationException("A changed clearance request must stay PENDING.");
        }
        requireStoredTaxpayer(newClearance.getTaxpayer());
        clearances[index] = newClearance;
    }

    public TaxDeclaration findActiveDeclarationForObligation(TaxObligation obligation) {
        for (int i = 0; i < declarationCount; i++) {
            if (declarations[i].getObligation() == obligation && declarations[i].isActive()) {
                return declarations[i];
            }
        }
        return null;
    }

    public TaxAssessment findActiveAssessmentForDeclaration(TaxDeclaration declaration) {
        for (int i = 0; i < assessmentCount; i++) {
            if (assessments[i].getDeclaration() == declaration && assessments[i].isActive()) {
                return assessments[i];
            }
        }
        return null;
    }

    public BigDecimal getTotalPaid(TaxAssessment assessment) {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < paymentCount; i++) {
            if (payments[i].getAssessment() == assessment) {
                total = total.add(payments[i].getAmount());
            }
        }
        return total;
    }

    public BigDecimal getTotalPenalties(TaxObligation obligation) {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < penaltyCount; i++) {
            if (penalties[i].getObligation() == obligation) {
                total = total.add(penalties[i].getPenaltyAmount());
            }
        }
        return total;
    }

    public BigDecimal getBalance(TaxAssessment assessment) {
        if (assessment == null || TaxAssessment.STATUS_CANCELLED.equals(assessment.getStatus())) {
            return BigDecimal.ZERO;
        }
        return assessment.getTotal()
                .add(getTotalPenalties(assessment.getObligation()))
                .subtract(getTotalPaid(assessment));
    }

    // What is still owed (never below zero).
    public BigDecimal getOutstandingBalance(TaxAssessment assessment) {
        BigDecimal balance = getBalance(assessment);
        if (balance.signum() < 0) {
            return BigDecimal.ZERO;
        }
        return balance;
    }

    public BigDecimal getOverpayment(TaxAssessment assessment) {
        BigDecimal balance = getBalance(assessment);
        if (balance.signum() < 0) {
            return balance.negate();
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getApprovedRefunds(TaxAssessment assessment) {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < refundCount; i++) {
            if (refunds[i].getAssessment() == assessment
                    && TaxRefund.STATUS_APPROVED.equals(refunds[i].getStatus())) {
                total = total.add(refunds[i].getRequestedAmount());
            }
        }
        return total;
    }

    public BigDecimal getAvailableOverpayment(TaxAssessment assessment) {
        return getOverpayment(assessment).subtract(getApprovedRefunds(assessment));
    }

    public boolean isObligationSettled(TaxObligation obligation) {
        TaxDeclaration declaration = findActiveDeclarationForObligation(obligation);
        if (declaration == null) {
            return false;
        }
        TaxAssessment assessment = findActiveAssessmentForDeclaration(declaration);
        if (assessment == null) {
            return false;
        }
        if (!TaxAssessment.STATUS_CONFIRMED.equals(assessment.getStatus())) {
            return false;
        }
        return getBalance(assessment).signum() <= 0;
    }

    public boolean hasUnresolvedDeclaration(RegisteredTaxpayer taxpayer) {
        for (int i = 0; i < declarationCount; i++) {
            if (declarations[i].getTaxpayer() == taxpayer
                    && (TaxDeclaration.STATUS_SUBMITTED.equals(declarations[i].getStatus())
                    || TaxDeclaration.STATUS_LATE.equals(declarations[i].getStatus()))) {
                return true;
            }
        }
        for (int i = 0; i < assessmentCount; i++) {
            if (assessments[i].getTaxpayer() == taxpayer
                    && TaxAssessment.STATUS_DRAFT.equals(assessments[i].getStatus())) {
                return true;
            }
        }
        return false;
    }


    public void checkClearanceEligibility(RegisteredTaxpayer taxpayer) throws ValidationException {
        requireStoredTaxpayer(taxpayer);
        if (!taxpayer.isActive()) {
            throw new ValidationException("Clearance denied: the taxpayer is not ACTIVE.");
        }
        for (int i = 0; i < obligationCount; i++) {
            if (obligations[i].getTaxpayer() == taxpayer && obligations[i].isOverdue()
                    && !isObligationSettled(obligations[i])) {
                throw new ValidationException("Clearance denied: obligation "
                        + obligations[i].getObligationId() + " is overdue and not fully paid.");
            }
        }
        if (hasUnresolvedDeclaration(taxpayer)) {
            throw new ValidationException("Clearance denied: the taxpayer has unresolved declarations.");
        }
    }

    public int getIndividualCount() { return individualCount; }
    public IndividualTaxpayer getIndividual(int index) { return individuals[index]; }

    public int getCorporateCount() { return corporateCount; }
    public CorporateTaxpayer getCorporate(int index) { return corporates[index]; }

    public int getOfficerCount() { return officerCount; }
    public RevenueOfficer getOfficer(int index) { return officers[index]; }

    public int getTaxTypeCount() { return taxTypeCount; }
    public TaxType getTaxType(int index) { return taxTypes[index]; }

    public int getObligationCount() { return obligationCount; }
    public TaxObligation getObligation(int index) { return obligations[index]; }

    public int getDeclarationCount() { return declarationCount; }
    public TaxDeclaration getDeclaration(int index) { return declarations[index]; }

    public int getAssessmentCount() { return assessmentCount; }
    public TaxAssessment getAssessment(int index) { return assessments[index]; }

    public int getPaymentCount() { return paymentCount; }
    public TaxPayment getPayment(int index) { return payments[index]; }

    public int getPenaltyCount() { return penaltyCount; }
    public Penalty getPenalty(int index) { return penalties[index]; }

    public int getRefundCount() { return refundCount; }
    public TaxRefund getRefund(int index) { return refunds[index]; }

    public int getClearanceCount() { return clearanceCount; }
    public TaxClearance getClearance(int index) { return clearances[index]; }
}