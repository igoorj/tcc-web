package br.ufjf.tcc.mail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.model.Participacao;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;

public class EnviadorEmailChainTAAProfessor extends EnviadorEmailChain {

	public EnviadorEmailChainTAAProfessor() {
		super(new EnviadorEmailChainTrabFinal());
	}

	@Override
	protected EmailBuilder gerarEmail(TCC tcc, String statusInicial) {
		EmailBuilder emailBuilder = null;
//		if(statusFoiAlteradoPara(tcc, statusInicial, "TAA")){
			String nomeAluno = tcc.getAluno().getNomeUsuario();
			emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Trabalho com defesa agendada - "+nomeAluno);
			emailBuilder.appendMensagem("Prezado(a) professor(a), ").breakLine().breakLine();
			emailBuilder.appendMensagem("O aluno(a) <b>" + nomeAluno + "</b> informou os dados da sua defesa de trabalho de conclus�o de curso.");
			emailBuilder.appendMensagem(" O texto do trabalho est� dispon�vel no sistema de acompanhamento de monografias. Para ter acesso ao trabalho, basta logar no sistema.").breakLine().breakLine();
			emailBuilder.appendHtmlTopico("Informa��es do trabalho:").breakLine();
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
			emailBuilder.appendMensagem("<b>Local de defesa:</b> "+tcc.getSala().getNomeSala()).breakLine().breakLine();
			emailBuilder.appendLinkSistema().breakLine().breakLine();
			
			List<Usuario> destinatarios = new ArrayList<>();
			destinatarios.addAll(tcc.getProfessoresParticipacoes());
			destinatarios.add(tcc.getOrientador());
			if(tcc.possuiCoorientador()) {
				destinatarios.add(tcc.getCoOrientador());
			}
			inserirDestinatarios(destinatarios, emailBuilder);
//	}
		return emailBuilder;
	}

}
