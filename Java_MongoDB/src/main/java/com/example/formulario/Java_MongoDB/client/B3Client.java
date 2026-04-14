package com.example.formulario.Java_MongoDB.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class B3Client {

    private final RestClient restClient;

    public B3Client(RestClient.Builder restClientBuilder,
            @Value("${b3.base-url:http://www.bmfbovespa.com.br/Pregao-OnLine/ExecutaAcaoCarregarDados.asp}") String baseUrl) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public String buscarDadosTicker(String ticker) {
        try {
            String response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.queryParam("CodDado", ticker).build())
                    .retrieve()
                    .body(String.class);

            if (response == null || response.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "Resposta vazia ao consultar o endpoint de acoes");
            }

            return response;
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Falha ao consultar o endpoint de acoes", ex);
        }
    }
}

