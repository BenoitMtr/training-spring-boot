package com.ecommerce.circuitbreaker;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
class CircuitbreakerApplicationTests {


    @InjectMocks
    CircuitbreakerApplication cba;

    @Test
    void contextLoads() {
    }

    @Test
    public void listProductMyShowFallBackTest(){
        //Arrange
        String test = "service gateway failed...";
        //Act
        String temp = cba.listProductMyShowFallBack();
        //Assert
        assertEquals(test,temp);
    }

    @Test
    public void callMargeProductServiceAndGetData_FallbackTest() {
        //Arrange
        String id = "2";
        String test = "CIRCUIT BREAKER ENABLED!!!No Response From Product Service at this moment. Service will be back shortly - " + new Date();
        // Act
        String temp = cba.callMargeProductServiceAndGetData_Fallback(id);
        //Assert
        assertEquals(test,temp);
    }

}
