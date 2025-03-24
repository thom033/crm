package site.easy.to.build.crm.service.expense;

import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Expense;

import java.util.List;

public interface ExpenseService {
    Expense save(Expense expense);
    List<Budget> findBudgetsByCustomerId(int customerId);
    double calculateTotalExpensesForBudget(int budgetId);
}