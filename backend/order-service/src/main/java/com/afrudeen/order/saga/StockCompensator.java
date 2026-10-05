package com.afrudeen.order.saga;

import com.afrudeen.order.client.ProductGateway;
import com.afrudeen.order.entity.PendingStockRestore;
import com.afrudeen.order.repository.PendingStockRestoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class StockCompensator {

    private static final Logger log = LoggerFactory.getLogger(StockCompensator.class);

    private final ProductGateway products;
    private final PendingStockRestoreRepository pending;
    private final TransactionTemplate newTx;

    public StockCompensator(ProductGateway products,
                            PendingStockRestoreRepository pending,
                            PlatformTransactionManager txManager) {
        this.products = products;
        this.pending = pending;
        // The retry record must survive even when the caller's transaction rolls back
        this.newTx = new TransactionTemplate(txManager);
        this.newTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /** Gives stock back. Tries now; if Product Service is unavailable, queues a retry. */
    public void restore(Long productId, int quantity, String reason) {
        try {
            products.increaseStock(productId, quantity);
            log.info("Stock restored: product {} +{} ({})", productId, quantity, reason);
        } catch (Exception e) {
            log.warn("Could not restore product {} (+{}), queued for retry: {}",
                    productId, quantity, e.getMessage());
            newTx.executeWithoutResult(status ->
                    pending.save(new PendingStockRestore(productId, quantity, reason)));
        }
    }
}