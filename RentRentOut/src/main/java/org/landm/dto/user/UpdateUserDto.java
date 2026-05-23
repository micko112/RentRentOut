package org.landm.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UpdateUserDto {

    @Size(min = 1, max = 100)
    private String firstname;
    @Size(min = 1, max = 100)
    private String lastname;
	@Email
    private String email;
	private String currency;
	@Size(max = 5000)
	private String description;
	@Size(max = 30)
	private String phoneNumber;
	@Size(max = 255)
	private String address;
	@Size(max = 2000)
	private String avatarUrl;

	private Long locationId;

    public UpdateUserDto() {}

	public Long getLocationId() { return locationId; }
	public void setLocationId(Long locationId) { this.locationId = locationId; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public String getPhoneNumber() { return phoneNumber; }
	public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }

	public String getAvatarUrl() { return avatarUrl; }
	public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstName) {
		this.firstname = firstName;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
}
