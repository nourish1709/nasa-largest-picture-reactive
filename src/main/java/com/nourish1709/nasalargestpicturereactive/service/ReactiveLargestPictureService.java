package com.nourish1709.nasalargestpicturereactive.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.nourish1709.nasalargestpicturereactive.dto.Picture;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Service
public class ReactiveLargestPictureService implements LargestPictureService {

    private static final String API_URL = "https://api.nasa.gov/mars-photos/api/v1/rovers/curiosity/photos";

    private static final String API_KEY = "DEMO_KEY";

    @Override
    public Mono<String> findLargestPictureUrl(int sol) {
        return WebClient.create(API_URL)
                .get()
                .uri(builder -> builder
                        .queryParam("api_key", API_KEY)
                        .queryParam("sol", sol)
                        .build())
                .exchangeToMono(clientResponse -> clientResponse.bodyToMono(JsonNode.class))
                .map(jsonNode -> jsonNode.get("photos"))
                .flatMapMany(Flux::fromIterable)
                .map(jsonNode -> jsonNode.get("img_src"))
                .map(JsonNode::asText)
                .flatMap(imgSrc -> WebClient.builder()
                        .clientConnector(
                                new ReactorClientHttpConnector(
                                        HttpClient.create()
                                                .baseUrl(imgSrc)
                                                .followRedirect(true)
                                                .resolver(DefaultAddressResolverGroup.INSTANCE)))
                        .baseUrl(imgSrc)
                        .build()
                        .head()
                        .exchangeToMono(ClientResponse::toBodilessEntity)
                        .map(HttpEntity::getHeaders)
                        .mapNotNull(HttpHeaders::getContentLength)
                        .map(size -> new Picture(imgSrc, size)))
                .reduce((picture1, picture2) -> picture1.size() > picture2.size() ? picture1 : picture2)
                .map(Picture::url);
    }
}
