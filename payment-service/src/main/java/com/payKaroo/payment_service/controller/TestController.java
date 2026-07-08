package com.payKaroo.payment_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/payments/test")
    public String test() {
        return "Payment Service is working!";
    }

}
