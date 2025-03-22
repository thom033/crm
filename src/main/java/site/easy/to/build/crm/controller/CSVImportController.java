package site.easy.to.build.crm.controller;

import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import site.easy.to.build.crm.service.ImportCSV.CsvImportService;

import java.io.File;
import java.io.IOException;

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
    public String importTickets(@RequestParam("file") MultipartFile file) {
        try {
            File tempFile = File.createTempFile("tickets", ".csv");
            file.transferTo(tempFile);
            csvImportService.importTicketsFromCsv(tempFile.getAbsolutePath());
            
            return "Tickets imported successfully";
        } catch (IOException | CsvException e) {
            return "Error importing tickets: " + e.getMessage();
        }
    }
}