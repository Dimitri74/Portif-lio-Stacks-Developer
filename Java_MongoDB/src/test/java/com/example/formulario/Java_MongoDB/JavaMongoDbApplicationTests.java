package com.example.formulario.Java_MongoDB;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class JavaMongoDbApplicationTests {

	@Test
	void deveConterMetodoMain() {
		assertDoesNotThrow(() -> JavaMongoDbApplication.class.getDeclaredMethod("main", String[].class));
	}

}
