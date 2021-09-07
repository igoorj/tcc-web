package br.ufjf.tcc.mail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import br.ufjf.tcc.model.Participacao;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;
import br.ufjf.tcc.pdfHandle.CartaParticipacaoBanca;


//Email nÃºmero 17 do drive
public class EnviadorEmailCartaParticipacao extends EnviadorEmailChain{
	
	private String nomeAluno;
	private String nomeOrientador;
	private String nomeCurso;
	private String titulo;
	private String dataDefesa;
	private List<String> suplentes = new ArrayList<String>();
	private List<String> membros = new ArrayList<String>();
	private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	
	public EnviadorEmailCartaParticipacao() {
		super(null);
	}

	
	protected EmailBuilder gerarEmail(TCC tcc, Participacao membro) {
		EmailBuilder emailBuilder = null;
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
		emailBuilder.appendMensagem("Data da Defesa: " + dataDefesa).breakLine();
		emailBuilder.appendMensagem("Banca Examinadora:").breakLine();
		for(String m : membros) {
			emailBuilder.appendMensagem("Membro da banca: " + m).breakLine();
		}
		for(String suplente : suplentes) {
			emailBuilder.appendMensagem("Suplente: " + suplente).breakLine();
		}
		emailBuilder.appendMensagem("Atenciosamente,").breakLine();
//		emailBuilder.appendMensagem("(assinatura digital do Coordenador)").breakLine(); 
//		emailBuilder.appendMensagem("______________________________________").breakLine(); 
		emailBuilder.appendMensagem("Coordenação do Curso " + nomeCurso).breakLine();
		emailBuilder.appendLinkSistema();
		
		List<Usuario> destinatarios = new ArrayList<>();
		destinatarios.add(membro.getProfessor());
		inserirDestinatarios(destinatarios, emailBuilder);
	
		return emailBuilder;
	}
	
	
	@Override
	public void enviarEmail(TCC tcc, String status) {
		List<Participacao> participacoes = tcc.getParticipacoes();
		// Verifica quem prticipou e separa em suplentes e membros
		
		for(Iterator<Participacao> i = participacoes.iterator(); i.hasNext();) {
			Participacao p = i.next();
			if(p.isParticipou()) {
				if(p.isSuplente())
					this.suplentes.add(p.getProfessor().getNomeUsuario());
				else
					this.membros.add(p.getProfessor().getNomeUsuario());
			}
			else
				i.remove();
		}
		
		this.nomeAluno = tcc.getAluno().getNomeUsuario();
		this.nomeCurso = tcc.getAluno().getCurso().getNomeCurso();
		this.titulo = tcc.getNomeTCC();
		this.nomeOrientador = tcc.getOrientador().getNomeUsuario();
		this.dataDefesa = formatter.format(tcc.getDataApresentacao());
		
		for(Participacao p : participacoes) {
			EmailBuilder builder = gerarEmail(tcc, p);
			CartaParticipacaoBanca cartaParticipacao = new CartaParticipacaoBanca();
			// Adicionar arquivo de carta de participaÃ§Ã£o
			try {
				String CoOrientador = " ";
				if(tcc.possuiCoorientador())
					CoOrientador = tcc.getCoOrientador().getNomeUsuario();
				cartaParticipacao.gerarCartaParticipacao( nomeCurso, p.getProfessor().getNomeUsuario(), nomeAluno, nomeOrientador, tcc.getIdTCC(),
						CoOrientador, titulo, dataDefesa, participacoes, p.getProfessor().getMatricula(), tcc.getCertificadoDigital());
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
