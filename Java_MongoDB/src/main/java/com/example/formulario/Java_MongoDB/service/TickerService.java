package com.example.formulario.Java_MongoDB.service;

import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.formulario.Java_MongoDB.client.B3Client;
import com.example.formulario.Java_MongoDB.dto.TickerResponseDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TickerService {

    private static final Pattern TICKER_PATTERN = Pattern.compile("^[A-Za-z0-9]{4,10}$");

    private final B3Client b3Client;

    public TickerResponseDTO consultarTicker(String ticker) {
        String tickerNormalizado = normalizarTicker(ticker);
        String retornoBruto = b3Client.buscarDadosTicker(tickerNormalizado);

        return TickerResponseDTO.builder()
                .ticker(tickerNormalizado)
                .fonte("BMFBovespa")
                .retornoBruto(retornoBruto)
                .build();
    }

    private String normalizarTicker(String ticker) {
        if (ticker == null || ticker.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticker obrigatorio");
        }

        String normalizado = ticker.trim().toUpperCase(Locale.ROOT);
        if (!TICKER_PATTERN.matcher(normalizado).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ticker invalido. Use de 4 a 10 caracteres alfanumericos.");
        }

        return normalizado;
    }
}

