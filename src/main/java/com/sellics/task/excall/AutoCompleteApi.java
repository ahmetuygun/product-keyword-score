package com.sellics.task.excall;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class AutoCompleteApi {


    @Value( "${amazon.url}" )
    private String URL ;


    public static final String SEARCH_ALIAS_PARAM = "search-alias";
    public static final String SEARCH_ALIAS_VALUE = "aps";
    public static final String CLIENT_PARAM = "client";
    public static final String CLIENT_VALUE = "amazon-search-ui";
    public static final String MKT_PARAM = "mkt";
    public static final String MKT_VALUE = "1";
    public static final String KEYWORD_PARAM = "q";
    public static final String AMAZON_AUTOCOMPLETE_URL = "https://completion.amazon.com/search/complete";
    public static final String ACCEPT_HEADER = "accept";
    public static final String ACCEPT_JSON = "application.propertiesion/json";

    public List<String> getSuccessions(String keyword) {
        HttpResponse<JsonNode> obj = Unirest.post(AMAZON_AUTOCOMPLETE_URL)
                .header(ACCEPT_HEADER, ACCEPT_JSON)
                .queryString(SEARCH_ALIAS_PARAM, SEARCH_ALIAS_VALUE)
                .queryString(CLIENT_PARAM, CLIENT_VALUE)
                .queryString(MKT_PARAM, MKT_VALUE)
                .queryString(KEYWORD_PARAM, keyword)
                .asJson();

        List<String> suggestions = obj.getBody().getArray().getJSONArray(1).toList();

        return suggestions;
    }

}
