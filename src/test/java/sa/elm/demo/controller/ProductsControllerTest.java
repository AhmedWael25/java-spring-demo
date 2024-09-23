package sa.elm.demo.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sa.elm.demo.service.ProductsService;
import sa.elm.models.ProductCreationRequest;
import sa.elm.models.ProductsResponse;
import sa.elm.models.StatisticsResponse;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class ProductsControllerTest {

  @MockBean
  private ProductsService productsService;

  @Autowired
  private ProductsController productsController;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(productsController).build();
  }


  @Test
  @WithMockUser(username = "1", password = "", authorities = "DEALER")
  void testGetProducts_WithDealerRole_Success() throws Exception {
    ProductsResponse productsResponse = ProductsResponse.builder().build();
    productsResponse.setTotal(10L);

    when(productsService.getAllDealerProducts(anyInt(), anyInt())).thenReturn(productsResponse);

    mockMvc.perform(get("/products")
            .param("limit", "10")
            .param("offset", "0")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(header().string(ProductsController.X_TOTAL_COUNT, "10"))
        .andExpect(jsonPath("$.total").value(10));

    verify(productsService, times(1)).getAllDealerProducts(anyInt(), anyInt());
  }

  @Test
  @WithMockUser(username = "1", password = "", authorities = "CLIENT")
  void testGetActiveProducts_WithClientRole_Success() throws Exception {
    ProductsResponse productsResponse = ProductsResponse.builder().build();
    productsResponse.setTotal(5L);

    when(productsService.getAllActiveProducts(anyInt(), anyInt())).thenReturn(productsResponse);

    mockMvc.perform(get("/products/user")
            .param("limit", "10")
            .param("offset", "0")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(header().string(ProductsController.X_TOTAL_COUNT, "5"))
        .andExpect(jsonPath("$.total").value(5));

    verify(productsService, times(1)).getAllActiveProducts(anyInt(), anyInt());
  }

  @Test
  @WithMockUser(username = "1", password = "", authorities = "DEALER")
  void testAddProduct_WithDealerRole_Success() throws Exception {
    doNothing().when(productsService).addNewProduct(any(ProductCreationRequest.class));

    mockMvc.perform(post("/products/add")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\": \"New Product\", \"price\": \"25.00\"}"))
        .andExpect(status().isOk());

    verify(productsService, times(1)).addNewProduct(any(ProductCreationRequest.class));
  }

  @Test
  @WithMockUser(username = "1", password = "", authorities = "ADMIN")
  void testGetAllProducts_WithAdminRole_Success() throws Exception {
    ProductsResponse productsResponse = ProductsResponse.builder().build();
    productsResponse.setTotal(20L);

    when(productsService.getAllProducts(anyInt(), anyInt())).thenReturn(productsResponse);

    mockMvc.perform(get("/products/admin")
            .param("limit", "10")
            .param("offset", "0")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(header().string(ProductsController.X_TOTAL_COUNT, "20"))
        .andExpect(jsonPath("$.total").value(20));

    verify(productsService, times(1)).getAllProducts(anyInt(), anyInt());
  }

  @Test
  @WithMockUser(username = "1", password = "", authorities = "DEALER")
  void testChangeProductStatus_WithDealerRole_Success() throws Exception {
    doNothing().when(productsService).changeProductStatus(anyLong());

    mockMvc.perform(post("/products/1/change-status")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(productsService, times(1)).changeProductStatus(anyLong());
  }

  @Test
  @WithMockUser(username = "1", password = "", authorities = "ADMIN")
  void testGetProductStatistics_WithAdminRole_Success() throws Exception {
    StatisticsResponse statisticsResponse = StatisticsResponse.builder().build();

    when(productsService.getProductStatistics(any(), any())).thenReturn(statisticsResponse);

    mockMvc.perform(get("/products/statistics")
            .param("from", "2023-01-01")
            .param("to", "2023-12-31")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(productsService, times(1)).getProductStatistics(any(), any());
  }
}