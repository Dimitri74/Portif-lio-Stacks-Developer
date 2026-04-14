package com.example.formulario.Java_MongoDB.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.formulario.Java_MongoDB.dto.TickerResponseDTO;
import com.example.formulario.Java_MongoDB.service.TickerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/acoes")
@RequiredArgsConstructor
public class TickerController {

    private final TickerService tickerService;

    @GetMapping("/{ticker}")
    public ResponseEntity<TickerResponseDTO> consultarTicker(@PathVariable String ticker) {
        return ResponseEntity.ok(tickerService.consultarTicker(ticker));
    }
}

