package site.easy.to.build.crm.service.ImportCSV;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import site.easy.to.build.crm.entity.Contract;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Expense;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.repository.ContractRepository;
import site.easy.to.build.crm.repository.CustomerRepository;
import site.easy.to.build.crm.repository.ExpenseRepository;
import site.easy.to.build.crm.repository.LeadRepository;
import site.easy.to.build.crm.repository.TicketRepository;
import site.easy.to.build.crm.repository.BudgetRepository;
import site.easy.to.build.crm.service.customer.CustomerLoginInfoService;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.user.UserService;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvImportService {

    // Static field to store errors
    private static List<String> importErrors = new ArrayList<>();

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    public List<String> validateTicketsFromCsv(String filePath) throws IOException, CsvException {
        List<String> errors = new ArrayList<>();
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            boolean isFirstLine = true;

            for (String[] record : records) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                if (record.length != 5) {
                    errors.add("Fichier " + fileName + " - Length of line is not 5 it is " + record.length + " at line " + (records.indexOf(record) + 1));
                    continue;
                }
                
                try {
                    Customer customer = customerService.findByEmail(record[0]);
                    if (customer == null) {
                        errors.add("Fichier " + fileName + " - Client non trouvé avec l'email: " + record[0] + " à la ligne " + (records.indexOf(record) + 1));
                    }

                    if (!record[2].equals("lead") && !record[2].equals("ticket")) {
                        errors.add("Fichier " + fileName + " - Type invalide à la ligne " + (records.indexOf(record) + 1) + ": " + record[2] + ". Doit être 'lead' ou 'ticket'");
                    }

                    try {
                        Double.parseDouble(record[4].replace(",", "."));
                    } catch (NumberFormatException e) {
                        errors.add("Fichier " + fileName + " - Format de montant invalide à la ligne " + (records.indexOf(record) + 1) + ": " + record[4]);
                    }
                } catch (Exception e) {
                    errors.add("Fichier " + fileName + " - Erreur lors du traitement de la ligne " + (records.indexOf(record) + 1) + ": " + String.join(",", record) + " - Erreur: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errors.add("Fichier " + fileName + " - Erreur lors de la lecture du fichier CSV: " + e.getMessage());
        }
        addImportErrors(errors);
        return errors;
    }

    public List<String> validateCustomersFromCsv(String filePath) throws IOException, CsvException {
        List<String> errors = new ArrayList<>();

        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            boolean isFirstLine = true;

            for (String[] record : records) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (record.length != 2) {
                    errors.add("Fichier " + fileName + " - Length of line is not 2 it is " + record.length + " at line " + (records.indexOf(record) + 1));
                    continue;
                }
                try {
                    if (record[0] == null || record[0].trim().isEmpty()) {
                        errors.add("Fichier " + fileName + " - Email manquant à la ligne " + (records.indexOf(record) + 1));
                    } else if (!record[0].matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                        errors.add("Fichier " + fileName + " - Format d'email invalide à la ligne " + (records.indexOf(record) + 1) + ": " + record[0]);
                    }
                    if (record[1] == null || record[1].trim().isEmpty()) {
                        errors.add("Fichier " + fileName + " - Nom manquant à la ligne " + (records.indexOf(record) + 1));
                    }
                } catch (Exception e) {
                    errors.add("Fichier " + fileName + " - Erreur lors du traitement de la ligne " + (records.indexOf(record) + 1) + ": " + String.join(",", record) + " - Erreur: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errors.add("Fichier " + fileName + " - Erreur lors de la lecture du fichier CSV: " + e.getMessage());
        }
        addImportErrors(errors);
        return errors;
    }

    public List<String> validateBudgetFromCsv(String filePath) throws IOException, CsvException {
        List<String> errors = new ArrayList<>();
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            boolean isFirstLine = true;

            for (String[] record : records) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (record.length != 2) {
                    errors.add("Fichier " + fileName + " - Format de ligne invalide à la ligne " + (records.indexOf(record) + 1) + ": " + String.join(",", record));
                    continue;
                }
                try {
                    Customer customer = customerService.findByEmail(record[0]);
                    if (customer == null) {
                        errors.add("Fichier " + fileName + " - Client non trouvé avec l'email: " + record[0] + " à la ligne " + (records.indexOf(record) + 1));
                    }
                    try {
                        new BigDecimal(record[1].replace(",", "."));
                    } catch (NumberFormatException e) {
                        errors.add("Fichier " + fileName + " - Format de montant invalide à la ligne " + (records.indexOf(record) + 1) + ": " + record[1]);
                    }
                } catch (Exception e) {
                    errors.add("Fichier " + fileName + " - Erreur lors du traitement de la ligne " + (records.indexOf(record) + 1) + ": " + String.join(",", record) + " - Erreur: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errors.add("Fichier " + fileName + " - Erreur lors de la lecture du fichier CSV: " + e.getMessage());
        }
        addImportErrors(errors);
        return errors;
    }

    public List<String> parseTicketsFromCsv(String filePath) throws IOException, CsvException {
        List<String> errors = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            boolean isFirstLine = true;

            for (String[] record : records) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (record.length == 5) {
                    try {
                        Customer customer = customerService.findByEmail(record[0]);
                        if (customer == null) {
                            errors.add("Client non trouvé: " + record[0]);
                            continue;
                        }
                        
                        int customerId = customer.getCustomerId();
                        System.out.println("Customer ID: " + customerId);

                        Expense expense = new Expense();
                        Lead lead = null;
                        Ticket ticket = null;

                        // Get a valid user for the expense
                        User user = userService.findById(55);
                        if (user == null) {
                            // Try to find any user if the specific one doesn't exist
                            List<User> users = userService.findAll();
                            if (!users.isEmpty()) {
                                user = users.get(0);
                            } else {
                                errors.add("Aucun utilisateur trouvé dans le système. Impossible de créer la dépense.");
                                continue;
                            }
                        }

                        if(record[2].equals("lead")){
                            lead = new Lead();
                            lead.setName(record[1]);
                            lead.setStatus(record[3]);
                            lead.setPhone("1234567890");
                            lead.setMeetingId(null);
                            lead.setGoogleDrive(false);
                            lead.setGoogleDriveFolderId("1234567890");
                            lead.setManager(user);
                            lead.setEmployee(user);
                            lead.setCustomer(customer);
                            lead.setCreatedAt(LocalDateTime.now());
                            leadRepository.save(lead);

                            expense.setExpenseTypeId(2);
                        }

                        if (record[2].equals("ticket")) { 
                            ticket = new Ticket();                           
                            ticket.setSubject(record[1]);
                            ticket.setDescription(record[1]);
                            ticket.setStatus(record[3]);
                            ticket.setPriority("low");
                            ticket.setManager(user);
                            ticket.setEmployee(user);
                            ticket.setCustomer(customer);
                            ticket.setCreatedAt(LocalDateTime.now());
                            ticketRepository.save(ticket);

                            expense.setExpenseTypeId(1);
                        }

                        
                        expense.setTicket(ticket);
                        expense.setLead(lead);
                        expense.setCustomer(customer);
                        expense.setAmount(Double.parseDouble(record[4].replace(",", ".")));
                        expense.setCreatedAt(LocalDateTime.now());
                        expense.setUpdatedAt(LocalDateTime.now());
                        expense.setExpenseDate(LocalDateTime.now());
                        
                        // Save the expense
                        expenseRepository.save(expense);
                    } catch (Exception e) {
                        errors.add("Erreur lors du traitement de la ligne " + (records.indexOf(record) + 1) + ": " + String.join(",", record) + " - Erreur: " + e.getMessage());
                    }
                } else {
                    errors.add("Format de ligne invalide à la ligne " + (records.indexOf(record) + 1) + ": " + String.join(",", record));
                }
            }
        }
        
        // Add errors to the static list
        addImportErrors(errors);
        
        return errors;
    }

    public List<Customer> parseCustomersFromCsv(String filePath) throws IOException, CsvException {
        List<Customer> customers = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            boolean isFirstLine = true;

            for (String[] record : records) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (record.length != 2) {
                    errors.add("Fichier " + fileName + " - Format de ligne invalide à la ligne " + (records.indexOf(record) + 1) + ": " + String.join(",", record));
                    continue;
                }
                try {
                    Customer customer = new Customer();
                    customer.setName(record[1]);
                    customer.setEmail(record[0]);

                    // Validation des champs requis
                    if (customer.getName() == null || customer.getName().trim().isEmpty()) {
                        errors.add("Fichier " + fileName + " - Nom manquant à la ligne " + (records.indexOf(record) + 1));
                    }
                    if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
                        errors.add("Fichier " + fileName + " - Email manquant à la ligne " + (records.indexOf(record) + 1));
                    } else if (!customer.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                        errors.add("Fichier " + fileName + " - Format d'email invalide à la ligne " + (records.indexOf(record) + 1) + ": " + customer.getEmail());
                    }

                    customers.add(customer);
                } catch (Exception e) {
                    errors.add("Fichier " + fileName + " - Erreur lors du traitement de la ligne " + (records.indexOf(record) + 1) + ": " + String.join(",", record) + " - Erreur: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errors.add("Fichier " + fileName + " - Erreur lors de la lecture du fichier CSV: " + e.getMessage());
        }
        addImportErrors(errors);
        return customers;
    }

    public List<Lead> parseLeadsFromCsv(String filePath) throws IOException, CsvException {
        List<Lead> leads = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            boolean isFirstLine = true;

            for (String[] record : records) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (record.length != 11) {
                    errors.add("Fichier " + fileName + " - Format de ligne invalide à la ligne " + (records.indexOf(record) + 1) + ": " + String.join(",", record));
                    continue;
                }
                try {
                    Lead lead = new Lead();
                    lead.setName(record[0]);
                    lead.setStatus(record[1]);
                    lead.setPhone(record[2]);
                    lead.setMeetingId(record[3]);
                    lead.setGoogleDrive(Boolean.parseBoolean(record[4]));
                    lead.setGoogleDriveFolderId(record[5]);

                    // Validation des champs requis
                    if (lead.getName() == null || lead.getName().trim().isEmpty()) {
                        errors.add("Fichier " + fileName + " - Nom manquant à la ligne " + (records.indexOf(record) + 1));
                    }
                    if (lead.getStatus() == null || lead.getStatus().trim().isEmpty()) {
                        errors.add("Fichier " + fileName + " - Statut manquant à la ligne " + (records.indexOf(record) + 1));
                    }

                    // Validation des relations
                    try {
                        User manager = userService.findById(Integer.parseInt(record[6]));
                        if (manager == null) {
                            errors.add("Fichier " + fileName + " - Gestionnaire non trouvé avec l'ID: " + record[6] + " à la ligne " + (records.indexOf(record) + 1));
                        } else {
                            lead.setManager(manager);
                        }
                    } catch (NumberFormatException e) {
                        errors.add("Fichier " + fileName + " - ID de gestionnaire invalide à la ligne " + (records.indexOf(record) + 1) + ": " + record[6]);
                    }

                    try {
                        User employee = userService.findById(Integer.parseInt(record[7]));
                        if (employee == null) {
                            errors.add("Fichier " + fileName + " - Employé non trouvé avec l'ID: " + record[7] + " à la ligne " + (records.indexOf(record) + 1));
                        } else {
                            lead.setEmployee(employee);
                        }
                    } catch (NumberFormatException e) {
                        errors.add("Fichier " + fileName + " - ID d'employé invalide à la ligne " + (records.indexOf(record) + 1) + ": " + record[7]);
                    }

                    try {
                        Customer customer = customerService.findByCustomerId(Integer.parseInt(record[8]));
                        if (customer == null) {
                            errors.add("Fichier " + fileName + " - Client non trouvé avec l'ID: " + record[8] + " à la ligne " + (records.indexOf(record) + 1));
                        } else {
                            lead.setCustomer(customer);
                        }
                    } catch (NumberFormatException e) {
                        errors.add("Fichier " + fileName + " - ID de client invalide à la ligne " + (records.indexOf(record) + 1) + ": " + record[8]);
                    }

                    try {
                        lead.setCreatedAt(LocalDateTime.parse(record[9]));
                    } catch (DateTimeParseException e) {
                        errors.add("Fichier " + fileName + " - Format de date invalide à la ligne " + (records.indexOf(record) + 1) + ": " + record[9]);
                        lead.setCreatedAt(LocalDateTime.now());
                    }

                    leads.add(lead);
                } catch (Exception e) {
                    errors.add("Fichier " + fileName + " - Erreur lors du traitement de la ligne " + (records.indexOf(record) + 1) + ": " + String.join(",", record) + " - Erreur: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errors.add("Fichier " + fileName + " - Erreur lors de la lecture du fichier CSV: " + e.getMessage());
        }
        addImportErrors(errors);
        return leads;
    }

    public List<Budget> parseBudgetFromCsv(String filePath) throws IOException, CsvException {
        List<Budget> budgets = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            boolean isFirstLine = true;

            for (String[] record : records) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (record.length != 2) {
                    errors.add("Fichier " + fileName + " - Format de ligne invalide à la ligne " + (records.indexOf(record) + 1) + ": " + String.join(",", record));
                    continue;
                }
                try {
                    Customer customer = customerService.findByEmail(record[0]);
                    if (customer == null) {
                        errors.add("Fichier " + fileName + " - Client non trouvé avec l'email: " + record[0] + " à la ligne " + (records.indexOf(record) + 1));
                        continue;
                    }

                    try {
                        Budget budget = new Budget();
                        budget.setCustomer(customer);
                        budget.setAmount(new BigDecimal(record[1].replace(",", ".")));
                        budget.setCreatedAt(LocalDateTime.now());
                        budget.setBudgetName("Budget de " + customer.getName());
                        budgets.add(budget);
                    } catch (NumberFormatException e) {
                        errors.add("Fichier " + fileName + " - Format de montant invalide à la ligne " + (records.indexOf(record) + 1) + ": " + record[1]);
                    }
                } catch (Exception e) {
                    errors.add("Fichier " + fileName + " - Erreur lors du traitement de la ligne " + (records.indexOf(record) + 1) + ": " + String.join(",", record) + " - Erreur: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errors.add("Fichier " + fileName + " - Erreur lors de la lecture du fichier CSV: " + e.getMessage());
        }
        addImportErrors(errors);
        return budgets;
    }

    public void saveTickets() {
        try {
            System.out.println("Fonction Misy SYSOUT ftsn");
            // Si vous avez une logique de sauvegarde de tickets ici, ajoutez-la
        } catch (Exception e) {
            if (e.getCause() instanceof jakarta.validation.ConstraintViolationException) {
                jakarta.validation.ConstraintViolationException cve = (jakarta.validation.ConstraintViolationException) e.getCause();
                for (jakarta.validation.ConstraintViolation<?> violation : cve.getConstraintViolations()) {
                    addImportErrors(List.of("Erreur de validation ticket: " + violation.getMessage() + " pour " + violation.getPropertyPath()));
                }
            } else {
                addImportErrors(List.of("Erreur lors de la sauvegarde des tickets: " + e.getMessage()));
            }
        }
    }

    public void saveCustomers(List<Customer> customers) {
        try {
            customerRepository.saveAll(customers);
        } catch (Exception e) {
            if (e.getCause() instanceof jakarta.validation.ConstraintViolationException) {
                jakarta.validation.ConstraintViolationException cve = (jakarta.validation.ConstraintViolationException) e.getCause();
                for (jakarta.validation.ConstraintViolation<?> violation : cve.getConstraintViolations()) {
                    addImportErrors(List.of("Erreur de validation client: " + violation.getMessage() + " pour " + violation.getPropertyPath()));
                }
            } else {
                addImportErrors(List.of("Erreur lors de la sauvegarde des clients: " + e.getMessage()));
            }
        }
    }

    public void saveLeads(List<Lead> leads) {
        try {
            leadRepository.saveAll(leads);
        } catch (Exception e) {
            if (e.getCause() instanceof jakarta.validation.ConstraintViolationException) {
                jakarta.validation.ConstraintViolationException cve = (jakarta.validation.ConstraintViolationException) e.getCause();
                for (jakarta.validation.ConstraintViolation<?> violation : cve.getConstraintViolations()) {
                    addImportErrors(List.of("Erreur de validation lead: " + violation.getMessage() + " pour " + violation.getPropertyPath()));
                }
            } else {
                addImportErrors(List.of("Erreur lors de la sauvegarde des leads: " + e.getMessage()));
            }
        }
    }

    public void saveBudgets(List<Budget> budgets) {
        try {
            budgetRepository.saveAll(budgets);
        } catch (Exception e) {
            if (e.getCause() instanceof jakarta.validation.ConstraintViolationException) {
                jakarta.validation.ConstraintViolationException cve = (jakarta.validation.ConstraintViolationException) e.getCause();
                for (jakarta.validation.ConstraintViolation<?> violation : cve.getConstraintViolations()) {
                    addImportErrors(List.of("Erreur de validation budget: " + violation.getMessage() + " pour " + violation.getPropertyPath()));
                }
            } else {
                addImportErrors(List.of("Erreur lors de la sauvegarde des budgets: " + e.getMessage()));
            }
        }
    }

    // Method to get all errors
    public List<String> getImportErrors() {
        return new ArrayList<>(importErrors);
    }

    // Method to clear errors
    public void clearImportErrors() {
        importErrors.clear();
    }

    // Method to add errors
    public void addImportErrors(List<String> errors) {
        if (errors != null && !errors.isEmpty()) {
            importErrors.addAll(errors);
        }
    }
}