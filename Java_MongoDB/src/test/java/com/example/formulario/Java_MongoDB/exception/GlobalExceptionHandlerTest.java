package com.example.formulario.Java_MongoDB.exception;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.example.formulario.Java_MongoDB.controller.FormularioController;
import com.example.formulario.Java_MongoDB.dto.FormularioDTO;
import com.example.formulario.Java_MongoDB.service.FormularioService;

@WebMvcTest(FormularioController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FormularioService formularioService;

    @Test
    void deveRetornarMensagemAmigavelSemStacktraceQuandoEmailJaCadastrado() throws Exception {
        when(formularioService.criar(any(FormularioDTO.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email ja cadastrado"));

        mockMvc.perform(post("/api/formularios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Marcus\",\"email\":\"marcus@email.com\",\"cep\":\"01001000\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.trace").value("Email ja cadastrado"))
                .andExpect(jsonPath("$.trace", not(containsString("org.springframework"))));
    }

    @Test
    void deveRetornarMensagemGenericaEmErroInterno() throws Exception {
        when(formularioService.listar()).thenThrow(new RuntimeException("falha inesperada"));

        mockMvc.perform(get("/api/formularios"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.trace").value("Ocorreu um erro interno. Tente novamente mais tarde."));
    }
}

