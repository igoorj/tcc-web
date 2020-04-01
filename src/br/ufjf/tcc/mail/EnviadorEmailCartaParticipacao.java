package br.ufjf.tcc.mail;

import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.business.UsuarioBusiness;
import br.ufjf.tcc.model.Participacao;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;
import br.ufjf.tcc.pdfHandle.CartaParticipacaoBanca;


//Email número 17 do drive
public class EnviadorEmailCartaParticipacao extends EnviadorEmailChain{
	
	
	public EnviadorEmailCartaParticipacao() {
		super(null);
	}

	
	protected EmailBuilder gerarEmail(TCC tcc, Participacao membro) {
		EmailBuilder emailBuilder = null;
		UsuarioBusiness usuarioBusiness = new UsuarioBusiness();
		
		List<Usuario> coordenadores = usuarioBusiness.getCoordenadoresByCurso(tcc.getAluno().getCurso());
		
		String nomeAluno = tcc.getAluno().getNomeUsuario();
		String nomeOrientador = tcc.getOrientador().getNomeUsuario();
		String nomeCoordenador = coordenadores.get(0).getNomeUsuario();
		String nomeCurso = tcc.getAluno().getCurso().getNomeCurso();
		String titulo = tcc.getNomeTCC();
		List<Participacao> participacoes = tcc.getParticipacoes();
		List<String> suplentes = new ArrayList<String>();
		List<String> membros = new ArrayList<String>();
		
		// Verifica quem prticipou e separa em suplentes e membros
		for(Participacao p : participacoes) {
			if(p.isParticipou()) {
				if(p.isSuplente())
					suplentes.add(p.getProfessor().getNomeUsuario());
				else
					membros.add(p.getProfessor().getNomeUsuario());
			}
			else
				participacoes.remove(p);
		}
		
		// Para cada membro, envia um e-mail
		String nomeMembro = membro.getProfessor().getNomeUsuario();
		
		emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Carta de participação da banca - " + nomeAluno);
		emailBuilder.appendMensagem("Prezado(a) " + nomeMembro + ", ").breakLine(); 
		emailBuilder.appendMensagem("Gostaríamos de agradecer, em nome do Curso " + nomeCurso + ", ");
		emailBuilder.appendMensagem("a sua participação como Membro em Banca Examinadora do ");
		emailBuilder.appendMensagem("Trabalho de Conclusão de Curso, conforme as especificações:").breakLine(); 
		emailBuilder.appendMensagem("Candidato(a): " + nomeAluno).breakLine();
		emailBuilder.appendMensagem("Orientador(a): " + nomeOrientador).breakLine();
		if(tcc.possuiCoorientador())
			emailBuilder.appendMensagem("Co-orientador(a): " + tcc.getCoOrientador().getNomeUsuario()).breakLine();
		emailBuilder.appendMensagem("Título: " + titulo).breakLine();
		emailBuilder.appendMensagem("Data da Defesa: (data e hora).").breakLine();
		emailBuilder.appendMensagem("Banca Examinadora:").breakLine();
		for(String m : membros) {
			emailBuilder.appendMensagem("Membro da banca: " + m).breakLine();
		}
		for(String suplente : suplentes) {
			emailBuilder.appendMensagem("Suplente: " + suplente).breakLine();
		}
		emailBuilder.appendMensagem("Atenciosamente,").breakLine();
		emailBuilder.appendMensagem("(assinatura digital do Coordenador)").breakLine(); 
		emailBuilder.appendMensagem("______________________________________").breakLine(); 
//			emailBuilder.appendMensagem(nomeCoordenador).breakLine();
		emailBuilder.appendMensagem("Coordenação do Curso " + nomeCurso).breakLine();
		emailBuilder.appendLinkSistema();
		CartaParticipacaoBanca cartaParticipacao = new CartaParticipacaoBanca();								
		try {
			String CoOrientador = " ";
			if(tcc.getCoOrientador() != null)
				CoOrientador = tcc.getCoOrientador().getNomeUsuario();
			
			cartaParticipacao.gerarCartaParticipacao( tcc.getAluno().getCurso().getNomeCurso(), nomeMembro, tcc.getAluno().getNomeUsuario(), tcc.getOrientador().getNomeUsuario(), tcc.getIdTCC(),
					CoOrientador, tcc.getNomeTCC(), tcc.getDataApresentacao().toString(), tcc.getParticipacoes(), membro.getProfessor().getMatricula(), tcc.getCertificadoDigital());
			emailBuilder.appendArquivo(cartaParticipacao.getCaminhoArquivo());
		 
		} catch(Exception e) {
			System.out.println("Exceção capturada ao gerar carta de participacao - EnviadorEmailCartaParticipacao");
			e.printStackTrace();
		}
		
		List<Usuario> destinatarios = new ArrayList<>();
		destinatarios.add(membro.getProfessor());
		inserirDestinatarios(destinatarios, emailBuilder);
	
		return emailBuilder;
	}
	
	
	
	public void enviarEmails(TCC tcc) {
		List<Participacao> participacoes = tcc.getParticipacoes();
		for(Participacao p : participacoes) {
			EmailBuilder builder = gerarEmail(tcc, p);
			CartaParticipacaoBanca cartaParticipacao = new CartaParticipacaoBanca();
			try {
				String CoOrientador = " ";
				if(tcc.getCoOrientador() != null)
					CoOrientador = tcc.getCoOrientador().getNomeUsuario();
				
				cartaParticipacao.gerarCartaParticipacao( tcc.getAluno().getCurso().getNomeCurso(), p.getProfessor().getNomeUsuario(), tcc.getAluno().getNomeUsuario(), tcc.getOrientador().getNomeUsuario(), tcc.getIdTCC(),
						CoOrientador, tcc.getNomeTCC(), tcc.getDataApresentacao().toString(), tcc.getParticipacoes(), p.getProfessor().getMatricula(), tcc.getCertificadoDigital());
				builder.appendArquivo(cartaParticipacao.getCaminhoArquivo());
			 
			} catch(Exception e) {
				System.out.println("Exceção capturada ao gerar carta de participacao - EnviadorEmailCartaParticipacao");
				e.printStackTrace();
			}
			Email email = new Email();
			email.enviar(builder);
			cartaParticipacao.apagarArquivo();
		}
	}


	@Override
	protected EmailBuilder gerarEmail(TCC tcc, String statusInicial) {
		return null;
	}
	
}
