package com.passvault.scheduler;

import com.passvault.service.AccessRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessExpiryJob {

    private final AccessRequestService accessRequestService;

    @Scheduled(fixedDelayString = "${app.jobs.access-expiry-ms:60000}", initialDelay = 10000)
    public void expireFinishedAccess() {
        run(accessRequestService.findFinishedGrantIds(), "finished grants");
    }

    @Scheduled(cron = "${app.jobs.pending-cleanup-cron:0 0 * * * *}")
    public void expireStaleRequests() {
        run(accessRequestService.findStalePendingIds(), "stale pending requests");
    }

    private void run(List<Long> ids, String label) {
        if (ids.isEmpty()) {
            return;
        }
        log.info("Expiring {} {}", ids.size(), label);
        for (Long id : ids) {
            try {
                accessRequestService.expire(id);
            } catch (Exception e) {
                log.error("Could not expire access request {}", id, e);
            }
        }
    }
}
