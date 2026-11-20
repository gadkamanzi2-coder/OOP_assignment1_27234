package com.auca.taxpayment;

import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Validatable;
import com.auca.main.ValidationUtil;
import com.auca.taxassessment.TaxAssessment;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TaxPayment implements Identifiable, Validatable {

    public static final String METHOD_CASH = "CASH";
    public static final String METHOD_BANK = "BANK_TRANSFER";
    public static final String METHOD_MOBILE = "MOBILE_MONEY";
    public static final String METHOD_CARD = "CARD";
    public static final String[] METHOD_CHOICES = {METHOD_CASH, METHOD_BANK, METHOD_MOBILE, METHOD_CARD};

    private String paymentId;
    private TaxAssessment assessment;
    private String paymentReference;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;

    public TaxPayment(String paymentId, TaxAssessment assessment, String paymentReference,
                      BigDecimal amount, LocalDate paymentDate, String paymentMethod) {
        this.paymentId = ValidationUtil.trim(paymentId);
        this.assessment = assessment;
        this.paymentReference = ValidationUtil.trim(paymentReference);
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = ValidationUtil.trim(paymentMethod);
    }

    @Override
    public String getId() {
        return paymentId;
    }

    @Override
    public String getDisplayName() {
        return "Payment " + paymentReference;
    }

    // A payment has no status; once recorded it counts as a confirmed payment.
    @Override
    public boolean isActive() {
        return assessment != null;
    }

    @Override
    public void validate() throws ValidationException {
        ValidationUtil.requireText(paymentId, "Payment ID", 4, 30);
        if (assessment == null) {
            throw new PrerequisiteMissingException("A tax assessment is required.");
        }
        if (!TaxAssessment.STATUS_CONFIRMED.equals(assessment.getStatus())) {
            throw new PrerequisiteMissingException("Payments require a CONFIRMED assessment.");
        }
        ValidationUtil.requireCode(paymentReference, "Payment reference", 6, 30);
        ValidationUtil.requirePositiveMoney(amount, "Payment amount");
        ValidationUtil.requireNotFuture(paymentDate, "Payment date");
        if (assessment.getAssessmentDate() != null
                && paymentDate.isBefore(assessment.getAssessmentDate())) {
            throw new ValidationException("Payment date cannot be before the assessment date.");
        }
        ValidationUtil.requireChoice(paymentMethod, "Payment method", METHOD_CHOICES);
    }

    @Override
    public void printDetails() {
        ValidationUtil.printField("Payment ID", paymentId);
        ValidationUtil.printField("Assessment", ValidationUtil.describe(assessment));
        ValidationUtil.printField("Payment reference", paymentReference);
        ValidationUtil.printField("Amount", amount);
        ValidationUtil.printField("Payment date", paymentDate);
        ValidationUtil.printField("Payment method", paymentMethod);
    }

    @Override
    public String getSummary() {
        return "Payment | " + paymentId + " | ref " + paymentReference + " | " + amount
                + " | " + paymentDate;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = ValidationUtil.trim(paymentId); }

    public TaxAssessment getAssessment() { return assessment; }
    public void setAssessment(TaxAssessment assessment) { this.assessment = assessment; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = ValidationUtil.trim(paymentReference); }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = ValidationUtil.trim(paymentMethod); }
}