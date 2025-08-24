package com.lending.app.service;

import com.lending.app.application.service.impl.LoanServiceImpl;
import com.lending.app.exception.NotFoundException;
import com.lending.app.mapper.LoanMapper;
import com.lending.app.model.entity.Loan;
import com.lending.app.model.record.loan.LoanMessage;
import com.lending.app.model.record.loan.SaveLoanCommand;
import com.lending.app.model.record.loan.UpdateLoanCommand;
import com.lending.app.repository.LoanRepository;
import com.lending.app.util.CalculatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoanService Tests")
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private LoanMapper loanMapper;

    @InjectMocks
    private LoanServiceImpl loanService;

    private Loan loan;
    private LoanMessage loanMessage;

    @BeforeEach
    void setUp() {
        loan = new Loan();
        loan.setId("loan1");

        loanMessage = new LoanMessage("loan1", "Loan 1", 1000, 10, 0, 0);
    }

    private void withLoan(Runnable action) {
        when(loanRepository.findById("loan1")).thenReturn(Optional.of(loan));
        action.run();
    }

    @Nested
    @DisplayName("Save Loan")
    class SaveLoanTests {
        @Test
        void shouldSaveLoanSuccessfully() {
            SaveLoanCommand command = new SaveLoanCommand("Loan 1", 1000, 10, 0, 0);
            when(loanMapper.toEntity(command)).thenReturn(loan);
            when(loanRepository.save(loan)).thenReturn(loan);
            when(loanMapper.toMessage(loan)).thenReturn(loanMessage);

            try (MockedStatic<CalculatorUtils> mockedStatic = mockStatic(CalculatorUtils.class)) {
                mockedStatic.when(() -> CalculatorUtils.calculateEachInstallmentAmount(1000, 10)).thenReturn(100L);

                LoanMessage res = loanService.save(command);

                assertThat(res).isEqualTo(loanMessage);
                verify(loanRepository).save(loan);
                verify(loanMapper).toMessage(loan);
            }
        }
    }

    @Nested
    @DisplayName("Update Loan")
    class UpdateLoanTests {
        @Test
        void shouldUpdateLoanSuccessfully() {
            UpdateLoanCommand command = new UpdateLoanCommand("loan1", 1L, "Loan 1", 1000L, 10, 0, 0);
            withLoan(() -> {
                doNothing().when(loanMapper).apply(command, loan);
                when(loanRepository.save(loan)).thenReturn(loan);
                when(loanMapper.toMessage(loan)).thenReturn(loanMessage);

                LoanMessage res = loanService.update(command);

                assertThat(res).isEqualTo(loanMessage);
                verify(loanMapper).apply(command, loan);
                verify(loanRepository).save(loan);
            });
        }

        @Test
        void shouldThrowWhenLoanNotFound() {
            UpdateLoanCommand command = new UpdateLoanCommand("loan1", 1L, "Loan 1", 1000L, 10, 0, 0);
            when(loanRepository.findById("loan1")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> loanService.update(command))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Loan not found");
        }
    }

    @Nested
    @DisplayName("Delete Loan")
    class DeleteLoanTests {
        @Test
        void shouldDeleteLoanSuccessfully() {
            when(loanRepository.existsById("loan1")).thenReturn(true);

            loanService.delete("loan1");

            verify(loanRepository).softDeleteById("loan1");
        }

        @Test
        void shouldThrowWhenLoanNotFound() {
            when(loanRepository.existsById("loan1")).thenReturn(false);

            assertThatThrownBy(() -> loanService.delete("loan1"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Loan not found");
        }
    }

    @Nested
    @DisplayName("Get Loan")
    class GetLoanTests {
        @Test
        void shouldReturnLoanMessageSuccessfully() {
            withLoan(() -> {
                when(loanMapper.toMessage(loan)).thenReturn(loanMessage);

                LoanMessage res = loanService.get("loan1");

                assertThat(res).isEqualTo(loanMessage);
            });
        }

        @Test
        void shouldThrowWhenLoanNotFound() {
            when(loanRepository.findById("loan1")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> loanService.get("loan1"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Loan not found");
        }
    }

    @Nested
    @DisplayName("Get Loan Entity")
    class GetLoanEntityTests {
        @Test
        void shouldReturnLoanSuccessfully() {
            withLoan(() -> {
                Loan res = loanService.getLoan("loan1");

                assertThat(res).isEqualTo(loan);
            });
        }

        @Test
        void shouldThrowWhenLoanNotFound() {
            when(loanRepository.findById("loan1")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> loanService.getLoan("loan1"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Loan not found");
        }
    }

    @Nested
    @DisplayName("Get All Loans")
    class GetAllLoansTests {
        @Test
        void shouldReturnAllLoans() {
            when(loanRepository.findAll()).thenReturn(List.of(loan));
            when(loanMapper.toMessage(loan)).thenReturn(loanMessage);

            List<LoanMessage> res = loanService.getAll();

            assertThat(res).containsExactly(loanMessage);
        }

        @Test
        void shouldReturnEmptyListWhenNoLoans() {
            when(loanRepository.findAll()).thenReturn(List.of());

            List<LoanMessage> res = loanService.getAll();

            assertThat(res).isEmpty();
        }
    }
}