package com.lpjpro.schedule;

import com.lpjpro.service.FeatureService;
import com.lpjpro.service.ProcessDataService;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataProcess {
    @Resource
    private ProcessDataService processDataService;


    @Scheduled(cron = "0 0/1 * * * ?")
    public void shortData() throws Exception {
        processDataService.userShortTermData();
    }

    @Scheduled(cron = "0 0 0 */4 * *", zone = "GMT+8")
    public void longData() {
        processDataService.userLongTermData();
    }

}
