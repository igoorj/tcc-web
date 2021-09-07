package br.ufjf.tcc.mail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.ufjf.tcc.business.UsuarioBusiness;
import br.ufjf.tcc.model.Curso;
import br.ufjf.tcc.model.Participacao;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;


// Email nÃºmero 07 do drive
public class EnviadorEmailInformesDadosDefesa extends EnviadorEmailChain{

	public EnviadorEmailInformesDadosDefesa() {
		super(null);
	}
	
	@Override
	protected EmailBuilder gerarEmail(TCC tcc, String statusInicial) {
		EmailBuilder emailBuilder = null;
		
		UsuarioBusiness ub = new UsuarioBusiness();
//		List<Usuario> coordenadores = usuarioBusiness.getCoordenadoresByCurso(tcc.getAluno().getCurso());
//		String nomeCoordenador = coordenadores.get(0).getNomeUsuario();
		
		String nomeAluno = tcc.getAluno().getNomeUsuario();
		String nomeOrientador = tcc.getOrientador().getNomeUsuario();
		Curso curso = tcc.getAluno().getCurso();
		String titulo = tcc.getNomeTCC();
		
		Date dataApresentacao = tcc.getDataApresentacao();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		String dataApresentacaoString = formatter.format(dataApresentacao);
		formatter.applyLocalizedPattern("HH:mm");
		String horaApresentacao = formatter.format(dataApresentacao);
		
		
		List<String> suplentes = new ArrayList<String>();
		List<String> membros = new ArrayList<String>();
		for(Participacao participacao : tcc.getParticipacoes()) {
			if(participacao.isSuplente())
				suplentes.add(participacao.getProfessor().getNomeUsuario());
			else
				membros.add(participacao.getProfessor().getNomeUsuario());
		}
		
		
		emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Informes dos Dados de Defesa - " + nomeAluno);
		emailBuilder.appendMensagem("Prezados, ").breakLine();
		emailBuilder.appendMensagem("no dia " + dataApresentacaoString + " às " + horaApresentacao + " na(o) " + tcc.getSala().getNomeSala() + " acontecerá a ");
		emailBuilder.appendMensagem("Defesa do Trabalho de Conclusão de Curso " + titulo);
		emailBuilder.appendMensagem(" do(a) discente ");
		emailBuilder.appendHtmlTextBold( nomeAluno ).appendMensagem(".").breakLine();
		emailBuilder.appendMensagem("A Banca Examinadora será composta por: ").breakLine().breakLine(); 
		emailBuilder.appendMensagem("<b>Orientador(a):</b> " + nomeOrientador).breakLine();
		if(tcc.possuiCoorientador())
			emailBuilder.appendMensagem("<b>Coorientador(a):</b> " + tcc.getCoOrientador().getNomeUsuario()).breakLine();
		for(String membro : membros) {
			emailBuilder.appendMensagem("<b>Membro da banca:</b> " + membro).breakLine();
		}
		for(String suplente : suplentes) {
			emailBuilder.appendMensagem("<b>Suplente:</b> " + suplente).breakLine();
		}
		emailBuilder.breakLine();
		emailBuilder.appendMensagem("A Coordenação do Curso " + curso.getNomeCurso() + " convida todos os interessados a participarem desta Defesa de TCC.").breakLine(); 
		emailBuilder.appendMensagem("Att.,").breakLine();
//		emailBuilder.appendMensagem(nomeCoordenador).breakLine();
		emailBuilder.appendMensagem("Coordenador(a) do Curso de " + curso.getNomeCurso()).breakLine();
		emailBuilder.appendLinkSistema();
		
		List<Usuario> destinatarios = new ArrayList<>();
		destinatarios.addAll(ub.getSecretariasByCurso(curso));
		inserirDestinatarios(destinatarios, emailBuilder);
	
		return emailBuilder;
		
	}
		
}
