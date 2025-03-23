package site.easy.to.build.crm.controller;

import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

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
    @PostMapping("/import-tickets")
    public String importTickets(@RequestParam("file") MultipartFile file, Model model) {
        try {
            System.out.println("Starting importTickets method");

            // Save the file temporarily to get the absolute path
            File tempFile = File.createTempFile("tickets", ".csv");
            file.transferTo(tempFile);

            String absoluteFilePath = tempFile.getAbsolutePath();
            System.out.println("Absolute file path: " + absoluteFilePath);

            List<String> errors = csvImportService.validateTicketsFromCsv(absoluteFilePath);
            if (!errors.isEmpty()) {
                System.out.println("Validation errors found: " + errors);
                model.addAttribute("errors", errors);
                return "csv/import-errors";
            }

            List<Ticket> tickets = csvImportService.parseTicketsFromCsv(absoluteFilePath);
            model.addAttribute("tickets", tickets);
            model.addAttribute("filePath", absoluteFilePath); // Add filePath to model
            model.addAttribute("type", "tickets"); // Add type to model
            System.out.println("Tickets parsed and added to model");
            
            return "csv/confirm-import";
        } catch (IOException | CsvException e) {
            String error = "Error importing tickets: " + e.getMessage();
            model.addAttribute("error", error);
            System.out.println("Error importing tickets: " + e.getMessage());
            return "csv/import-errors";
        }
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
                    List<Ticket> tickets = csvImportService.parseTicketsFromCsv(filePath);
                    csvImportService.saveTickets(tickets);
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
    @PostMapping("/import-customers")
    public String importCustomers(@RequestParam("file") MultipartFile file, Model model) {
        try {
            File tempFile = File.createTempFile("customers", ".csv");
            file.transferTo(tempFile);

            String absoluteFilePath = tempFile.getAbsolutePath();

            List<String> errors = csvImportService.validateCustomersFromCsv(absoluteFilePath);
            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                return "csv/import-errors";
            }

            List<Customer> customers = csvImportService.parseCustomersFromCsv(absoluteFilePath);
            model.addAttribute("customers", customers);
            model.addAttribute("filePath", absoluteFilePath);
            model.addAttribute("type", "customers"); // Add type to model
            
            return "csv/confirm-import";
        } catch (IOException | CsvException e) {
            model.addAttribute("error", "Error importing customers: " + e.getMessage());
            return "csv/import-errors";
        }
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PostMapping("/import-leads")
    public String importLeads(@RequestParam("file") MultipartFile file, Model model) {
        try {
            File tempFile = File.createTempFile("leads", ".csv");
            file.transferTo(tempFile);

            String absoluteFilePath = tempFile.getAbsolutePath();

            List<String> errors = csvImportService.validateLeadsFromCsv(absoluteFilePath);
            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                return "csv/import-errors";
            }

            List<Lead> leads = csvImportService.parseLeadsFromCsv(absoluteFilePath);
            model.addAttribute("leads", leads);
            model.addAttribute("filePath", absoluteFilePath);
            model.addAttribute("type", "leads"); // Add type to model
            
            return "csv/confirm-import";
        } catch (IOException | CsvException e) {
            model.addAttribute("error", "Error importing leads: " + e.getMessage());
            return "csv/import-errors";
        }
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PostMapping("/import-contracts")
    public String importContracts(@RequestParam("file") MultipartFile file, Model model) {
        try {
            File tempFile = File.createTempFile("contracts", ".csv");
            file.transferTo(tempFile);

            String absoluteFilePath = tempFile.getAbsolutePath();

            List<String> errors = csvImportService.validateContractsFromCsv(absoluteFilePath);
            if (!errors.isEmpty()) {
                model.addAttribute("errors", errors);
                return "csv/import-errors";
            }

            List<Contract> contracts = csvImportService.parseContractsFromCsv(absoluteFilePath);
            System.out.println("Parsed contracts: " + contracts); // Add this line to log the parsed contracts
            model.addAttribute("contracts", contracts);
            model.addAttribute("filePath", absoluteFilePath);
            model.addAttribute("type", "contracts"); // Add type to model
            
            return "csv/confirm-import";
        } catch (IOException | CsvException e) {
            model.addAttribute("error", "Error importing contracts: " + e.getMessage());
            return "csv/import-errors";
        }
    }
}