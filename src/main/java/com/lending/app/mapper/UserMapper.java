package com.lending.app.mapper;

import com.lending.app.message.user.CreateUserCommand;
import com.lending.app.message.user.UpdateUserCommand;
import com.lending.app.message.user.UserMessage;
import com.lending.app.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    User toEntity(CreateUserCommand command);

    UserMessage toMessage(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void apply(UpdateUserCommand command, @MappingTarget User target);
}


