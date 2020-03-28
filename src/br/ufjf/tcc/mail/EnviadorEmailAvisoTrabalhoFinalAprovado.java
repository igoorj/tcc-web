package br.ufjf.tcc.mail;

import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;

public class EnviadorEmailAvisoTrabalhoFinalAprovado extends EnviadorEmailChain{
	
	public EnviadorEmailAvisoTrabalhoFinalAprovado() {
		super(null);
	}

	@Override
	protected EmailBuilder gerarEmail(TCC tcc, String statusInicial) {
		EmailBuilder emailBuilder = null;
		
		String nomeAluno = tcc.getAluno().getNomeUsuario();
		String nomeCurso = tcc.getAluno().getCurso().getNomeCurso();
		
		emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Trabalho Aprovado - " + nomeAluno);
		emailBuilder.appendMensagem("Prezado(a) " + nomeAluno).breakLine().breakLine();
		emailBuilder.appendMensagem("Parabéns. O seu trabalho foi aprovado pela Coordenação de Curso e ");
		emailBuilder.appendMensagem("está disponível para acesso público no repositório de trabalhos acadêmicos.");
		emailBuilder.appendMensagem("Att.,").breakLine();
		emailBuilder.appendMensagem("Coordenação do Curso de " + nomeCurso).breakLine();
		emailBuilder.appendLinkSistema();
		
		
		List<Usuario> destinatarios = new ArrayList<>();
		destinatarios.add(tcc.getAluno());
		inserirDestinatarios(destinatarios, emailBuilder);
	
		return emailBuilder;
		
	}
}
