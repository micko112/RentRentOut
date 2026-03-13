package org.landm.mapper;


import org.landm.dto.user.UserDto;
import org.landm.dto.user.UpdateUserDto;
import org.landm.dto.user.UserProfileDto;
import org.landm.dto.user.UserShortDto;
import org.landm.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) return null;

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFirstname(user.getFirstname());
        dto.setLastname(user.getLastname());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setIdentified(user.isIdentified());
        dto.setEmail(user.getEmail());
        dto.setCredit(user.getCredit());
        dto.setCurrency(user.getCurrency() != null ? user.getCurrency().toString() : "RSD");
        dto.setPositiveReviews(user.getPositiveReviews());
        dto.setNegativeReviews(user.getNegativeReviews());
        dto.setPhoneNumber(user.getPhoneNumber());
        return dto;
    }


    public UpdateUserDto toEditDto(User user) {
        if (user == null) return null;

        UpdateUserDto editDto = new UpdateUserDto();
        editDto.setFirstname(user.getFirstname());
        editDto.setLastname(user.getLastname());
        editDto.setEmail(user.getEmail());
        editDto.setCurrency(user.getCurrency() != null ? user.getCurrency().toString() : "RSD");
        return editDto;
    }

    public UserProfileDto toUserProfileDto(User user) {
        if (user == null) return null;

        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setDisplayName(user.getFirstname() + " " + user.getLastname());
        dto.setDescription(user.getDescription());
        dto.setAvatarUrl(user.getAvatarUrl() != null ? user.getAvatarUrl() : "assets/default-avatar.png");

        if (user.getLocation() != null) {
            dto.setLocationDisplay(user.getLocation().getCity() + ", " + user.getLocation().getMunicipality());
        } else {
            dto.setLocationDisplay("Nepoznata lokacija");
        }

        dto.setCreatedAt(user.getCreatedAt());
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()) {
            dto.setPhoneNumber("06x / xxx-xxxx");
        } else{
            dto.setPhoneNumber(null);
    }
        dto.setPositiveReviews(user.getPositiveReviews());
        dto.setNegativeReviews(user.getNegativeReviews());

        return dto;
    }


    public UserShortDto toUserShortDto(User user){
        UserShortDto dto = new UserShortDto();
        dto.setId(user.getId());
        dto.setDisplayName(user.getFirstname() + " " + user.getLastname());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setIdentified(user.isIdentified());

        if (user.getLocation() != null) {
            dto.setLocationDisplay(user.getLocation().getCity() + ", " + user.getLocation().getMunicipality());
        } else {
            dto.setLocationDisplay("Nepoznata lokacija");
        }
        dto.setCreatedAt(user.getCreatedAt());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setPositiveReviews(user.getPositiveReviews());
        dto.setNegativeReviews(user.getNegativeReviews());
        dto.setCreatedAt(user.getCreatedAt());

        return dto;
    }

}
