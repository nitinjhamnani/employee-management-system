package com.app.config;

import com.app.service.SalesTargetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SalaryCalculationScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(SalaryCalculationScheduler.class);
    
    @Autowired
    private SalesTargetService salesTargetService;
    
    /**
     * Runs daily at 2 AM to calculate salaries for completed monthly targets
     * This ensures that salaries are automatically calculated when targets are achieved
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void calculateSalariesForCompletedMonths() {
        logger.info("Starting automatic salary calculation for completed monthly targets...");
        try {
            salesTargetService.calculateSalariesForCompletedMonths();
            logger.info("Automatic salary calculation completed successfully.");
        } catch (Exception e) {
            logger.error("Error during automatic salary calculation: ", e);
        }
    }
    
    /**
     * Runs daily at 3 AM to update achieved amounts for all active targets
     * This ensures that sales data is always up-to-date
     */
    @Scheduled(cron = "0 0 3 * * ?") // Daily at 3 AM
    public void updateAchievedAmounts() {
        logger.info("Updating achieved amounts for all sales targets...");
        try {
            salesTargetService.updateAllAchievedAmounts();
            logger.info("Achieved amounts updated successfully.");
        } catch (Exception e) {
            logger.error("Error updating achieved amounts: ", e);
        }
    }
}

