package com.lending.app.service;

import com.lending.app.application.service.impl.LoanTransactionServiceImpl;
import com.lending.app.exception.NotFoundException;
import com.lending.app.model.entity.Loan;
import com.lending.app.model.entity.LoanTransaction;
import com.lending.app.model.entity.User;
import com.lending.app.repository.LoanTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoanTransactionService Tests")
class LoanTransactionServiceTest {

    @Mock
    private LoanTransactionRepository repository;

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
}