package br.ufjf.tcc.mail;

import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.business.UsuarioBusiness;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;

public class EnviadorEmailAvisoTrabalhoFinalAprovado extends EnviadorEmailChain{
	
	public EnviadorEmailAvisoTrabalhoFinalAprovado() {
		super(null);
	}

	@Override
	protected EmailBuilder gerarEmail(TCC tcc, String statusInicial) {
		EmailBuilder emailBuilder = null;
		
		UsuarioBusiness ub = new UsuarioBusiness();
		
		String nomeAluno = tcc.getAluno().getNomeUsuario();
		String nomeCurso = tcc.getAluno().getCurso().getNomeCurso();
		
		emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Trabalho Aprovado - " + nomeAluno);
		emailBuilder.appendMensagem("Prezado(a) <b>" + nomeAluno + "</b>,").breakLine().breakLine();
		emailBuilder.appendMensagem("Parabéns. O seu trabalho foi aprovado pela Coordenação de Curso e ");
		emailBuilder.appendMensagem("está disponível para acesso público no repositório de trabalhos acadêmicos.");
		emailBuilder.appendMensagem("Att.,").breakLine();
		emailBuilder.appendMensagem("Coordenação do Curso de " + nomeCurso).breakLine();
		emailBuilder.appendLinkSistema();
		
		
		List<Usuario> destinatarios = new ArrayList<>();
		destinatarios.add(tcc.getAluno());
		destinatarios.add(ub.getCoordenadoresByCurso(tcc.getAluno().getCurso()).get(0));
		inserirDestinatarios(destinatarios, emailBuilder);
	
		return emailBuilder;
		
	}
}
