package com.klkt.supervision.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Mono;

@Controller
public class HomeController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Mono<Resource> index() {
        return Mono.just(new ClassPathResource("static/index.html"));
    }

    @GetMapping("/favicon.ico")
    @ResponseBody
    public Mono<ResponseEntity<Void>> favicon() {
        // Return 204 No Content to prevent 400/404 errors for favicon requests
        return Mono.just(ResponseEntity.noContent().build());
    }

    @GetMapping("/health")
    @ResponseBody
    public Mono<String> health() {
        return Mono.just("OK");
    }
}
