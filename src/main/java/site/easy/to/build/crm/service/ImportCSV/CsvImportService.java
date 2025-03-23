package site.easy.to.build.crm.service.ImportCSV;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import site.easy.to.build.crm.entity.Contract;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.CustomerLoginInfo;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.repository.ContractRepository;
import site.easy.to.build.crm.repository.CustomerRepository;
import site.easy.to.build.crm.repository.LeadRepository;
import site.easy.to.build.crm.repository.TicketRepository;
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
                if (record.length != 8) {
                    errors.add("Invalid record length at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record));
                    continue;
                }
                try {
                    try {
                        Integer.parseInt(record[4]);
                    } catch (NumberFormatException e) {
                        errors.add("Invalid manager ID format at line " + (records.indexOf(record) + 1) + ": " + record[4]);
                        continue;
                    }
                    try {
                        Integer.parseInt(record[5]);
                    } catch (NumberFormatException e) {
                        errors.add("Invalid employee ID format at line " + (records.indexOf(record) + 1) + ": " + record[5]);
                        continue;
                    }
                    try {
                        Integer.parseInt(record[6]);
                    } catch (NumberFormatException e) {
                        errors.add("Invalid customer ID format at line " + (records.indexOf(record) + 1) + ": " + record[6]);
                        continue;
                    }
                    try {
                        LocalDateTime.parse(record[7]);
                    } catch (DateTimeParseException e) {
                        errors.add("Invalid date format at line " + (records.indexOf(record) + 1) + ": " + record[7] + ". Expected format: yyyy-MM-ddTHH:mm:ss");
                        continue;
                    }

                    User manager = userService.findById(Integer.parseInt(record[4]));
                    User employee = userService.findById(Integer.parseInt(record[5]));
                    Customer customer = customerService.findByCustomerId(Integer.parseInt(record[6]));

                    if (manager == null) {
                        errors.add("Manager not found at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record) + " manager id => " + record[4]);
                    }
                    if (employee == null) {
                        errors.add("Employee not found at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record) + " employee id => " + record[5]);
                    }
                    if (customer == null) {
                        errors.add("Customer not found at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record) + " customer id => " + record[6]);
                    }

                } catch (Exception e) {
                    errors.add("Unexpected error at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record));
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
                if (record.length != 15) {
                    errors.add("Invalid record length at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record));
                    continue;
                }
                try {
                    try {
                        LocalDateTime.parse(record[12]);
                    } catch (DateTimeParseException e) {
                        errors.add("Invalid date format at line " + (records.indexOf(record) + 1) + ": " + record[12] + ". Expected format: yyyy-MM-ddTHH:mm:ss");
                        continue;
                    }
                } catch (Exception e) {
                    errors.add("Unexpected error at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record) + " erreur => " + e.getMessage());
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

    public List<Ticket> parseTicketsFromCsv(String filePath) throws IOException, CsvException {
        List<Ticket> tickets = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            boolean isFirstLine = true;

            for (String[] record : records) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (record.length == 8) {
                    try {
                        Ticket ticket = new Ticket();
                        ticket.setSubject(record[0]);
                        ticket.setDescription(record[1]);
                        ticket.setStatus(record[2]);
                        ticket.setPriority(record[3]);
                        ticket.setManager(userService.findById(Integer.parseInt(record[4])));
                        ticket.setEmployee(userService.findById(Integer.parseInt(record[5])));
                        ticket.setCustomer(customerService.findByCustomerId(Integer.parseInt(record[6])));
                        ticket.setCreatedAt(LocalDateTime.parse(record[7]));
                        tickets.add(ticket);
                    } catch (Exception e) {
                        System.err.println("Error parsing record at line " + (records.indexOf(record) + 1) + ": " + String.join(",", record));
                    }
                }
            }
        }
        return tickets;
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
                if (record.length == 15) {
                    try {
                        Customer customer = new Customer();
                        customer.setName(record[0]);
                        customer.setPhone(record[1]);
                        customer.setAddress(record[2]);
                        customer.setCity(record[3]);
                        customer.setState(record[4]);
                        customer.setCountry(record[5]);

                        // User user = userService.findById(Integer.parseInt(record[6]));
                        User user = userService.findById(55);
                        customer.setUser(user);

                        customer.setDescription(record[7]);
                        customer.setPosition(record[8]);
                        customer.setTwitter(record[9]);
                        customer.setFacebook(record[10]);
                        customer.setYoutube(record[11]);
                        customer.setCreatedAt(LocalDateTime.parse(record[12]));
                        customer.setEmail(record[13]);

                        CustomerLoginInfo customerLoginInfo = new CustomerLoginInfo();
                        customerLoginInfo.setEmail(record[13]);
                        customerLoginInfo.setPassword("defaultPassword");
                        customerLoginInfo.setPasswordSet(false);
                        
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

    public void saveTickets(List<Ticket> tickets) {
        ticketRepository.saveAll(tickets);
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
}