package br.ufjf.tcc.mail;

import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;

//Email número 14 do drive
public class EnviadorEmailAvisoTrabalhoFinalReprovado extends EnviadorEmailChain{

	public EnviadorEmailAvisoTrabalhoFinalReprovado() {
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
		String justificativa = tcc.getJustificativaReprovacao();
		
		emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Trabalho Final Reprovado - "+nomeAluno);
		emailBuilder.appendMensagem("Prezado <b>" + nomeAluno + "</b>, ").breakLine();
		emailBuilder.appendMensagem("o(a) orientador(a), <b>" + nomeOrientador + "</b>,  Reprovou a vers�o ");
		emailBuilder.appendMensagem("Final do seu Trabalho de Conclus�o de Curso submetido no ").breakLine();
		emailBuilder.appendMensagem("Sistema de Monografias. Segue abaixo o(s) motivo(s) da reprova��o:");
		emailBuilder.breakLine().breakLine();
		emailBuilder.appendMensagem( justificativa + ".").breakLine().breakLine();
		emailBuilder.appendMensagem("Voc� tem at� 2 (dois) dias ap�s o fim do prazo ");
		emailBuilder.appendMensagem("para corrigir seu trabalho e submeter a nova ");
		emailBuilder.appendMensagem("vers�o para ser avaliada pelo(a) orientador(a)."); 
		
		emailBuilder.breakLine().breakLine();
		emailBuilder.appendMensagem("Att.,").breakLine();
//		emailBuilder.appendMensagem( nomeCoordenador).breakLine();
		emailBuilder.appendMensagem("Coordenador(a) do Curso de " + nomeCurso).breakLine();
		emailBuilder.appendLinkSistema();
		
		List<Usuario> destinatarios = new ArrayList<>();
		destinatarios.add(tcc.getAluno());
		inserirDestinatarios(destinatarios, emailBuilder);
	
		return emailBuilder;
		
	}

}
