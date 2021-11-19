package main.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponse extends SimpleResultResponse{

    @JsonProperty("user")
    private UserLoginResponse userLoginResponse;


    public LoginResponse() {
    }

    public LoginResponse(UserLoginResponse userLoginResponse) {
        this.setResult(true);
        this.userLoginResponse = userLoginResponse;
    }


    public UserLoginResponse getUserLoginResponse() {
        return userLoginResponse;
    }

    public void setUserLoginResponse(UserLoginResponse userLoginResponse) {
        this.userLoginResponse = userLoginResponse;
    }
}
