package com.lending.app.mapper;

import com.lending.app.model.record.user.CreateUserCommand;
import com.lending.app.model.record.user.UpdateUserCommand;
import com.lending.app.model.record.user.UserMessage;
import com.lending.app.model.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    User toEntity(CreateUserCommand command);

    UserMessage toMessage(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void apply(UpdateUserCommand command, @MappingTarget User target);
}


