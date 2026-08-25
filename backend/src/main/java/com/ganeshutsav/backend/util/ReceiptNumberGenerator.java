package com.ganeshutsav.backend.util;

import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ReceiptNumberGenerator {

    // Simple in-memory counter seeded per JVM start; for stronger guarantees
    // rely on the unique DB constraint + retry, or a DB sequence table.
    private final AtomicLong counter = new AtomicLong(System.currentTimeMillis() % 100000);

    public String next() {
        int year = Year.now().getValue();
        long seq = counter.incrementAndGet();
        return String.format("GU-%d-%05d", year, seq);
    }
}
