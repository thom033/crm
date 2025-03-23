package site.easy.to.build.crm.service.ImportCSV;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.repository.TicketRepository;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.user.UserService;

import java.io.FileReader;
import java.io.IOException;
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

    public void saveTickets(List<Ticket> tickets) {
        ticketRepository.saveAll(tickets);
    }
}