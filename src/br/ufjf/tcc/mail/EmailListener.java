package br.ufjf.tcc.mail;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.log4j.Logger;

import br.ufjf.tcc.business.CalendarioSemestreBusiness;
import br.ufjf.tcc.business.PrazoBusiness;
import br.ufjf.tcc.business.TCCBusiness;
import br.ufjf.tcc.model.CalendarioSemestre;
import br.ufjf.tcc.model.Prazo;
import br.ufjf.tcc.model.TCC;

//@Startup
//@Singleton
public class EmailListener {
	TCCBusiness tccBusiness;
	CalendarioSemestreBusiness calendarioBusiness;
	PrazoBusiness prazoBusiness;
	private Logger logger = Logger.getLogger(EmailListener.class);

	public EmailListener() {
		this.tccBusiness = new TCCBusiness();
		this.calendarioBusiness = new CalendarioSemestreBusiness();
		this.prazoBusiness = new PrazoBusiness();
	}

	@PostConstruct
	public void onStartup() {
		try {
			listener();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final AtomicBoolean alreadyRunning = new AtomicBoolean(false);

	@Schedule(hour = "*/6", persistent = false)
	@Lock(LockType.READ)
	public void listener() throws IOException {
		if (alreadyRunning.getAndSet(true))
			return;

		try {
			logger.info("Executando rotina de envio de e-mails.");
			List<CalendarioSemestre> calendarios = (List<CalendarioSemestre>) this.calendarioBusiness
					.getCurrentCalendars();
			for (CalendarioSemestre calendario : calendarios) {
				this.verificarPrazos(calendario, 2);
			}
		} finally {
			alreadyRunning.set(false);
		}
	}

	public void verificarPrazos(CalendarioSemestre calendario, int diasParaAlerta) {
		logger.info("Verificando prazos do calend�rio de " + calendario.getCurso().getNomeCurso());
		List<Prazo> prazos = prazoBusiness.getPrazosByCalendario(calendario);
		Calendar hoje = Calendar.getInstance();
		List<TCC> tccs = this.tccBusiness.getTrabalhosAndProjetosByCalendar(calendario);

		for (Prazo prazo : prazos) {
			Calendar dataPrazo = (dateToCalendar(prazo.getDataFinal()));
			long diasEntre = this.compareCalendars(hoje, dataPrazo, false);
			if ((diasEntre < 0 || diasEntre > diasParaAlerta)) {
				logger.info("N�o � data para envio. Prazo: " + prazo.getTipo() + ". Data: " + dataPrazo.getTime());
				continue;
			}
			switch (prazo.getTipo()) {
			case Prazo.PRAZO_PROJETO:
				this.enviarEmailAlertaSubmeterProjeto(tccs);
				break;
			case Prazo.ENTREGA_BANCA:
				this.enviarEmailAlertaDadosDeDefesa(tccs);
				break;
//				case Prazo.DEFESA:
//					this.enviarEmailAlertaSubmeterTCC(tccs);
//					break;
			case Prazo.ENTREGA_FINAL:
				this.enviarEmailAlertaSubmeterTCCFinal(tccs);
				break;
			default:
				break;
			}
		}
	}

	/*
	 * Para cada calendário atual, notifica os alunos que ainda não concluiram o
	 * projeto, x dias antes da data limite (por parâmetro)
	 */
	@Lock(LockType.READ)
	public void enviarEmailAlertaSubmeterProjeto(List<TCC> tccs) {
		logger.info("Enviando e-mails de alerta para submeter projeto");
		System.out.println("Enviando e-mails de alerta para submeter projeto");
		List<TCC> projetos = this.tccBusiness.filtraProjetosIncompletos(tccs);
		if (projetos == null)
			return;
		logger.info("Passou");
		System.out.println("Passou");
		for (TCC projeto : projetos) {
			System.out.println("Projeto: " + projeto.isProjeto());
			if (tccBusiness.isProjetoIncompleto(projeto) && !projeto.isEmailAlertaPrazoProjetoSubmetidoEnviado()) {
				logger.info("Nome:" + projeto.getNomeTCC());
				logger.info("Id: " + projeto.getIdTCC());

				EnviadorEmailChain email = new EnviadorEmailAlertaSubmissaoProjeto();
				email.enviarEmail(projeto, null);
				projeto.setEmailAlertaEnviado(1);
				this.tccBusiness.edit(projeto);
			}
		}
	}

	public void enviarEmailAlertaDadosDeDefesa(List<TCC> tccs) {
		logger.info("Enviando e-mails de alerta para submeter dados de defesa");
		List<TCC> trabalhos = this.tccBusiness.filtraTrabalhosIncompletos(tccs);
		if (trabalhos == null)
			return;

		for (TCC trabalho : trabalhos) {
			if (!trabalho.isEmailAlertaPrazoDadosDefesaEnviado()) {
				logger.info("Nome:" + trabalho.getNomeTCC());
				logger.info("Id: " + trabalho.getIdTCC());

				EnviadorEmailChain email = new EnviadorEmailAlertaDadosDeDefesa();
				email.enviarEmail(trabalho, null);
				trabalho.setEmailAlertaEnviado(2);
				this.tccBusiness.edit(trabalho);
			}
		}
	}

//	public void enviarEmailAlertaSubmeterTCC(List<TCC> tccs) {
//		logger.info("Enviando e-mails de alerta para submeter trabalho");
//		List<TCC> trabalhos = this.tccBusiness.filtraTrabalhosIncompletos(tccs);
//		if(trabalhos == null)
//			return;
//		
//		for(TCC trabalho : trabalhos) {
//			if(!trabalho.isEmailAlertaPrazoTrabalhoEnviado()) {
//				logger.info("Nome:" + trabalho.getNomeTCC());
//				logger.info("Id: " + trabalho.getIdTCC());
//				
//				EnviadorEmailChain email = new EnviadorEmailAlertaSubmissaoTrabalho();
//				email.enviarEmail(trabalho, null);
////				trabalho.setEmailAlertaEnviado(3);
////				this.tccBusiness.edit(trabalho);
//			}
//		}
//	}

	public void enviarEmailAlertaSubmeterTCCFinal(List<TCC> tccs) {
		logger.info("Enviando e-mails de alerta para submeter trabalho final");
		List<TCC> trabalhos = this.tccBusiness.filtraTrabalhosEnviadosParaBanca(tccs);
		if (trabalhos == null)
			return;
		for (TCC trabalho : trabalhos) {
			if (!trabalho.isEmailAlertaPrazoTrabalhoFinalEnviado()) {
				logger.info("Nome:" + trabalho.getNomeTCC());
				logger.info("Id: " + trabalho.getIdTCC());

				EnviadorEmailChain email = new EnviadorEmailAlertaSubmissaoTrabalhoFinal();
				email.enviarEmail(trabalho, null);
				trabalho.setEmailAlertaEnviado(3);
				this.tccBusiness.edit(trabalho);
			}
		}
	}

	private Calendar dateToCalendar(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	/**
	 * Compara dois calendários e retorna o número de dias entre os calendários. O
	 * parâmetro compareHour indica se a hora será levada em consideração na
	 * comparação.
	 */
	private long compareCalendars(Calendar prazo, Calendar hoje, boolean compareHour) {

		long daysBetween = ChronoUnit.DAYS.between(prazo.toInstant(), hoje.toInstant());
		if (!compareHour) {
			prazo = getZeroTimeCalendar(prazo);
			hoje = getZeroTimeCalendar(hoje);
		}
		daysBetween = ChronoUnit.DAYS.between(prazo.toInstant(), hoje.toInstant());
		return daysBetween;
	}

	/**
	 * Zera as horas, minutos e segundos. Função utilizada em comparações de datas
	 * em que são considerados apenas os dias, e não a hora exata.
	 */
	private Calendar getZeroTimeCalendar(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}
}
