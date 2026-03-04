package com.example.global.security.blacklist.support;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class BlacklistedTokenCleanupLogWriter {

    public void logCleanup(final LocalDateTime cutoffAt, final long deletedCount, final long elapsedMs) {
        log.info(
                """
                        [BLACKLIST_CLEANUP]
                        target={}
                        cutoffAt={}
                        deletedCount={}
                        elapsedMs={}
                        result={}
                        """.stripTrailing(),
                "BLACKLISTED_TOKEN",
                cutoffAt,
                deletedCount,
                elapsedMs,
                "SUCCESS"
        );
    }
}
