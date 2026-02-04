package com.example.stock.transaction;

import com.example.stock.service.StockService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionStockService {

    private final StockService stockService;

    public TransactionStockService(StockService stockService) {
        this.stockService = stockService;
    }

    public void decrease(Long id, Long quantity) {
        startTransaction();

        stockService.decrease(id, quantity);

        endTransaction();
    }

    private void startTransaction() {
        log.info("Transaction Start");
    }

    private void endTransaction() {
        log.info("Commit");
    }
}
