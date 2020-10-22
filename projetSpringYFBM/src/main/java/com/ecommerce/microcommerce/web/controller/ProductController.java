package com.ecommerce.microcommerce.web.controller;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.exceptions.ProduitGratuitException;
import com.ecommerce.microcommerce.web.exceptions.ProduitIntrouvableException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;


@Api( description="API pour les opérations CRUD sur les produits.")
@RestController
public class ProductController implements ErrorController {

    @Autowired
    private ProductDao productDao;

    public ProductDao getProductDao() {
        return productDao;
    }

    public void setProductDao(ProductDao productDao) {
        this.productDao = productDao;
    }

    //Récupérer la liste des produits
    @ApiOperation(value = "Obtenir la liste des produits", response = Iterable.class, tags = "getProducts")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message="Success"),
            @ApiResponse(code = 500, message="Serveur"),
            @ApiResponse(code = 401, message="not authorized"),
            @ApiResponse(code = 403, message="forbidden"),
            @ApiResponse(code = 404, message="")
    })

    @RequestMapping(value = "/getProducts", method = RequestMethod.GET)
    public MappingJacksonValue listeProduits() {

        Iterable<Product> produits = productDao.findAll();

        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");

        FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);

        MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);

        produitsFiltres.setFilters(listDeNosFiltres);
        return produitsFiltres;
    }

    @ApiOperation(value = "Obtenir la marge pour un id de produit", response = Iterable.class, tags = "calculerMarge")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message="Success"),
            @ApiResponse(code = 500, message="Serveur"),
            @ApiResponse(code = 401, message="not authorized"),
            @ApiResponse(code = 403, message="forbidden"),
            @ApiResponse(code = 404, message="")
    })

    @GetMapping(value = "/calculerMarge/{id}")
    public double calculerMargeProduit(@PathVariable int id)
    {
        Product prod= productDao.findById(id);
        return (prod.getPrix()-prod.getPrixAchat());
    }


    @ApiOperation(value = "Calculer la marge de chaque produit présent dans la base", response = Iterable.class, tags = "AdminProduits")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message="Success"),
            @ApiResponse(code = 500, message="Serveur"),
            @ApiResponse(code = 401, message="not authorized"),
            @ApiResponse(code = 403, message="forbidden"),
            @ApiResponse(code = 404, message="")
    })


    @GetMapping(value = "/AdminProduits")
    public String calculerMargeProduit()
    {
        StringBuilder temp = new StringBuilder("");
        for(Product p:productDao.findAll())
        {
            temp.append(p.toString()).append(": ").append(p.getPrix()-p.getPrixAchat()).append(", ");
        }
        return temp.toString();
    }

    @ApiOperation(value = "Trier la liste des produits par ordre alphabétique français, par nom croissant", response = Iterable.class, tags = "triOrdreAlphabetique")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message="Success"),
            @ApiResponse(code = 500, message="Serveur"),
            @ApiResponse(code = 401, message="not authorized"),
            @ApiResponse(code = 403, message="forbidden"),
            @ApiResponse(code = 404, message="")
    })

    @GetMapping(value = "/triOrdreAlphabetique")
    public MappingJacksonValue trierProduitsParOrdreAlphabetique()
    {
        Iterable<Product> sortedProducts=productDao.triAlphabetique();

        MappingJacksonValue produitsFiltres = new MappingJacksonValue(sortedProducts);

        return produitsFiltres;
    }


    //Récupérer un produit par son Id
    @ApiOperation(value = "Récupère un produit grâce à son ID et l'affiche", response = Iterable.class, tags = "Produits")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message="Success"),
            @ApiResponse(code = 500, message="Serveur"),
            @ApiResponse(code = 401, message="not authorized"),
            @ApiResponse(code = 403, message="forbidden"),
            @ApiResponse(code = 404, message="")
    })


    @GetMapping(value = "/Produits/{id}")
    public Product afficherUnProduit(@PathVariable int id) {

        Product produit = productDao.findById(id);

        if(produit==null) throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est INTROUVABLE. Écran Bleu si je pouvais.");

        return produit;
    }


    @ApiOperation(value = "Ajouter un produit", response = Iterable.class, tags = "Produits")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message="Success"),
            @ApiResponse(code = 500, message="Serveur"),
            @ApiResponse(code = 401, message="not authorized"),
            @ApiResponse(code = 403, message="forbidden"),
            @ApiResponse(code = 404, message="")
    })
    //ajouter un produit
    @PostMapping(value = "/Produits")

    public ResponseEntity<Void> ajouterProduit(@Valid @RequestBody Product product) {

        if(product.getPrixAchat()==0) throw new ProduitGratuitException("Le prix de vente doit être supérieur à 0");

        if(product!=null) productDao.save(product);
        else return ResponseEntity.noContent().build();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(product.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @ApiOperation(value = "Suppression d'un produit", response = Iterable.class, tags = "Produits")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message="Success"),
            @ApiResponse(code = 500, message="Serveur"),
            @ApiResponse(code = 401, message="not authorized"),
            @ApiResponse(code = 403, message="forbidden"),
            @ApiResponse(code = 404, message="")
    })

    @DeleteMapping (value = "/Produits/{id}")
    public void supprimerProduit(@PathVariable int id) {

        productDao.delete(id);
    }

    @ApiOperation(value = "Mise à jour d'un produit", response = Iterable.class, tags = "Produits")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message="Success"),
            @ApiResponse(code = 500, message="Serveur"),
            @ApiResponse(code = 401, message="not authorized"),
            @ApiResponse(code = 403, message="forbidden"),
            @ApiResponse(code = 404, message="")
    })

    @PutMapping (value = "/Produits")
    public void updateProduit(@RequestBody Product product) {

        productDao.save(product);
    }

    @ApiOperation(value = "Tests", response = Iterable.class, tags = "test/produits/")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message="Success"),
            @ApiResponse(code = 500, message="Serveur"),
            @ApiResponse(code = 401, message="not authorized"),
            @ApiResponse(code = 403, message="forbidden"),
            @ApiResponse(code = 404, message="")
    })


    //Pour les tests
    @GetMapping(value = "test/produits/{prix}")
    public List<Product>  testeDeRequetes(@PathVariable int prix) {

        return productDao.chercherUnProduitCher(400);
    }

    @ApiOperation(value = "Gestion des erreurs http", response = Iterable.class, tags = "error")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message="Success"),
            @ApiResponse(code = 500, message="Serveur"),
            @ApiResponse(code = 401, message="not authorized"),
            @ApiResponse(code = 403, message="forbidden"),
            @ApiResponse(code = 404, message="")
    })

   @RequestMapping(value = "/error", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
    public String renderErrorPage(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        return "Erreur "+ statusCode+": "+getErrorMessage(statusCode);
    }

    public String getErrorMessage(int HTTPCode)
    {
        switch(HTTPCode)
        {
            case 400: return "Il y a une erreur dans la requête";
            case 401: return "Une autorisation est nécessaire pour accéder à la requête";
            case 403: return "Droits d'accès insuffisants";
            case 404: return "La ressource est introuvable";
            case 500: return "Ah, le serveur a rencontré un problème!";
            case 502: return "Un serveur en amont nous a envoyé une réponse invalide";
        }
        return "";
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
