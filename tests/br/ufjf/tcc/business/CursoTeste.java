package br.ufjf.tcc.business;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import br.ufjf.tcc.model.Curso;

class CursoTeste {

	@Test
	void test() {
		
		Curso curso = new Curso("324356", "Ciência da Computação");
		assertEquals("Ciência da Computação", curso.getNomeCurso());
	}

}
