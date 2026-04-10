package com.example.formulario.Java_MongoDB.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.formulario.Java_MongoDB.client.ViaCepClient;
import com.example.formulario.Java_MongoDB.dto.FormularioDTO;
import com.example.formulario.Java_MongoDB.dto.ViaCepResponseDTO;
import com.example.formulario.Java_MongoDB.model.Formulario;
import com.example.formulario.Java_MongoDB.repository.FormularioRepository;

@ExtendWith(MockitoExtension.class)
class FormularioServiceTest {

    @Mock
    private FormularioRepository formularioRepository;

    @Mock
    private ViaCepClient viaCepClient;

    @InjectMocks
    private FormularioService formularioService;

    @Test
    void deveBuscarEnderecoPorCepValido() {
        ViaCepResponseDTO viaCepResponseDTO = ViaCepResponseDTO.builder()
                .cep("01001-000")
                .logradouro("Praça da Sé")
                .bairro("Sé")
                .localidade("São Paulo")
                .uf("SP")
                .estado("São Paulo")
                .regiao("Sudeste")
                .ibge("3550308")
                .gia("1004")
                .ddd("11")
                .siafi("7107")
                .build();

        when(viaCepClient.buscarEnderecoPorCep("01001000")).thenReturn(viaCepResponseDTO);

        FormularioDTO endereco = formularioService.buscarEnderecoPorCep("01001-000");

        assertEquals("01001-000", endereco.getCep());
        assertEquals("Praça da Sé", endereco.getLogradouro());
        assertEquals("São Paulo", endereco.getLocalidade());
    }

    @Test
    void deveLancarBadRequestQuandoCepForInvalido() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> formularioService.buscarEnderecoPorCep("950100100"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void deveLancarNotFoundQuandoCepNaoExistir() {
        when(viaCepClient.buscarEnderecoPorCep("99999999"))
                .thenReturn(ViaCepResponseDTO.builder().erro(true).build());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> formularioService.buscarEnderecoPorCep("99999999"));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void deveEnriquecerEnderecoAoCriarFormulario() {
        FormularioDTO formularioDTO = FormularioDTO.builder()
                .nome("Marcus")
                .email("marcus@email.com")
                .cep("01001-000")
                .complemento("apto 10")
                .build();

        ViaCepResponseDTO viaCepResponseDTO = ViaCepResponseDTO.builder()
                .cep("01001-000")
                .logradouro("Praça da Sé")
                .complemento("lado ímpar")
                .bairro("Sé")
                .localidade("São Paulo")
                .uf("SP")
                .estado("São Paulo")
                .regiao("Sudeste")
                .ibge("3550308")
                .gia("1004")
                .ddd("11")
                .siafi("7107")
                .build();

        when(viaCepClient.buscarEnderecoPorCep("01001000")).thenReturn(viaCepResponseDTO);
        when(formularioRepository.save(any(Formulario.class))).thenAnswer(invocation -> {
            Formulario formulario = invocation.getArgument(0);
            formulario.setId("1");
            return formulario;
        });

        FormularioDTO salvo = formularioService.criar(formularioDTO);

        ArgumentCaptor<Formulario> captor = ArgumentCaptor.forClass(Formulario.class);
        verify(formularioRepository).save(captor.capture());
        Formulario formularioSalvo = captor.getValue();

        assertEquals("Praça da Sé", formularioSalvo.getLogradouro());
        assertEquals("apto 10", formularioSalvo.getComplemento());
        assertEquals("São Paulo", salvo.getLocalidade());
    }

    @Test
    void deveEnriquecerEnderecoAoAtualizarFormulario() {
        Formulario existente = Formulario.builder()
                .id("1")
                .nome("Marcus")
                .email("antigo@email.com")
                .build();

        FormularioDTO atualizacao = FormularioDTO.builder()
                .nome("Marcus Dimitri")
                .email("novo@email.com")
                .cep("01001000")
                .build();

        ViaCepResponseDTO viaCepResponseDTO = ViaCepResponseDTO.builder()
                .cep("01001-000")
                .logradouro("Praça da Sé")
                .bairro("Sé")
                .localidade("São Paulo")
                .uf("SP")
                .estado("São Paulo")
                .regiao("Sudeste")
                .ibge("3550308")
                .gia("1004")
                .ddd("11")
                .siafi("7107")
                .build();

        when(formularioRepository.findById("1")).thenReturn(Optional.of(existente));
        when(viaCepClient.buscarEnderecoPorCep("01001000")).thenReturn(viaCepResponseDTO);
        when(formularioRepository.save(any(Formulario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FormularioDTO atualizado = formularioService.atualizar("1", atualizacao);

        assertEquals("Marcus Dimitri", atualizado.getNome());
        assertEquals("Praça da Sé", atualizado.getLogradouro());
        assertEquals("SP", atualizado.getUf());
    }
}

