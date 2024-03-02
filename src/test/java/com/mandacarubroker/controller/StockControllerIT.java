package com.mandacarubroker.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mandacarubroker.MandacaruBrokerApplication;
import com.mandacarubroker.domain.Role;
import com.mandacarubroker.domain.user.User;
import com.mandacarubroker.domain.user.UserRepository;
import com.mandacarubroker.dtos.RequestLoginDTO;
import com.mandacarubroker.dtos.RequestStockDTO;
import com.mandacarubroker.domain.stock.Stock;
import com.mandacarubroker.domain.stock.StockRepository;
import org.aspectj.apache.bcel.Repository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = MandacaruBrokerApplication.class)
@ActiveProfiles(value = "test")
@AutoConfigureMockMvc
@Transactional
@ContextConfiguration
class StockControllerIT {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    private Stock stock;
    private Long totalStocks;
    private String existingId;
    private static User normalUser;
    private static User adminUser;
    private static final String noEncryptedPassword = "12345678";
    private static String EncryptedPassword;
    @BeforeEach
    void setup() {
        stock = new Stock("1","ABC1","ABC COMPANY",50.0);
        Stock savedStock = stockRepository.save(stock);
        totalStocks=stockRepository.count();
        existingId = savedStock.getId();

        EncryptedPassword = passwordEncoder.encode(noEncryptedPassword);
        normalUser = new User("paulo",EncryptedPassword,"paulo@email.com","Paulo","Herbert", LocalDate.parse("2000-05-04"),100.0);
        normalUser.setRole(Role.NORMAL);
        adminUser = new User("admin",EncryptedPassword,"admin@email.com","","", LocalDate.parse("2000-05-04"),0.0);
        adminUser.setRole(Role.ADMIN);
        userRepository.saveAll(List.of(normalUser,adminUser));
    }

    private String authenticateUser(User adminUser) throws Exception {
        RequestLoginDTO reqLoginDto = new RequestLoginDTO(adminUser.getUsername(),noEncryptedPassword);
        var responseBody = mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(reqLoginDto)))
                .andExpect(status().isOk()).andReturn();

        return objectMapper.readTree(responseBody.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void createShouldCreateAStockWhenUserIsAdmin() throws Exception {
        String token = authenticateUser(adminUser);

        RequestStockDTO validDto = new RequestStockDTO(stock.getSymbol(),stock.getCompanyName(),stock.getPrice());
        String jsonObject = objectMapper.writeValueAsString(validDto);

        MvcResult result = mockMvc.perform(post("/stocks").contentType(MediaType.APPLICATION_JSON).content(jsonObject).header("Authorization","Bearer "+token))
                .andExpect(status().isCreated()).andReturn();

        String jsonRespStock = result.getResponse().getContentAsString();
        Stock respStock= objectMapper.readValue(jsonRespStock,Stock.class);

        assertEquals(totalStocks+1,stockRepository.count());
        assertTrue(stockRepository.existsById(respStock.getId()));
    }

    @Test
    void createShouldNotCreateAStockWhenUserIsNotAdmin() throws Exception {
        String token = authenticateUser(normalUser);

        RequestStockDTO validDto = new RequestStockDTO(stock.getSymbol(),stock.getCompanyName(),stock.getPrice());
        String jsonObject = objectMapper.writeValueAsString(validDto);

        MvcResult result = mockMvc.perform(post("/stocks").contentType(MediaType.APPLICATION_JSON).content(jsonObject)
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isForbidden()).andReturn();

        String jsonRespStock = result.getResponse().getContentAsString();
        assertEquals(jsonRespStock,"");
        assertEquals(totalStocks,stockRepository.count());
    }

    @Test
    void createShouldNotCreateAStockWhenInvalidInputAndUserIsAdmin() throws Exception {
        String token = authenticateUser(adminUser);
        RequestStockDTO invalidDto = new RequestStockDTO("AB","",0);
        String jsonObject = objectMapper.writeValueAsString(invalidDto);

        mockMvc.perform(post("/stocks").contentType(MediaType.APPLICATION_JSON).content(jsonObject)
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(containsString("Validation failed")));

        assertEquals(totalStocks,stockRepository.count());
    }

    @Test
    void findByIdShouldReturnAStockWithoutLogin() throws Exception {
        mockMvc.perform(get("/stocks/{id}",existingId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.notNullValue()))
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("$.symbol").value(stock.getSymbol()))
                .andExpect(jsonPath("$.companyName").value(stock.getCompanyName()))
                .andExpect(jsonPath("$.price").value(stock.getPrice()));

    }
    @Test
    void findAllShouldReturnStocksWhenUserHasAnyAuthority() throws Exception {
        String token = authenticateUser(adminUser);
        mockMvc.perform(get("/stocks",existingId).contentType(MediaType.APPLICATION_JSON)
                .header("Authorization","Bearer "+token))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.notNullValue()))
                .andExpect(jsonPath("$").isArray());

        token = authenticateUser(normalUser);
        mockMvc.perform(get("/stocks",existingId).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.notNullValue()))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updateShouldUpdateAStockWhenUserIsAdmin() throws Exception {
        String token = authenticateUser(adminUser);
        RequestStockDTO validDto = new RequestStockDTO(stock.getSymbol(),stock.getCompanyName(),stock.getPrice());
        double expectedPrice = validDto.price()+stock.getPrice();
        String jsonObject = objectMapper.writeValueAsString(validDto);

        mockMvc.perform(put("/stocks/{id}",existingId).contentType(MediaType.APPLICATION_JSON).content(jsonObject)
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isOk()).andExpect(content().string(notNullValue()));

        Stock updatedStock = stockRepository.findById(existingId).orElseThrow();

        assertEquals(totalStocks,stockRepository.count());
        assertEquals(validDto.symbol(),updatedStock.getSymbol());
        assertEquals(validDto.companyName(),updatedStock.getCompanyName());
        assertEquals(expectedPrice,updatedStock.getPrice());
    }

    @Test
    void updateShouldNotUpdateAStockWhenUserIsNotAdmin() throws Exception {
        String token = authenticateUser(normalUser);
        RequestStockDTO validDto = new RequestStockDTO("BBA1","its a company name",stock.getPrice());
        String jsonObject = objectMapper.writeValueAsString(validDto);
        Stock beforeUpdate = stockRepository.findById(existingId).orElseThrow();

        mockMvc.perform(put("/stocks/{id}",existingId).contentType(MediaType.APPLICATION_JSON).content(jsonObject)
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isForbidden()).andExpect(content().string(notNullValue()));

        Stock updatedStock = stockRepository.findById(existingId).orElseThrow();

        assertEquals(totalStocks,stockRepository.count());
        assertEquals(beforeUpdate.getSymbol(),updatedStock.getSymbol());
        assertEquals(beforeUpdate.getCompanyName(),updatedStock.getCompanyName());
        assertEquals(beforeUpdate.getPrice(),updatedStock.getPrice());
    }

    @Test
    void updateShouldNotUpdateAStockWhenInvalidDataAndUserIsAdmin() throws Exception {
        String token = authenticateUser(adminUser);
        RequestStockDTO invalidDto = new RequestStockDTO("","",0);
        String jsonObject = objectMapper.writeValueAsString(invalidDto);
        Stock beforeUpdate = stockRepository.findById(existingId).orElseThrow();

        mockMvc.perform(put("/stocks/{id}",existingId).contentType(MediaType.APPLICATION_JSON).content(jsonObject)
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string(containsString("Validation failed")));

        Stock updatedStock = stockRepository.findById(existingId).orElseThrow();

        assertEquals(totalStocks,stockRepository.count());
        assertEquals(beforeUpdate.getSymbol(),updatedStock.getSymbol());
        assertEquals(beforeUpdate.getCompanyName(),updatedStock.getCompanyName());
        assertEquals(beforeUpdate.getPrice(),updatedStock.getPrice());
    }

    @Test
    void deleteShouldDeleteAStockWhenUserIsAdmin() throws Exception {
        String token = authenticateUser(adminUser);
        mockMvc.perform(delete("/stocks/{id}",existingId).contentType(MediaType.APPLICATION_JSON).header("Authorization","Bearer "+token))
                .andExpect(status().isNoContent())
                .andExpect(content().string(blankOrNullString()));

        assertEquals(totalStocks-1,stockRepository.count());
        assertTrue(stockRepository.findById(existingId).isEmpty());
    }

    @Test
    void deleteShouldNotDeleteAStockWhenUserIsAdminAndNonExistingId() throws Exception {
        String nonExistingId = "none";
        String token = authenticateUser(adminUser);
        mockMvc.perform(delete("/stocks/{id}",nonExistingId).contentType(MediaType.APPLICATION_JSON).header("Authorization","Bearer "+token))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Stock Not Found")));

        assertEquals(totalStocks,stockRepository.count());
        assertTrue(stockRepository.findById(nonExistingId).isEmpty());
    }

    @Test
    void deleteShouldNotDeleteAStockWhenUserIsNotAdmin() throws Exception {
        String token = authenticateUser(normalUser);
        mockMvc.perform(delete("/stocks/{id}",existingId).contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization","Bearer "+token))
                .andExpect(status().isForbidden());
        assertEquals(totalStocks,stockRepository.count());
    }
}
