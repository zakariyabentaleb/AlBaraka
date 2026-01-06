package ma.albaraka.bank.service;

import ma.albaraka.bank.dto.request.LoginRequest;
import ma.albaraka.bank.dto.request.RegisterRequest;
import ma.albaraka.bank.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);
}
