package com.soundhub.api.util.mappers;

import com.soundhub.api.dto.UserDto;
import com.soundhub.api.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    void updateUserFromDto(UserDto userDto, @MappingTarget User entity);

    @Mapping(source = "online", target = "online")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    UserDto userToUserDto(User user);

    @Mapping(target = "role", ignore = true)
    User userDtoToUser(UserDto userDto);
}
