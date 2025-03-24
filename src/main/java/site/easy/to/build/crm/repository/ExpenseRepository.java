package site.easy.to.build.crm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Expense;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {
    List<Expense> findByBudgetBudgetId(int budgetId);
}