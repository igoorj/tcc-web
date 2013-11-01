package br.ufjf.tcc.controller;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

import br.ufjf.tcc.business.ParticipacaoBusiness;
import br.ufjf.tcc.business.QuestionarioBusiness;
import br.ufjf.tcc.model.Participacao;
import br.ufjf.tcc.model.Questionario;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;

public class HomeProfessorController extends CommonsController {
	private List<TCC> tccs = new ArrayList<TCC>();
	private Questionario currentQuestionary;
	private boolean currentQuestionaryExists = true,
			currentQuestionaryUsed = true;

	/*
	 * Pega toas as TCCs em que o Usuário tem Participação e verifica se o
	 * Questionário do seu Curso já existe.
	 */
	@Init
	public void init() {
		if (getUsuario().getTipoUsuario().getIdTipoUsuario() < Usuario.PROFESSOR
				&& !checaPermissao("hc__"))
			super.paginaProibida();

		else {
			getUsuario().setParticipacoes(new ParticipacaoBusiness()
					.getParticipacoesByUser(getUsuario()));
			for (Participacao p : getUsuario().getParticipacoes()) {
				tccs.add(p.getTcc());
			}
		}

		currentQuestionary = new QuestionarioBusiness()
				.getCurrentQuestionaryByCurso(getUsuario().getCurso());

		if (currentQuestionary == null)
			currentQuestionaryExists = false;

		currentQuestionaryUsed = new QuestionarioBusiness()
				.isQuestionaryUsed(currentQuestionary);
	}

	public List<TCC> getTccs() {
		return tccs;
	}

	public boolean isCurrentQuestionaryExists() {
		return currentQuestionaryExists;
	}

	public boolean isCurrentQuestionaryUsed() {
		return currentQuestionaryUsed;
	}

	// Formata a data de apresentação para String
	@Command
	public void getTCCDateApresentacao(@BindingParam("tcc") TCC tcc,
			@BindingParam("lbl") Label lbl) {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy, hh:mm");
		lbl.setValue(dateFormat.format(tcc.getDataApresentacao()));
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
		new QuestionarioBusiness().update(currentQuestionary, true, true);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("quest", currentQuestionary);
		map.put("editing", true);
		final Window dialog = (Window) Executions.createComponents(
				"/pages/cadastro-questionario.zul", null, map);
		dialog.doModal();
	}

	@Command
	public void canAnswerTCC(@BindingParam("tcc") TCC tcc,
			@BindingParam("btn") Button btn) {
		btn.setDisabled(tcc.getDataApresentacao().after(
				new Timestamp(new Date().getTime())));
	}

	@Command
	public void showTCC(@BindingParam("tcc") TCC tcc) {
		Sessions.getCurrent().setAttribute("tcc", tcc);
		Sessions.getCurrent().setAttribute("answerTcc", true);
		Executions.sendRedirect("/pages/visualiza-tcc.zul");
	}

}
