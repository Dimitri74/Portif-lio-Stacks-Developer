package com.example.formulario.Java_MongoDB.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "formularios")
public class Formulario {

    @Id
    private String id;
    private String nome;
    private String email;
    private String  cep;
    private String logradouro;
    private String complemento;
    private String unidade;
    private String bairro;
    private String localidade;
    private String  uf;
    private String  estado;
    private String regiao;
    private String  ibge;
    private String  gia;
    private String  ddd;
    private String  siafi;


}



