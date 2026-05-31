package com.Dummy.demo.monitoring.reliability.model;

public class ReliabilityMetrics {
    private double failureRate;
    private double mtbf;
    private double mttr;
    private double availability;

    public ReliabilityMetrics() {
    }

    public ReliabilityMetrics(double failureRate, double mtbf, double mttr, double availability) {
        this.failureRate = failureRate;
        this.mtbf = mtbf;
        this.mttr = mttr;
        this.availability = availability;
    }

    public double getFailureRate() {
        return failureRate;
    }

    public void setFailureRate(double failureRate) {
        this.failureRate = failureRate;
    }

    public double getMtbf() {
        return mtbf;
    }

    public void setMtbf(double mtbf) {
        this.mtbf = mtbf;
    }

    public double getMttr() {
        return mttr;
    }

    public void setMttr(double mttr) {
        this.mttr = mttr;
    }

    public double getAvailability() {
        return availability;
    }

    public void setAvailability(double availability) {
        this.availability = availability;
    }
}
