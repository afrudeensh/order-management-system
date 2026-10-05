package com.afrudeen.order.saga;

import com.afrudeen.order.client.ProductGateway;
import com.afrudeen.order.entity.PendingStockRestore;
import com.afrudeen.order.repository.PendingStockRestoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StockRestoreRetryJob {

    private static final Logger log = LoggerFactory.getLogger(StockRestoreRetryJob.class);
    private static final int MAX_ATTEMPTS = 10;

    private final PendingStockRestoreRepository pending;
    private final ProductGateway products;

    public StockRestoreRetryJob(PendingStockRestoreRepository pending, ProductGateway products) {
        this.pending = pending;
        this.products = products;
    }

    @Scheduled(fixedDelay = 30_000, initialDelay = 30_000)
    public void run() {
        for (PendingStockRestore p :
                pending.findTop50ByDoneFalseAndAttemptsLessThanOrderByIdAsc(MAX_ATTEMPTS)) {
            try {
                products.increaseStock(p.getProductId(), p.getQuantity());
                p.markDone();
                log.info("Retry succeeded: product {} +{} ({})",
                        p.getProductId(), p.getQuantity(), p.getReason());
            } catch (Exception e) {
                p.recordFailure();
                log.warn("Retry {} failed for product {}: {}",
                        p.getAttempts(), p.getProductId(), e.getMessage());
            }
            pending.save(p);
        }
    }
}