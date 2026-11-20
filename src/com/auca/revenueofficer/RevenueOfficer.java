package com.auca.revenueofficer;

import com.auca.abstracts.TaxParty;
import com.auca.exceptions.ValidationException;
import com.auca.main.ValidationUtil;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RevenueOfficer extends TaxParty {

    private String staffNumber;
    private String department;
    private String role;
    private LocalDate hireDate;
    private BigDecimal approvalLimit;
    private boolean active;


    public RevenueOfficer(String partyId, String firstName, String lastName, String email,
                          String phoneNumber, LocalDate dateOfBirth,
                          String staffNumber, String department, String role,
                          LocalDate hireDate, BigDecimal approvalLimit, boolean active) {
        super(partyId, firstName, lastName, email, phoneNumber, dateOfBirth);
        this.staffNumber = ValidationUtil.trim(staffNumber);
        this.department = ValidationUtil.trim(department);
        this.role = ValidationUtil.trim(role);
        this.hireDate = hireDate;
        this.approvalLimit = approvalLimit;
        this.active = active;
    }

    public RevenueOfficer(String partyId, String firstName, String lastName, String email,
                          String phoneNumber, LocalDate dateOfBirth,
                          String staffNumber, String department, String role,
                          LocalDate hireDate, BigDecimal approvalLimit) {
        this(partyId, firstName, lastName, email, phoneNumber, dateOfBirth,
                staffNumber, department, role, hireDate, approvalLimit, true);
    }

    public boolean canApprove(BigDecimal amount) {
        if (!active || amount == null || approvalLimit == null) {
            return false;
        }
        return amount.compareTo(approvalLimit) <= 0;
    }

    @Override
    public String getRoleName() {
        return "Revenue Officer";
    }

    @Override
    public String getDisplayName() {
        return "Officer " + getFullName();
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        ValidationUtil.requireCode(staffNumber, "Staff number", 3, 20);
        ValidationUtil.requireText(department, "Department", 2, 60);
        ValidationUtil.requireText(role, "Role", 2, 60);
        ValidationUtil.requireNotFuture(hireDate, "Hire date");
        ValidationUtil.requireBefore(getDateOfBirth(), "Date of birth", hireDate, "Hire date");
        ValidationUtil.requireMoney(approvalLimit, "Approval limit");
    }

    @Override
    public void printDetails() {
        super.printDetails();
        ValidationUtil.printField("Staff number", staffNumber);
        ValidationUtil.printField("Department", department);
        ValidationUtil.printField("Role", role);
        ValidationUtil.printField("Hire date", hireDate);
        ValidationUtil.printField("Approval limit", approvalLimit);
        ValidationUtil.printField("Active", active ? "YES" : "NO");
    }

    @Override
    public String getSummary() {
        return super.getSummary() + " | " + staffNumber + " | " + department;
    }

    public String getStaffNumber() { return staffNumber; }
    public void setStaffNumber(String staffNumber) { this.staffNumber = ValidationUtil.trim(staffNumber); }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = ValidationUtil.trim(department); }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = ValidationUtil.trim(role); }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public BigDecimal getApprovalLimit() { return approvalLimit; }
    public void setApprovalLimit(BigDecimal approvalLimit) { this.approvalLimit = approvalLimit; }

    public void setActive(boolean active) { this.active = active; }
}