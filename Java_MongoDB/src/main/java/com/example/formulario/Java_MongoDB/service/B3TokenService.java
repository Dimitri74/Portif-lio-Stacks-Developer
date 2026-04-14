package com.example.formulario.Java_MongoDB.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.formulario.Java_MongoDB.client.B3AuthClient;
import com.example.formulario.Java_MongoDB.dto.B3TokenResponseDTO;

@Service
public class B3TokenService {

    private final B3AuthClient b3AuthClient;
    private final String clientId;
    private final String clientSecret;
    private final String grantType;

    private String cachedToken;
    private Instant tokenExpiresAt;

    public B3TokenService(B3AuthClient b3AuthClient,
            @Value("${b3.oauth.client-id:}") String clientId,
            @Value("${b3.oauth.client-secret:}") String clientSecret,
            @Value("${b3.oauth.grant-type:client_credentials}") String grantType) {
        this.b3AuthClient = b3AuthClient;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.grantType = grantType;
    }

    public synchronized String obterTokenAcesso() {
        if (tokenValido()) {
            return cachedToken;
        }

        validarCredenciais();
        B3TokenResponseDTO tokenResponse = b3AuthClient.solicitarToken(clientId, clientSecret, grantType);

        long expiresIn = tokenResponse.getExpiresIn() != null && tokenResponse.getExpiresIn() > 0
                ? tokenResponse.getExpiresIn()
                : 300L;

        this.cachedToken = tokenResponse.getAccessToken();
        this.tokenExpiresAt = Instant.now().plusSeconds(Math.max(1L, expiresIn - 30L));
        return cachedToken;
    }

    private boolean tokenValido() {
        return cachedToken != null && tokenExpiresAt != null && Instant.now().isBefore(tokenExpiresAt);
    }

    private void validarCredenciais() {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Credenciais OAuth da B3 nao configuradas. Preencha b3.oauth.client-id e b3.oauth.client-secret.");
        }
    }
}

