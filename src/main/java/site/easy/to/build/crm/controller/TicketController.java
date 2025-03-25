package site.easy.to.build.crm.controller;

import jakarta.persistence.EntityManager;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.entity.settings.TicketEmailSettings;
import site.easy.to.build.crm.google.service.acess.GoogleAccessService;
import site.easy.to.build.crm.google.service.gmail.GoogleGmailApiService;
import site.easy.to.build.crm.repository.CustomerRepository;
import site.easy.to.build.crm.service.budget.BudgetService;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.expense.ExpenseService;
import site.easy.to.build.crm.service.settings.TicketEmailSettingsService;
import site.easy.to.build.crm.service.tauxalerte.TauxAlerteService;
import site.easy.to.build.crm.service.ticket.TicketService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/employee/ticket")
public class TicketController {
    @Autowired
    private TauxAlerteService tauxAlerteService;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ExpenseService expsenseService;
    @Autowired
    private BudgetService budgetService;
    private final TicketService ticketService;
    private final AuthenticationUtils authenticationUtils;
    private final UserService userService;
    private final CustomerService customerService;
    private final TicketEmailSettingsService ticketEmailSettingsService;
    private final GoogleGmailApiService googleGmailApiService;
    private final EntityManager entityManager;


    @Autowired
    public TicketController(TicketService ticketService, AuthenticationUtils authenticationUtils, UserService userService, CustomerService customerService,
                            TicketEmailSettingsService ticketEmailSettingsService, GoogleGmailApiService googleGmailApiService, EntityManager entityManager) {
        this.ticketService = ticketService;
        this.authenticationUtils = authenticationUtils;
        this.userService = userService;
        this.customerService = customerService;
        this.ticketEmailSettingsService = ticketEmailSettingsService;
        this.googleGmailApiService = googleGmailApiService;
        this.entityManager = entityManager;
    }

    @GetMapping("/show-ticket/{id}")
    public String showTicketDetails(@PathVariable("id") int id, Model model, Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(userId);
        if(loggedInUser.isInactiveUser()) {
            return "error/account-inactive";
        }

        Ticket ticket = ticketService.findByTicketId(id);
        if(ticket == null) {
            return "error/not-found";
        }
        User employee = ticket.getEmployee();
        if(!AuthorizationUtil.checkIfUserAuthorized(employee,loggedInUser) && !AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
            return "error/access-denied";
        }

        model.addAttribute("ticket",ticket);
        return "ticket/show-ticket";
    }

    @GetMapping("/manager/all-tickets")
    public String showAllTickets(Model model) {
        List<Ticket> tickets = ticketService.findAll();
        model.addAttribute("tickets",tickets);
        return "ticket/my-tickets";
    }

    @GetMapping("/created-tickets")
    public String showCreatedTicket(Model model, Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        List<Ticket> tickets = ticketService.findManagerTickets(userId);
        model.addAttribute("tickets",tickets);
        return "ticket/my-tickets";
    }

    @GetMapping("/assigned-tickets")
    public String showEmployeeTicket(Model model, Authentication authentication, 
                                    RedirectAttributes redirectAttributes) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        List<Ticket> tickets = ticketService.findEmployeeTickets(userId);
        model.addAttribute("tickets", tickets);
        
        // Récupérer le message d'alerte des attributs flash s'il existe
        if (model.asMap().containsKey("alertMessage")) {
            // Le message est déjà dans le modèle grâce aux attributs flash
            // Pas besoin de le réajouter
        }
        
        return "ticket/my-tickets";
    }

    @GetMapping("/create-ticket")
    public String showTicketCreationForm(Model model, Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User user = userService.findById(userId);
        if(user.isInactiveUser()) {
            return "error/account-inactive";
        }
        List<User> employees = new ArrayList<>();
        List<Customer> customers;

        if(AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
            employees = userService.findAll();
            customers = customerService.findAll();
        } else {
            employees.add(user);
            customers = customerService.findByUserId(user.getId());
        }

        model.addAttribute("employees",employees);
        model.addAttribute("customers",customers);
        model.addAttribute("ticket", new Ticket());
        return "ticket/create-ticket";
    }

    @PostMapping("/create-ticket")
    public String createTicket(@ModelAttribute("ticket") @Validated Ticket ticket,
                            BindingResult bindingResult,
                            @RequestParam("customerId") int customerId,
                            @RequestParam Map<String, String> formParams,
                            Model model,
                            @RequestParam("employeeId") int employeeId,
                            @RequestParam(value = "montant", required = false) BigDecimal montant,
                            @RequestParam(value = "confirmBudgetOverrun", required = false) Boolean confirmBudgetOverrun,
                            RedirectAttributes redirectAttributes,
                            Authentication authentication) {


        System.out.println("=== DÉBUT DE LA MÉTHODE createTicket ===");
        System.out.println("Ticket: " + ticket);
        System.out.println("customerId: " + customerId);
        System.out.println("employeeId: " + employeeId);
        System.out.println("montant: " + montant);
        System.out.println("confirmBudgetOverrun: " + confirmBudgetOverrun);
        
        // Vérification immédiate du dépassement de budget si un montant est spécifié
        // et que la confirmation n'a pas déjà été donnée
        if (montant != null && montant.compareTo(BigDecimal.ZERO) > 0 && confirmBudgetOverrun == null) {
            System.out.println("Vérification du budget pour le montant: " + montant);
            
            // Récupérer le budget total du client
            BigDecimal budgetTotal = budgetService.getTotalBudget(customerId);
            System.out.println("budgetTotal: " + budgetTotal);

            if (budgetTotal != null && budgetTotal.compareTo(BigDecimal.ZERO) > 0) {
                // Récupérer le total des dépenses actuelles
                BigDecimal totalDepenses = BigDecimal.ZERO;
                totalDepenses = expsenseService.getTotalDepensesByCustomerId(customerId);
                if (totalDepenses == null) {
                    totalDepenses = BigDecimal.ZERO;
                }
                System.out.println("totalDepenses: " + totalDepenses);

                // Ajouter le nouveau montant
                BigDecimal newTotalDepenses = totalDepenses.add(montant);
                System.out.println("newTotalDepenses: " + newTotalDepenses);

                // Vérifier si le budget est dépassé (comparaison directe)
                boolean budgetDepasse = newTotalDepenses.compareTo(budgetTotal) > 0;
                System.out.println("Le budget est-il dépassé? " + budgetDepasse);

                if (budgetDepasse) {
                    // Créer un message d'alerte simple
                    String alertMessage = "Attention : Le total des dépenses (" + newTotalDepenses +
                        " €) dépasse le budget total (" + budgetTotal + " €) pour ce client. Voulez-vous continuer malgré ce dépassement?";
                    
                    System.out.println("Message d'alerte: " + alertMessage);
                    
                    // Préparer les données pour la page de confirmation
                    model.addAttribute("alertMessage", alertMessage);
                    
                    // Préparer les données du formulaire pour la confirmation
                    Map<String, String> formDataMap = new HashMap<>(formParams);
                    formDataMap.put("customerId", String.valueOf(customerId));
                    formDataMap.put("employeeId", String.valueOf(employeeId));
                    if (montant != null) {
                        formDataMap.put("montant", montant.toString());
                    }
                    
                    model.addAttribute("formData", formDataMap);
                    model.addAttribute("confirmationUrl", "/employee/ticket/create-ticket");
                    model.addAttribute("cancelUrl", "/employee/ticket/create-ticket");
                    
                    // Définir le titre de la page
                    Customer customer = customerService.findByCustomerId(customerId);
                    model.addAttribute("title", "Confirmation de dépassement de budget");
                    model.addAttribute("subtitle", "Ticket pour " + customer.getName() + " " + customer.getName());
                    
                    System.out.println("Redirection vers la page de confirmation de budget");
                    redirectAttributes.addFlashAttribute("alertMessage", alertMessage);
                    return "budget/budget-confirmation";
                }
            }
        }        
        // Si nous arrivons ici, soit il n'y a pas de dépassement de budget,
        // soit l'utilisateur a confirmé le dépassement

        System.out.println("bindingResult.hasErrors(): " + bindingResult.hasErrors());
        if (bindingResult.hasErrors()) {
            System.out.println("Erreurs de validation: " + bindingResult.getAllErrors());
        }

        int userId = authenticationUtils.getLoggedInUserId(authentication);
        System.out.println("userId: " + userId);
        
        User manager = userService.findById(userId);
        System.out.println("manager: " + manager);

        if(manager == null) {
            System.out.println("Manager est null, redirection vers error/500");
            return "error/500";
        }

        if(manager.isInactiveUser()) {
            System.out.println("Manager est inactif, redirection vers error/account-inactive");
            return "error/account-inactive";
        }

        if(bindingResult.hasErrors()) {
            List<User> employees = new ArrayList<>();
            List<Customer> customers;

            if(AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
                employees = userService.findAll();
                customers = customerService.findAll();
            } else {
                employees.add(manager);
                customers = customerService.findByUserId(manager.getId());
            }

            model.addAttribute("employees",employees);
            model.addAttribute("customers",customers);
            return "ticket/create-ticket";
        }

        User employee = userService.findById(employeeId);
        System.out.println("employee: " + employee);
        
        Customer customer = customerService.findByCustomerId(customerId);
        System.out.println("customer: " + customer);

        if(employee == null || customer == null) {
            System.out.println("Employee ou customer est null, redirection vers error/500");
            return "error/500";
        }

        if(AuthorizationUtil.hasRole(authentication, "ROLE_EMPLOYEE")) {
            System.out.println("Utilisateur a le rôle EMPLOYEE");
            if(userId != employeeId || customer.getUser().getId() != userId) {
                System.out.println("Accès non autorisé, redirection vers error/500");
                return "error/500";
            }
        }

        // Vérification du taux d'alerte (même si le budget total n'est pas dépassé)
        if (montant != null && montant.compareTo(BigDecimal.ZERO) > 0) {
            System.out.println("Vérification du taux d'alerte pour le montant: " + montant);
            
            // Récupérer le budget total du client
            BigDecimal budgetTotal = budgetService.getTotalBudget(customerId);
            System.out.println("budgetTotal: " + budgetTotal);
    
            if (budgetTotal != null && budgetTotal.compareTo(BigDecimal.ZERO) > 0) {
                // Récupérer le total des dépenses actuelles
                BigDecimal totalDepenses = expsenseService.getTotalDepensesByCustomerId(customerId);
                if (totalDepenses == null) {
                    totalDepenses = BigDecimal.ZERO;
                }
                System.out.println("totalDepenses: " + totalDepenses);
        
                // Ajouter le nouveau montant
                BigDecimal newTotalDepenses = totalDepenses.add(montant);
                System.out.println("newTotalDepenses: " + newTotalDepenses);
        
                // Récupérer le taux d'alerte
                BigDecimal tauxAlerte = tauxAlerteService.getLatestTauxAlerte();
                System.out.println("tauxAlerte (original): " + tauxAlerte);
                
                // Convertir le taux de pourcentage à décimal si nécessaire
                if (tauxAlerte.compareTo(new BigDecimal("1")) > 0) {
                    tauxAlerte = tauxAlerte.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                }
                System.out.println("tauxAlerte (ajusté): " + tauxAlerte);
        
                // Calculer le seuil d'alerte
                BigDecimal seuilAlerte = budgetTotal.multiply(tauxAlerte);
                System.out.println("seuilAlerte: " + seuilAlerte);
        
                // Vérifier si le seuil est dépassé
                boolean seuilDepasse = newTotalDepenses.compareTo(seuilAlerte) >= 0;
                System.out.println("Le seuil est-il dépassé? " + seuilDepasse);
            
                if (seuilDepasse) {
                    // Pour l'affichage, utiliser le taux en pourcentage
                    BigDecimal tauxAlertePercent = tauxAlerte.multiply(new BigDecimal("100"));
                    
                    String alertMessage = "Attention : Le total des dépenses (" + newTotalDepenses +
                        " €) dépasse " + tauxAlertePercent +
                        "% du budget total (" + budgetTotal + " €) pour ce client.";
                    
                    System.out.println("Message d'alerte: " + alertMessage);
                    model.addAttribute("alertMessage", alertMessage);
                    
                    // Préparer les données du formulaire pour la réaffichage
                    List<User> employees = new ArrayList<>();
                    List<Customer> customers;

                    if(AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
                        employees = userService.findAll();
                        customers = customerService.findAll();
                    } else {
                        employees.add(manager);
                        customers = customerService.findByUserId(manager.getId());
                    }

                    model.addAttribute("alertMessage", alertMessage);
                    model.addAttribute("employees", employees);
                    model.addAttribute("customers", customers);
                    model.addAttribute("ticket", ticket);
                    
                    return "ticket/create-ticket";
                }
            }
        }

        ticket.setCustomer(customer);
        ticket.setManager(manager);
        ticket.setEmployee(employee);
        ticket.setCreatedAt(LocalDateTime.now());
        System.out.println("Ticket après configuration: " + ticket);

        // Sauvegarder le ticket
        Ticket savedTicket = ticketService.save(ticket);
        System.out.println("Ticket sauvegardé: " + savedTicket);

        // Créer une dépense si nécessaire
        if (montant != null && montant.compareTo(BigDecimal.ZERO) > 0) {
            Expense depense = expsenseService.createDepenseForTicket(savedTicket, montant);
            System.out.println("Dépense créée: " + depense);
        }

        System.out.println("Redirection vers: /employee/ticket/assigned-tickets");
        System.out.println("=== FIN DE LA MÉTHODE createTicket ===");

        return "redirect:/employee/ticket/assigned-tickets";
    }

    

    @GetMapping("/update-ticket/{id}")
    public String showTicketUpdatingForm(Model model, @PathVariable("id") int id, Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(userId);
        if(loggedInUser.isInactiveUser()) {
            return "error/account-inactive";
        }

        Ticket ticket = ticketService.findByTicketId(id);
        if(ticket == null) {
            return "error/not-found";
        }

        User employee = ticket.getEmployee();
        if(!AuthorizationUtil.checkIfUserAuthorized(employee,loggedInUser) && !AuthorizationUtil.hasRole(authentication,"ROLE_MANAGER")) {
            return "error/access-denied";
        }

        List<User> employees = new ArrayList<>();
        List<Customer> customers = new ArrayList<>();

        if(AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
            employees = userService.findAll();
            customers = customerService.findAll();
        } else {
            employees.add(loggedInUser);
            //In case Employee's manager assign lead for the employee with a customer that's not created by this employee
            //As a result of that the employee mustn't change the customer
            if(!Objects.equals(employee.getId(), ticket.getManager().getId())) {
                customers.add(ticket.getCustomer());
            } else {
                customers = customerService.findByUserId(loggedInUser.getId());
            }
        }

        model.addAttribute("employees",employees);
        model.addAttribute("customers",customers);
        model.addAttribute("ticket", ticket);
        return "ticket/update-ticket";
    }

    @PostMapping("/update-ticket")
    public String updateTicket(@ModelAttribute("ticket") @Validated Ticket ticket, BindingResult bindingResult,
                               @RequestParam("customerId") int customerId, @RequestParam("employeeId") int employeeId,
                               Authentication authentication, Model model) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(userId);
        if(loggedInUser.isInactiveUser()) {
            return "error/account-inactive";
        }

        Ticket previousTicket = ticketService.findByTicketId(ticket.getTicketId());
        if(previousTicket == null) {
            return "error/not-found";
        }
        Ticket originalTicket = new Ticket();
        BeanUtils.copyProperties(previousTicket, originalTicket);

        User manager = originalTicket.getManager();
        User employee = userService.findById(employeeId);
        Customer customer = customerService.findByCustomerId(customerId);

        if(manager == null || employee ==null || customer == null) {
            return "error/500";
        }

        if(bindingResult.hasErrors()) {
            ticket.setEmployee(employee);
            ticket.setManager(manager);
            ticket.setCustomer(customer);

            List<User> employees = new ArrayList<>();
            List<Customer> customers = new ArrayList<>();

            if(AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
                employees = userService.findAll();
                customers = customerService.findAll();
            } else {
                employees.add(loggedInUser);
                //In case Employee's manager assign lead for the employee with a customer that's not created by this employee
                //As a result of that the employee mustn't change the customer
                if(!Objects.equals(employee.getId(), ticket.getManager().getId())) {
                    customers.add(ticket.getCustomer());
                } else {
                    customers = customerService.findByUserId(loggedInUser.getId());
                }
            }

            model.addAttribute("employees",employees);
            model.addAttribute("customers",customers);
            return "ticket/update-ticket";
        }
        if(manager.getId() == employeeId) {
            if (!AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER") && customer.getUser().getId() != userId) {
                return "error/500";
            }
        } else {
            if(!AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER") && originalTicket.getCustomer().getCustomerId() != customerId) {
                return "error/500";
            }
        }

        if(AuthorizationUtil.hasRole(authentication, "ROLE_EMPLOYEE") && employee.getId() != userId) {
            return "error/500";
        }

        ticket.setCustomer(customer);
        ticket.setManager(manager);
        ticket.setEmployee(employee);
        Ticket currentTicket = ticketService.save(ticket);

        List<String> properties = DatabaseUtil.getColumnNames(entityManager, Ticket.class);
        Map<String, Pair<String,String>> changes = LogEntityChanges.trackChanges(originalTicket,currentTicket,properties);
        boolean isGoogleUser = !(authentication instanceof UsernamePasswordAuthenticationToken);

        if(isGoogleUser && googleGmailApiService != null) {
            OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
            if(oAuthUser.getGrantedScopes().contains(GoogleAccessService.SCOPE_GMAIL)) {
                processEmailSettingsChanges(changes, userId, oAuthUser, customer);
            }
        }

        return "redirect:/employee/ticket/assigned-tickets";
    }

    @PostMapping("/delete-ticket/{id}")
    public String deleteTicket(@PathVariable("id") int id, Authentication authentication){
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(userId);
        if(loggedInUser.isInactiveUser()) {
            return "error/account-inactive";
        }

        Ticket ticket = ticketService.findByTicketId(id);

        User employee = ticket.getEmployee();
        if(!AuthorizationUtil.checkIfUserAuthorized(employee,loggedInUser)) {
            return "error/access-denied";
        }

        ticketService.delete(ticket);
        return "redirect:/employee/ticket/assigned-tickets";
    }

    private void processEmailSettingsChanges(Map<String, Pair<String, String>> changes, int userId, OAuthUser oAuthUser,
                                             Customer customer) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (Map.Entry<String, Pair<String, String>> entry : changes.entrySet()) {
            String property = entry.getKey();
            String propertyName = StringUtils.replaceCharToCamelCase(property, '_');
            propertyName = StringUtils.replaceCharToCamelCase(propertyName, ' ');

            String prevState = entry.getValue().getFirst();
            String nextState = entry.getValue().getSecond();

            TicketEmailSettings ticketEmailSettings = ticketEmailSettingsService.findByUserId(userId);

            CustomerLoginInfo customerLoginInfo = customer.getCustomerLoginInfo();
            TicketEmailSettings customerTicketEmailSettings = ticketEmailSettingsService.findByCustomerId(customerLoginInfo.getId());

            if (ticketEmailSettings != null) {
                String getterMethodName = "get" + StringUtils.capitalizeFirstLetter(propertyName);
                Method getterMethod = TicketEmailSettings.class.getMethod(getterMethodName);
                Boolean propertyValue = (Boolean) getterMethod.invoke(ticketEmailSettings);

                Boolean isCustomerLikeToGetNotified = true;
                if(customerTicketEmailSettings != null) {
                    isCustomerLikeToGetNotified = (Boolean) getterMethod.invoke(customerTicketEmailSettings);
                }

                if (isCustomerLikeToGetNotified != null && propertyValue != null && propertyValue && isCustomerLikeToGetNotified) {
                    String emailTemplateGetterMethodName = "get" + StringUtils.capitalizeFirstLetter(propertyName) + "EmailTemplate";
                    Method emailTemplateGetterMethod = TicketEmailSettings.class.getMethod(emailTemplateGetterMethodName);
                    EmailTemplate emailTemplate = (EmailTemplate) emailTemplateGetterMethod.invoke(ticketEmailSettings);
                    String body = emailTemplate.getContent();

                    property = property.replace(' ', '_');
                    String regex = "\\{\\{(.*?)\\}\\}";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(body);

                    while (matcher.find()) {
                        String placeholder = matcher.group(1);
                        if (placeholder.contains("previous") && placeholder.contains(property)) {
                            body = body.replace("{{" + placeholder + "}}", prevState);
                        } else if (placeholder.contains("next") && placeholder.contains(property)) {
                            body = body.replace("{{" + placeholder + "}}", nextState);
                        }
                    }

                    try {
                        googleGmailApiService.sendEmail(oAuthUser, customer.getEmail(), emailTemplate.getName(), body);
                    } catch (IOException | GeneralSecurityException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
