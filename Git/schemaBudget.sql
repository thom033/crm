---------------- budget ----------------------
CREATE TABLE IF NOT EXISTS `customer_budget` (
  `budget_id` int NOT NULL AUTO_INCREMENT,
  `customer_id` int unsigned NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `created_at` date NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`budget_id`),
  KEY `customer_id` (`customer_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `budget_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`),
  CONSTRAINT `budget_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

---------------- depenses ----------------------
CREATE TABLE IF NOT EXISTS `customer_expenses` (
  `expense_id` int NOT NULL AUTO_INCREMENT,
  `lead_id` int unsigned DEFAULT NULL,
  `ticket_id` int unsigned DEFAULT NULL,
  `amount` decimal(10,2) NOT NULL,
  `create_at` datetime DEFAULT NULL,
  `update_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `user_id` int DEFAULT NULL,
  PRIMARY KEY (`expense_id`),
  KEY `lead_id` (`lead_id`),
  KEY `ticket_id` (`ticket_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `depense_ibfk_1` FOREIGN KEY (`lead_id`) REFERENCES `trigger_lead` (`lead_id`),
  CONSTRAINT `depense_ibfk_2` FOREIGN KEY (`ticket_id`) REFERENCES `trigger_ticket` (`ticket_id`),
  CONSTRAINT `depense_ibfk_3` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

---------------- alerte ----------------------
CREATE TABLE IF NOT EXISTS `alert_rate` (
  `rate_id` int NOT NULL AUTO_INCREMENT,
  `alert_percentage` decimal(5,2) NOT NULL,
  `created_at` datetime DEFAULT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`rate_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `alert_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
