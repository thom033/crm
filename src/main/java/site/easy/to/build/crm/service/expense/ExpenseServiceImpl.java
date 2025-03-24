package site.easy.to.build.crm.service.expense;

import org.springframework.stereotype.Service;

import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Expense;
import site.easy.to.build.crm.repository.BudgetRepository;
import site.easy.to.build.crm.repository.ExpenseRepository;

import java.util.List;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository, BudgetRepository budgetRepository) {
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
    }

    @Override
    public Expense save(Expense expense) {
        return expenseRepository.save(expense);
    }

    @Override
    public List<Budget> findBudgetsByCustomerId(int customerId) {
        return budgetRepository.findByCustomerCustomerId(customerId);
    }

    @Override
    public double calculateTotalExpensesForBudget(int budgetId) {
        List<Expense> expenses = expenseRepository.findByBudgetBudgetId(budgetId);
        return expenses.stream().mapToDouble(Expense::getAmount).sum();
    }
}