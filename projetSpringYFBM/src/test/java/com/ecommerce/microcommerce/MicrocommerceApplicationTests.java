package com.ecommerce.microcommerce;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.web.controller.ProductController;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
//TODO finish Unit Tests
public class MicrocommerceApplicationTests {

	@MockBean
	ProductDao productDao;
	ProductController pController;

	@Test
	public void contextLoads() {
	}

	@Before
	public void reset()
	{
		productDao.deleteAll();
	}

	@Test
	public void calculerMargeProduit() {
		/*pController=new ProductController();
		Product p=new Product(1, "test", 50, 30);
		pController.ajouterProduit(p);

		Assert.assertEquals(20,pController.calculerMargeProduit(1));*/
	}

	@Test
	public void calculerMargeProduit1() {
		/*Product p=new Product(2, "test", 50, 30);
		pController.ajouterProduit(p);

		Assert.assertEquals("Product{id="+p.getId()+", nom='"+p.getNom()+"', prix="+p.getPrix()+"}: "+pController.calculerMargeProduit(p.getId())+",",pController.calculerMargeProduit());*/
	}

	@Test
	public void trierProduitsParOrdreAlphabetique() {
		/*Product p=new Product(3, "a", 50, 30);
		Product p2=new Product(4, "b", 50, 30);
		Product p3=new Product(5, "c", 50, 30);

		pController.ajouterProduit(p);
		pController.ajouterProduit(p2);
		pController.ajouterProduit(p3);

		List<Product> sortedList= pController.trierProduitsParOrdreAlphabetique();

		Assert.assertEquals("3", sortedList.get(0).getId());*/

	}

	@Test
	public void afficherUnProduit() {
	}

	@Test
	public void ajouterProduit() {
	}

	@Test
	public void supprimerProduit() {
	}

	@Test
	public void updateProduit() {
	}

}
