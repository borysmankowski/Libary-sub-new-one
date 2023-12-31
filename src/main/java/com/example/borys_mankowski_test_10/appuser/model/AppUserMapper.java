package com.example.borys_mankowski_test_10.appuser.model;

import org.springframework.stereotype.Component;

@Component
public class AppUserMapper {

    public AppUser fromDto(CreateAppUserCommand command) {
        AppUser appUser = new AppUser();
        appUser.setFirstName(command.getFirstName());
        appUser.setLastName(command.getLastName());
        appUser.setEmail(command.getEmail());
        return appUser;
    }

    public AppUserDto toDTO(AppUser appUser) {
        AppUserDto dto = new AppUserDto();
        dto.setId(appUser.getId());
        dto.setFirstName(appUser.getFirstName());
        dto.setLastName(appUser.getLastName());
        dto.setEmail(appUser.getEmail());
        dto.setEnabled(appUser.isEnabled());
        return dto;
    }
}
