package com.lending.app.service;

import com.lending.app.application.service.impl.InstallmentServiceImpl;
import com.lending.app.exception.NotFoundException;
import com.lending.app.model.entity.Installment;
import com.lending.app.model.entity.LoanTransaction;
import com.lending.app.repository.InstallmentRepository;
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
@DisplayName("InstallmentService Tests")
class InstallmentServiceTest {

    @Mock
    private InstallmentRepository installmentRepository;

    @InjectMocks
    private InstallmentServiceImpl installmentService;

    private Installment installment;

    @BeforeEach
    void setUp() {
        LoanTransaction loanTransaction = new LoanTransaction();
        loanTransaction.setId("loan1");

        installment = new Installment();
        installment.setId("inst1");
        installment.setLoanTransaction(loanTransaction);
        installment.setDueDate(LocalDateTime.now().plusDays(10));
    }

    private void withSavedInstallment(Runnable action) {
        when(installmentRepository.save(any(Installment.class))).thenReturn(installment);
        when(installmentRepository.saveAndFlush(any(Installment.class))).thenReturn(installment);
        action.run();
        reset(installmentRepository);
    }

    private void withFoundInstallment(Runnable action) {
        when(installmentRepository.findLastByLoanTransactionId("loan1"))
                .thenReturn(Optional.of(installment));
        action.run();
        reset(installmentRepository);
    }

    private void withMissingInstallment(Runnable action) {
        when(installmentRepository.findLastByLoanTransactionId("loan1"))
                .thenReturn(Optional.empty());
        action.run();
        reset(installmentRepository);
    }

    @Nested
    @DisplayName("Save")
    class SaveTests {
        @Test
        @DisplayName("should save installment successfully")
        void shouldSaveInstallment() {
            withSavedInstallment(() -> {
                Installment saved = installmentService.save(installment);

                assertThat(saved).isEqualTo(installment);
                verify(installmentRepository).save(installment);
            });
        }
    }

    @Nested
    @DisplayName("SaveAndFlush")
    class SaveAndFlushTests {
        @Test
        @DisplayName("should save and flush installment successfully")
        void shouldSaveAndFlushInstallment() {
            withSavedInstallment(() -> {
                Installment saved = installmentService.saveAndFlush(installment);

                assertThat(saved).isEqualTo(installment);
                verify(installmentRepository).saveAndFlush(installment);
            });
        }
    }

    @Nested
    @DisplayName("Find Not Paid Installment By LoanTransactionId")
    class FindNotPaidInstallmentTests {
        @Test
        @DisplayName("should return last not paid installment successfully")
        void shouldReturnNotPaidInstallment() {
            withFoundInstallment(() -> {
                Installment res = installmentService.findNotPaidInstallmentByLoanTransactionId("loan1");
                assertThat(res).isEqualTo(installment);
            });
        }

        @Test
        @DisplayName("should throw NotFoundException when no installment found")
        void shouldThrowWhenNotFound() {
            withMissingInstallment(() -> {
                assertThatThrownBy(() ->
                        installmentService.findNotPaidInstallmentByLoanTransactionId("loan1"))
                        .isInstanceOf(NotFoundException.class)
                        .hasMessage("Installment not found");
            });
        }
    }
}
