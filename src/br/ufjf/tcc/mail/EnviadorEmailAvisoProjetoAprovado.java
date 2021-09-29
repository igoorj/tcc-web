package br.ufjf.tcc.mail;

import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.business.UsuarioBusiness;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;

public class EnviadorEmailAvisoProjetoAprovado extends EnviadorEmailChain{
	
	
	public EnviadorEmailAvisoProjetoAprovado() {
		super(null);
	}

	@Override
	protected EmailBuilder gerarEmail(TCC tcc, String statusInicial) {
		EmailBuilder emailBuilder = null;
		
		UsuarioBusiness usuarioBusiness = new UsuarioBusiness();
		List<Usuario> coordenadores = usuarioBusiness.getCoordenadoresByCurso(tcc.getAluno().getCurso());
//		String nomeCoordenador = coordenadores.get(0).getNomeUsuario();
		
		String nomeAluno = tcc.getAluno().getNomeUsuario();
		String nomeCurso = tcc.getAluno().getCurso().getNomeCurso();
		
		emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Projeto Aprovado - " + nomeAluno);
		emailBuilder.appendMensagem("Prezado(a) <b>" + nomeAluno + "</b>, ").breakLine();
		emailBuilder.appendMensagem("O seu projeto de TCC foi aprovado pela Coordena��o de Curso.").breakLine();
		emailBuilder.appendMensagem("Atente-se ao calend�rio definido pela sua Coordena��o como prazo ");
		emailBuilder.appendMensagem("m�ximo para envio dos dados da sua defesa e ");
		emailBuilder.appendMensagem("envio da vers�o digital do seu trabalho. ");
		emailBuilder.breakLine().breakLine();
		emailBuilder.appendMensagem("Att.,").breakLine();
		emailBuilder.appendMensagem("Coordena��o do Curso de " + nomeCurso).breakLine();
		
		List<Usuario> destinatarios = new ArrayList<>();
		destinatarios.add(tcc.getAluno());
		destinatarios.add(usuarioBusiness.getCoordenadoresByCurso(tcc.getAluno().getCurso()).get(0));
		inserirDestinatarios(destinatarios, emailBuilder);
	
		return emailBuilder;
		
	}
}
