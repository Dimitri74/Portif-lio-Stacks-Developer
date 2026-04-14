package com.example.formulario.Java_MongoDB.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.example.formulario.Java_MongoDB.dto.B3TokenResponseDTO;

@Component
public class B3AuthClient {

    private final RestClient restClient;
    private final String tokenPath;

    public B3AuthClient(RestClient.Builder restClientBuilder,
            @Value("${b3.oauth.base-url:}") String oauthBaseUrl,
            @Value("${b3.oauth.token-path:/oauth/token}") String tokenPath) {
        this.restClient = restClientBuilder.baseUrl(oauthBaseUrl).build();
        this.tokenPath = tokenPath;
    }

    public B3TokenResponseDTO solicitarToken(String clientId, String clientSecret, String grantType) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("grant_type", grantType);

        try {
            B3TokenResponseDTO response = restClient.post()
                    .uri(tokenPath)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(B3TokenResponseDTO.class);

            if (response == null || response.getAccessToken() == null || response.getAccessToken().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Token OAuth da B3 nao retornado pela fonte externa");
            }

            return response;
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Falha ao autenticar na B3 (OAuth Client Credentials)", ex);
        }
    }
}

