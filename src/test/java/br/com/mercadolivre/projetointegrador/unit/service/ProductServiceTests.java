package br.com.mercadolivre.projetointegrador.unit.service;

import br.com.mercadolivre.projetointegrador.test_utils.WarehouseTestUtils;
import br.com.mercadolivre.projetointegrador.warehouse.enums.CategoryEnum;
import br.com.mercadolivre.projetointegrador.warehouse.exception.db.NotFoundException;
import br.com.mercadolivre.projetointegrador.warehouse.exception.db.ProductAlreadyExists;
import br.com.mercadolivre.projetointegrador.warehouse.model.Product;
import br.com.mercadolivre.projetointegrador.warehouse.model.ProductInWarehouses;
import br.com.mercadolivre.projetointegrador.warehouse.repository.BatchRepository;
import br.com.mercadolivre.projetointegrador.warehouse.repository.ProductRepository;
import br.com.mercadolivre.projetointegrador.warehouse.service.ProductService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTests {

  @Mock private ProductRepository productRepository;

  @Mock private BatchRepository batchRepository;

  @InjectMocks private ProductService productService;

  private Product newTestProduct;

  @BeforeEach
  public void createMockProduct() {
    newTestProduct = new Product();
    newTestProduct.setId(1L);
    newTestProduct.setName("new product");
    newTestProduct.setCategory(CategoryEnum.FS);
  }

  @Test
  @DisplayName(
      "Given a valid product, when call createProduct, then createProduct must be called once.")
  public void createProductMustBeCalledOnce() throws ProductAlreadyExists {

    Mockito.when(productRepository.save(Mockito.any())).thenReturn(newTestProduct);

    this.productService.createProduct(newTestProduct);

    Mockito.verify(productRepository, Mockito.times(1)).save(newTestProduct);
  }

  @Test
  @DisplayName(
      "Given an existing product, when call find method with id from that product, then return this"
          + " one")
  public void shouldReturnProductById() throws NotFoundException {

    Mockito.when(productRepository.findById(Mockito.anyLong()))
        .thenReturn(Optional.of(newTestProduct));

    Product result = productService.findById(1L);

    assertEquals(newTestProduct, result);
  }

  @Test
  @DisplayName(
      "Given an existing product, when call find product in warehouse method, "
          + "then return a exact product and all their locations(warehouses)")
  public void shouldReturnAListOfWarehousesThatContainASpecificProduct() throws NotFoundException {

    ProductInWarehouses expectedProduct = WarehouseTestUtils.getProductInWarehouse();
    Mockito.when(batchRepository.findAllByProductId(1L)).thenReturn(WarehouseTestUtils.getBatch());

    ProductInWarehouses founded = productService.findProductInWarehouse(1L);

    assertEquals(expectedProduct.getProductId(), founded.getProductId());
  }

  @Test
  @DisplayName(
      "Given a non existing product, when a call find product in warehouse method, the throw an"
          + " error")
  public void shouldThrownNotFoundExceptionWhenTheProductDoesExistsInWarehouse() {
    Exception thrown =
        Assertions.assertThrows(
            NotFoundException.class, () -> productService.findProductInWarehouse(10L));

    Assertions.assertEquals("Produto " + 10 + " não encontrado.", thrown.getMessage());
  }

  @Test
  @DisplayName(
      "Given an id from a non existing product, when call find by id method, then throw an error")
  public void shouldThrownNotFoundExceptionWhenIdDoesNotExists() {

    Exception thrown =
        Assertions.assertThrows(NotFoundException.class, () -> productService.findById(10L));

    Assertions.assertEquals("Produto " + 10 + " não encontrado.", thrown.getMessage());
  }

  @Test
  @DisplayName(
      "Given an valid id and valid updated information, when call updateProduct, then updateProduct"
          + " must be called once.")
  public void shouldUpdateProduct() throws NotFoundException {

    Product updatedProduct = new Product();
    updatedProduct.setName("novo nome");
    updatedProduct.setCategory(CategoryEnum.FF);

    Mockito.when(productRepository.findById(1L)).thenReturn(Optional.ofNullable(newTestProduct));

    newTestProduct.setName("novo nome");
    newTestProduct.setCategory(CategoryEnum.FF);

    Mockito.when(productRepository.save(newTestProduct)).thenReturn(newTestProduct);
    productService.updateProduct(1L, updatedProduct);

    Mockito.verify(productRepository, Mockito.times(1)).save(newTestProduct);
  }

  @Test
  @DisplayName("Given a valid id, when call delete, the delete must be called once")
  public void shouldDeleteProduct() throws NotFoundException {
    Mockito.when(productRepository.findById(1L)).thenReturn(Optional.ofNullable(newTestProduct));
    productService.delete(1L);
    Mockito.verify(productRepository, Mockito.times(1)).delete(newTestProduct);
  }

  @Test
  @DisplayName("Given an invalid id, when call delete, then throw an Exception.")
  public void shouldNotDeleteInexistingProduct() throws NotFoundException {

    Exception thrown =
        Assertions.assertThrows(NotFoundException.class, () -> productService.delete(2L));

    Assertions.assertEquals("Produto " + 2 + " não encontrado.", thrown.getMessage());

    Mockito.verify(productRepository, Mockito.times(0)).delete(Mockito.any());
  }

  @Test
  @DisplayName("Given a category, should return a product list")
  public void shouldFindAllProductsByCategory() {
    List<Product> expected = List.of(newTestProduct);
    Mockito.when(productRepository.findAllByCategory(Mockito.any(CategoryEnum.class)))
        .thenReturn(expected);

    List<Product> result = productService.findAllByCategory(CategoryEnum.FS);

    Mockito.verify(productRepository, Mockito.times(1)).findAllByCategory(CategoryEnum.FS);
    assertEquals(expected, result);
  }

  @Test
  @DisplayName("Given a category, should return a product list even if dont find any result")
  public void shouldFindAllProductsByCategoryAndReturnEmptyList() {

    Mockito.when(productRepository.findAllByCategory(Mockito.any(CategoryEnum.class)))
        .thenReturn(Collections.emptyList());

    List<Product> result = productService.findAllByCategory(CategoryEnum.FS);

    Mockito.verify(productRepository, Mockito.times(1)).findAllByCategory(CategoryEnum.FS);
    assertEquals(0, result.size());
  }
}
