package com.elomonosov.lateness.controller;

import com.elomonosov.lateness.LatenessApplication;
import com.elomonosov.lateness.model.Debtor;
import com.elomonosov.lateness.model.Record;
import com.elomonosov.lateness.model.SlackResponse;
import com.elomonosov.lateness.repository.DebtorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = LatenessApplication.class)
@Disabled
class LatenessControllerTest {

    protected MockMvc mockMvc;
    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    DebtorRepository debtorRepository;

    private final String ROOT_ENDPOINT_URL = "/lateness";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        debtorRepository.deleteAll();
    }

    @Test
    void addDebt() throws Exception {
        String debtorName = "vasya";

        MvcResult mvcResult = mockMvc.perform(post(ROOT_ENDPOINT_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("team_domain", debtorName)
                .param("channel_id", debtorName)
                .param("channel_name", debtorName)
                .param("user_id", debtorName)
                .param("user_name", debtorName)
                .param("command", debtorName)
                .param("text", debtorName)
                .param("response_url", debtorName)
        )
                .andExpect(status().isOk())
                .andReturn();

        String jsonResult = mvcResult.getResponse().getContentAsString();
        System.out.println("Response: " + jsonResult);
        SlackResponse slackResponse1 = new ObjectMapper().readValue(jsonResult, SlackResponse.class);
        assertTrue(slackResponse1.getText().endsWith("500"));

        mvcResult = mockMvc.perform(post(ROOT_ENDPOINT_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("team_domain", debtorName)
                .param("channel_id", debtorName)
                .param("channel_name", debtorName)
                .param("user_id", debtorName)
                .param("user_name", debtorName)
                .param("command", debtorName)
                .param("text", debtorName)
                .param("response_url", debtorName)
        )
                .andExpect(status().isOk())
                .andReturn();

        jsonResult = mvcResult.getResponse().getContentAsString();
        System.out.println("Response: " + jsonResult);
        SlackResponse slackResponse2 = new ObjectMapper().readValue(jsonResult, SlackResponse.class);
        assertTrue(slackResponse2.getText().endsWith("1000"));
    }

    @Test
    void getBalance() throws Exception {
        String debtorName = "vasya";
        Debtor debtor = new Debtor(debtorName);
        debtor.addRecord(500, Record.Type.DEBT, debtorName);
        debtor.addRecord(500, Record.Type.DEBT, debtorName);
        debtor.addRecord(500, Record.Type.DEBT, debtorName);
        debtor.addRecord(500, Record.Type.PAYMENT, debtorName);
        debtorRepository.save(debtor);

        MvcResult mvcResult = mockMvc.perform(post(ROOT_ENDPOINT_URL + "/balance")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("team_domain", debtorName)
                .param("channel_id", debtorName)
                .param("channel_name", debtorName)
                .param("user_id", debtorName)
                .param("user_name", debtorName)
                .param("command", debtorName)
                .param("text", "@" + debtorName)
                .param("response_url", debtorName)
        )
                .andExpect(status().isOk())
                .andReturn();

        String jsonResult = mvcResult.getResponse().getContentAsString();
        System.out.println("Response: " + jsonResult);
        SlackResponse slackResponse1 = new ObjectMapper().readValue(jsonResult, SlackResponse.class);
        assertTrue(slackResponse1.getText().endsWith("1000"));

    }

    @Test
    void addAuthorizedPayment() throws Exception {
        String debtorName = "vasya";
        Debtor debtor = new Debtor(debtorName);
        debtor.addRecord(1000, Record.Type.DEBT, debtorName);
        debtorRepository.save(debtor);

        MvcResult mvcResult = mockMvc.perform(post(ROOT_ENDPOINT_URL + "/payment")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("team_domain", debtorName)
                .param("channel_id", debtorName)
                .param("channel_name", debtorName)
                .param("user_id", "UC7JD0PTK")
                .param("user_name", "tasha090287")
                .param("command", debtorName)
                .param("text", "@" + debtorName + " " + "500")
                .param("response_url", debtorName)
        )
                .andExpect(status().isOk())
                .andReturn();

        String jsonResult = mvcResult.getResponse().getContentAsString();
        System.out.println("Response: " + jsonResult);
        SlackResponse slackResponse1 = new ObjectMapper().readValue(jsonResult, SlackResponse.class);
        assertTrue(slackResponse1.getText().endsWith("500"));
    }

    @Test
    void addNotAuthorizedPayment() throws Exception {
        String debtorName = "vasya";
        Debtor debtor = new Debtor(debtorName);
        debtor.addRecord(1000, Record.Type.DEBT, debtorName);
        debtorRepository.save(debtor);

        MvcResult mvcResult = mockMvc.perform(post(ROOT_ENDPOINT_URL + "/payment")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("team_domain", debtorName)
                .param("channel_id", debtorName)
                .param("channel_name", debtorName)
                .param("user_id", debtorName)
                .param("user_name", debtorName)
                .param("command", debtorName)
                .param("text", "@" + debtorName + " " + "500")
                .param("response_url", debtorName)
        )
                .andExpect(status().isOk())
                .andReturn();

        String jsonResult = mvcResult.getResponse().getContentAsString();
        System.out.println("Response: " + jsonResult);
        SlackResponse slackResponse1 = new ObjectMapper().readValue(jsonResult, SlackResponse.class);
        assertEquals("Not authorized", slackResponse1.getText());
    }

    @Test
    void getRating() throws Exception {
        String debtor1Name = "vasya";
        Debtor debtor1 = new Debtor(debtor1Name);
        debtor1.addRecord(1000, Record.Type.DEBT, debtor1Name);
        debtorRepository.save(debtor1);

        String debtor2Name = "petya";
        Debtor debtor2 = new Debtor(debtor2Name);
        debtor2.addRecord(500, Record.Type.DEBT, debtor2Name);
        debtorRepository.save(debtor2);

        String debtor3Name = "kolya";
        Debtor debtor3 = new Debtor(debtor3Name);
        debtor3.addRecord(2000, Record.Type.DEBT, debtor2Name);
        debtorRepository.save(debtor3);

        MvcResult mvcResult = mockMvc.perform(post(ROOT_ENDPOINT_URL + "/rating")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .param("team_domain", "")
                .param("channel_id", "")
                .param("channel_name", "")
                .param("user_id", "")
                .param("user_name", "")
                .param("command", "")
                .param("text", "")
                .param("response_url", "")
        )
                .andExpect(status().isOk())
                .andReturn();

        String jsonResult = mvcResult.getResponse().getContentAsString();
        System.out.println("Response: " + jsonResult);
        SlackResponse slackResponse1 = new ObjectMapper().readValue(jsonResult, SlackResponse.class);
        assertTrue(slackResponse1.getText().contains("kolya=2000"));
        assertTrue(slackResponse1.getText().contains("vasya=1000"));
        assertTrue(slackResponse1.getText().contains("petya=500"));
    }

}