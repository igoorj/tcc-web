package br.ufjf.tcc.mail;

import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;

// Não está sendo usado
//Email número 08 do drive
public class EnviadorEmailAlertaSubmissaoTrabalho extends EnviadorEmailChain{
	
	
	public EnviadorEmailAlertaSubmissaoTrabalho() {
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
		
		emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Aviso de prazo de submiss�o de trabalho - " + nomeAluno);
		emailBuilder.appendMensagem("Prezados <b>" + nomeAluno + "</b> e <b>" + nomeOrientador +"</b>,");
		emailBuilder.breakLine().breakLine();
		emailBuilder.appendMensagem("dentro de 2 dias se encerra o prazo para incluir no Sistema de Monografias");
		emailBuilder.appendMensagem("a vers�o do TCC a ser avaliado pelos membros da Banca Examinadora. ").breakLine();
		emailBuilder.appendMensagem("� necess�rio preencher todas as informa��es no sistema para esta atividade se tornar completa, ");
		emailBuilder.appendMensagem("pois ainda n�o consta que o(a) discente realizou esta atividade completamente.").breakLine();
		emailBuilder.appendMensagem("Se n�o cumprir essa tarefa dentro do prazo, n�o haver� como dar andamento das");
		emailBuilder.appendMensagem("demais atividades, e desta forma n�o ser� poss�vel gerar");
		emailBuilder.appendMensagem("a documenta��o necess�ria para a Defesa do TCC.").breakLine();
		emailBuilder.appendMensagem("Att.,").breakLine();
//		emailBuilder.appendMensagem(nomeCoordenador).breakLine();
		emailBuilder.appendMensagem("Coordenador(a) do Curso de " + nomeCurso).breakLine();
		emailBuilder.appendLinkSistema();
		
		List<Usuario> destinatarios = new ArrayList<>();
		destinatarios.add(tcc.getAluno());
		destinatarios.add(tcc.getOrientador());
		inserirDestinatarios(destinatarios, emailBuilder);
	
		return emailBuilder;
		
	}
}
