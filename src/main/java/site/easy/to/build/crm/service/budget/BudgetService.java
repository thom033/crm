package site.easy.to.build.crm.service.budget;

import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.repository.BudgetRepository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class BudgetService {
    private BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    public Budget findByBudgetId(int id) {
        return budgetRepository.findByBudgetId(id);
    }

    public List<Budget> findAll() {
        return budgetRepository.findAll();
    }

    public List<Budget> getCustomerBudgets(int customerId) {
        return budgetRepository.findByCustomerCustomerId(customerId);
    }

    public Budget save(Budget budget) {
        return budgetRepository.save(budget);
    }

    public void delete(Budget budget) {
        budgetRepository.delete(budget);
    }

    public void deleteAllByCustomer(Customer customer) {
        budgetRepository.deleteAllByCustomer(customer);
    }

    public BigDecimal getTotalBudget(int customerId) {
        List<Budget> budgets = getCustomerBudgets(customerId);
        return budgets.stream().map(Budget::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
