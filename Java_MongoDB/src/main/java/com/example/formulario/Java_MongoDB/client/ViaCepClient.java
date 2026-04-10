package com.example.formulario.Java_MongoDB.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.example.formulario.Java_MongoDB.dto.ViaCepResponseDTO;

@Component
public class ViaCepClient {

    private final RestClient restClient;

    public ViaCepClient(RestClient.Builder restClientBuilder,
            @Value("${viacep.base-url:https://viacep.com.br/ws}") String viaCepBaseUrl) {
        this.restClient = restClientBuilder.baseUrl(viaCepBaseUrl).build();
    }

    public ViaCepResponseDTO buscarEnderecoPorCep(String cep) {
        try {
            ViaCepResponseDTO response = restClient.get()
                    .uri("/{cep}/json/", cep)
                    .retrieve()
                    .body(ViaCepResponseDTO.class);

            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Resposta vazia ao consultar o ViaCEP");
            }

            return response;
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Falha ao consultar o servico ViaCEP", ex);
        }
    }
}

