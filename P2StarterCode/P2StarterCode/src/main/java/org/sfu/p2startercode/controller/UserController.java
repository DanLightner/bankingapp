package org.sfu.p2startercode.controller;

import org.sfu.p2startercode.model.User;
import org.sfu.p2startercode.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// import for the UserAccount model class
import org.sfu.p2startercode.model.UserAccount;

// used for extracting request parameters from HTTP requests
import org.springframework.web.bind.annotation.RequestParam;


import jakarta.servlet.http.HttpSession; // manages user sessions in the application
import jakarta.validation.Valid; // validates model objects according to annotations

@Controller
public class UserController {
    // NATE DUMM and I worked on this together - him and I basically started it from scratch
    //I did give sebastian kline some pointers on how to do the logout pointers
    //also too, I uploaded the project to github so I could work on it on other devices

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/loginBank")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new User()); // adds an empty user object to the model for form binding
        return "login";
    }


    // processes login form submission
    @PostMapping("/loginBank")
    public String loginUser(@Valid @ModelAttribute("user") User user, BindingResult result, Model model, HttpSession session) {
        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> System.out.println(error.getDefaultMessage()));
            return "login";
        }

        User dbUser = userRepository.findByUsername(user.getUsername());
        if (dbUser == null || !dbUser.getPassword().equals(user.getPassword())) {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }


        session.setAttribute("loggedInUser", dbUser); // stores the logged-in user in the session
        return "redirect:/account"; // redirects to the account page
    }


    // displays the account page for the logged-in user
    @GetMapping("/account")
    public String showAccountPage(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/loginBank"; // if you try to start in account without session token, redirect back to login
        }
        //every user must have user account
        model.addAttribute("loggedInUser", loggedInUser); // passes the logged-in user's details to the view
        model.addAttribute("userAccount", loggedInUser.getUserAccount()); // passes the user's account details to the view

        return "account";
    }


    // redirects /login to /loginBank
    @GetMapping("/login")
    public String redirectToLoginBank() {
        return "redirect:/loginBank";  // Redirect /login to /loginBank
    }

    // logs out the user and invalidates the session
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();  // Invalidate the session
        return "redirect:/loginBank";  // Redirect to the login page
    }


    // handles deposit or withdrawal transactions
    @PostMapping("/account/transaction")
    public String handleTransaction(@RequestParam("transactionType") String transactionType,
                                    @RequestParam("amount") Float amount,
                                    HttpSession session,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            model.addAttribute("error", "You must log in to perform transactions.");
            return "redirect:/loginBank";
        }

        UserAccount userAccount = loggedInUser.getUserAccount();

        if (transactionType.equals("deposit")) { // processes deposit
            userAccount.setBalance(userAccount.getBalance() + amount);
        } else if (transactionType.equals("withdraw")) {
            if (userAccount.getBalance() < amount) { // checks if balance is sufficient
                redirectAttributes.addFlashAttribute("error", "Insufficient balance for the withdrawal.");
                return "redirect:/account";
            }
            userAccount.setBalance(userAccount.getBalance() - amount);
        } else { // handles invalid transaction type - incase something would go horribly wrong somehow
            redirectAttributes.addFlashAttribute("error", "Invalid transaction type.");
            return "redirect:/account";
        }

        userRepository.save(loggedInUser);
        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("userAccount", userAccount);
        return "redirect:/account";
    }

    // updates user details like username and password
    @PostMapping("/account/update")
    public String updateUserDetails(@RequestParam("username") String newUsername,
                                    @RequestParam("password") String newPassword,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            redirectAttributes.addFlashAttribute("error", "You must log in to update details.");
            return "redirect:/loginBank";
        }

        loggedInUser.setUsername(newUsername); // updates the username
        loggedInUser.setPassword(newPassword); // updates the password
        userRepository.save(loggedInUser); // saves the changes to the database

        session.setAttribute("loggedInUser", loggedInUser);
        redirectAttributes.addFlashAttribute("success", "Your account details have been updated successfully.");

        return "redirect:/account";  //reloads the account page
    }



}