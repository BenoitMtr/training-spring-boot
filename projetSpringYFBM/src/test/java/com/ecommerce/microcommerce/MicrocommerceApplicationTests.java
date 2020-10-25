package com.ecommerce.microcommerce;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.controller.ProductController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MicrocommerceApplicationTests {

	@Mock
	ProductDao productDao;

	@InjectMocks
	ProductController pController;

	@Test
	public void contextLoads() {
	}

	@Test
	public void calculerMargeProduit() {
		Product p=new Product(5, "test", 50, 30);
		when(productDao.save(p)).thenReturn(p);
		when(productDao.findById(p.getId())).thenReturn(p);
        pController.ajouterProduit(p);
        assertEquals(20,pController.calculerMargeProduit(5),1);
	}

	@Test
	public void calculerMargeProduit1() {
		Product p=new Product(2, "test", 50, 30);
		ArrayList<Product> list=new ArrayList<Product>();
		list.add(p);
		HashMap<String, Double> margesTest=new HashMap<>();
		margesTest.put(p.toString(), (double) (p.getPrix()-p.getPrixAchat()));
		when(productDao.findAll()).thenReturn(list);

		assertThat(pController.calculerMargeProduit(), is(margesTest));
	}

	@Test
	public void trierProduitsParOrdreAlphabetique() {

	}

	@Test
	public void afficherUnProduit() {
		Product p=new Product(5, "test", 50, 30);
		when(productDao.findById(p.getId())).thenReturn(p);
		pController.ajouterProduit(p);
		assertEquals(p,pController.afficherUnProduit(p.getId()));

	}

	@Test
	public void ajouterProduit() {
		Product p=new Product(5, "test", 50, 30);
		URI location = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(p.getId())
				.toUri();
		when(productDao.save(p)).thenReturn(p);
		assertEquals(ResponseEntity.created(location).build(),pController.ajouterProduit(p));
	}

	@Test
	public void supprimerProduit() {
		Product p=new Product(5, "test", 50, 30);
		when(productDao.save(p)).thenReturn(p);
		pController.ajouterProduit(p);
		doNothing().when(productDao).delete(p);
		pController.supprimerProduit(p.getId());
	}

	@Test
	public void updateProduit() {
		Product p=new Product(5, "test", 50, 30);
		when(productDao.save(p)).thenReturn(p);
		pController.ajouterProduit(p);

		p.setPrix(77);
		pController.updateProduit(p);
	}

	@Test
	public void renderErrorPage()
	{
		HttpServletRequest request = mock(HttpServletRequest.class);
		when((Integer) request.getAttribute("javax.servlet.error.status_code")).thenReturn(400);

		assertEquals("Erreur 400: "+pController.getErrorMessage(400), pController.renderErrorPage(request));

		when((Integer) request.getAttribute("javax.servlet.error.status_code")).thenReturn(500);

		assertEquals("Erreur 500: "+pController.getErrorMessage(500), pController.renderErrorPage(request));

	}
}
