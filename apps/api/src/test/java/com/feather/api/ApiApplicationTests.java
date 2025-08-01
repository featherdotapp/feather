package com.feather.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class ApiApplicationTests {

    @Test
    void contextLoads() {
        // test
    }

}

@SpringBootTest
@ActiveProfiles("prod")
class ApiApplicationProdProfileTests {

    @Test
    void contextLoads() {
        // test
    }

}