package site.easy.to.build.crm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import site.easy.to.build.crm.customValidations.contract.StartDateBeforeEndDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_budget")
@StartDateBeforeEndDate
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    private int budgetId;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull(message = "Amount is required")
    @Digits(integer = 10, fraction = 2, message = "Amount must be a valid number with up to 2 decimal places")
    @DecimalMin(value = "0.00", inclusive = true, message = "Amount must be ≥ 0.00")
    @DecimalMax(value = "9999999.99", inclusive = true, message = "Amount must be ≤ 9999999.99")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private double amount;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private int userId;

    public Budget() {
    }

    public Budget(Customer customer, double amount, LocalDateTime createdAt, int userId) {
        this.amount = amount;
        this.customer = customer;
        this.createdAt = createdAt;
        this.userId = userId;
    }

    public int getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(int budgetId) {
        this.budgetId = budgetId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
