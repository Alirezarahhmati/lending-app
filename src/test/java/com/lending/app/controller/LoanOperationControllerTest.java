package com.lending.app.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lending.app.Application;
import com.lending.app.model.entity.Installment;
import com.lending.app.model.entity.Loan;
import com.lending.app.model.entity.LoanTransaction;
import com.lending.app.model.entity.User;
import com.lending.app.model.enums.Role;
import com.lending.app.model.record.base.BaseResponse;
import com.lending.app.model.record.loan.LoanApplicationCommand;
import com.lending.app.model.record.loan.LoanApplicationMessage;
import com.lending.app.model.record.loan.LoanInstallmentCommand;
import com.lending.app.repository.InstallmentRepository;
import com.lending.app.repository.LoanRepository;
import com.lending.app.repository.LoanTransactionRepository;
import com.lending.app.repository.UserRepository;
import com.lending.app.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class, properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@Import({NoOpCacheManager.class})
@DisplayName("LoanOperationController Integration Tests")
class LoanOperationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoanTransactionRepository loanTransactionRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private InstallmentRepository installmentRepository;

    private final String baseUrl = "/api/operation";
    private User borrower;
    private Loan loan;
    private LoanTransaction transaction;

    @BeforeEach
    void setup() {

        borrower = new User();
        borrower.setUsername("test");
        borrower.setPassword("password");
        borrower.setEmail("test@example.com");
        borrower.setScore(100);
        borrower.setRole(Role.USER);
        borrower = userRepository.save(borrower);

        loan = new Loan();
        loan.setAmount(1000);
        loan.setRequiredScore(50);
        loan.setEachInstallmentAmount(100);
        loan.setNumberOfInstallments(10);
        loan.setAwardScore(20);
        loan.setName("Test Loan");
        loan = loanRepository.save(loan);

        transaction = new LoanTransaction();
        transaction.setBorrower(borrower);
        transaction.setLoan(loan);
        transaction.setStartDate(LocalDateTime.now());
        transaction = loanTransactionRepository.save(transaction);

        Installment installment = new Installment();
        installment.setLoanTransaction(transaction);
        installment.setDueDate(LocalDateTime.now().plusDays(30));
        installment.setPaid(false);
        installmentRepository.save(installment);
    }

    @Nested
    @DisplayName("Loan Application Tests")
    class LoanApplicationTests {
        @Test
        @WithMockUser(username = "test")
        void shouldProcessLoanApplicationSuccessfully() throws Exception {
            LoanApplicationCommand command = new LoanApplicationCommand(loan.getId(), null);

            try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
                securityMock.when(SecurityUtils::getCurrentUserId).thenReturn(borrower.getId());

                MvcResult result = mockMvc.perform(post(baseUrl + "/loan")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(command)))
                        .andExpect(status().isOk())
                        .andReturn();

                BaseResponse<LoanApplicationMessage> response = objectMapper.readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {}
                );

                LoanApplicationMessage message = response.result();
                assertThat(message).isNotNull();
                assertThat(message.message()).isNotBlank();
            }
        }
    }

    @Nested
    @DisplayName("Loan Installment Tests")
    class LoanInstallmentTests {
        @Test
        @WithMockUser(username = "test")
        void shouldProcessLoanInstallmentSuccessfully() throws Exception {
            LoanInstallmentCommand command = new LoanInstallmentCommand(transaction.getId());

            try (MockedStatic<SecurityUtils> securityMock = mockStatic(SecurityUtils.class)) {
                securityMock.when(SecurityUtils::getCurrentUserId).thenReturn(borrower.getId());

                MvcResult result = mockMvc.perform(post(baseUrl + "/installment")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(command)))
                        .andExpect(status().isOk())
                        .andReturn();

                BaseResponse<LoanApplicationMessage> response = objectMapper.readValue(
                        result.getResponse().getContentAsString(),
                        new TypeReference<>() {}
                );

                LoanApplicationMessage message = response.result();
                assertThat(message).isNotNull();
                assertThat(message.message()).isNotBlank();
            }
        }
    }
}