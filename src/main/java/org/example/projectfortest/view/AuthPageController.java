package org.example.projectfortest.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthPageController {

    @GetMapping("/auth")
    public String getPage() {
        return "redirect:/auth.html";
    }

    @GetMapping("/tasks")
    public String getTasksPage() {
        return "tasks.html";
    }
}
