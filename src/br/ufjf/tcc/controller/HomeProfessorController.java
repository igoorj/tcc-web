package br.ufjf.tcc.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.South;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Window;

import br.ufjf.tcc.business.AvisoBusiness;
import br.ufjf.tcc.business.CursoBusiness;
import br.ufjf.tcc.business.ParticipacaoBusiness;
import br.ufjf.tcc.business.PrazoBusiness;
import br.ufjf.tcc.business.QuestionarioBusiness;
import br.ufjf.tcc.business.SalaBusiness;
import br.ufjf.tcc.business.TCCBusiness;
import br.ufjf.tcc.library.SessionManager;
import br.ufjf.tcc.mail.EnviadorEmailAvisoProjetoAprovado;
import br.ufjf.tcc.mail.EnviadorEmailChain;
import br.ufjf.tcc.mail.EnviadorEmailEmMassa;
import br.ufjf.tcc.model.Aviso;
import br.ufjf.tcc.model.CalendarioSemestre;
import br.ufjf.tcc.model.Curso;
import br.ufjf.tcc.model.Participacao;
import br.ufjf.tcc.model.Prazo;
import br.ufjf.tcc.model.Questionario;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;

public class HomeProfessorController extends CommonsController {
	private List<TCC> allTccs = new ArrayList<TCC>(); // inclui TCCs finalizados
	private List<TCC> tccs = new ArrayList<TCC>(); // apenas TCCs em aberto
	private List<TCC> filterTccs; // apenas TCCs do tipo selecionado
	private Questionario currentQuestionary;
	private boolean currentQuestionaryExists = true,
			currentQuestionaryUsed = true, currentCalendarExists = true,
			showAll = false;
	private List<Aviso> avisos;
	private Aviso aviso = new Aviso();
	private List<Prazo> prazos;
	private int currentPrazo = 0;
	private PrazoBusiness prazoBusiness = new PrazoBusiness();
	private int typeSelecionado = 0;

	/*
	 * Pega todas as TCCs em que o Prof/Coord tem Participa√ß√£o e verifica se o
	 * Question√°rio e o Calend√°rio do seu Curso j√° existe.
	 */
	@Init
	public void init() {
		allTccs.addAll(new TCCBusiness()
				.getTCCsByUserParticipacao(getUsuario()));
		allTccs.addAll(new TCCBusiness().getTCCsByOrientador(getUsuario()));

		Collections.sort(allTccs, new Comparator<TCC>() {
			@Override
			public int compare(TCC arg0, TCC arg1) {
				if (arg0.getDataApresentacao() == null)
					return -1;
				else if (arg1.getDataApresentacao() == null)
					return 1;
				else
					return (arg0.getDataApresentacao().before(
							arg1.getDataApresentacao()) ? -1 : (arg0
							.getDataApresentacao().equals(
									arg1.getDataApresentacao()) ? 0 : 1));
			}
		});

		for (TCC tcc : allTccs)
			if (tcc.getDataEnvioFinal() == null)
				tccs.add(tcc);

		filterTccs = tccs;
		
		CalendarioSemestre currentCalendar = getCurrentCalendar();
		currentCalendarExists = currentCalendar != null;
		if (currentCalendarExists) {
			currentQuestionary = new QuestionarioBusiness()
					.getCurrentQuestionaryByCurso(getUsuario().getCurso());

			currentQuestionaryExists = currentQuestionary != null;
			if (currentQuestionary != null) {
				currentQuestionaryUsed = new QuestionarioBusiness()
						.isQuestionaryUsed(currentQuestionary);
			}
			
			prazos = getCurrentCalendar().getPrazos();
			
			DateTime currentDay = new DateTime(new Date());

			for (int i = prazos.size() - 1; i >= 0; i--)
				if (currentDay.isAfter(new DateTime(prazos.get(i)
						.getDataFinal()))) {
					currentPrazo = i + 1;
					break;
				}
		}
	}
	
	public List<Prazo> getPrazos() {
		return prazos;
	}
	
	public void setPrazos(List<Prazo> prazos) {
		this.prazos = prazos;
	}
	
	public int getCurrentPrazo() {
		return currentPrazo;
	}

	public List<Aviso> getAvisos() {
		if (avisos == null){
			if(getUsuario().getTipoUsuario().getIdTipoUsuario()==Usuario.ADMINISTRADOR){
				avisos = (new AvisoBusiness().getAllAvisos());
			}else{
			avisos = (new AvisoBusiness()).getAvisosByCurso(getUsuario()
					.getCurso());
			}

		}
		return avisos;
	}

	public Aviso getAviso() {
		return aviso;
	}
	
	@SuppressWarnings({"unchecked","rawtypes"})
	@Command
	public void envarEmailEmMassa(@BindingParam("window") final Window window, 
				@BindingParam("corpoEmail") final Textbox corpoEmail, 
				@BindingParam("tituloEmail") final Textbox tituloEmail, 
				@BindingParam("enviarParaAlunos") final Checkbox enviarParaAlunos,
				@BindingParam("enviarParaOrientadores") final Checkbox enviarParaOrientadores) {
		final boolean fromAdmin = getUsuario().getTipoUsuario().getIdTipoUsuario() == Usuario.ADMINISTRADOR;
		if(!enviarParaAlunos.isChecked() && !enviarParaOrientadores.isChecked()) {
			Messagebox.show("… necess·rio informar para quem o e-mail ser· enviado.", "Aviso", Messagebox.OK, Messagebox.ERROR);
			return;
		}
		if(corpoEmail == null || corpoEmail.getValue().equals("")) {
			Messagebox.show("… necess·rio informar o corpo do e-mail.", "Aviso", Messagebox.OK, Messagebox.ERROR);
			return;
		}
		Messagebox.show("VocÍ tem certeza que deseja enviar o e-mail?", "ConfirmaÁ„o", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, new org.zkoss.zk.ui.event.EventListener() {
		    public void onEvent(Event evt) throws InterruptedException {
		        if (evt.getName().equals("onYes")) {
		        	String titulo = tituloEmail.getValue();
		        	if(titulo == null || titulo.equals("")) {
		        		titulo = fromAdmin ? 
	        				"Mensagem do Administrador do Sistema" :
	    					"Mensagem do Coordenador de Curso";
		        	}
		        	EnviadorEmailEmMassa emailEmMassa = new EnviadorEmailEmMassa();
		        	emailEmMassa.setEnviarParaAlunos(enviarParaAlunos.isChecked());
		        	emailEmMassa.setEnviarParaOrientadores(enviarParaOrientadores.isChecked());
		        	emailEmMassa.setCorpoEmail(corpoEmail.getValue());
		        	emailEmMassa.setCurso(getUsuario().getCurso());
		        	emailEmMassa.setCalendario(getCurrentCalendar());
		        	emailEmMassa.setFromAdmin(fromAdmin);
		        	emailEmMassa.setTitulo(titulo);
		        	emailEmMassa.enviarEmail(null, null);
		        	if (window != null)
						window.detach();
		        	Executions.sendRedirect("/pages/home-professor.zul");
		        }
		    }
		});
	}

	@Command
	public void addAviso() {
		AvisoBusiness avisoBussiness = new AvisoBusiness();
		List<Curso> cursos = new CursoBusiness().getAll();
		if(getUsuario().getTipoUsuario().getIdTipoUsuario()== Usuario.ADMINISTRADOR ){
			int k=0;
			for(int i=0;i<cursos.size();i++){
				aviso.setCurso(cursos.get(i));
				if(avisoBussiness.save(aviso)){
					
					k++;
					
				}else
					Messagebox.show("Erro ao salvar!", "Erro", Messagebox.OK,
							Messagebox.ERROR);
			}
			
			
			if(k==(cursos.size())){
				avisos.add(aviso);
				BindUtils.postNotifyChange(null, null, this, "aviso");
				BindUtils.postNotifyChange(null, null, this, "avisos");
				Messagebox.show("Mensagem salva com sucesso.", "Enviada",
						Messagebox.OK, Messagebox.INFORMATION);
				
			}
		}else{
			aviso.setCurso(getUsuario().getCurso());
			if (avisoBussiness.validate(aviso)) {
				if (avisoBussiness.save(aviso)) {
					Messagebox.show("Mensagem salva com sucesso.", "Enviada",
							Messagebox.OK, Messagebox.INFORMATION);
					avisos.add(aviso);
					aviso = new Aviso();
					BindUtils.postNotifyChange(null, null, this, "aviso");
					BindUtils.postNotifyChange(null, null, this, "avisos");
				} else
					Messagebox.show("Erro ao salvar!", "Erro", Messagebox.OK,
							Messagebox.ERROR);
			} else {
				String errorMessage = "";
				for (String error : avisoBussiness.getErrors())
					errorMessage += error;
				Messagebox.show(errorMessage, "Dados insuficientes / inv√°lidos",
						Messagebox.OK, Messagebox.ERROR);
			}
		}
	}

	@Command
	public void deleteAviso(@BindingParam("aviso") Aviso aviso) {
		if(getUsuario().getTipoUsuario().getIdTipoUsuario()==Usuario.ADMINISTRADOR){
			if ((new AvisoBusiness()).deleteAll(aviso)) {
				avisos.remove(aviso);
				BindUtils.postNotifyChange(null, null, this, "avisos");
			} else
				Messagebox.show("Erro ao deletar!", "Erro", Messagebox.OK,
						Messagebox.ERROR);
		}else{
			if ((new AvisoBusiness()).delete(aviso)) {
				avisos.remove(aviso);
				BindUtils.postNotifyChange(null, null, this, "avisos");
			} else
				Messagebox.show("Erro ao deletar!", "Erro", Messagebox.OK,
						Messagebox.ERROR);
		}
	}

	public List<TCC> getFilterTccs() {
		return filterTccs;
	}

	public boolean isCurrentQuestionaryExists() {
		return currentQuestionaryExists;
	}

	public boolean isCurrentQuestionaryUsed() {
		return currentQuestionaryUsed;
	}

	public boolean isCurrentCalendarExists() {
		return currentCalendarExists;
	}

	public boolean isShowAll() {
		return showAll;
	}

	public void setShowAll(boolean showAll) {
		this.showAll = showAll;
	}

	@NotifyChange("filterTccs")
	@Command
	public void filterType(@BindingParam("type") int type) {
		this.typeSelecionado = type;
		int idUsuarioLogado = getUsuario().getIdUsuario();
		switch (typeSelecionado) {
		case 0:
			filterTccs = showAll ? allTccs : tccs;
			break;
		case 1:
			filterTccs = new ArrayList<TCC>();
			for (TCC t : showAll ? allTccs : tccs){
				int idOrientador = t.getOrientador().getIdUsuario();
				int idCoOrientador = t.getCoOrientador() !=null ? t.getCoOrientador().getIdUsuario() : -1;
				if (idOrientador == idUsuarioLogado || idCoOrientador == idUsuarioLogado){
					filterTccs.add(t);
				}
			}
			break;
		case 2:
			filterTccs = new ArrayList<TCC>();
			for (TCC t : showAll ? allTccs : tccs){
				if (isInParticipacoesTCC(getUsuario(), t))
					filterTccs.add(t);
			}
			break;
		}
	}
	
	private boolean isInParticipacoesTCC(Usuario professor, TCC t) {
		List<Participacao> parts = new ParticipacaoBusiness().getParticipacoesUsuarioByTCC(t);
		for (Participacao participacao : parts) {
			if(participacao.getProfessor().getIdUsuario() == professor.getIdUsuario()){
				return true;
			}
		}
		return false;
	}

	@NotifyChange("filterTccs")
	@Command
	public void showAllTccs() {
		if(showAll){
			filterTccs = allTccs;
		}else{
			filterType(this.typeSelecionado);
		}
	}

	// Formata a data de apresenta√ß√£o para String
	@Command
	public void getTCCApresentacao(@BindingParam("tcc") TCC tcc,
			@BindingParam("lbl") Label lbl) {
		if (tcc.getDataApresentacao() != null) {
			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy, HH:mm");
			lbl.setValue(dateFormat.format(tcc.getDataApresentacao()));
			
			if (tcc.getSala() != null) {
				lbl.setValue(lbl.getValue() + " - Sala " + new SalaBusiness().getSalaByTcc(tcc).getNomeSala());
			}
			else if(tcc.getSalaDefesa() != null) {
				lbl.setValue(lbl.getValue() + " - Sala " + tcc.getSalaDefesa());
			}
			
		} else{
			lbl.setValue("N„o Informada");
		}
	}

	@Command
	public void createQuestionary() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("curso", getUsuario().getCurso());
		map.put("editing", false);
		final Window dialog = (Window) Executions.createComponents(
				"/pages/cadastro-questionario.zul", null, map);
		dialog.doModal();
	}

	@Command
	public void createQuestionaryFromOld() {
		final Window dialog = (Window) Executions.createComponents(
				"/pages/lista-questionarios.zul", null, null);
		dialog.doModal();
	}

	@Command
	public void editQuestionary() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("quest", currentQuestionary);
		map.put("editing", true);
		final Window dialog = (Window) Executions.createComponents(
				"/pages/cadastro-questionario.zul", null, map);
		dialog.doModal();
	}

	@Command
	public void createCalendar() {
		final Window dialog = (Window) Executions.createComponents(
				"/pages/cadastro-calendario.zul", null, null);		
		dialog.doModal();
	
	}

	@Command
	public void editCalendar() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("calendar", getCurrentCalendar());
		map.put("editing", true);
		final Window dialog = (Window) Executions.createComponents(
				"/pages/cadastro-prazos.zul", null, map);
		dialog.doModal();
	}

	@Command
	public void showTCC(@BindingParam("tcc") TCC tcc) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", tcc.getIdTCC());
		
		final Window window = (Window) Executions.createComponents(
				"/pages/visualiza.zul", null, map);
		window.doModal();
	}
	
	@Command
	public void formatDate(@BindingParam("dataFinal") Date dataFinal,
			@BindingParam("label") Label label) {
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		label.setValue(df.format(dataFinal));
	}

	@Command
	public void getDescription(@BindingParam("tipo") int type,
			@BindingParam("label") Label label) {
		label.setValue(prazoBusiness.getDescription(type));
	}
}
