package com.example.formulario.Java_MongoDB.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.example.formulario.Java_MongoDB.dto.BrapiResponseDTO;

@Component
public class BrapiClient {

    private final RestClient restClient;

    public BrapiClient(RestClient.Builder restClientBuilder,
            @Value("${brapi.base-url:https://brapi.dev/api}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public BrapiResponseDTO consultarTickers(String tickers) {
        try {
            BrapiResponseDTO response = restClient.get()
                    .uri("/quote/{tickers}", tickers)
                    .retrieve()
                    .body(BrapiResponseDTO.class);

            if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Nenhum ticker encontrado na Brapi");
            }

            return response;
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Falha ao consultar tickers na Brapi", ex);
        }
    }
}

