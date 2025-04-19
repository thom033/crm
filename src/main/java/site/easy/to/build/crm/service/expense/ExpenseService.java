package site.easy.to.build.crm.service.expense;

import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Expense;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.repository.BudgetRepository;
import site.easy.to.build.crm.repository.ExpenseRepository;
import site.easy.to.build.crm.service.user.UserService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserService userService;

    public ExpenseService(ExpenseRepository expenseRepository, BudgetRepository budgetRepository) {
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
    }

    public Expense save(Expense expense) {
        return expenseRepository.save(expense);
    }

    public List<Budget> findBudgetsByCustomerId(int customerId) {
        return budgetRepository.findByCustomerCustomerId(customerId);
    }

    public BigDecimal getTotalDepensesByCustomerId(int customerId) {
        return expenseRepository.getTotalDepensesByCustomerId(customerId);
    }

    public Expense createDepenseForTicket(Ticket ticket, BigDecimal amount) {
        Expense expense = new Expense();
        expense.setTicket(ticket);
        expense.setAmount(amount.doubleValue());
        expense.setCreatedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());

        return expenseRepository.save(expense);
    }

    public Expense createDepenseForLead(Lead lead, BigDecimal amount) {
        Expense expense = new Expense();
        expense.setLead(lead);
        expense.setAmount(amount.doubleValue());
        expense.setCreatedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());

        return expenseRepository.save(expense);
    }

}