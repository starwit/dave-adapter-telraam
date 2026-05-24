package de.starwit.telraam.scheduler;

import de.starwit.telraam.service.TransferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Triggers the Telraam → DAVe data transfer every 15 minutes.
 *
 * <p>The cron expression {@code 0 0/15 * * * *} fires at :00, :15, :30
 * and :45 of every hour, every day. Adjust via the
 * {@code telraam.scheduler.cron} property if needed.</p>
 */
@Component
public class TransferScheduler {

    private static final Logger log = LoggerFactory.getLogger(TransferScheduler.class);

    private final TransferService transferService;

    public TransferScheduler(TransferService transferService) {
        this.transferService = transferService;
    }

    /**
     * Fires every 15 minutes (at :00, :15, :30, :45).
     * Spring cron format: {@code second minute hour day month weekday}
     */
    @Scheduled(cron = "${telraam.scheduler.cron:0 0/15 * * * *}")
    public void runTransfer() {
        log.info("Scheduled transfer triggered");
        try {
            transferService.transferLatest();
        } catch (Exception ex) {
            // Catch-all so the scheduler stays alive even on unexpected errors
            log.error("Unexpected error during scheduled transfer: {}", ex.getMessage(), ex);
        }
    }
}
