package com.lending.app.mapper;

import com.lending.app.model.entity.Loan;
import com.lending.app.model.record.loan.LoanMessage;
import com.lending.app.model.record.loan.SaveLoanCommand;
import com.lending.app.model.record.loan.UpdateLoanCommand;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LoanMapper {

    @Mapping(target = "id", ignore = true)
    Loan toEntity(SaveLoanCommand command);

    LoanMessage toMessage(Loan loan);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void apply(UpdateLoanCommand command, @MappingTarget Loan target);
}



