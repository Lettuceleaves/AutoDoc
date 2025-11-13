package com.letuc.tester.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

@Data
@AllArgsConstructor
public class SimpleTask {
    private final String taskName;
    private final String url;
    private final HttpMethod method;
    private final HttpHeaders headers;
    private final String requestBodyTemplate;
    private final String expectedResponseTemplate;
}