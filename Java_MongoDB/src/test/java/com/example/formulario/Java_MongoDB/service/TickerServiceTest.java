package com.example.formulario.Java_MongoDB.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.formulario.Java_MongoDB.client.B3Client;
import com.example.formulario.Java_MongoDB.dto.TickerResponseDTO;

@ExtendWith(MockitoExtension.class)
class TickerServiceTest {

    @Mock
    private B3Client b3Client;

    @InjectMocks
    private TickerService tickerService;

    @Test
    void deveConsultarTickerComSucesso() {
        when(b3Client.buscarDadosTicker("PETR4")).thenReturn("{\"ticker\":\"PETR4\",\"ultimo\":34.21}");

        TickerResponseDTO response = tickerService.consultarTicker("petr4");

        assertEquals("PETR4", response.getTicker());
        assertEquals("BMFBovespa", response.getFonte());
        assertEquals("{\"ticker\":\"PETR4\",\"ultimo\":34.21}", response.getRetornoBruto());
    }

    @Test
    void deveLancarBadRequestQuandoTickerForInvalido() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> tickerService.consultarTicker("PETR4!"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }
}

