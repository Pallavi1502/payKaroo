package com.payKaroo.notification_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/notifications/test")
    public String test() {
        return "Notification Service is working!";
    }

}
