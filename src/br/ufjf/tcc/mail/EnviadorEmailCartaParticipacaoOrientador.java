package br.ufjf.tcc.mail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import br.ufjf.tcc.model.Participacao;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;
import br.ufjf.tcc.pdfHandle.CartaParticipacao;
import br.ufjf.tcc.pdfHandle.CartaParticipacaoOrientador;


//Email número 17 do drive
public class EnviadorEmailCartaParticipacaoOrientador extends EnviadorEmailChain{
	
	private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	
	public EnviadorEmailCartaParticipacaoOrientador() {
		super(null);
	}

	@Override
	protected EmailBuilder gerarEmail(TCC tcc, String statusInicial) {
		List<String> suplentes = new ArrayList<String>();
		List<String> membros = new ArrayList<String>();
		List<Participacao> participacoes = tcc.getParticipacoes();
		
		// Verifica quem prticipou e separa em suplentes e membros
		for(Iterator<Participacao> i = participacoes.iterator(); i.hasNext();) {
			Participacao p = i.next();
			if(p.isParticipou()) {
				if(p.isSuplente())
					suplentes.add(p.getProfessor().getNomeUsuario());
				else
					membros.add(p.getProfessor().getNomeUsuario());
			}
			else
				i.remove();
		}
		
		String nomeAluno = tcc.getAluno().getNomeUsuario();
		String nomeOrientador = tcc.getOrientador().getNomeUsuario();
		String nomeCurso = tcc.getAluno().getCurso().getNomeCurso();
		String titulo = tcc.getNomeTCC();
		String dataDefesa = formatter.format(tcc.getDataApresentacao());
		EmailBuilder emailBuilder = null;
		
		emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Carta de participação da banca - " + nomeAluno);
		emailBuilder.appendMensagem("Prezado(a) " + nomeOrientador + ", ").breakLine(); 
		emailBuilder.appendMensagem("Gostaríamos de agradecer, em nome do Curso " + nomeCurso + ", ");
		emailBuilder.appendMensagem("a sua participação como Orientador(a) do ");
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
		destinatarios.add(tcc.getOrientador());
		inserirDestinatarios(destinatarios, emailBuilder);
		CartaParticipacaoOrientador  cartaParticipacao = new CartaParticipacaoOrientador();
		try {
			String CoOrientador = " ";
			if(tcc.possuiCoorientador())
				CoOrientador = tcc.getCoOrientador().getNomeUsuario();
			cartaParticipacao.gerarCartaParticipacao( nomeCurso, tcc.getOrientador().getNomeUsuario(), nomeAluno, nomeOrientador, tcc.getIdTCC(),
					CoOrientador, titulo, dataDefesa, participacoes, tcc.getOrientador().getMatricula(), tcc.getCertificadoDigital());
			emailBuilder.appendArquivo(cartaParticipacao.getCaminhoArquivo());
			
		} catch(Exception e) {
			System.out.println("Exceção capturada ao gerar carta de participacao - EnviadorEmailCartaParticipacao");
			e.printStackTrace();
		}
		return emailBuilder;
	}
}
