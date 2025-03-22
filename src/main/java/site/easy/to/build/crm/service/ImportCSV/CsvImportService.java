package site.easy.to.build.crm.service.ImportCSV;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.repository.TicketRepository;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.user.UserService;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
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

    public void importTicketsFromCsv(String filePath) throws IOException, CsvException {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> records = reader.readAll();
            List<Ticket> tickets = new ArrayList<>();

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

            ticketRepository.saveAll(tickets);
        }
    }
}