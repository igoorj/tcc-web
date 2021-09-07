package br.ufjf.tcc.mail;

import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.business.UsuarioBusiness;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;

//Email número 13 do drive
public class EnviadorEmailAvisoTrabalhoFinalSubmetido extends EnviadorEmailChain{

	public EnviadorEmailAvisoTrabalhoFinalSubmetido() {
		super(null);
	}
	
	@Override
	protected EmailBuilder gerarEmail(TCC tcc, String statusInicial) {
		EmailBuilder emailBuilder = null;
		UsuarioBusiness usuarioBusiness = new UsuarioBusiness();
		
		List<Usuario> coordenadores = usuarioBusiness.getCoordenadoresByCurso(tcc.getAluno().getCurso());
		
		String nomeAluno = tcc.getAluno().getNomeUsuario();
		String nomeOrientador = tcc.getOrientador().getNomeUsuario();
//		String nomeCoordenador = coordenadores.get(0).getNomeUsuario();
		String nomeCurso = tcc.getAluno().getCurso().getNomeCurso();
		String titulo = tcc.getNomeTCC();
		
		emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Aviso de TCC submetido - "+nomeAluno);
		emailBuilder.appendMensagem("Prezado <b>" + nomeOrientador + "</b>, ").breakLine();
		emailBuilder.appendMensagem("a Vers�o Final do Trabalho de Conclus�o de Curso do(a) discente ");
		emailBuilder.appendHtmlTextBold( nomeAluno );
		emailBuilder.appendMensagem(", com o t�tulo <b>" + titulo + "</b>, que voc� orientou, ");
		emailBuilder.appendMensagem("se encontra dispon�vel no Sistema de Monografias aguardando a ");
		emailBuilder.appendMensagem("sua avalia��o: Aprova��o ou Reprova��o.").breakLine(); 
		emailBuilder.appendMensagem("No caso de <b>Reprova��o</b>, descrever o(s) motivo(s) para ");
		emailBuilder.appendMensagem("ser(em) enviado(s) para o(a) discente. O(a) mesmo(a) ter� o prazo ");
		emailBuilder.appendMensagem("de 2 (dois) dias ap�s o prazo final para corrigir o(s) problema(s) e submeter uma nova ");
		emailBuilder.appendMensagem("Vers�o Final, que tamb�m dever� receber a sua avalia��o.");
		
		emailBuilder.breakLine().breakLine();
		emailBuilder.appendMensagem("Att.,").breakLine();
//		emailBuilder.appendMensagem(nomeCoordenador).breakLine();
		emailBuilder.appendMensagem("Coordenador(a) do Curso de " + nomeCurso).breakLine();
		emailBuilder.appendLinkSistema();
		
		List<Usuario> destinatarios = new ArrayList<>();
		destinatarios.add(tcc.getOrientador());
		inserirDestinatarios(destinatarios, emailBuilder);
	
		return emailBuilder;
		
	}
	
}
