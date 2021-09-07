package br.ufjf.tcc.mail;

import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;

public class EnviadorEmailAvisoProjetoReprovado extends EnviadorEmailChain{
	
	
	public EnviadorEmailAvisoProjetoReprovado() {
		super(null);
	}

	@Override
	protected EmailBuilder gerarEmail(TCC tcc, String statusInicial) {
		EmailBuilder emailBuilder = null;
		
//		UsuarioBusiness usuarioBusiness = new UsuarioBusiness();
//		List<Usuario> coordenadores = usuarioBusiness.getCoordenadoresByCurso(tcc.getAluno().getCurso());
//		String nomeCoordenador = coordenadores.get(0).getNomeUsuario();
		
		String nomeAluno = tcc.getAluno().getNomeUsuario();
		String nomeOrientador = tcc.getOrientador().getNomeUsuario();
		String nomeCurso = tcc.getAluno().getCurso().getNomeCurso();
		String titulo = tcc.getNomeTCC();
		String justificativa = tcc.getJustificativaReprovacao();
		
		emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Aviso de projeto reprovado - "+nomeAluno);
		emailBuilder.appendMensagem("Prezados <b>" + nomeAluno + "</b> e <b>" + nomeOrientador + "</b>, ").breakLine();
		emailBuilder.appendMensagem("o Projeto de Trabalho de Conclus�o de Curso, com o t�tulo <b>" + titulo + "</b>, ");
		emailBuilder.appendMensagem("submetido no Sistema de Monografias pelo(a) discente foi Reprovado.").breakLine();
		emailBuilder.appendMensagem("Segue abaixo o(s) motivos da reprova��o:").breakLine().breakLine();
		emailBuilder.appendMensagem( justificativa + ".").breakLine().breakLine(); 
		emailBuilder.appendMensagem("O(a) discente tem at� 7 (sete) dias corridos ap�s o fim do prazo para corrigir o Projeto e submeter a vers�o corrigida no Sistema de Monografias.");
		emailBuilder.breakLine().breakLine();
		emailBuilder.appendMensagem("Att.,").breakLine();
//		emailBuilder.appendMensagem(nomeCoordenador).breakLine();
		emailBuilder.appendMensagem("Coordenena��o do Curso de " + nomeCurso).breakLine();
		emailBuilder.appendLinkSistema();
		
		List<Usuario> destinatarios = new ArrayList<>();
		destinatarios.add(tcc.getAluno());
		destinatarios.add(tcc.getOrientador());
		inserirDestinatarios(destinatarios, emailBuilder);
	
		return emailBuilder;
		
	}
}
