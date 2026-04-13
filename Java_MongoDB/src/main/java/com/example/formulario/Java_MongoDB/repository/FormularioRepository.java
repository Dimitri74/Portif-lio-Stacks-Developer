package com.example.formulario.Java_MongoDB.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.formulario.Java_MongoDB.model.Formulario;

public interface FormularioRepository extends MongoRepository<Formulario, String> {

	boolean existsByEmail(String email);

	boolean existsByEmailAndIdNot(String email, String id);
}

