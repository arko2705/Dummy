package com.Dummy.demo.monitoring.error.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.Dummy.demo.monitoring.error.model.errorLog;

@Service
public class errorLogService {

    private List<errorLog> logs = new ArrayList<>();

    public void log(errorLog log) {
        logs.add(log);
    }

    public List<errorLog> getLogs() {
        return logs;
    }

    public void clearLogs() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clearLogs'");
    }
}