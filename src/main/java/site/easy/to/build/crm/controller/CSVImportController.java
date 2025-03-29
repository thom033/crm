package site.easy.to.build.crm.controller;

import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Contract;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.Ticket;
import site.easy.to.build.crm.service.ImportCSV.CsvImportService;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/employee/csv")
public class CSVImportController {

    @Autowired
    private CsvImportService csvImportService;

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @GetMapping("/import-tickets")
    public ModelAndView redirectToImportPage() {
        return new ModelAndView("csv/import"); // Return the import.html template
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PostMapping("/confirm-import")
    public String confirmImport(@RequestParam("filePath") String filePath, @RequestParam("type") String type, Model model) {
        System.out.println("Starting confirmImport method");
        System.out.println("File path: " + filePath);
        System.out.println("Import type: " + type);
        
        if (filePath == null) {
            model.addAttribute("error", "No file path found.");
            System.out.println("No file path found in model");
            return "csv/import-errors";
        }
        
        try {
            switch (type) {
                case "tickets":
                    csvImportService.parseTicketsFromCsv(filePath);
                    csvImportService.saveTickets();
                    System.out.println("Tickets saved successfully");
                    return "redirect:/employee/ticket/manager/all-tickets";
                case "customers":
                    List<Customer> customers = csvImportService.parseCustomersFromCsv(filePath);
                    csvImportService.saveCustomers(customers);
                    System.out.println("Customers saved successfully");
                    return "redirect:/employee/customer/manager/all-customers";
                case "leads":
                    List<Lead> leads = csvImportService.parseLeadsFromCsv(filePath);
                    csvImportService.saveLeads(leads);
                    System.out.println("Leads saved successfully");
                    return "redirect:/employee/lead/manager/all-leads";
                case "contracts":
                    List<Contract> contracts = csvImportService.parseContractsFromCsv(filePath);
                    csvImportService.saveContracts(contracts);
                    System.out.println("Contracts saved successfully");
                    return "redirect:/employee/contract/manager/show-all";
                case "budget": // Add handling for budget
                    List<Budget> budgets = csvImportService.parseBudgetFromCsv(filePath);
                    csvImportService.saveBudgets(budgets);
                    System.out.println("Budgets saved successfully");
                    return "redirect:/employee/csv/import-tickets";
                default:
                    model.addAttribute("error", "Invalid import type.");
                    System.out.println("Invalid import type: " + type);
                    return "csv/import-errors";
            }
        } catch (IOException | CsvException e) {
            model.addAttribute("error", "Error saving " + type + ": " + e.getMessage());
            System.out.println("Error saving " + type + ": " + e.getMessage());
            return "csv/import-errors";
        }
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PostMapping("/import-data")
    @Transactional // Add transactional annotation to ensure all-or-nothing behavior
    public String importData(
            @RequestParam("fileCustomer") MultipartFile fileCustomer,
            @RequestParam("fileBudget") MultipartFile fileBudget,
            @RequestParam("fileTicket") MultipartFile fileTicket,
            Model model) {
        try {
            // Process Customers
            File tempCustomerFile = File.createTempFile("customers", ".csv");
            fileCustomer.transferTo(tempCustomerFile);
            String customerFilePath = tempCustomerFile.getAbsolutePath();

            List<String> customerErrors = csvImportService.validateCustomersFromCsv(customerFilePath);
            if (!customerErrors.isEmpty()) {
                model.addAttribute("errors", customerErrors);
                return "csv/import-errors"; // Return error page if validation fails
            }
            List<Customer> customers = csvImportService.parseCustomersFromCsv(customerFilePath);
            csvImportService.saveCustomers(customers);

            // Process Budgets
            File tempBudgetFile = File.createTempFile("budgets", ".csv");
            fileBudget.transferTo(tempBudgetFile);
            String budgetFilePath = tempBudgetFile.getAbsolutePath();

            List<String> budgetErrors = csvImportService.validateBudgetFromCsv(budgetFilePath);
            if (!budgetErrors.isEmpty()) {
                model.addAttribute("errors", budgetErrors);
                return "csv/import-errors"; // Return error page if validation fails
            }
            List<Budget> budgets = csvImportService.parseBudgetFromCsv(budgetFilePath);
            csvImportService.saveBudgets(budgets);

            // Process Tickets
            File tempTicketFile = File.createTempFile("tickets", ".csv");
            fileTicket.transferTo(tempTicketFile);
            String ticketFilePath = tempTicketFile.getAbsolutePath();

            List<String> ticketErrors = csvImportService.validateTicketsFromCsv(ticketFilePath);
            if (!ticketErrors.isEmpty()) {
                model.addAttribute("errors", ticketErrors);
                return "csv/import-errors"; // Return error page if validation fails
            }
            csvImportService.parseTicketsFromCsv(ticketFilePath);
            csvImportService.saveTickets();

            return "redirect:/employee/csv/import-tickets"; // Redirect to a success page
        } catch (IOException | CsvException e) {
            model.addAttribute("error", "Error importing data: " + e.getMessage());
            return "csv/import-errors"; // Return error page on exception
        }
    }
}