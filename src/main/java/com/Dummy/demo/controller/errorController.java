package com.Dummy.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.Dummy.demo.monitoring.error.model.errorLog;
import com.Dummy.demo.monitoring.error.service.errorLogService;

@RestController
@RequestMapping("/api/errors")
public class errorController {

    @Autowired
    private errorLogService errorService;

    @GetMapping
    public List<errorLog> getErrors() {
        return errorService.getLogs();
    }

    @DeleteMapping
    public String clearErrors() {
        errorService.clearLogs();
        return "Logs cleared";
    }
}