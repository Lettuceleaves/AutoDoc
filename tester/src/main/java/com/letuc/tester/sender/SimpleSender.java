package com.letuc.tester.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class SimpleSender {

    private static final Logger log = LoggerFactory.getLogger(SimpleSender.class);

    private final RestTemplate restTemplate;

    @Autowired
    public SimpleSender(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> sendGenericRequest(String url, HttpMethod method, HttpHeaders headers, Object body) {
        try {
            HttpEntity<Object> requestEntity;
            if (method == HttpMethod.GET || method == HttpMethod.DELETE) {
                requestEntity = new HttpEntity<>(headers);
            } else {
                requestEntity = new HttpEntity<>(body, headers);
            }

            ResponseEntity<String> response = restTemplate.exchange(url, method, requestEntity, String.class);

            log.info("{} request to {} successful, status code: {}", method, url, response.getStatusCode());
            return response;
        } catch (RestClientException e) {
            log.error("Error during {} request to {}: {}", method, url, e.getMessage());
            return null;
        }
    }

//    @Override
//    public void run(String... args) throws Exception {
//        String url = "http://localhost:8000/test/hello";
//        HttpMethod method = HttpMethod.GET;
//        HttpHeaders headers = new HttpHeaders();
//        Object body = new Object();
//        while (true) {
//            ResponseEntity<String> response = sendGenericRequest(url, method, headers, body);
//            if (response == null) {
//                sleep(3000);
//                continue;
//            }
//            log.info("{} request to {} successful, status code: {}", method, url, response.getStatusCode());
//            System.out.println(response.getBody());
//            sleep(3000);
//        }
//    }
}