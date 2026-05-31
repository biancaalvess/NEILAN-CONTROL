package com.neilan.control.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping("/")
    public String root() {
        return "redirect:/index.html";
    }
}
