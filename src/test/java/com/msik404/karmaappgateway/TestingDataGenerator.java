package com.msik404.karmaappgateway;

import org.springframework.lang.NonNull;

public class TestingDataGenerator {

    @NonNull
    public static String getIdHexString(long id) {

        return String.format("%024d", id);
    }

}
