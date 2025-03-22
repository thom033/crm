package site.easy.to.build.crm.service.ImportCSV;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.Ticket;
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
                    errors.add("Invalid record length: " + String.join(",", record));
                    continue;
                }
                try {
                    Integer.parseInt(record[4]);
                    Integer.parseInt(record[5]);
                    Integer.parseInt(record[6]);
                    LocalDateTime.parse(record[7]);
                } catch (NumberFormatException | DateTimeParseException e) {
                    errors.add("Invalid data format: " + String.join(",", record));
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
                }
            }
        }
        return tickets;
    }

    public void saveTickets(List<Ticket> tickets) {
        ticketRepository.saveAll(tickets);
    }
}