package br.ufjf.tcc.mail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.ufjf.tcc.business.PrazoBusiness;
import br.ufjf.tcc.model.Prazo;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;


//Email número 12 do drive
public class EnviadorEmailAlertaSubmissaoTrabalhoFinal extends EnviadorEmailChain{

	public EnviadorEmailAlertaSubmissaoTrabalhoFinal() {
		super(null);
	}
	
	@Override
	protected EmailBuilder gerarEmail(TCC tcc, String statusInicial) {
		EmailBuilder emailBuilder = null;
		
//		UsuarioBusiness usuarioBusiness = new UsuarioBusiness();
//		List<Usuario> coordenadores = usuarioBusiness.getCoordenadoresByCurso(tcc.getAluno().getCurso());
//		String nomeCoordenador = coordenadores.get(0).getNomeUsuario();
		
		String nomeAluno = tcc.getAluno().getNomeUsuario();
		String nomeOrientador = tcc.getOrientador().getNomeUsuario();
		String nomeCurso = tcc.getAluno().getCurso().getNomeCurso();
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Prazo prazo = new PrazoBusiness().getPrazoByTipoAndCalendario(Prazo.ENTREGA_FINAL, tcc.getCalendarioSemestre());
		String prazoString = formatter.format(prazo.getDataFinal());
		
		emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Aviso de prazo de submiss�o de trabalho final - " + nomeAluno);
		emailBuilder.appendMensagem("Prezados <b>" + nomeAluno + "</b> e <b>" + nomeOrientador + "</b>, ");
		emailBuilder.breakLine().breakLine();
		emailBuilder.appendMensagem("dentro de <b>2 dias (" + prazoString + ")</b> se encerra o prazo ");
		emailBuilder.appendMensagem("para incluir no Sistema de Monografias a vers�o Final ");
		emailBuilder.appendMensagem("do TCC, ap�s corre��es sugeridas pelos membros da Banca Examinadora. ").breakLine().breakLine();
		emailBuilder.appendMensagem("Ainda n�o consta no sistema que o(a) discente realizou esta atividade ");
		emailBuilder.appendMensagem("completamente. Por isso a Coordena��o solicita que o(a) discente ");
		emailBuilder.appendMensagem("preencha todas as informa��es no sistema para esta atividade se tornar completa.");
		emailBuilder.breakLine().breakLine();
		emailBuilder.appendMensagem("Se essa tarefa n�o for cumprida dentro do prazo, ");
		emailBuilder.appendMensagem("n�o haver� como dar andamento das demais atividades. ");
		emailBuilder.appendMensagem("Desta forma n�o ser� poss�vel deixar esse TCC P�blico no Sistema de Monografias.").breakLine();
		emailBuilder.appendMensagem("Att.,").breakLine();
//		emailBuilder.appendMensagem(nomeCoordenador).breakLine();
		emailBuilder.appendMensagem("Coordenador(a) do Curso de " + nomeCurso).breakLine();
		emailBuilder.appendLinkSistema();
		
		List<Usuario> destinatarios = new ArrayList<>();
		destinatarios.add(tcc.getAluno());
		destinatarios.add(tcc.getOrientador());
		inserirDestinatarios(destinatarios, emailBuilder);
	
		return emailBuilder;
		
	}
	
	private Calendar dateToCalendar(Date date){ 
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

}
