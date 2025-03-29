package site.easy.to.build.crm.service.ImportCSV;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import site.easy.to.build.crm.entity.Contract;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.CustomerLoginInfo;
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

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerLoginInfoService customerLoginInfoService;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    public List<String> validateTicketsFromCsv(String filePath) throws IOException, CsvException {
        List<String> errors = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            boolean isFirstLine = true;

            for (String[] record : records) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (record.length != 5) {
                    errors.add("Invalid record length at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record));
                    continue;
                }
                try {
                    Customer customer = customerService.findByEmail(record[0]);

                    if (customer == null) {
                        errors.add("Customer not found at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record));
                    }

                    if (!record[2].equals("lead") && !record[2].equals("ticket")){
                        errors.add("Invalid type at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record) + " type => " + record[2]);
                    }

                } catch (Exception e) {
                    errors.add("Unexpected error at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record) + " error => " + e.getMessage());
                }
            }
        }
        return errors;
    }

    public List<String> validateCustomersFromCsv(String filePath) throws IOException, CsvException {
        List<String> errors = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            boolean isFirstLine = true;

            for (String[] record : records) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (record.length != 2) {
                    errors.add("Invalid record length at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record));
                    continue;
                }
            }
        }
        return errors;
    }

    public List<String> validateLeadsFromCsv(String filePath) throws IOException, CsvException {
        List<String> errors = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            boolean isFirstLine = true;

            for (String[] record : records) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (record.length != 11) {
                    errors.add("Invalid record length at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record));
                    continue;
                }
                // Add more validation logic as needed
                try {
                    try {
                        Integer.parseInt(record[8]);
                    } catch (NumberFormatException e) {
                        errors.add("Invalid lead ID format at line " + (records.indexOf(record) + 1) + ": " + record[8]);
                        continue;
                    }
                    try {
                        Integer.parseInt(record[9]);
                    } catch (NumberFormatException e) {
                        errors.add("Invalid user ID format at line " + (records.indexOf(record) + 1) + ": " + record[9]);
                        continue;
                    }
                    try {
                        Integer.parseInt(record[10]);
                    } catch (NumberFormatException e) {
                        errors.add("Invalid customer ID format at line " + (records.indexOf(record) + 1) + ": " + record[10]);
                        continue;
                    }
                    try {
                        LocalDateTime.parse(record[11]);
                    } catch (DateTimeParseException e) {
                        errors.add("Invalid date format at line " + (records.indexOf(record) + 1) + ": " + record[11] + ". Expected format: yyyy-MM-ddTHH:mm:ss");
                        continue;
                    }

                    Lead lead = leadRepository.findById(Integer.parseInt(record[8])).orElse(null);
                    User user = userService.findById(Integer.parseInt(record[9]));
                    Customer customer = customerService.findByCustomerId(Integer.parseInt(record[10]));

                    if (lead == null) {
                        errors.add("Lead not found at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record) + " lead id => " + record[8]);
                    }
                    if (user == null) {
                        errors.add("User not found at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record) + " user id => " + record[9]);
                    }
                    if (customer == null) {
                        errors.add("Customer not found at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record) + " customer id => " + record[10]);
                    }

                } catch (Exception e) {
                    errors.add("Unexpected error at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record));
                }
            }
        }
        return errors;
    }

    public List<String> validateContractsFromCsv(String filePath) throws IOException, CsvException {
        List<String> errors = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            boolean isFirstLine = true;

            for (String[] record : records) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (record.length != 12) {
                    errors.add("Invalid record length at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record));
                    continue;
                }
                // Add more validation logic as needed
                try {
                    try {
                        Integer.parseInt(record[8]);
                    } catch (NumberFormatException e) {
                        errors.add("Invalid lead ID format at line " + (records.indexOf(record) + 1) + ": " + record[8]);
                        continue;
                    }
                    try {
                        Integer.parseInt(record[9]);
                    } catch (NumberFormatException e) {
                        errors.add("Invalid user ID format at line " + (records.indexOf(record) + 1) + ": " + record[9]);
                        continue;
                    }
                    try {
                        Integer.parseInt(record[10]);
                    } catch (NumberFormatException e) {
                        errors.add("Invalid customer ID format at line " + (records.indexOf(record) + 1) + ": " + record[10]);
                        continue;
                    }
                    try {
                        LocalDateTime.parse(record[11]);
                    } catch (DateTimeParseException e) {
                        errors.add("Invalid date format at line " + (records.indexOf(record) + 1) + ": " + record[11] + ". Expected format: yyyy-MM-ddTHH:mm:ss");
                        continue;
                    }

                    Lead lead = leadRepository.findById(Integer.parseInt(record[8])).orElse(null);
                    User user = userService.findById(Integer.parseInt(record[9]));
                    Customer customer = customerService.findByCustomerId(Integer.parseInt(record[10]));

                    if (lead == null) {
                        errors.add("Lead not found at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record) + " lead id => " + record[8]);
                    }
                    if (user == null) {
                        errors.add("User not found at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record) + " user id => " + record[9]);
                    }
                    if (customer == null) {
                        errors.add("Customer not found at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record) + " customer id => " + record[10]);
                    }

                } catch (Exception e) {
                    errors.add("Unexpected error at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record));
                }
                
            }
        }
        return errors;
    }

    public List<String> validateBudgetFromCsv(String filePath) throws IOException, CsvException {
        List<String> errors = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            boolean isFirstLine = true;

            for (String[] record : records) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (record.length != 2) {
                    errors.add("Invalid record length at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record));
                    continue;
                }
                try {
                    Customer customer = customerService.findByEmail(record[0]);
                    if (customer == null) {
                        errors.add("Customer not found at line " + (records.indexOf(record) + 1) + ": " + record[0]);
                    }
                    new BigDecimal(record[1]); // Validate amount format
                } catch (NumberFormatException e) {
                    errors.add("Invalid amount format at line " + (records.indexOf(record) + 1) + ": " + record[1]);
                }
            }
        }
        return errors;
    }

    public void parseTicketsFromCsv(String filePath) throws IOException, CsvException {
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
                        int customerId = customer.getCustomerId();
                        System.out.println("Customer ID: " + customerId);

                        Lead lead = null;
                        Ticket ticket = null;

                        Expense expense = new Expense();

                        if(record[2].equals("lead")){
                            lead = new Lead();
                            lead.setName(record[1]);
                            lead.setStatus(record[3]);
                            lead.setPhone("1234567890");
                            lead.setMeetingId(null);
                            lead.setGoogleDrive(false);
                            lead.setGoogleDriveFolderId("1234567890");
                            lead.setManager(userService.findById(55));
                            lead.setEmployee(userService.findById(55));
                            lead.setCustomer(customer);
                            lead.setCreatedAt(LocalDateTime.now());
                            leadRepository.save(lead);
                        }

                        if (record[2].equals("ticket")) { 
                            ticket = new Ticket();                           
                            ticket.setSubject(record[1]);
                            ticket.setDescription(record[1]);
                            ticket.setStatus(record[3]);
                            ticket.setPriority("low");
                            ticket.setManager(userService.findById(55));
                            ticket.setEmployee(userService.findById(55));
                            ticket.setCustomer(customer);
                            ticket.setCreatedAt(LocalDateTime.now());
                            ticketRepository.save(ticket);
                        }

                        expense.setTicket(ticket);
                        expense.setLead(lead);
                        expense.setAmount(Double.parseDouble(record[4].replace(",", ".")));
                        expense.setCreatedAt(LocalDateTime.now());
                        expense.setUpdatedAt(LocalDateTime.now());
                        expense.setUser(userService.findById(55));
                        expenseRepository.save(expense);
                    } catch (Exception e) {
                        System.err.println("Error parsing record at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record) + " error => " + e.getMessage());
                    }
                }
            }
        }
    }

    public List<Customer> parseCustomersFromCsv(String filePath) throws IOException, CsvException {
        List<Customer> customers = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) { 
            List<String[]> records = reader.readAll();
            boolean isFirstLine = true;

            for (String[] record : records) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (record.length == 2) {
                    try {
                        Customer customer = new Customer();
                        customer.setName(record[1]);
                        customer.setEmail(record[0]);

                        customer.setPosition("default position");
                        customer.setPhone("1234567890");
                        customer.setAddress("default address");
                        customer.setCity("default city");
                        customer.setState("default state");
                        customer.setCountry("Madagascar");
                        customer.setTwitter("default twitter");
                        customer.setDescription("default description");
                        customer.setFacebook("facebook");
                        customer.setYoutube("youtube");

                        CustomerLoginInfo customerLoginInfo = new CustomerLoginInfo();
                        customerLoginInfo.setEmail(record[1]);
                        customerLoginInfo.setPassword("password");

                        // Add userId of the connected user
                        User user = userService.findById(55);
                        customer.setUser(user);

                        customerLoginInfo.setPasswordSet(true);

                        // Save CustomerLoginInfo first
                        customerLoginInfo = customerLoginInfoService.save(customerLoginInfo);
                        
                        customer.setCustomerLoginInfo(customerLoginInfo);
                        
                        customers.add(customer);
                    } catch (Exception e) {
                        System.err.println("Error parsing record at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record));
                    }
                }
            }
        }
        return customers;
    }

    public List<Lead> parseLeadsFromCsv(String filePath) throws IOException, CsvException {
        List<Lead> leads = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            boolean isFirstLine = true;

            for (String[] record : records) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (record.length == 11) {
                    try {
                        Lead lead = new Lead();
                        lead.setName(record[0]);
                        lead.setStatus(record[1]);
                        lead.setPhone(record[2]);
                        lead.setMeetingId(record[3]);
                        lead.setGoogleDrive(Boolean.parseBoolean(record[4]));
                        lead.setGoogleDriveFolderId(record[5]);
                        lead.setManager(userService.findById(Integer.parseInt(record[6])));
                        lead.setEmployee(userService.findById(Integer.parseInt(record[7])));
                        lead.setCustomer(customerService.findByCustomerId(Integer.parseInt(record[8])));
                        lead.setCreatedAt(LocalDateTime.parse(record[9]));
                        leads.add(lead);
                    } catch (Exception e) {
                        System.err.println("Error parsing record at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record));
                    }
                }
            }
        }
        return leads;
    }

    public List<Contract> parseContractsFromCsv(String filePath) throws IOException, CsvException {
        List<Contract> contracts = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            boolean isFirstLine = true;

            for (String[] record : records) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                System.out.println("Parsing record: " + String.join(",", record)); // Add this line to log the record
                System.out.println("Record length: " + record.length); // Add this line to log the record length
                if (record.length == 12) {
                    try {
                        Contract contract = new Contract();
                        contract.setSubject(record[0]);
                        contract.setStatus(record[1]);
                        contract.setDescription(record[2]);
                        contract.setStartDate(record[3]);
                        contract.setEndDate(record[4]);
                        contract.setAmount(new BigDecimal(record[5]));
                        contract.setGoogleDrive(Boolean.parseBoolean(record[6]));
                        contract.setGoogleDriveFolderId(record[7]);
                        contract.setLead(leadRepository.findById(Integer.parseInt(record[8])).orElse(null));
                        contract.setUser(userService.findById(Integer.parseInt(record[9])));
                        contract.setCustomer(customerService.findByCustomerId(Integer.parseInt(record[10])));
                        contract.setCreatedAt(LocalDateTime.parse(record[11]));
                        contracts.add(contract);
                    } catch (Exception e) {
                        System.err.println("Error parsing record at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record));
                    }
                } else {
                    System.err.println("Invalid record length at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record));
                }
            }
        }
        return contracts;
    }

    public List<Budget> parseBudgetFromCsv(String filePath) throws IOException, CsvException {
        List<Budget> budgets = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            boolean isFirstLine = true;

            for (String[] record : records) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (record.length == 2) {
                    try {
                        Customer customer = customerService.findByEmail(record[0]);
                        if (customer != null) {
                            Budget budget = new Budget();
                            budget.setCustomer(customer);
                            budget.setAmount(new BigDecimal(record[1]));
                            budget.setCreatedAt(LocalDateTime.now());
                            budgets.add(budget);
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing record at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record));
                    }
                }
            }
        }
        return budgets;
    }

    public void saveTickets() {
        System.out.println("Fonction Misy SYSOUT ftsn");
    }

    public void saveCustomers(List<Customer> customers) {
        customerRepository.saveAll(customers);
    }

    public void saveLeads(List<Lead> leads) {
        leadRepository.saveAll(leads);
    }

    public void saveContracts(List<Contract> contracts) {
        System.out.println("Saving contracts: " + contracts); // Add this line to log the contracts
        contractRepository.saveAll(contracts);
    }

    public void saveBudgets(List<Budget> budgets) {
        budgetRepository.saveAll(budgets);
    }
}