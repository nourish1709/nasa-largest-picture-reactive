package com.nourish1709.nasalargestpicturereactive.service;

import reactor.core.publisher.Mono;

public interface LargestPictureService {

    Mono<String> findLargestPictureUrl(int sol);
}
