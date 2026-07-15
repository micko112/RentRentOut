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
        dto.setAddress(user.getAddress());
        dto.setDescription(user.getDescription());
        if (user.getRole() != null) {
            dto.setRole(user.getRole().getName());
        }
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        if (user.getLocation() != null) {
            dto.setLocationId(user.getLocation().getId());
            String city = user.getLocation().getCity();
            String muni = user.getLocation().getMunicipality();
            boolean showMuni = muni != null && !muni.isBlank() && !muni.equals(city);
            dto.setLocationDisplay(showMuni ? city + ", " + muni : city);
        }
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
        dto.setAvatarUrl(user.getAvatarUrl());

        if (user.getLocation() != null) {
            String city = user.getLocation().getCity();
            String muni = user.getLocation().getMunicipality();
            boolean showMuni = muni != null && !muni.isBlank() && !muni.equals(city);
            dto.setLocationDisplay(showMuni ? city + ", " + muni : city);
        } else {
            dto.setLocationDisplay(null);
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
        if (user == null) return null;
        UserShortDto dto = new UserShortDto();
        dto.setId(user.getId());
        dto.setDisplayName(user.getFirstname() + " " + user.getLastname());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setIdentified(user.isIdentified());

        if (user.getLocation() != null) {
            String city = user.getLocation().getCity();
            String muni = user.getLocation().getMunicipality();
            boolean showMuni = muni != null && !muni.isBlank() && !muni.equals(city);
            dto.setLocationDisplay(showMuni ? city + ", " + muni : city);
        } else {
            dto.setLocationDisplay(null);
        }
        dto.setCreatedAt(user.getCreatedAt());
        dto.setPositiveReviews(user.getPositiveReviews());
        dto.setNegativeReviews(user.getNegativeReviews());
        dto.setPhoneNumber(user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank()
                ? "06x / xxx-xxxx" : null);
        dto.setDescription(user.getDescription());

        return dto;
    }

}
