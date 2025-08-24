package com.lending.app.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lending.app.Application;
import com.lending.app.model.entity.Loan;
import com.lending.app.model.entity.LoanTransaction;
import com.lending.app.model.entity.User;
import com.lending.app.model.enums.Role;
import com.lending.app.model.record.base.BaseResponse;
import com.lending.app.model.record.loan.UserLoanTransactionMessage;
import com.lending.app.repository.LoanRepository;
import com.lending.app.repository.LoanTransactionRepository;
import com.lending.app.repository.UserRepository;
import com.lending.app.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class, properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@Import({NoOpCacheManager.class})
@DisplayName("LoanTransactionController Integration Tests")
class LoanTransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private LoanTransactionRepository loanTransactionRepository;

    private String testUserId;
    private String testLoanTransactionId;

    @BeforeEach
    void setup() {
        // Setup user
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encoded_password"); // PasswordEncoder is not used here directly
        user.setEmail("testuser@example.com");
        user.setScore(100);
        user.setRole(Role.USER);
        user = userRepository.save(user);
        testUserId = user.getId();

        // Setup loan
        Loan loan = new Loan();
        loan.setName("Test Loan");
        loan.setAmount(10000);
        loan.setNumberOfInstallments(10);
        loan.setEachInstallmentAmount(1000);
        loan.setRequiredScore(50);
        loan.setAwardScore(10);
        loan = loanRepository.save(loan);

        // Setup loan transaction
        LoanTransaction loanTransaction = new LoanTransaction();
        loanTransaction.setBorrower(user);
        loanTransaction.setLoan(loan);
        loanTransaction.setPaidAmount(0);
        loanTransaction.setStartDate(LocalDateTime.now());
        loanTransaction = loanTransactionRepository.save(loanTransaction);
        testLoanTransactionId = loanTransaction.getId();
    }

    @Nested
    @DisplayName("Get User Loan Transactions Tests")
    class GetUserLoanTransactionsTests {

        @Test
        @WithMockUser(username = "testuser")
        void shouldGetUserLoanTransactions() throws Exception {
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn(testUserId);

                MvcResult result = mockMvc.perform(get("/api/loan-transactions/my-loans"))
                        .andExpect(status().isOk())
                        .andReturn();

                BaseResponse<List<UserLoanTransactionMessage>> response = objectMapper.readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {}
                );

                List<UserLoanTransactionMessage> transactions = response.result();
                assertThat(transactions).isNotNull().hasSize(1);
                assertThat(transactions.getFirst().loanTransactionId()).isEqualTo(testLoanTransactionId);
                assertThat(transactions.getFirst().loan().getName()).isEqualTo("Test Loan");
                assertThat(transactions.getFirst().paidAmount()).isEqualTo(0);
                assertThat(transactions.getFirst().startDate()).isNotNull();
            }
        }

        @Test
        void shouldReturnUnauthorizedForGetWithoutAuthentication() throws Exception {
            mockMvc.perform(get("/api/loan-transactions/my-loans"))
                    .andExpect(status().isUnauthorized());
        }

    }
} 