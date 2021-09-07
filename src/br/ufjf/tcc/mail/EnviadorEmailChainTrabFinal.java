package br.ufjf.tcc.mail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.business.UsuarioBusiness;
import br.ufjf.tcc.model.Participacao;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;

public class EnviadorEmailChainTrabFinal extends EnviadorEmailChain {

	public EnviadorEmailChainTrabFinal() {
		super(null);
	}

	@Override
	protected EmailBuilder gerarEmail(TCC tcc, String statusInicial) {
		EmailBuilder emailBuilder = null;
		if(tcc.isTrabFinal()) {
			String nomeAluno = tcc.getAluno().getNomeUsuario();
			emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Trabalho aguardando aprova��o - " + nomeAluno);
			emailBuilder.appendMensagem("Prezado(a) coordenador(a) de curso, ").breakLine().breakLine();
			emailBuilder.appendMensagem("O aluno(a) <b>"+nomeAluno+"</b> enviou a vers�o final do seu trabalho de conclus�o de curso e aguarda aprova��o da coordena��o de curso.").breakLine();
			emailBuilder.appendMensagem(" Ap�s a aprova��o do trabalho, este estar� dispon�vel para acesso p�blico no reposit�rio de trabalhos acad�micos.").breakLine().breakLine();
			emailBuilder.appendHtmlTopico("Informa��es do trabalho:").breakLine().breakLine();
			emailBuilder.appendHtmlTextBold("T�tulo: ");
			emailBuilder.appendMensagem(tcc.getNomeTCC()).breakLine();
			emailBuilder.appendHtmlTextBold("Resumo: ");
			emailBuilder.appendMensagem(tcc.getResumoTCC()).breakLine();
			emailBuilder.appendMensagem("<b>Orientador(a):</b> " + tcc.getOrientador().getNomeUsuario()).breakLine();
			if(tcc.possuiCoorientador()) {
				emailBuilder.appendMensagem("<b>Coorientador(a):</b> " + tcc.getCoOrientador().getNomeUsuario()).breakLine();
			}
			emailBuilder.appendHtmlTextBold("Banca examinadora: ").breakLine();
			for(Participacao p : tcc.getParticipacoes()) {
				if(p.isSuplente()){
					emailBuilder.appendMensagem("  - " + p.getProfessor().getNomeUsuario() + " (Suplente)").breakLine();
				} else {
					emailBuilder.appendMensagem("  - " + p.getProfessor().getNomeUsuario()).breakLine();
				}
			}
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
			String dataFormatada = formatter.format(tcc.getDataApresentacao().getTime());
			emailBuilder.appendMensagem("<b>Data da apresenta��o:</b> "+dataFormatada).breakLine();
			emailBuilder.appendMensagem("<b>Local de defesa:</b> "+tcc.getSalaDefesa()).breakLine().breakLine();
			emailBuilder.appendLinkSistema();
			UsuarioBusiness ub = new UsuarioBusiness();
			List<Usuario> destinatarios = new ArrayList<>();
			destinatarios.addAll(ub.getCoordenadoresByCurso(tcc.getAluno().getCurso()));
			destinatarios.addAll(ub.getSecretariasByCurso(tcc.getAluno().getCurso()));
			destinatarios.add(tcc.getOrientador());
			inserirDestinatarios(destinatarios, emailBuilder);
		}
		return emailBuilder;
	}

}
