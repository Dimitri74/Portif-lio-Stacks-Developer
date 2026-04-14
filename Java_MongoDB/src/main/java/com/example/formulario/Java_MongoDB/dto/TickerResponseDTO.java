package com.example.formulario.Java_MongoDB.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TickerResponseDTO {

    private String ticker;
    private String fonte;
    private String retornoBruto;
}

