package com.sellics.task.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class EstimationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EstimationService.class);

    final
    AsyncProcessor asyncProcessor;
    private List<String> prefixes;

    public EstimationService(AsyncProcessor asyncProcessor) {
        this.asyncProcessor = asyncProcessor;
    }

    public BigDecimal estimate(final String keyword) throws Exception {
        final long start = System.currentTimeMillis();

        /* getting all possible prefix,  like i, ip, ipa, ipad*/
        List<String> prefixes = getAllPrefix(keyword);

        /* create async task for each prefix*/
        List<CompletableFuture<BigDecimal>> completableFutures = prefixes
                .stream()
                .map(part -> asyncProcessor.getScore(part, keyword))
                .collect(Collectors.toList());

        /* wait until all them finish their job*/
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).join();

        BigDecimal total = BigDecimal.ZERO;
        for (CompletableFuture feature : completableFutures) {
            total = total.add((BigDecimal) feature.get());
        }

        LOGGER.info("Overall score for " + keyword + " : " +total);

        LOGGER.info("Elapsed time: {}", (System.currentTimeMillis() - start));
        return total.setScale(2, BigDecimal.ROUND_HALF_EVEN);
    }

    private List<String> getAllPrefix(String keyword) {

        List<String> prefixList = new ArrayList<String>();
        for (int i = 0; i < keyword.length(); i++) {
            String prefix = keyword.substring(0, i + 1);
            prefixList.add(prefix);
        }
        return prefixList;
    }

}
