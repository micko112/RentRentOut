package org.landm.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangeUserPasswordDto {

	@NotBlank
	private String oldPassword;
	@NotBlank
	@Size(min = 6)
	private String newPassword;
	
	public ChangeUserPasswordDto() {
		
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	
	
	
}
