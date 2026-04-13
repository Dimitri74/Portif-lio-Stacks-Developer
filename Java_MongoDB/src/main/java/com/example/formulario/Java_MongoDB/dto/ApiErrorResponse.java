package com.example.formulario.Java_MongoDB.dto;

import java.time.OffsetDateTime;

public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String trace) {
}

