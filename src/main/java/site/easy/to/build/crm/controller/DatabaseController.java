package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import site.easy.to.build.crm.service.reset.DatabaseService;

@RestController
@RequestMapping("/database")
public class DatabaseController {

    @Autowired
    private DatabaseService databaseService;

    @GetMapping("/reset")
    public RedirectView resetDatabaseAndRedirect() {
        try {
            databaseService.resetDatabase();
            return new RedirectView("/employee/settings/google-services"); 
        } catch (Exception e) {
            return new RedirectView("/error");
        }
    }
}