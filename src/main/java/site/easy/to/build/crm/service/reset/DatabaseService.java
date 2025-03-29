package site.easy.to.build.crm.service.reset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void resetDatabase() {
        String[] sql = {
            "SET FOREIGN_KEY_CHECKS = 0;",
            "DELETE FROM email_template;",
            "DELETE FROM contract_settings;",
            "DELETE FROM trigger_contract;",
            "DELETE FROM trigger_ticket;",
            "DELETE FROM trigger_lead;",
            "DELETE FROM employee;",
            "DELETE FROM customer_login_info;",
            "DELETE FROM customer;",
            "DELETE FROM customer_expenses;",
            "DELETE FROM customer_budget;",
            "SET FOREIGN_KEY_CHECKS = 1;"
        };
        for (String query : sql) {
            jdbcTemplate.execute(query);
        }
        System.out.println("Database reset successfully");
    }
}