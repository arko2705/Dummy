package com.Dummy.demo.service.internalSimulation;

import java.util.Map;

public class Context {
    public String service; // cart, product, order, payment
    public String operation; // add, update, fetch, delete
    public int load; // simulated system load
    public long latency; // optional future use
    public Map<String, Object> data;
}