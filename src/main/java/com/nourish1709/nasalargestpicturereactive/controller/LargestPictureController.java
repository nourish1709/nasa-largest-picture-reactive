package com.nourish1709.nasalargestpicturereactive.controller;

import com.nourish1709.nasalargestpicturereactive.service.LargestPictureService;
import io.netty.resolver.DefaultAddressResolverGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@RestController
@RequestMapping("/pictures")
@RequiredArgsConstructor
public class LargestPictureController {

    private final LargestPictureService largestPictureService;

    @GetMapping(value = "/{sol}/largest", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<byte[]> getLargestPicture(@PathVariable int sol) {
        return largestPictureService.findLargestPictureUrl(sol)
                .flatMap(largestPictureUrl -> WebClient.builder()
                        .clientConnector(new ReactorClientHttpConnector(
                                HttpClient.create()
                                        .followRedirect(true)
                                        .resolver(DefaultAddressResolverGroup.INSTANCE)))
                        .baseUrl(largestPictureUrl)
                        .build()
                        .mutate()
                        .codecs(config -> config.defaultCodecs().maxInMemorySize(10_000_000))
                        .build()
                        .get()
                        .exchangeToMono(response -> response.bodyToMono(byte[].class)));
    }
}
