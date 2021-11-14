package com.sellics.task.controller;


import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.sellics.task.dto.EstimationResponse;
import com.sellics.task.dto.Result;
import com.sellics.task.service.EstimationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/estimate")
public class KeywordScoreController {

    final
    EstimationService estimationService;

    public KeywordScoreController(EstimationService estimationService) {
        this.estimationService = estimationService;
    }

    @HystrixCommand(fallbackMethod = "fallback", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "10000")
    })
    @GetMapping
    public ResponseEntity<EstimationResponse> estimate(@RequestParam String keyword) {


        if(keyword == null || keyword.isEmpty()) {
            return new ResponseEntity<>(buildFailResponse("Keyword cant be empty!"), HttpStatus.BAD_REQUEST);
        }else{
            keyword = keyword.trim();
            keyword = keyword.toLowerCase();
        }

        BigDecimal estimation = null;
        try {
            estimation = estimationService.estimate(keyword);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(buildFailResponse("An Error occurred while calculating!"), HttpStatus.BAD_REQUEST);
        }

        EstimationResponse response = EstimationResponse
                .builder()
                .score(estimation)
                .keyword(keyword)
                .result(Result.builder().success(true).message("Success!").build())
                .build();


        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private EstimationResponse buildFailResponse(String s) {
        EstimationResponse response = EstimationResponse
                .builder()
                .result(Result.builder().success(false).message(s).build())
                .build();
        return response;
    }

    public ResponseEntity<EstimationResponse> fallback(String keyWord) {
        return new ResponseEntity<>(buildFailResponse("Timeout!"), HttpStatus.REQUEST_TIMEOUT);
    }
}
