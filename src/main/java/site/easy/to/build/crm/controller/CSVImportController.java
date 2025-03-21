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
    public String confirmImport(@RequestParam("filePath") String filePath, Model model) {
        System.out.println("Starting confirmImport method");
        System.out.println("new File path:" + filePath);
        if (filePath == null) {
            model.addAttribute("error", "No file path found.");
            System.out.println("No file path found in model");
            return "csv/import-errors";
        }
        try {
            List<Ticket> tickets = csvImportService.parseTicketsFromCsv(filePath);
            csvImportService.saveTickets(tickets);
            System.out.println("Tickets saved successfully");
            return "redirect:/employee/ticket/manager/all-tickets";
        
        } catch (IOException | CsvException e) {
            model.addAttribute("error", "Error saving tickets: " + e.getMessage());
            System.out.println("Error saving tickets: " + e.getMessage());
            return "csv/import-errors";
        }
    }
}