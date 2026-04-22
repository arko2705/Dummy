package com.Dummy.demo.service;

import org.springframework.stereotype.Service;
import com.Dummy.demo.model.hostMetricModel;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import com.sun.management.OperatingSystemMXBean;

@Service // a bean is created on startup. This is all the project info withOUT visiting
         // any endpoint,not @Service specifically but @Service and @RestController
public class HostMetricsService {
    public double gb = 1073741824;

    public hostMetricModel gethostMetrics() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        OperatingSystemMXBean osInfoBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        double initialMem = memoryMXBean.getHeapMemoryUsage().getInit() / gb;
        double currentMem = memoryMXBean.getHeapMemoryUsage().getUsed() / gb;
        double maxMem = memoryMXBean.getHeapMemoryUsage().getMax() / gb;

        double CPULoad = osInfoBean.getCpuLoad() * 100;
        double totalMem = osInfoBean.getTotalMemorySize() / gb;
        double freeMem = osInfoBean.getFreeMemorySize() / gb;
        return new hostMetricModel(initialMem, currentMem, maxMem, CPULoad, totalMem, freeMem);
    }
}
