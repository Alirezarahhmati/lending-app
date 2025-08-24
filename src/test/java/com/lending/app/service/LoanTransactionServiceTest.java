package com.lending.app.service;

import com.lending.app.application.service.impl.LoanTransactionServiceImpl;
import com.lending.app.exception.NotFoundException;
import com.lending.app.mapper.LoanTransactionMapper;
import com.lending.app.model.entity.Loan;
import com.lending.app.model.entity.LoanTransaction;
import com.lending.app.model.entity.User;
import com.lending.app.model.record.loan.UserLoanTransactionMessage;
import com.lending.app.repository.LoanTransactionRepository;
import com.lending.app.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoanTransactionService Tests")
class LoanTransactionServiceTest {

    @Mock
    private LoanTransactionRepository repository;

    @Mock
    private LoanTransactionMapper loanTransactionMapper;

    @InjectMocks
    private LoanTransactionServiceImpl service;

    private LoanTransaction transaction;

    @BeforeEach
    void setUp() {
        User borrower = new User();
        borrower.setId("user1");
        Loan loan = new Loan();
        loan.setId("loan1");

        transaction = new LoanTransaction();
        transaction.setId("tx1");
        transaction.setBorrower(borrower);
        transaction.setLoan(loan);
        transaction.setStartDate(LocalDateTime.now());
        transaction.setPaidAmount(500);
    }

    private void withTransaction(Runnable action) {
        when(repository.findById("tx1")).thenReturn(Optional.of(transaction));
        action.run();
    }

    @Nested
    @DisplayName("Save and Flush")
    class SaveAndFlushTests {
        @Test
        void shouldSaveAndFlushTransaction() {
            when(repository.saveAndFlush(transaction)).thenReturn(transaction);

            service.saveAndFlush(transaction);

            verify(repository).saveAndFlush(transaction);
        }
    }

    @Nested
    @DisplayName("Find By Id")
    class FindByIdTests {

        @Test
        void shouldReturnTransactionSuccessfully() {
            withTransaction(() -> {
                LoanTransaction res = service.findById("tx1");
                assertThat(res).isEqualTo(transaction);
            });
        }

        @Test
        void shouldThrowWhenTransactionNotFound() {
            when(repository.findById("tx1")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById("tx1"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("LoanTransaction not found");
        }
    }

    @Nested
    @DisplayName("Find User Loan Transactions By User ID")
    class FindUserLoanTransactionsByUserIdTests {

        @Test
        void shouldReturnUserLoanTransactions() {
            String userId = "user1";
            UserLoanTransactionMessage expectedMessage = new UserLoanTransactionMessage(
                    "tx1",
                    transaction.getLoan(),
                    transaction.getPaidAmount(),
                    transaction.getStartDate(),
                    transaction.getEndDate()
            );

            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);
                when(repository.findByUserId(userId)).thenReturn(Collections.singletonList(transaction));
                when(loanTransactionMapper.toMessage(transaction)).thenReturn(expectedMessage);

                List<UserLoanTransactionMessage> result = service.findUserLoanTransactionsByUserId();

                assertThat(result).isNotNull().hasSize(1);
                assertThat(result.getFirst().loanTransactionId()).isEqualTo(expectedMessage.loanTransactionId());
                assertThat(result.getFirst().loan()).isEqualTo(expectedMessage.loan());
                assertThat(result.getFirst().paidAmount()).isEqualTo(expectedMessage.paidAmount());
                assertThat(result.getFirst().startDate()).isEqualTo(expectedMessage.startDate());
                verify(repository, times(1)).findByUserId(userId);
                verify(loanTransactionMapper, times(1)).toMessage(transaction);
            }
        }

        @Test
        void shouldReturnEmptyListWhenNoTransactions() {
            String userId = "user1";
            try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
                mocked.when(SecurityUtils::getCurrentUserId).thenReturn(userId);
                when(repository.findByUserId(userId)).thenReturn(Collections.emptyList());

                List<UserLoanTransactionMessage> result = service.findUserLoanTransactionsByUserId();

                assertThat(result).isNotNull().isEmpty();
                verify(repository, times(1)).findByUserId(userId);
                verifyNoInteractions(loanTransactionMapper);
            }
        }
    }
}