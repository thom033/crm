package site.easy.to.build.crm.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.expense.ExpenseService;
import site.easy.to.build.crm.service.lead.LeadService;
import site.easy.to.build.crm.service.ticket.TicketService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.AuthenticationUtils;
import site.easy.to.build.crm.util.AuthorizationUtil;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/employee/expense")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CustomerService customerService;
    private final AuthenticationUtils authenticationUtils;
    private final UserService userService;
    private final LeadService leadService;
    private final TicketService ticketService;

    public ExpenseController(
        ExpenseService expenseService, 
        CustomerService customerService,
        LeadService leadService,
        TicketService ticketService,
        AuthenticationUtils authenticationUtils, 
        UserService userService) {
        this.expenseService = expenseService;
        this.customerService = customerService;
        this.authenticationUtils = authenticationUtils;
        this.userService = userService;
        this.leadService = leadService;
        this.ticketService = ticketService;
    }

    @GetMapping("/create-expense")
    public String showCreateForm(Model model, Authentication authentication) {
        // int userId = authenticationUtils.getLoggedInUserId(authentication);
        // List<Customer> customers = AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")
        //         ? customerService.findAll()
        //         : customerService.findByUserId(userId);

        model.addAttribute("expense", new Expense());
        // model.addAttribute("customers", customers);
        model.addAttribute("leads", leadService.findAll());   // Remplace par ton service de Lead
        model.addAttribute("tickets", ticketService.findAll()); // Remplace par ton service de Ticket
        return "expense/create-expense";
    }
    @PostMapping("/create-expense")
    public String createExpense(@ModelAttribute("expense") @Valid Expense expense,
                                BindingResult result,
                                @RequestParam("type") String type,
                                @RequestParam(name = "leadId", required = false) Integer leadId,
                                @RequestParam(name = "ticketId", required = false) Integer ticketId,
                                Authentication authentication,
                                Model model) {
    
        if (result.hasErrors()) {
            int userId = authenticationUtils.getLoggedInUserId(authentication);
            model.addAttribute("customers", customerService.findByUserId(userId));
            model.addAttribute("leads", leadService.findAll());
            model.addAttribute("tickets", ticketService.findAll());
            return "expense/create-expense";
        }
    
        if ("0".equals(type)) {
            if (leadId != null) {
                expense.setLead(leadService.findByLeadId(leadId));
            }
            expense.setTicket(null);
        } else if ("1".equals(type)) { 
            if (ticketId != null) {
                expense.setTicket(ticketService.findByTicketId(ticketId));
            }
            expense.setLead(null);
        }
    
        expense.setCreatedAt(LocalDateTime.now());
        expenseService.save(expense);
        return "redirect:/employee/expense/create-expense?success";
    }
}
