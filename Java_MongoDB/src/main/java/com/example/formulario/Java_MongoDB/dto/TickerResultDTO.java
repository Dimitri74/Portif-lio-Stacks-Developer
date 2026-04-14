package com.example.formulario.Java_MongoDB.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerResultDTO {

    private String symbol;

    @JsonProperty("regularMarketPrice")
    private Double regularMarketPrice;

    @JsonProperty("regularMarketChangePercent")
    private Double regularMarketChangePercent;
}

