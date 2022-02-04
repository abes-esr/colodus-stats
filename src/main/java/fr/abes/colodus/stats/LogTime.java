package fr.abes.colodus.stats;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;

@Slf4j
public class LogTime {
    public static void logDebutTraitement(StepExecution stepExecution) {
        stepExecution.getJobExecution().getExecutionContext().put("debut", System.currentTimeMillis());
    }

    public static void logFinTraitement(StepExecution stepExecution) {
        long dureeMs = System.currentTimeMillis() - (long) stepExecution.getJobExecution().getExecutionContext().get("debut");
        log.info("temps total execution (ms) = " + dureeMs);
        int dureeMinutes = (int) ((dureeMs / (1000 * 60)) % 60);
        log.info("temps total execution (minutes) = " + dureeMinutes);
    }

    private LogTime(){}
}
