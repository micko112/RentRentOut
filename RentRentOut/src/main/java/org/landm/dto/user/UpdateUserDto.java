package org.landm.dto.user;

import org.springframework.stereotype.Component;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Component
public class UpdateUserDto {

	@NotBlank
    private String firstname;
	@NotBlank
    private String lastname;
	@NotBlank
	@Email
    private String email;
    
    public UpdateUserDto() {
    	
    }
    
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
    
    
	
}
