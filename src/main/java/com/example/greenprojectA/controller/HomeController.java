package com.example.greenprojectA.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class HomeController {

  @GetMapping({"/", "/index", "/h", "/home"})
  public String homeGet(@ModelAttribute("message") String message) {
    return "home";
  }

  @GetMapping("/admin/adminMenu")
  public String adminMenuGet() {
    return "admin/adminMenu";
  }

  @GetMapping("/error/accessDenied")
  public String accessDeniedGet() {
    return "error/accessDenied";
  }

}
