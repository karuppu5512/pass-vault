package com.passvault.scheduler;

import com.passvault.service.RotationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordRotationJob {

    private final RotationService rotationService;

    @Scheduled(fixedDelayString = "${app.jobs.rotation-ms:60000}", initialDelay = 15000)
    public void rotateDuePasswords() {
        List<Long> due = rotationService.findDuePolicyIds();
        if (due.isEmpty()) {
            return;
        }
        log.info("Rotating passwords for {} account(s)", due.size());
        for (Long policyId : due) {
            try {
                rotationService.runScheduledRotation(policyId);
            } catch (Exception e) {
                log.error("Rotation failed for policy {}", policyId, e);
            }
        }
    }
}
