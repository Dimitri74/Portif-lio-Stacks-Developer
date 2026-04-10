package com.example.formulario.Java_MongoDB.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.formulario.Java_MongoDB.dto.FormularioDTO;
import com.example.formulario.Java_MongoDB.service.FormularioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/formularios")
@RequiredArgsConstructor
public class FormularioController {

    private final FormularioService formularioService;

    @PostMapping
    public ResponseEntity<FormularioDTO> criar(@RequestBody FormularioDTO formularioDTO) {
        return ResponseEntity.ok(formularioService.criar(formularioDTO));
    }

    @GetMapping
    public ResponseEntity<List<FormularioDTO>> listar() {
        return ResponseEntity.ok(formularioService.listar());
    }

    @GetMapping("/cep/{cep}")
    public ResponseEntity<FormularioDTO> buscarEnderecoPorCep(@PathVariable String cep) {
        return ResponseEntity.ok(formularioService.buscarEnderecoPorCep(cep));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FormularioDTO> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(formularioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FormularioDTO> atualizar(@PathVariable String id, @RequestBody FormularioDTO formularioDTO) {
        return ResponseEntity.ok(formularioService.atualizar(id, formularioDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        formularioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

