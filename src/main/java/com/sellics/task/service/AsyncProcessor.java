package com.sellics.task.service;

import com.sellics.task.excall.AutoCompleteApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncProcessor {


    @Value( "${horizontalCoefficient}" )
    private float horizontalCoefficient;


    @Value( "${verticalCoefficient}" )
    private float verticalCoefficient;

    final
    AutoCompleteApi keywordService;
    private static final Logger LOGGER = LoggerFactory.getLogger(EstimationService.class);

    public AsyncProcessor(AutoCompleteApi keywordService) {
        this.keywordService = keywordService;
    }


    @Async
    public CompletableFuture<BigDecimal> getScore(String partOfKeyword, String keyword) {
        BigDecimal score = BigDecimal.ZERO;
        List<String> successions = null;
        try {
            /* getting all suggestions by prefix*/
            successions = keywordService.getSuccessions(partOfKeyword);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        score = calculateRate(partOfKeyword, keyword, successions);

        return CompletableFuture.completedFuture(score);

    }

    private BigDecimal calculateRate(String partOfKeyword, String keyword, List<String> successions) {

        float perWordPercentage = (100f / (float) keyword.length());

        float horizontalEffect;
        float verticalEffect;
        float horizontalScore;
        float verticalScore;


        int index = successions.indexOf(keyword);
        if (index == -1) {
            return BigDecimal.ZERO;
        } else {

            /* applying the formula mentioned in read.me  */

            horizontalEffect = (10 + horizontalCoefficient) / 10f;
            horizontalEffect = horizontalEffect - (horizontalCoefficient * (partOfKeyword.length() - 1) / ((5 * keyword.length()) - 5));
            horizontalEffect = perWordPercentage * horizontalEffect;
            horizontalScore = horizontalEffect * (100 - verticalCoefficient) / 100f;

            verticalEffect = (100 - (index * 10)) / 100f;
            verticalScore = (horizontalEffect - horizontalScore) *   (verticalCoefficient / 100f);
            verticalScore = verticalScore * verticalEffect;

            BigDecimal total = BigDecimal.valueOf(verticalScore + horizontalScore)
                    .setScale(2, BigDecimal.ROUND_HALF_EVEN);
            LOGGER.info(" Prefix:" + partOfKeyword  + ", horizontalScore: " + horizontalScore + " verticalScore: "+verticalScore+  "total:" + total  );

            return total;
        }

    }

}
