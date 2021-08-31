package br.ufjf.tcc.businessc;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import br.ufjf.tcc.model.Curso;

class TesteAviso {

	@Test
	void test() {
		
		Curso curso = new Curso("546546456", "Ciencias Exatas");
		assertEquals("Ciencias Exatas 2", curso.getNomeCurso());
	}

}
