package br.ufjf.tcc.mail;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import br.ufjf.tcc.business.CalendarioSemestreBusiness;
import br.ufjf.tcc.business.PrazoBusiness;
import br.ufjf.tcc.business.TCCBusiness;
import br.ufjf.tcc.model.CalendarioSemestre;
import br.ufjf.tcc.model.Prazo;
import br.ufjf.tcc.model.TCC;

@Startup
@Singleton
public class EmailListener {
	TCCBusiness tccBusiness;
	CalendarioSemestreBusiness calendarioBusiness;
	PrazoBusiness prazoBusiness;
	Calendar calendar;
	
	public EmailListener() {
		this.calendar = null;
		this.tccBusiness = new TCCBusiness();
		this.calendarioBusiness = new CalendarioSemestreBusiness();
		this.prazoBusiness = new PrazoBusiness();
	}
		
	private final AtomicBoolean alreadyRunning = new AtomicBoolean(false);
	
	@Schedule(hour="*", minute="*", second="*/10", persistent = true)
	@Lock(LockType.READ)
	public void listener() throws IOException {
		if (alreadyRunning.getAndSet(true)) return;
		
		try
        {
			System.out.println("teste listeneeer");
//			TCC tcc = this.tccBusiness.getTCCById(438);
//			EnviadorEmailCartaParticipacao email = new EnviadorEmailCartaParticipacao();
//			email.enviarEmails(tcc);
			List<CalendarioSemestre> calendarios = (List<CalendarioSemestre>) this.calendarioBusiness.getCurrentCalendars();
			this.calendar = Calendar.getInstance();
			for(CalendarioSemestre calendario : calendarios) {
				this.verificarPrazos(calendario, 2);
			}
			
        }
        finally
        {
            alreadyRunning.set(false);
        }
	}
	
	public void verificarPrazos(CalendarioSemestre calendario, int diasParaAlerta) {
		List<Prazo> prazos = prazoBusiness.getPrazosByCalendario(calendario);
		Calendar dataOffset = (Calendar) this.calendar.clone();
		dataOffset.add(Calendar.DATE, diasParaAlerta);							// Adiciona dias ao dia de hoje
		
		for (Prazo prazo : prazos) {
			switch (prazo.getTipo()){
				case Prazo.PRAZO_PROJETO:
					this.verificarPrazoProjetoSubmetido(calendario, dataOffset, prazo);
					break;
				case Prazo.ENTREGA_BANCA:
					this.verificarPrazoDadosDeDefesa(calendario, dataOffset, prazo);
					break;
				case Prazo.DEFESA:
					this.verificarPrazoSubmissaoTCC(calendario, dataOffset, prazo);
					break;
				case Prazo.ENTREGA_FINAL:
					this.verificarPrazoSubmissaoTCCfinal(calendario, dataOffset, prazo);
					break;
				default:
					break;	
			}
		}
	}
	
	/**
	 * Para cada calendário atual, notifica os alunos 
	 * que ainda não concluiram o projeto, x dias antes da data limite (por parâmetro) 
	 */
	@Lock(LockType.READ)
	public void verificarPrazoProjetoSubmetido(CalendarioSemestre calendario, Calendar dataOffset,  Prazo prazo) {
		Calendar dataFinalPrazo = (dateToCalendar(prazo.getDataFinal()));	// Transforma a data final do calendario do semestre (Date) em Calendar
		boolean datasIguais = this.compareCalendars(dataOffset, dataFinalPrazo, false);
		if(!datasIguais)
			return;
		
		List<TCC> projetos = this.tccBusiness.getProjetosByCalendar(calendario);
		projetos = this.tccBusiness.filtraProjetosIncompletos(projetos);
		if(projetos == null)
			return;
		for(TCC projeto : projetos) {
			if(tccBusiness.isProjetoIncompleto(projeto) && !projeto.isEmailAlertaPrazoProjetoSubmetidoEnviado()) {
				System.out.println("Enviando e-mail de alerta para submeter projeto");
				System.out.println("Nome:" + projeto.getNomeTCC());
				System.out.println("Id: " + projeto.getIdTCC());
				
				EnviadorEmailChain email = new EnviadorEmailAlertaSubmissaoProjeto();
				email.enviarEmail(projeto, null);
				projeto.setEmailAlertaEnviado(1);
				this.tccBusiness.edit(projeto);
			}
		}
	}
	
	
	public void verificarPrazoDadosDeDefesa(CalendarioSemestre calendario, Calendar dataOffset,  Prazo prazo) {
		Calendar dataFinalPrazo = (dateToCalendar(prazo.getDataFinal()));	// Transforma a data final do calendario do semestre (Date) em Calendar
		boolean datasIguais = this.compareCalendars(dataOffset, dataFinalPrazo, false);
		if(!datasIguais)
			return;
//		System.out.println(dataOffset.getTime() + "  " + dataFinalPrazo.getTime());
		List<TCC> trabalhos = this.tccBusiness.getTrabalhosByCalendar(calendario);
		trabalhos = this.tccBusiness.filtraTrabalhosIncompletos(trabalhos);
		System.out.println("teste 1");
		if(trabalhos == null)
			return;
		
		for(TCC trabalho : trabalhos) {
			System.out.println(trabalho.getIdTCC());
			System.out.println(trabalho.getEmailsAlertaEnviados());
			if(tccBusiness.isTrabalhoIncompleto(trabalho) && !trabalho.isEmailAlertaPrazoDadosDefesaEnviado()) {
				System.out.println("Enviando e-mail de alerta para submeter dados de defesa");
				System.out.println("Nome:" + trabalho.getNomeTCC());
				System.out.println("Id: " + trabalho.getIdTCC());
				
				EnviadorEmailChain email = new EnviadorEmailAlertaDadosDeDefesa();
				email.enviarEmail(trabalho, null);
				trabalho.setEmailAlertaEnviado(2);
				this.tccBusiness.edit(trabalho);
			}
		}
	}
	
	
	public void verificarPrazoSubmissaoTCC(CalendarioSemestre calendario, Calendar dataOffset,  Prazo prazo) {
		Calendar dataFinalPrazo = (dateToCalendar(prazo.getDataFinal()));	// Transforma a data final do calendario do semestre (Date) em Calendar
		boolean datasIguais = this.compareCalendars(dataOffset, dataFinalPrazo, false);
		if(!datasIguais)
			return;
		
		List<TCC> trabalhos = this.tccBusiness.getTrabalhosByCalendar(calendario);
		trabalhos = this.tccBusiness.filtraTrabalhosIncompletos(trabalhos);
		
		if(trabalhos == null)
			return;
		
		for(TCC trabalho : trabalhos) {
			if(!trabalho.isEmailAlertaPrazoTrabalhoEnviado()) {
				System.out.println("Enviando e-mail de alerta para submeter trabalho");
				System.out.println("Nome:" + trabalho.getNomeTCC());
				System.out.println("Id: " + trabalho.getIdTCC());
				
				EnviadorEmailChain email = new EnviadorEmailAlertaSubmissaoTrabalho();
				email.enviarEmail(trabalho, null);
				trabalho.setEmailAlertaEnviado(3);
				this.tccBusiness.edit(trabalho);
			}
		}
	}
	
	
	public void verificarPrazoSubmissaoTCCfinal(CalendarioSemestre calendario, Calendar dataOffset,  Prazo prazo) {
		Calendar dataFinalPrazo = (dateToCalendar(prazo.getDataFinal()));	// Transforma a data final do calendario do semestre (Date) em Calendar
		boolean datasIguais = this.compareCalendars(dataOffset, dataFinalPrazo, false);
		if(!datasIguais)
			return;
		
		List<TCC> trabalhos = this.tccBusiness.getTrabalhosByCalendar(calendario);
		trabalhos = this.tccBusiness.filtraTrabalhosEnviadosParaBanca(trabalhos);
		if(trabalhos == null)
			return;
		
		for(TCC trabalho : trabalhos) {
			if(tccBusiness.isTrabalhoEnviadoParaBanca(trabalho) && !trabalho.isEmailAlertaPrazoTrabalhoFinaloEnviado()) {
				System.out.println("Enviando e-mail de alerta para submeter trabalho final");
				System.out.println("Nome:" + trabalho.getNomeTCC());
				System.out.println("Id: " + trabalho.getIdTCC());
				
				EnviadorEmailChain email = new EnviadorEmailAlertaSubmissaoTrabalhoFinal();
				email.enviarEmail(trabalho, null);
				trabalho.setEmailAlertaEnviado(4);
				this.tccBusiness.edit(trabalho);
			}
		}
	}
	
	private Calendar dateToCalendar(Date date){ 
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}
	
	
	/**
	 * Compara dois calendários e retorna verdadeiro se forem iguais.
	 *  O parâmetro compareHour indica se a hora será levada em 
	 *  consideração na comparação.
	 */
	private boolean compareCalendars(Calendar cal1, Calendar cal2, boolean compareHour) {
		if(!compareHour) {
			cal1 = getZeroTimeCalendar(cal1);
			cal2 = getZeroTimeCalendar(cal2);
		}
		if(cal1.compareTo(cal2) == 0)
			return true;
		return false;
	}
	
	/**
	 * Zera as horas, minutos e segundos.
	 * Função utilizada em comparações de datas em 
	 * que são considerados apenas os dias, e não a hora exata.
	 */
	private Calendar getZeroTimeCalendar(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}
}
