package com.Dummy.demo.model;

//these variable names are displayed on the site
public class hostMetricModel {
    // related to jvm
    public double initialMemoryJVM;
    public double currentUsageJVM;
    public double maxMemAvailableJVM;
    public double CPULoadSYS;
    public double totalMemSYS;
    public double freeMemSYS;

    public hostMetricModel(double initialMemory, double currentUsage, double maxMemAvailable, double CPULoad,
            double totalMem, double freeMem) {
        this.initialMemoryJVM = initialMemory;
        this.currentUsageJVM = currentUsage;
        this.maxMemAvailableJVM = maxMemAvailable;
        this.CPULoadSYS = CPULoad;
        this.totalMemSYS = totalMem;
        this.freeMemSYS = freeMem;
    }
}
