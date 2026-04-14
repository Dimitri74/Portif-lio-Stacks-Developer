package com.example.formulario.Java_MongoDB.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.formulario.Java_MongoDB.client.B3AuthClient;
import com.example.formulario.Java_MongoDB.dto.B3TokenResponseDTO;

@ExtendWith(MockitoExtension.class)
class B3TokenServiceTest {

    @Mock
    private B3AuthClient b3AuthClient;

    @Test
    void deveBuscarTokenQuandoNaoExisteTokenEmCache() {
        B3TokenService tokenService = new B3TokenService(b3AuthClient, "client-id", "client-secret", "client_credentials");

        when(b3AuthClient.solicitarToken("client-id", "client-secret", "client_credentials"))
                .thenReturn(B3TokenResponseDTO.builder().accessToken("token-abc").expiresIn(300L).build());

        String token = tokenService.obterTokenAcesso();

        assertEquals("token-abc", token);
        verify(b3AuthClient, times(1)).solicitarToken("client-id", "client-secret", "client_credentials");
    }

    @Test
    void deveReutilizarTokenDoCacheEnquantoValido() {
        B3TokenService tokenService = new B3TokenService(b3AuthClient, "client-id", "client-secret", "client_credentials");

        when(b3AuthClient.solicitarToken("client-id", "client-secret", "client_credentials"))
                .thenReturn(B3TokenResponseDTO.builder().accessToken("token-abc").expiresIn(300L).build());

        String primeiro = tokenService.obterTokenAcesso();
        String segundo = tokenService.obterTokenAcesso();

        assertEquals("token-abc", primeiro);
        assertEquals("token-abc", segundo);
        verify(b3AuthClient, times(1)).solicitarToken("client-id", "client-secret", "client_credentials");
    }

    @Test
    void deveLancarErroQuandoCredenciaisNaoForemConfiguradas() {
        B3TokenService tokenService = new B3TokenService(b3AuthClient, "", "", "client_credentials");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, tokenService::obterTokenAcesso);

        assertEquals(HttpStatus.BAD_GATEWAY, ex.getStatusCode());
        assertEquals("Credenciais OAuth da B3 nao configuradas. Preencha b3.oauth.client-id e b3.oauth.client-secret.",
                ex.getReason());
    }
}

