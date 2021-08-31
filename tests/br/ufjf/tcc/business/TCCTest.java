package br.ufjf.tcc.business;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import antlr.collections.List;
import br.ufjf.tcc.model.CalendarioSemestre;
import br.ufjf.tcc.model.Participacao;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;

class TCCTest {

	@Test
	void test() {
		
		Usuario professor1 = new Usuario();
		professor1.setNomeUsuario("A");
		//professor1.setTipoUsuario(3);
		Usuario professor2 = new Usuario();
		professor2.setNomeUsuario("A");
		Usuario professor3 = new Usuario();
		professor3.setNomeUsuario("A");
		Usuario professor4 = new Usuario();
		professor4.setNomeUsuario("A");
		
		//List<Participacao> participacoes = new ArrayList<>();
		
		Participacao usuarios = new Participacao();
		//usuarios.setProfessor();
		Participacao usuarios2 = new Participacao();
		usuarios2.setProfessor(professor1);
		Participacao usuarios3 = new Participacao();
		usuarios3.setProfessor(professor1);
		Participacao usuarios4 = new Participacao();
		usuarios4.setProfessor(professor1);
		
		ArrayList<Participacao> membros = new ArrayList<Participacao>();
		membros.add(usuarios);
		membros.add(usuarios2);
		membros.add(usuarios3);
		membros.add(usuarios4);
				
		Usuario aluno = new Usuario();
		aluno.setNomeUsuario("Aluno teste");
		
		Usuario orientador = new Usuario();
		orientador.setAtivo(true);
		orientador.setNomeUsuario("Arnaldo");
		
		TCC tcc1 = new TCC();
		tcc1.setOrientador(orientador);
		tcc1.setAluno(aluno);
		tcc1.setProjeto(true);
		tcc1.setPublicado(true);
		tcc1.setOrientador(professor1);
		tcc1.setCoOrientador(professor2);
		tcc1.setParticipacoes(membros);
		
		assertEquals(4, tcc1.getProfessoresParticipacoes());
		

		
	}

}
