package com.alejandro.app.auth;

import com.alejandro.app.auth.request.AuthenticationRequest;
import com.alejandro.app.auth.request.RefreshRequest;
import com.alejandro.app.auth.request.RegistrationRequest;
import com.alejandro.app.auth.response.AuthenticationResponse;

public interface AuthenticationService {

    AuthenticationResponse login(AuthenticationRequest request);

    void register(RegistrationRequest request);

    AuthenticationResponse refreshToken(RefreshRequest request);
}
