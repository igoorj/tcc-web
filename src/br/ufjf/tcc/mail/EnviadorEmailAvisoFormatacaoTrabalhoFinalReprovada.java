package br.ufjf.tcc.mail;

import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;

//Email nÃºmero 16 do drive
public class EnviadorEmailAvisoFormatacaoTrabalhoFinalReprovada extends EnviadorEmailChain{
	
	
	public EnviadorEmailAvisoFormatacaoTrabalhoFinalReprovada() {
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
		
		emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Formatação do Trabalho Final Reprovada - " + nomeAluno);
		emailBuilder.appendMensagem("Prezados <b>" + nomeAluno + "</b> e <b>" + nomeOrientador + "</b>, ");
		emailBuilder.breakLine().breakLine();
		emailBuilder.appendMensagem("a Coordenação  Reprovou a formatação da versão Final do seu Trabalho de Conclusão ");
		emailBuilder.appendMensagem("de Curso <b>" + titulo + "</b> submetido no Sistema de Monografias.");
		emailBuilder.breakLine().breakLine();
		emailBuilder.appendMensagem("Segue abaixo o(s) motivo(s) da reprovação:").breakLine().breakLine();
		emailBuilder.appendMensagem( justificativa + ".").breakLine().breakLine();
		emailBuilder.appendMensagem("O(a) discente tem até 2 (dois) dias após o fim do prazo ");
		emailBuilder.appendMensagem("para corrigir o TCC e enviar a nova versão para ser avaliada.").breakLine();
		emailBuilder.appendMensagem("Att.,").breakLine();
//		emailBuilder.appendMensagem(nomeCoordenador).breakLine();
		emailBuilder.appendMensagem("Coordenador(a) do Curso " + nomeCurso).breakLine();
		emailBuilder.appendLinkSistema();
		
		List<Usuario> destinatarios = new ArrayList<>();
		destinatarios.add(tcc.getAluno());
		destinatarios.add(tcc.getOrientador());
		inserirDestinatarios(destinatarios, emailBuilder);
	
		return emailBuilder;
		
	}
}
