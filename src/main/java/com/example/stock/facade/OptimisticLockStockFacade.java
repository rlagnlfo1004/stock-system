package com.example.stock.facade;

import com.example.stock.service.OptimisticLockStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Optimistic Lock 에서의 업데이트 실패시 재시도를 위한 Facade
 */
@Component
@RequiredArgsConstructor
public class OptimisticLockStockFacade {

    private final OptimisticLockStockService stockService;

    public void decrease(Long id, Long quantity) throws InterruptedException {
        while (true) {
            try {
                stockService.decrease(id, quantity);

                break; // 정상 업데이트 시
            } catch (Exception e) {
                Thread.sleep(50);
            }
        }

    }
}
