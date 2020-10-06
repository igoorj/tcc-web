package br.ufjf.tcc.mail;

import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;

public class EnviadorEmailAvisoProjetoAprovado extends EnviadorEmailChain{
	
	
	public EnviadorEmailAvisoProjetoAprovado() {
		super(null);
	}

	@Override
	protected EmailBuilder gerarEmail(TCC tcc, String statusInicial) {
		EmailBuilder emailBuilder = null;
		
//		UsuarioBusiness usuarioBusiness = new UsuarioBusiness();
//		List<Usuario> coordenadores = usuarioBusiness.getCoordenadoresByCurso(tcc.getAluno().getCurso());
//		String nomeCoordenador = coordenadores.get(0).getNomeUsuario();
		
		String nomeAluno = tcc.getAluno().getNomeUsuario();
		String nomeCurso = tcc.getAluno().getCurso().getNomeCurso();
		
		emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Projeto Aprovado - " + nomeAluno);
		emailBuilder.appendMensagem("Prezado(a) <b>" + nomeAluno + "</b>, ").breakLine();
		emailBuilder.appendMensagem("O seu projeto de TCC foi aprovado pela Coordenação de Curso.").breakLine();
		emailBuilder.appendMensagem("Atente-se ao calendário definido pela sua Coordenação como prazo ");
		emailBuilder.appendMensagem("máximo para envio dos dados da sua defesa e ");
		emailBuilder.appendMensagem("envio da versão digital do seu trabalho. ");
		emailBuilder.breakLine().breakLine();
		emailBuilder.appendMensagem("Att.,").breakLine();
		emailBuilder.appendMensagem("Coordenação do Curso de " + nomeCurso).breakLine();
		
		List<Usuario> alunos = new ArrayList<>();
		alunos.add(tcc.getAluno());
		inserirDestinatarios(alunos, emailBuilder);
	
		return emailBuilder;
		
	}
}
