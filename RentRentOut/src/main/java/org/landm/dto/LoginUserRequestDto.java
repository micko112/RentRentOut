package org.landm.dto;

public class LoginUserRequestDto {

    private String email;
    private String password;

    public LoginUserRequestDto(){
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
