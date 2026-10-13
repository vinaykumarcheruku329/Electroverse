package com.electroverse.scheduler;

import com.electroverse.entity.PriceAlert;
import com.electroverse.repository.PriceAlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PriceAlertScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceAlertScheduler.class);

    private final PriceAlertRepository priceAlertRepository;

    public PriceAlertScheduler(PriceAlertRepository priceAlertRepository) {
        this.priceAlertRepository = priceAlertRepository;
    }

    @Scheduled(fixedDelayString = "${app.alerts.check-interval-ms:3600000}")
    @Transactional
    public void evaluateAlerts() {
        List<PriceAlert> active = priceAlertRepository.findByActiveTrue();
        int triggered = 0;
        for (PriceAlert alert : active) {
            if (alert.isTriggered() || alert.getProduct() == null) {
                continue;
            }
            var currentPrice = alert.getProduct().getPrice();
            alert.setLastObservedPrice(currentPrice);
            if (currentPrice != null && currentPrice.compareTo(alert.getTargetPrice()) <= 0) {
                alert.setTriggered(true);
                alert.setTriggeredAt(LocalDateTime.now());
                triggered++;
            }
            priceAlertRepository.save(alert);
        }
        if (triggered > 0) {
            log.info("Price alert evaluation triggered {} alert(s)", triggered);
        }
    }
}
