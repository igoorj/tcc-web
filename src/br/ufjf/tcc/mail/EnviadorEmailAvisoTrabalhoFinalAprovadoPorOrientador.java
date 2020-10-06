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
		emailBuilder.appendMensagem("Prezada Coordenação, ").breakLine();
		emailBuilder.appendMensagem("a Versão Final do Trabalho de Conclusão de Curso do(a) discente <b>" + nomeAluno + "</b>, com o título ");
		emailBuilder.appendHtmlTextBold( titulo );
		emailBuilder.appendMensagem( ", foi aprovada pelo(a) orientador(a) <b>" + nomeOrientador + "</b>.").breakLine();
		emailBuilder.appendMensagem("A Coordenação/bolsista da Coordenação precisa avaliar ");
		emailBuilder.appendMensagem("a formatação do TCC, conferir a documentação de Defesa e confirmar ");
		emailBuilder.appendMensagem("no Sistema de Monografias os membros que efetivamente participaram da Banca. ").breakLine();
		emailBuilder.appendMensagem("No caso de Reprovação, descrever o(s) motivo(s) para ");
		emailBuilder.appendMensagem("ser(em) enviado(s) para o(a) discente que terá o prazo máximo ");
		emailBuilder.appendMensagem("de 2 (dois) dias depois do último dia letivo do ");
		emailBuilder.appendMensagem("semestre, para submeter uma nova versão final, que também deverá receber a ");
		emailBuilder.appendMensagem("sua avaliação. Caso contrário, deve-se aprovar o Trabalho de ");
		emailBuilder.appendMensagem("Conclusão de Curso para se tornar Público no Sistema de Monografias.");
		emailBuilder.breakLine().breakLine();
		emailBuilder.appendLinkSistema();
		
		
		List<Usuario> destinatarios = new ArrayList<>();
		destinatarios.addAll(ub.getSecretariasByCurso(tcc.getAluno().getCurso()));
		inserirDestinatarios(destinatarios, emailBuilder);
	
		return emailBuilder;
		
	}
}
