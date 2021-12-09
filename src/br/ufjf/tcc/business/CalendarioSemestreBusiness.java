package br.ufjf.tcc.business;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import br.ufjf.tcc.model.CalendarioSemestre;
import br.ufjf.tcc.model.Curso;
import br.ufjf.tcc.model.Prazo;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.persistent.impl.CalendarioSemestreDAO;

public class CalendarioSemestreBusiness {
	
	private CalendarioSemestreDAO calendarioSemestreDAO;
	private List<String> errors;

	public CalendarioSemestreBusiness() {
		this.errors = new ArrayList<String>();
		this.calendarioSemestreDAO = new CalendarioSemestreDAO();
	}

	public List<String> getErrors() {
		return errors;
	}

	public boolean validate(CalendarioSemestre calendarioSemestre) {
		errors.clear();

		validateName(calendarioSemestre.getNomeCalendarioSemestre());
		validateDates(calendarioSemestre);

		return errors.size() == 0;
	}

	public void validateName(String name) {
		if (name == null || name.trim().length() == 0)
			errors.add("É necessário informar o nome do calendário;\n");
	}

	public void validateDates(CalendarioSemestre calendario) {
		if (calendario.getFinalSemestre() == null)
			errors.add("É necessário informar a data final;\n");
		else if (new DateTime(calendario.getFinalSemestre()).isBeforeNow())
			errors.add("O final do semestre deve ser em uma data futura;\n");
		Calendar dataFinal = Calendar.getInstance();
		dataFinal.setTime(calendario.getFinalSemestre());
		dataFinal.set(Calendar.HOUR_OF_DAY, 23);
		dataFinal.set(Calendar.MINUTE, 59);		
		calendario.setFinalSemestre(dataFinal.getTime());

		
		String imprimeDataFinal = "\nData Final";
		imprimeDataFinal += "\ngetTime():" + dataFinal.getTime();
		imprimeDataFinal += "\ngetTimeZone(): " + dataFinal.getTimeZone();
		imprimeDataFinal += "\ngetDate(): " + dataFinal.getTime().getDate();
		imprimeDataFinal += "\ngetMonth(): " + dataFinal.getTime().getMonth();
		imprimeDataFinal += "\ngetHour(): " + dataFinal.getTime().getHours();
		imprimeDataFinal += "\ngetMinute(): " + dataFinal.getTime().getMinutes();
		
		imprimeDataFinal += "\n\nCalendario Semestre Final";
		imprimeDataFinal += "\ngetDate(): " +calendario.getFinalSemestre().getDate();
		imprimeDataFinal += "\ngetMonth(): " +calendario.getFinalSemestre().getMonth();
		imprimeDataFinal += "\ngetHour(): " +calendario.getFinalSemestre().getHours();
		imprimeDataFinal += "\ngetMinute(): " + calendario.getFinalSemestre().getMinutes();
	
		System.out.println(imprimeDataFinal);
	
	}

	public boolean save(CalendarioSemestre calendarioSemestre) {
		return calendarioSemestreDAO.salvar(calendarioSemestre);
	}

	public CalendarioSemestre getCurrentCalendarByCurso(Curso curso) {
		return calendarioSemestreDAO.getCalendarByDateAndCurso(new Date(),
				curso);
	}

	public CalendarioSemestre getCalendarById(int id) {
		return calendarioSemestreDAO.getCalendarById(id);
	}
	
	public CalendarioSemestre getCalendarByTCC(TCC tcc) {
		return calendarioSemestreDAO.getCalendarByTCC(tcc);
	}
	
	public boolean updateFimSemCalendarById(Date fim,int id) {
		return calendarioSemestreDAO.updateFimSemCalendarById(fim, id);
	}
	
	public List<CalendarioSemestre> getCurrentCalendars(){
		return calendarioSemestreDAO.getCalendarsByDate(new Date());
	}
}
