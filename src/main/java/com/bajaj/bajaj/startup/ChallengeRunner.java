package com.bajaj.bajaj.startup;

import com.bajaj.bajaj.service.ChallengeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ChallengeRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ChallengeRunner.class);

    private final ChallengeService challengeService;

    public ChallengeRunner(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @Override
    public void run(String... args) {
        log.info("Application started, triggering challenge flow...");
        challengeService.runChallenge();
    }
}

