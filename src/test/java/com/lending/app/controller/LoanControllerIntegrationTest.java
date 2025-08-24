package com.lending.app.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lending.app.Application;
import com.lending.app.model.record.base.BaseResponse;
import com.lending.app.model.record.loan.LoanMessage;
import com.lending.app.model.record.loan.LoanMessageSet;
import com.lending.app.model.record.loan.SaveLoanCommand;
import com.lending.app.model.record.loan.UpdateLoanCommand;
import com.lending.app.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class, properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@Import({NoOpCacheManager.class})
@DisplayName("LoanController Integration Tests")
public class LoanControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    LoanRepository loanRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String savedLoanId;

    private final String baseUrl = "/api/loans";

    @BeforeEach
    @WithMockUser(username = "test", roles = "ADMIN")
    void setupLoan() throws Exception {
        loanRepository.deleteAll();
        SaveLoanCommand saveCommand = new SaveLoanCommand("Test Loan Setup", 1500, 12, 0, 0);
        MvcResult saveResult = mockMvc.perform(post(baseUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveCommand)))
                .andExpect(status().isOk())
                .andReturn();

        BaseResponse<LoanMessage> saveResponse = objectMapper.readValue(
                saveResult.getResponse().getContentAsString(),
                new TypeReference<>() {}
        );

        savedLoanId = saveResponse.result().id();
    }

    @Nested
    @DisplayName("Save Loan Tests")
    class SaveLoan {
        @Test
        @WithMockUser(username = "test", roles = "ADMIN")
        void testSaveLoan() throws Exception {
            SaveLoanCommand saveCommand = new SaveLoanCommand("New Loan", 1000, 10, 0, 0);

            MvcResult saveResult = mockMvc.perform(post(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(saveCommand)))
                    .andExpect(status().isOk())
                    .andReturn();

            BaseResponse<LoanMessage> response = objectMapper.readValue(
                    saveResult.getResponse().getContentAsString(),
                    new TypeReference<>() {}
            );

            LoanMessage loan = response.result();
            assertThat(loan).isNotNull();
            assertThat(loan.name()).isEqualTo("New Loan");
        }
    }

    @Nested
    @DisplayName("Get Loan Tests")
    class GetLoan {
        @Test
        @WithMockUser(username = "test", roles = "ADMIN")
        void testGetLoan() throws Exception {
            MvcResult getResult = mockMvc.perform(get(baseUrl + "/" + savedLoanId))
                    .andExpect(status().isOk())
                    .andReturn();

            BaseResponse<LoanMessage> getResponse = objectMapper.readValue(
                    getResult.getResponse().getContentAsString(),
                    new TypeReference<>() {}
            );

            LoanMessage loan = getResponse.result();
            assertThat(loan).isNotNull();
            assertThat(loan.id()).isEqualTo(savedLoanId);
        }
    }

    @Nested
    @DisplayName("Update Loan Tests")
    class UpdateLoan {
        @Test
        @WithMockUser(username = "test", roles = "ADMIN")
        void testUpdateLoan() throws Exception {
            UpdateLoanCommand updateCommand = new UpdateLoanCommand(savedLoanId, 0L, "Updated Loan", 3000L, 30, 10, 20);

            MvcResult updateResult = mockMvc.perform(put(baseUrl)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateCommand)))
                    .andExpect(status().isOk())
                    .andReturn();

            BaseResponse<LoanMessage> updateResponse = objectMapper.readValue(
                    updateResult.getResponse().getContentAsString(),
                    new TypeReference<>() {}
            );

            LoanMessage updatedLoan = updateResponse.result();
            assertThat(updatedLoan).isNotNull();
            assertThat(updatedLoan.name()).isEqualTo("Updated Loan");
            assertThat(updatedLoan.amount()).isEqualTo(3000);
        }
    }

    @Nested
    @DisplayName("Delete Loan Tests")
    class DeleteLoan {
        @Test
        @WithMockUser(username = "test", roles = "ADMIN")
        void testDeleteLoan() throws Exception {
            mockMvc.perform(delete(baseUrl + "/" + savedLoanId))
                    .andExpect(status().isOk());

            mockMvc.perform(get(baseUrl + "/" + savedLoanId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Get All Loans Tests")
    class GetAllLoans {
        @Test
        @WithMockUser(username = "test", roles = "ADMIN")
        void testGetAllLoans() throws Exception {
            MvcResult getResult = mockMvc.perform(get(baseUrl))
                    .andExpect(status().isOk())
                    .andReturn();

            BaseResponse<LoanMessageSet> getResponse = objectMapper.readValue(
                    getResult.getResponse().getContentAsString(),
                    new TypeReference<>() {}
            );

            LoanMessageSet loans = getResponse.result();
            assertThat(loans).isNotNull();
        }
    }
}