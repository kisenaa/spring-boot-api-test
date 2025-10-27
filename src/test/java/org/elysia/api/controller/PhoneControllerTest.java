package org.elysia.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.elysia.api.DTO.phone.PhoneCreateRequest;
import org.elysia.api.DTO.phone.PhoneDTO;
import org.elysia.api.DTO.phone.PhoneUpdateRequest;
import org.elysia.api.entity.PhoneNumber;
import org.elysia.api.mapper.PhoneMapper;
import org.elysia.api.repository.PhoneRepository;
import org.elysia.api.service.PhoneService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password="

}
)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PhoneControllerTest {
    private final PhoneRepository phoneRepository;
    private final PhoneController phoneController;
    private final PhoneMapper phoneMapper;
    private final PhoneService  phoneService;
    private final MockMvc mockMvc;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ObjectMapper objectMapper = getOjectMapper();

    static ObjectMapper getOjectMapper() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModules(javaTimeModule, new ParameterNamesModule(), new Jdk8Module())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        return objectMapper;
    }

    @Autowired
    public PhoneControllerTest(PhoneRepository phoneRepository, PhoneController phoneController, PhoneMapper phoneMapper, PhoneService phoneService, MockMvc mockMvc) {
        this.phoneRepository = phoneRepository;
        this.phoneController = phoneController;
        this.phoneMapper = phoneMapper;
        this.phoneService = phoneService;
        this.mockMvc = mockMvc;
    }

    @Test
    @Order(1)
    public void testValidInsertPhone() throws Exception {
        PhoneCreateRequest addPnRequest = new PhoneCreateRequest("elysia0", "0812949483844");
        String jsonRequest = objectMapper.writeValueAsString(addPnRequest);

        String response = mockMvc.perform(post("/phone/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        PhoneDTO phoneNumberResp = objectMapper.readValue(response, PhoneDTO.class);
        assertEquals(addPnRequest.name(), phoneNumberResp.name());
        assertEquals(addPnRequest.number(), phoneNumberResp.number());
    }

    @Test
    @Order(2)
    public void testInvalidInsertPhone() throws Exception {
        PhoneCreateRequest addPnRequest = new PhoneCreateRequest("elysia1", "0812949483844");
        String jsonRequest = objectMapper.writeValueAsString(addPnRequest);

        mockMvc.perform(post("/phone/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(3)
    public void testUpdatePhone() throws Exception {
        PhoneUpdateRequest updateRequest = new PhoneUpdateRequest("elysia1", "0812949483843");
        String jsonRequest = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(patch("/phone/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    public void testUpdateNotFound() throws Exception {
        PhoneUpdateRequest phoneUpdateRequest = new PhoneUpdateRequest("elysia1", "0812949483843");
        String jsonRequest = objectMapper.writeValueAsString(phoneUpdateRequest);

        mockMvc.perform(patch("/phone/update/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(5)
    public void testGetById() throws Exception {
        String response = mockMvc.perform(get("/phone/get/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get("/phone/get/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    @Order(6)
    public void testDeletePhone() throws Exception {
        String response = mockMvc.perform(delete("/phone/delete/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(delete("/phone/delete/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }


}