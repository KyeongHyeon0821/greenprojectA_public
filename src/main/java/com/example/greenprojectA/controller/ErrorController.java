package com.example.greenprojectA.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorController {

    @RequestMapping("/accessDenied")
    public String accessDenied() {
        return "error/accessDenied";
    }


}
