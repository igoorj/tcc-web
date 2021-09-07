package br.ufjf.tcc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.DependsOn;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import br.ufjf.tcc.business.CursoBusiness;
import br.ufjf.tcc.business.SalaBusiness;
import br.ufjf.tcc.model.Curso;
import br.ufjf.tcc.model.Sala;
import br.ufjf.tcc.model.Usuario;

public class GerenciamentoSalaController extends CommonsController {
	private SalaBusiness salaBusiness = new SalaBusiness();
	private Sala auxSala = new Sala();
	private Map<Integer, Sala> editTemp = new HashMap<Integer, Sala>();
	private List<Sala> allSalas = salaBusiness.getAll();
	private List<Sala> filterSalas = allSalas;
	private List<Curso> cursos = (new CursoBusiness()).getAll();	
	private String filterString = "";
	private boolean submitSalaListenerExists = false, editingSala = false;
		
	
	
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

	public Sala getAuxSala() {
		return this.auxSala;
	}
	
	public List<Curso> getCursos() {
		return this.cursos;
	}
	
	
	public void setAuxSala(Sala sala) {
		this.auxSala = sala;
//		BindUtils.postNotifyChange(null, null, this, "auxSala");
	}

	public String getFilterString() {
		return filterString;
	}

	public void setFilterString(String filterString) {
		this.filterString = filterString;
	}

	@Command
	public void confirm(@BindingParam("sala") Sala sala) {
		if (salaBusiness.validate(sala)){
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

	@NotifyChange("auxSala")
	@Command 
	public void formSala(@BindingParam("window") Window window, @BindingParam("sala") Sala sala) {
		if(sala == null) {
			editingSala = false;
			sala = new Sala();
			window.setTitle("Criar sala");
		} else {
			editingSala = true;
			window.setTitle("Editar sala");
		}
		this.auxSala = sala;
		window.doModal();
	}
	

	@Command
	public void submitSala(@BindingParam("window") final Window window) {
		Clients.showBusy(window, "Cadastrando...");

		if (!submitSalaListenerExists) {
			submitSalaListenerExists = true;
			window.addEventListener(Events.ON_CLIENT_INFO, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					if (salaBusiness.validate(auxSala)) {
						if (salaBusiness.salvaOuEdita(auxSala)) {
							if(!editingSala) {
								allSalas.add(auxSala);
							}
							filterSalas = allSalas;
							notifySalas();
							Clients.clearBusy(window);
							Messagebox.show("Sala adicionada com sucesso!", "Sucesso", Messagebox.OK,
									Messagebox.INFORMATION);
							limpa();
						} else {
							Clients.clearBusy(window);
							Messagebox.show("Sala não foi adicionada! Ocorreu um erro no banco", "Erro", Messagebox.OK,
									Messagebox.ERROR);
						}
						window.setVisible(false);
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

	
	public void notifySalas() {
		BindUtils.postNotifyChange(null, null, this, "filterSalas");
	}
	
	
	public void limpa() {
		this.auxSala = new Sala();
		BindUtils.postNotifyChange(null, null, this, "auxSala");
	}

}
