
---------------- budget ----------------------
CREATE TABLE IF NOT EXISTS customer_budget (
  budget_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  budget_name VARCHAR(255) NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  customer_id INT UNSIGNED NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (budget_id),
  KEY customer_id (customer_id),
  CONSTRAINT customer_budget_ibfk_1 FOREIGN KEY (customer_id) REFERENCES customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS customer_expenses (
  expense_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  expense_name VARCHAR(255) NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  price DECIMAL(10,2) NOT NULL, 
  date_expense DATE NOT NULL,
  ticket_id INT UNSIGNED DEFAULT NULL,
  lead_id INT UNSIGNED DEFAULT NULL,
  customer_id INT UNSIGNED NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (expense_id), 
  KEY ticket_id (ticket_id),
  KEY lead_id (lead_id),
  KEY customer_id (customer_id),
  CONSTRAINT customer_expenses_ibfk_1 FOREIGN KEY (ticket_id) REFERENCES trigger_ticket (ticket_id),
  CONSTRAINT customer_expenses_ibfk_2 FOREIGN KEY (lead_id) REFERENCES trigger_lead (lead_id),
  CONSTRAINT customer_expenses_ibfk_4 FOREIGN KEY (customer_id) REFERENCES customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS alert_rate (
  rate_id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  alert_percentage DECIMAL(5,2) NOT NULL DEFAULT 5.00,  -- Default to 5%
  date_rate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (rate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;