package com.lending.app.mapper;

import com.lending.app.model.entity.LoanTransaction;
import com.lending.app.model.record.loan.UserLoanTransactionMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LoanTransactionMapper {

    @Mapping(source = "id", target = "loanTransactionId")
    @Mapping(source = "loan", target = "loan")
    @Mapping(source = "paidAmount", target = "paidAmount")
    @Mapping(source = "startDate", target = "startDate")
    UserLoanTransactionMessage toMessage(LoanTransaction loanTransaction);
} 