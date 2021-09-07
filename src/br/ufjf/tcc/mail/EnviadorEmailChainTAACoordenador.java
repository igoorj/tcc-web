package br.ufjf.tcc.mail;

import java.text.SimpleDateFormat;

import br.ufjf.tcc.business.UsuarioBusiness;
import br.ufjf.tcc.model.Participacao;
import br.ufjf.tcc.model.TCC;

public class EnviadorEmailChainTAACoordenador extends EnviadorEmailChain {

	public EnviadorEmailChainTAACoordenador() {
		super(new EnviadorEmailChainTAAProfessor());
	}

	@Override
	protected EmailBuilder gerarEmail(TCC tcc, String statusInicial) {
		EmailBuilder emailBuilder = null;
		if(statusFoiAlteradoPara(tcc, statusInicial, "TAA")){
			String nomeAluno = tcc.getAluno().getNomeUsuario();
			emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Trabalho com defesa agendada - "+nomeAluno);
			emailBuilder.appendMensagem("Prezado(a) coordenador(a) de curso, ").breakLine().breakLine();
			emailBuilder.appendMensagem("O aluno(a) <b>" + nomeAluno + "</b> informou os dados da sua defesa de trabalho de conclus�o de curso.");
			emailBuilder.appendMensagem(" Ap�s a defesa, o aluno(a) dever� enviar a vers�o final do trabalho para ser publicado no reposit�rio de trabalhos acad�micos.").breakLine().breakLine();
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
			inserirDestinatarios(ub.getCoordenadoresByCurso(tcc.getAluno().getCurso()), emailBuilder);
		}
		return emailBuilder;
	}

}
