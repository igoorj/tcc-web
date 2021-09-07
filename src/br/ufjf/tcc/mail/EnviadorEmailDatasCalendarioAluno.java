package br.ufjf.tcc.mail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


import br.ufjf.tcc.business.PrazoBusiness;
import br.ufjf.tcc.model.CalendarioSemestre;
import br.ufjf.tcc.model.Prazo;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;


// Email n√∫mero 01 do drive
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
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		CalendarioSemestre calendario = tcc.getCalendarioSemestre();
		List<Prazo> prazos = prazoBusiness.getPrazosByCalendario(calendario);
		String dataLimiteSubmissaoProjeto = formatter.format(prazos.get(0).getDataFinal());
		String dataLimiteEntregaBanca = formatter.format(prazos.get(1).getDataFinal());
		String dataLimiteDefesa = formatter.format(prazos.get(2).getDataFinal());
		String dataLimiteSubmissaoTrabalhoFinal = formatter.format(prazos.get(3).getDataFinal());
				
		emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Datas do calend·rio");
		emailBuilder.appendMensagem("Prezado(a) <b>" + nomeAluno + "</b>,").breakLine();
		emailBuilder.appendMensagem(" vocÍ foi matriculado na disciplina de Trabalho de Conclus„o de Curso (TCC), ");
		emailBuilder.appendMensagem("tendo como orientador(a) <b>"+ nomeOrientador + "</b>. "); 
		
		emailBuilder.breakLine().breakLine();
		emailBuilder.appendHtmlTopico("Prazos para as atividades da disciplina:"); 
		emailBuilder.breakLine().breakLine();
		emailBuilder.appendMensagem("<b>[" + dataLimiteSubmissaoProjeto + "]</b> - Submiss„o do <b>Projeto de TCC</b> no Sistema de Monografias.").breakLine(); 
		emailBuilder.appendMensagem("<b>[" + dataLimiteEntregaBanca + "]</b> - Informar no sistema os <b>Dados da Defesa do TCC</b>, fazer a submiss„o ");
		emailBuilder.appendMensagem("do mesmo e entregar o TCC para a Banca Examinadora.").breakLine();
		emailBuilder.appendMensagem("<b>[" + dataLimiteDefesa + "]</b> - Defesa do TCC.").breakLine();
		emailBuilder.appendMensagem("<b>[" + dataLimiteSubmissaoTrabalhoFinal + "]</b> - Entrega na CoordenaÁ„o das <b>Fichas de AvaliaÁ„o da Banca Examinadora</b> e da <b>Ata de Defesa</b> e, "); 
		emailBuilder.appendMensagem("fazer a <b>submiss„o</b> da Vers„o Final do TCC no Sistema de Monografias.");
//		emailBuilder.appendMensagem("Segue abaixo os prazos limites das atividades desta disciplina:").breakLine(); 
//		emailBuilder.appendMensagem("<b>" + dataLimiteSubmissaoProjeto + "</b> Data limite para submiss√£o do Projeto de TCC no Sistema de Monografias.").breakLine(); 
//		emailBuilder.appendMensagem("<b>" + dataLimiteEntregaBanca + "</b> Data limite para informar no sistema os dados da Defesa do TCC, fazer a submiss√£o ").breakLine();
//		emailBuilder.appendMensagem("do mesmo e entregar o TCC para a Banca Examinadora.").breakLine();
//		emailBuilder.appendMensagem("<b>" + dataLimiteDefesa + "</b> Data limite para a Defesa do TCC.").breakLine();
//		emailBuilder.appendMensagem("<b>" + dataLimiteSubmissaoTrabalhoFinal + "</b> Data limite para entrega na Coordena√ß√£o das Fichas de Avalia√ß√£o da Banca Examinadora e da Ata de Defesa e, ").breakLine(); 
//		emailBuilder.appendMensagem("fazer a submiss√£o da Vers√£o Final do TCC no Sistema de Monografias.");
		
		emailBuilder.breakLine().breakLine();
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
