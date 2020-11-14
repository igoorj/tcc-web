package br.ufjf.tcc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import br.ufjf.tcc.business.DepartamentoBusiness;
import br.ufjf.tcc.business.SalaBusiness;
import br.ufjf.tcc.model.Departamento;
import br.ufjf.tcc.model.Sala;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;

public class GerenciamentoSalaController extends CommonsController {
	private SalaBusiness salaBusiness = new SalaBusiness();
	private Sala novaSala = null;
	private Map<Integer, Sala> editTemp = new HashMap<Integer, Sala>();
	private List<Sala> allSalas = salaBusiness.getAll();
	private List<Sala> filterSalas = allSalas;
	private String filterString = "";
	private boolean submitUserListenerExists = false;
		
	
	
	@Init
	public void init() {
		int tipoUsuario = getUsuario().getTipoUsuario().getIdTipoUsuario();
		if(tipoUsuario != Usuario.ADMINISTRADOR) {
			redirectHome();
			return;
		}
		
	}
	
	public List<Sala> getFilterSalas() {
		return filterSalas;
	}

	public Sala getNovaSala() {
		return this.novaSala;
	}

	public void setNovaSala(Sala novaSala) {
		this.novaSala = novaSala;
	}

	public String getFilterString() {
		return filterString;
	}

	public void setFilterString(String filterString) {
		this.filterString = filterString;
	}

//	@Command
//	public void changeEditableStatus(@BindingParam("departamento") Sala sala) {
//		if (!sala.getEditingStatus()) {
//			Sala temp = new Sala();
//			temp.copy(sala);
//			editTemp.put(sala.getIdSala(), temp);
//			departamento.setEditingStatus(true);
//		} else {
//			departamento.copy(editTemp.get(departamento.getIdDepartamento()));
//			editTemp.remove(departamento.getIdDepartamento());
//			departamento.setEditingStatus(false);
//		}
//		refreshRowTemplate(departamento);
//	}

	@Command
	public void confirm(@BindingParam("sala") Sala sala) {
		if (salaBusiness.validate(sala)){
			System.out.println("Entrou no if");
			System.out.println("Id: " + sala.getIdSala());
			System.out.println("Online: " + sala.getOnline());
			System.out.println("Nome: " + sala.getNomeSala());
//				editTemp.get(sala.getIdSala()).getNomeSala())) {
			if (!salaBusiness.editar(sala)) {
				System.out.println("Ocorreu erro");
				Messagebox.show("Não foi possível editar a sala.", "Erro", Messagebox.OK, Messagebox.ERROR);
			}
			editTemp.remove(sala.getIdSala());
			sala.setEditingStatus(false);
			refreshRowTemplate(sala);
		} else {
			String errorMessage = "";
			for (String error : salaBusiness.getErrors())
				errorMessage += error;
			Messagebox.show(errorMessage, "Dados insuficientes / inválidos", Messagebox.OK, Messagebox.ERROR);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Command
	public void delete(@BindingParam("sala") final Sala sala) {
		Messagebox.show(
				"Você tem certeza que deseja deletar a sala: " + sala.getNomeSala() + "?",
				"Confirmação", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					public void onEvent(Event e) {
						if (Messagebox.ON_OK.equals(e.getName())) {

							if (salaBusiness.exclui(sala)) {
								removeFromList(sala);
								Messagebox.show("A sala foi excluída com sucesso.", "Sucesso", Messagebox.OK,
										Messagebox.INFORMATION);
							} else {
								String errorMessage = "A sala não pôde ser excluída.\n";
								for (String error : salaBusiness.getErrors())
									errorMessage += error;
								Messagebox.show(errorMessage, "Erro", Messagebox.OK, Messagebox.ERROR);
							}

						}
					}
				});
	}

	public void removeFromList(Sala sala) {
		filterSalas.remove(sala);
		allSalas.remove(sala);
		BindUtils.postNotifyChange(null, null, this, "filterSalas");
	}

	public void refreshRowTemplate(Sala sala) {
		BindUtils.postNotifyChange(null, null, sala, "editingStatus");
	}

	@Command
	public void filtra() {
		filterSalas = new ArrayList<Sala>();
		String filter = filterString.toLowerCase().trim();
		for (Sala s : allSalas) {
			if(s.getNomeSala().toLowerCase().contains(filter)) {
				filterSalas.add(s);
			}
		}
		BindUtils.postNotifyChange(null, null, this, "filterSalas");
	}

	@Command
	public void addSala(@BindingParam("window") Window window) {
		this.limpa();
		window.doModal();
	}

	@Command
	public void submitSala(@BindingParam("window") final Window window) {
		Clients.showBusy(window, "Cadastrando...");

		if (!submitUserListenerExists) {
			submitUserListenerExists = true;
			window.addEventListener(Events.ON_CLIENT_INFO, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					if (salaBusiness.validate(novaSala)) {
						if (salaBusiness.salvar(novaSala)) {
							allSalas.add(novaSala);
							filterSalas = allSalas;
							notifySalas();
							Clients.clearBusy(window);
							Messagebox.show("Sala adicionada com sucesso!", "Sucesso", Messagebox.OK,
									Messagebox.INFORMATION);
							limpa();
						} else {
							Clients.clearBusy(window);
							Messagebox.show("Sala não foi adicionada!", "Erro", Messagebox.OK,
									Messagebox.ERROR);
						}
					} else {
						String errorMessage = "";
						for (String error : salaBusiness.getErrors())
							errorMessage += error;
						Clients.clearBusy(window);
						Messagebox.show(errorMessage, "Dados insuficientes / inválidos", Messagebox.OK,
								Messagebox.ERROR);
					}
				}
			});
		}

		Events.echoEvent(Events.ON_CLIENT_INFO, window, null);
	}

	@Command
	public void changeEditableStatus(@BindingParam("sala") Sala sala) {
		System.out.println("Id da sala: " + sala.getIdSala());
		if (!sala.getEditingStatus()) {
			Sala temp = new Sala();
			temp.copy(sala);
			editTemp.put(sala.getIdSala(), temp);
			sala.setEditingStatus(true);
		} else {
			sala.copy(editTemp.get(sala.getIdSala()));
			editTemp.remove(sala.getIdSala());
			sala.setEditingStatus(false);
		}
		refreshRowTemplate(sala);
	}
	
	public void notifySalas() {
		BindUtils.postNotifyChange(null, null, this, "filterSalas");
	}

	public void limpa() {
		novaSala = new Sala();
		BindUtils.postNotifyChange(null, null, this, "novaSala");
	}

}
