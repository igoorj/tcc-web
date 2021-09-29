package br.ufjf.tcc.mail;

import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.business.UsuarioBusiness;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;

// Email número 4 do drive
public class EnviadorEmailAvisoProjetoSubmetido extends EnviadorEmailChain{

	public EnviadorEmailAvisoProjetoSubmetido() {
		super(null);
	}
	
	@Override
	protected EmailBuilder gerarEmail(TCC tcc, String statusInicial) {
		EmailBuilder emailBuilder = null;
		UsuarioBusiness ub = new UsuarioBusiness();
		String nomeAluno = tcc.getAluno().getNomeUsuario();
		String titulo = tcc.getNomeTCC();
		
		emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Aviso de projeto submetido - " + nomeAluno);
		emailBuilder.appendMensagem("Prezado(a) Coordenador(a),").breakLine();
		emailBuilder.appendMensagem("O Projeto do Trabalho de Conclus�o de Curso do(a) discente ");
		emailBuilder.appendMensagem( "<b>" + nomeAluno + "</b>, com o t�tulo <b>" + titulo + "</b>, ");
		emailBuilder.appendMensagem("j� foi submetido ao Sistema de Monografias.").breakLine();
		emailBuilder.appendMensagem("Agora � necess�rio receber a sua avalia��o, de Aprova��o ou Reprova��o, ");
		emailBuilder.appendMensagem("para dar continuidade das atividades do TCC. ").breakLine();
		emailBuilder.appendMensagem("Em caso de reprova��o, descreva o(s) motivo(s). O(a) discente ter� o prazo de 7 ");
		emailBuilder.appendMensagem("(sete) dias corridos depois do prazo final para submeter o projeto corrigido ");
		emailBuilder.appendMensagem("para ser novamente avaliado.").breakLine();
		emailBuilder.appendLinkSistema();
		
		
		List<Usuario> destinatarios = new ArrayList<>();
		destinatarios.add(ub.getCoordenadoresByCurso(tcc.getAluno().getCurso()).get(0));
		destinatarios.addAll(ub.getSecretariasByCurso(tcc.getAluno().getCurso()));
		inserirDestinatarios(destinatarios, emailBuilder);
	
		return emailBuilder;
		
	}

}
