package com.ecommerce.circuitbreaker;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

@SpringBootApplication
@RestController
@EnableHystrix
public class CircuitbreakerApplication {

    @Autowired
    private RestTemplate template;

    public static void main(String[] args) {
        SpringApplication.run(CircuitbreakerApplication.class, args);
    }

    @Bean
    public RestTemplate template(){
        return new RestTemplate();
    }


    @ApiOperation(value = "Obtenir la liste des produits", response = Iterable.class, tags = "listProductNow")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message="Success"),
            @ApiResponse(code = 500, message="Serveur"),
            @ApiResponse(code = 401, message="not authorized"),
            @ApiResponse(code = 403, message="forbidden"),
            @ApiResponse(code = 404, message="Page Introuvable")
    })

    @HystrixCommand(groupKey = "java yan", commandKey = "java yan", fallbackMethod = "listProductMyShowFallBack")
    @RequestMapping(value = "/listProductNow",method = RequestMethod.GET)
    public String listProductShow(){
        String listProductServiceResponse = template.getForObject("http://localhost:9090/Produits",String.class);
        return listProductServiceResponse;
    }

    @ApiOperation(value = "Obtenir la marge d'un produit", response = Iterable.class, tags = "calculerMargeProduitNow")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message="Success"),
            @ApiResponse(code = 500, message="Serveur"),
            @ApiResponse(code = 401, message="not authorized"),
            @ApiResponse(code = 403, message="forbidden"),
            @ApiResponse(code = 404, message="Page Introuvable")
    })
    @HystrixCommand(groupKey = "java yan", commandKey = "java yan", fallbackMethod = "callMargeProductServiceAndGetData_Fallback")
    @RequestMapping(value = "/calculerMargeProduitNow/{id}", method = RequestMethod.GET)
    public String calculerMargeProduitShow(@PathVariable String id){
        System.out.println("Obtenir la marge pour le produit " + id);
        int idValue = Integer.parseInt(id);
        String response = template
                .exchange("http://localhost:9090/calculerMarge/{idValue}"
                        , HttpMethod.GET
                        , null
                        , new ParameterizedTypeReference<String>() {
                        }, idValue).getBody();

        System.out.println("Response Received as " + response + " -  " + new Date());
        return response;
    }

    @ApiOperation(value = "Calculer la marge de chaque produit présent dans la base", response = Iterable.class, tags = "AdminMargeProduitNow")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message="Success"),
            @ApiResponse(code = 500, message="Serveur"),
            @ApiResponse(code = 401, message="not authorized"),
            @ApiResponse(code = 403, message="forbidden"),
            @ApiResponse(code = 404, message="")
    })
    @HystrixCommand(groupKey = "java yan", commandKey = "java yan", fallbackMethod = "listProductMyShowFallBack")
    @RequestMapping(value = "/AdminMargeProduitNow",method = RequestMethod.GET)
    public String calculerMargeProduit()
    {
        String calculerMargeProduitResponse = template.getForObject("http://localhost:9090/AdminProduits",String.class);
        return calculerMargeProduitResponse;
    }

    @ApiOperation(value = "Obtenir la liste des produits dans l'ordre alphabétique", response = Iterable.class, tags = "triOrdreAlphabetiqueNow")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message="Success"),
            @ApiResponse(code = 500, message="Serveur"),
            @ApiResponse(code = 401, message="not authorized"),
            @ApiResponse(code = 403, message="forbidden"),
            @ApiResponse(code = 404, message="")
    })
    @HystrixCommand(groupKey = "java yan", commandKey = "java yan", fallbackMethod = "listProductMyShowFallBack")
    @RequestMapping(value = "/triOrdreAlphabetiqueNow", method = RequestMethod.GET)
    public String trierProduitsParOrdreAlphabetiqueShow(){
        String trierProduitsParOrdreAlphabetique = template.getForObject("http://localhost:9090/triOrdreAlphabetique",String.class);
        return trierProduitsParOrdreAlphabetique;
    }

    @ApiOperation(value = "Obtenir un produit à partir d'un ID", response = Iterable.class, tags = "afficherUnProduitNow")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message="Success"),
            @ApiResponse(code = 500, message="Serveur"),
            @ApiResponse(code = 401, message="not authorized"),
            @ApiResponse(code = 403, message="forbidden"),
            @ApiResponse(code = 404, message="")
    })
    @HystrixCommand(groupKey = "java yan", commandKey = "java yan", fallbackMethod = "callAfficherProduitServiceAndGetData_Fallback")
    @RequestMapping(value = "/afficherUnProduitNow/{id}" ,method = RequestMethod.GET)
    public String afficherUnProduitShow(@PathVariable String id){
        System.out.println("Afficher le produit " + id);
        int idValue = Integer.parseInt(id);
        String response = template
                .exchange("http://localhost:9090/Produits/{id}"
                        , HttpMethod.GET
                        , null
                        , new ParameterizedTypeReference<String>() {
                        }, idValue).getBody();

        System.out.println("Response Received as " + response + " -  " + new Date());
        return response;
    }

    public String listProductMyShowFallBack()
    {
        return "service gateway failed...";
    }

    public String callMargeProductServiceAndGetData_Fallback(String id) {
        System.out.println("Product Service is down!!! fallback route enabled...");
        return "CIRCUIT BREAKER ENABLED!!!No Response From Product Service at this moment. Service will be back shortly - " + new Date();
    }

    private String callAfficherProduitServiceAndGetData_Fallback(String id) {
        System.out.println("Product Service is down!!! fallback route enabled...");
        return "CIRCUIT BREAKER ENABLED!!!No Response From Product Service at this moment. Service will be back shortly - " + new Date();
    }
}
