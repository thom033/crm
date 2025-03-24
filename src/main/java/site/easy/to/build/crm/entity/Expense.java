package site.easy.to.build.crm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private int expenseId;

    @NotBlank(message = "Expense name is required")
    @Column(name = "expense_name")
    private String expenseName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00", message = "Amount must be ≥ 0.00")
    @DecimalMax(value = "9999999.99", message = "Amount must be ≤ 9999999.99")
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be ≥ 0.00")
    @DecimalMax(value = "9999999.99", message = "Price must be ≤ 9999999.99")
    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @NotBlank(message = "Date is required")
    @Column(name = "date_expense")
    private String startDate;

    @ManyToOne
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @ManyToOne 
    @JoinColumn(name = "lead_id")
    private Lead lead;

    // @ManyToOne
    // @JoinColumn(name = "customer_id", nullable = false)
    // private Customer customer;

    // @ManyToOne
    // @JoinColumn(name = "customer_budget_id")
    // private Budget budget;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters and Setters

    public int getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(int expenseId) {
        this.expenseId = expenseId;
    }

    public String getExpenseName() {
        return expenseName;
    }

    public void setExpenseName(String expenseName) {
        this.expenseName = expenseName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public Lead getLead() {
        return lead;
    }

    public void setLead(Lead lead) {
        this.lead = lead;
    }

    // public Customer getCustomer() {
    //     return customer;
    // }

    // public void setCustomer(Customer customer) {
    //     this.customer = customer;
    // }

    // public Budget getBudget() {
    //     return budget;
    // }

    // public void setBudget(Budget budget) {
    //     this.budget = budget;
    // }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
