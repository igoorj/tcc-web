package br.ufjf.tcc.mail;

import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.business.UsuarioBusiness;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;

//Email número 15 do drive
public class EnviadorEmailAvisoTrabalhoFinalAprovadoPorOrientador extends EnviadorEmailChain{
	
	
	public EnviadorEmailAvisoTrabalhoFinalAprovadoPorOrientador() {
		super(null);
	}

	@Override
	protected EmailBuilder gerarEmail(TCC tcc, String statusInicial) {
		EmailBuilder emailBuilder = null;
		UsuarioBusiness ub = new UsuarioBusiness();
		String nomeAluno = tcc.getAluno().getNomeUsuario();
		String nomeOrientador = tcc.getOrientador().getNomeUsuario();
		String titulo = tcc.getNomeTCC();
		
		emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Trabalho Final Aprovado pelo Orientador - " + nomeAluno);
		emailBuilder.appendMensagem("Prezada Coordena��o, ").breakLine();
		emailBuilder.appendMensagem("a Vers�o Final do Trabalho de Conclus�o de Curso do(a) discente <b>" + nomeAluno + "</b>, com o t�tulo ");
		emailBuilder.appendHtmlTextBold( titulo );
		emailBuilder.appendMensagem( ", foi aprovada pelo(a) orientador(a) <b>" + nomeOrientador + "</b>.").breakLine();
		emailBuilder.appendMensagem("A Coordena��o/bolsista da Coordena��o precisa avaliar ");
		emailBuilder.appendMensagem("a formata��o do TCC, conferir a documenta��o de Defesa e confirmar ");
		emailBuilder.appendMensagem("no Sistema de Monografias os membros que efetivamente participaram da Banca. ").breakLine();
		emailBuilder.appendMensagem("No caso de Reprova��o, descrever o(s) motivo(s) para ");
		emailBuilder.appendMensagem("ser(em) enviado(s) para o(a) discente que ter� o prazo m�ximo ");
		emailBuilder.appendMensagem("de 2 (dois) dias depois do �ltimo dia letivo do ");
		emailBuilder.appendMensagem("semestre, para submeter uma nova vers�o final, que tamb�m dever� receber a ");
		emailBuilder.appendMensagem("sua avalia��o. Caso contr�rio, deve-se aprovar o Trabalho de ");
		emailBuilder.appendMensagem("Conclus�o de Curso para se tornar P�blico no Sistema de Monografias.");
		emailBuilder.breakLine().breakLine();
		emailBuilder.appendLinkSistema();
		
		
		List<Usuario> destinatarios = new ArrayList<>();
		destinatarios.addAll(ub.getSecretariasByCurso(tcc.getAluno().getCurso()));
		inserirDestinatarios(destinatarios, emailBuilder);
	
		return emailBuilder;
		
	}
}
