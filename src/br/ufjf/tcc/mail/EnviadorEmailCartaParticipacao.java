package br.ufjf.tcc.mail;

import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.business.UsuarioBusiness;
import br.ufjf.tcc.model.Participacao;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;


//Email número 17 do drive
public class EnviadorEmailCartaParticipacao extends EnviadorEmailChain{
	
	
	public EnviadorEmailCartaParticipacao() {
		super(null);
	}

	@Override
	protected EmailBuilder gerarEmail(TCC tcc, String statusInicial) {
		EmailBuilder emailBuilder = null;
		UsuarioBusiness usuarioBusiness = new UsuarioBusiness();
		
		List<Usuario> coordenadores = usuarioBusiness.getCoordenadoresByCurso(tcc.getAluno().getCurso());
		
		String nomeAluno = tcc.getAluno().getNomeUsuario();
		String nomeOrientador = tcc.getOrientador().getNomeUsuario();
		String nomeCoordenador = coordenadores.get(0).getNomeUsuario();
		String nomeCurso = tcc.getAluno().getCurso().getNomeCurso();
		String titulo = tcc.getNomeTCC();
		List<Participacao> participacoes = tcc.getParticipacoes();
		
		// Verifica quem prticipou
		for(Participacao participacao : participacoes) {
			if(!participacao.isParticipou()) {
				participacoes.remove(participacao);
			}
		}
		
		
		for(Participacao participacao : participacoes) {
			
			emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Carta de participação da banca - " + nomeAluno);
			emailBuilder.appendMensagem("Prezado(a) " + participacao.getProfessor().getNomeUsuario() + ", ").breakLine(); 
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
			for(Participacao part : participacoes) {
				emailBuilder.appendMensagem(part.getProfessor().getNomeUsuario()).breakLine();				
			}
			emailBuilder.appendMensagem("Atenciosamente,").breakLine();
			emailBuilder.appendMensagem("(assinatura digital do Coordenador)").breakLine(); 
			emailBuilder.appendMensagem("______________________________________").breakLine(); 
			emailBuilder.appendMensagem(nomeCoordenador).breakLine();
			emailBuilder.appendMensagem("Coordenação do Curso " + nomeCurso).breakLine();
			emailBuilder.appendLinkSistema();
			
			List<Usuario> destinatarios = new ArrayList<>();
			destinatarios.add(participacao.getProfessor());
			inserirDestinatarios(destinatarios, emailBuilder);
	
		}
		return emailBuilder;
		
	}
}
