package br.ufjf.tcc.mail;

import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.business.UsuarioBusiness;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;


// Email número xx do drive
public class EnviadorEmailAvisoTrabalhoReprovadoDefinitivo extends EnviadorEmailChain{
	
	public EnviadorEmailAvisoTrabalhoReprovadoDefinitivo() {
		super(null);
	}

	@Override
	protected EmailBuilder gerarEmail(TCC tcc, String statusInicial) {
		EmailBuilder emailBuilder = null;
		
		UsuarioBusiness ub = new UsuarioBusiness();
//		List<Usuario> coordenadores = usuarioBusiness.getCoordenadoresByCurso(tcc.getAluno().getCurso());
//		String nomeCoordenador = coordenadores.get(0).getNomeUsuario();
		
		String nomeAluno = tcc.getAluno().getNomeUsuario();
		String nomeOrientador = tcc.getOrientador().getNomeUsuario();
		String titulo = tcc.getNomeTCC();
		
		emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Trabalho Final Reprovado por Definitivo - " + nomeAluno);
		emailBuilder.appendMensagem("Prezada Coordena��o e " + nomeOrientador + ", ").breakLine();
		emailBuilder.appendMensagem("a Vers�o Final do Trabalho de Conclus�o de Curso ");
		emailBuilder.appendMensagem("do(a) discente <b>" + nomeAluno + "</b>, com o t�tulo ");
		emailBuilder.appendHtmlTextBold( titulo );
		emailBuilder.appendMensagem( ", foi reprovada por definitivo pelo(a) orientador(a) <b>" + nomeOrientador + "</b>.").breakLine();
		emailBuilder.appendMensagem("O TCC foi exclu�do do sistema, junto de seus arquivos e ");
		emailBuilder.appendMensagem("o aluno foi desativado.");
		emailBuilder.breakLine().breakLine();
		emailBuilder.appendLinkSistema();
		
		List<Usuario> destinatarios = new ArrayList<>();
		destinatarios.addAll(ub.getSecretariasByCurso(tcc.getAluno().getCurso()));
		destinatarios.add(tcc.getOrientador());
		inserirDestinatarios(destinatarios, emailBuilder);
	
		return emailBuilder;
		
	}
}
