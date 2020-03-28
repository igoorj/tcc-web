package br.ufjf.tcc.mail;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import br.ufjf.tcc.business.PrazoBusiness;
import br.ufjf.tcc.model.CalendarioSemestre;
import br.ufjf.tcc.model.Prazo;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;


// Email número 01 do drive
public class EnviadorEmailDatasCalendarioAluno extends  EnviadorEmailChain{
	
	
	public EnviadorEmailDatasCalendarioAluno() {
		super(null);
	}

	@Override
	protected EmailBuilder gerarEmail(TCC tcc, String statusInicial) {
		EmailBuilder emailBuilder = null;
		PrazoBusiness prazoBusiness = new PrazoBusiness();
		
//		UsuarioBusiness usuarioBusiness = new UsuarioBusiness();
//		List<Usuario> coordenadores = usuarioBusiness.getCoordenadoresByCurso(tcc.getAluno().getCurso());
//		String nomeCoordenador = coordenadores.get(0).getNomeUsuario();
		
		String nomeAluno = tcc.getAluno().getNomeUsuario();
		String nomeOrientador = tcc.getOrientador().getNomeUsuario();
		String nomeCurso = tcc.getAluno().getCurso().getNomeCurso();
		
		CalendarioSemestre calendario = tcc.getCalendarioSemestre();
		List<Prazo> prazos = prazoBusiness.getPrazosByCalendario(calendario);
		String dataLimiteSubmissaoProjeto = new DateTime(prazos.get(0).getDataFinal()).toString("dd/MM/yyyy");
		String dataLimiteEntregaBanca = new DateTime(prazos.get(1).getDataFinal()).toString("dd/MM/yyyy");
		String dataLimiteDefesa = new DateTime(prazos.get(2).getDataFinal()).toString("dd/MM/yyyy");
		String dataLimiteSubmissaoTrabalhoFinal = new DateTime(prazos.get(3).getDataFinal()).toString("dd/MM/yyyy");
				
		emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Datas do calendário");
		emailBuilder.appendMensagem("Prezado(a) " + nomeAluno + ",").breakLine();
		emailBuilder.appendMensagem(" você foi matriculado na disciplina de Trabalho de Conclusão de Curso (TCC), ");
		emailBuilder.appendMensagem("tendo como orientador(a) "+ nomeOrientador + ". ").breakLine(); 
		emailBuilder.appendMensagem("Segue abaixo os prazos limites das atividades desta disciplina:").breakLine(); 
		emailBuilder.appendMensagem(dataLimiteSubmissaoProjeto + " Data limite para submissão do Projeto de TCC no Sistema de Monografias.").breakLine(); 
		emailBuilder.appendMensagem(dataLimiteEntregaBanca + " Data limite para informar os dados da Defesa do TCC.").breakLine();
		emailBuilder.appendMensagem(dataLimiteEntregaBanca + " Data limite para submissão do TCC no Sistema de Monografias e entrega do trabalho para a Banca Examinadora.").breakLine(); 
		emailBuilder.appendMensagem(dataLimiteDefesa + " Data limite para a Defesa do TCC.").breakLine();
		emailBuilder.appendMensagem(dataLimiteSubmissaoTrabalhoFinal + " Data limite para entrega na Coordenação das Fichas de Avaliação da Banca Examinadora da Ata de Defesa.").breakLine();
		emailBuilder.appendMensagem(dataLimiteSubmissaoTrabalhoFinal + " Data limite para submissão da Versão Final do TCC no Sistema de Monografias.").breakLine().breakLine();
		emailBuilder.appendMensagem("Att.,").breakLine();
//		emailBuilder.appendMensagem(nomeCoordenador).breakLine(); 
		emailBuilder.appendMensagem("Coordenador(a) do Curso de " + nomeCurso).breakLine();
		emailBuilder.appendLinkSistema();
		
		List<Usuario> destinatarios = new ArrayList<>();
		destinatarios.add(tcc.getAluno());
		inserirDestinatarios(destinatarios, emailBuilder);
	
		return emailBuilder;
		
	}
}
