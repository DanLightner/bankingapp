package org.sfu.p2startercode.controller;

import org.sfu.p2startercode.model.User;
import org.sfu.p2startercode.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.sfu.p2startercode.model.UserAccount;
import org.springframework.web.bind.annotation.RequestParam;


import jakarta.servlet.http.HttpSession; // Added for session handling
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserController {
    @Autowired
    private UserRepository userRepository;
    //ADDED USERREPOSITORY

    @GetMapping("/loginBank")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

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

        // Set logged-in user in the session
        session.setAttribute("loggedInUser", dbUser);

        return "redirect:/account";
    }


    @GetMapping("/account")
    public String showAccountPage(HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            model.addAttribute("error", "You must log in to view your account.");
            return "redirect:/loginBank";
        }

        if (loggedInUser.getUserAccount() == null) {
            model.addAttribute("error", "No account found for the user.");
            return "error"; // Return an error page or appropriate view
        }



        // Add logged-in user and user account to the model
        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("userAccount", loggedInUser.getUserAccount());

        return "account"; // Return the Thymeleaf template name
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Invalidate the session to log the user out
        return "redirect:/loginBank"; // Redirect to the login page
    }

    @PostMapping("/account/transaction")
    public String handleTransaction(@RequestParam("transactionType") String transactionType,
                                    @RequestParam("amount") Float amount,
                                    HttpSession session,
                                    Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            model.addAttribute("error", "You must log in to perform transactions.");
            return "redirect:/loginBank";
        }

        UserAccount userAccount = loggedInUser.getUserAccount();
        if (userAccount == null) {
            model.addAttribute("error", "No account found for the user.");
            return "error";
        }

        // Update balance based on transaction type
        if (transactionType.equals("deposit")) {
            userAccount.setBalance(userAccount.getBalance() + amount);
        } else if (transactionType.equals("withdraw")) {
            if (userAccount.getBalance() < amount) {
                model.addAttribute("error", "Insufficient balance for the withdrawal.");
                return "account";
            }
            userAccount.setBalance(userAccount.getBalance() - amount);
        } else {
            model.addAttribute("error", "Invalid transaction type.");
            return "account";
        }

        // Save the updated account
        userRepository.save(loggedInUser);

        // Update the model for the account view
        model.addAttribute("loggedInUser", loggedInUser);
        model.addAttribute("userAccount", userAccount);
        return "redirect:/account"; // Redirect to reload the account page
    }


    @PostMapping("/account/update")
    public String updateUserDetails(@RequestParam("username") String newUsername, @RequestParam("password") String newPassword, HttpSession session, Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            model.addAttribute("error", "You must log in to update details.");
            return "redirect:/loginBank";
        }

        loggedInUser.setUsername(newUsername);
        loggedInUser.setPassword(newPassword);

        // Save the updated user
        userRepository.save(loggedInUser);

        session.setAttribute("loggedInUser", loggedInUser);
        model.addAttribute("loggedInUser", loggedInUser);

        return "redirect:/account";
    }


}