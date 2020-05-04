package br.ufjf.tcc.controller;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.AMedia;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Iframe;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import br.ufjf.tcc.business.DepartamentoBusiness;
import br.ufjf.tcc.business.ParticipacaoBusiness;
import br.ufjf.tcc.business.PrazoBusiness;
import br.ufjf.tcc.business.TCCBusiness;
import br.ufjf.tcc.business.UsuarioBusiness;
import br.ufjf.tcc.library.FileManager;
import br.ufjf.tcc.library.SessionManager;
import br.ufjf.tcc.mail.EnviadorEmailAvisoProjetoSubmetido;
import br.ufjf.tcc.mail.EnviadorEmailAvisoTrabalhoFinalSubmetido;
import br.ufjf.tcc.mail.EnviadorEmailChain;
import br.ufjf.tcc.mail.EnviadorEmailChainTAAProfessor;
import br.ufjf.tcc.mail.EnviadorEmailProjetoCriado;
import br.ufjf.tcc.model.Departamento;
import br.ufjf.tcc.model.Participacao;
import br.ufjf.tcc.model.Prazo;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.TipoUsuario;
import br.ufjf.tcc.model.Usuario;

public class EditorTccController extends CommonsController {

	private TCCBusiness tccBusiness = new TCCBusiness();
	private Usuario tempUser = null;
	private TCC tcc = null;
	private String statusInicialTCC = "";
	private Iframe iframe;
	private InputStream tccFile = null, extraFile = null;
	private AMedia pdf = null;
	private List<Departamento> departamentos;
	private List<Usuario> orientadores = new ArrayList<Usuario>();
	private boolean canChangeOrientacao = false, alunoEditBlock = true, canChangeMatricula = false, canEditUser = false,
			alunoVerified = false, tccFileChanged = false, extraFileChanged = false, hasSubtitulo = false,
			canChangeParticipacao = false, hasCoOrientador = false, orientadorWindow = true, trabFinal = false,
			canSubmitTCC = true;
	private EnviadorEmailChain enviadorEmail = new EnviadorEmailProjetoCriado();

	@Init
	public void init() {
		String tccId = Executions.getCurrent().getParameter("id");
		int tipoUsuario = getUsuario().getTipoUsuario().getIdTipoUsuario();
		switch (tipoUsuario) {
		case Usuario.ALUNO:
			TCC tempTCC = tccBusiness.getCurrentNotFinishedTCCByAuthor(getUsuario(), getCurrentCalendar());
			// getUsuario().getTcc().clear();
			// if (tempTCC != null) {
			// getUsuario().getTcc().add(tempTCC);
			// tcc = getUsuario().getTcc().get(0);
			// statusInicialTCC = tcc.getStatusTCC();
			// }
			if (tempTCC != null) {
				tcc = tempTCC;
				statusInicialTCC = tcc.getStatusTCC();
			}
			// TODO Remover essa função daqui (cadastrar tcc)
			else {
				if (!getUsuario().isAtivo())
					redirectHome();
				else {
					tcc = new TCC();
					tcc.setAluno(new Usuario());
					tcc.setProjeto(true);
					if (getUsuario().getCurso() != null)
						tcc.getAluno().setCurso(getUsuario().getCurso());
					tcc.setAluno(getUsuario());

					statusInicialTCC = tcc.getStatusTCC();

				}
			}
			canChangeOrientacao = false;
			verificarCanChangeParticipacao();
			verificarCanSubmitTCC();

			break;

		case Usuario.ADMINISTRADOR:

		case Usuario.COORDENADOR:
			canChangeMatricula = true;
			canChangeOrientacao = true;

		case Usuario.SECRETARIA:
			canEditUser = true;
			canChangeOrientacao = true;

		default:
			if (tccId != null && tccId.trim().length() > 0) {
				tcc = tccBusiness.getTCCById(Integer.parseInt(tccId.trim()));
			} else if (canEditUser) {
				tcc = new TCC();
				tcc.setAluno(new Usuario());
				if (getUsuario().getCurso() != null)
					tcc.getAluno().setCurso(getUsuario().getCurso());
				canChangeMatricula = true;
			}

			if (tcc == null || !canEdit())
				redirectHome();

		}
		if (tcc != null) {
			hasCoOrientador = (tcc.getCoOrientador() != null);
			hasSubtitulo = (tcc.getSubNomeTCC() != null);
		}
		departamentos = (new DepartamentoBusiness()).getAll();
	}

	private boolean canEdit() {
		return (tcc.getOrientador() == null || tcc.getOrientador().getIdUsuario() == getUsuario().getIdUsuario()
				|| getUsuario().getTipoUsuario().getIdTipoUsuario() == Usuario.ADMINISTRADOR
				|| (getUsuario().getTipoUsuario().getIdTipoUsuario() == Usuario.COORDENADOR
						|| (getUsuario().getTipoUsuario().getIdTipoUsuario() == Usuario.SECRETARIA
								&& tcc.getDataEnvioFinal() != null)
								&& getUsuario().getCurso().getIdCurso() == tcc.getAluno().getCurso().getIdCurso()));
	}

	public boolean isCanChangeOrientacao() {
		return canChangeOrientacao;
	}
	
	public boolean isCanChangeParticipacao() {
		return canChangeParticipacao;
	}
	
	public boolean isCanSubmitTCC() {
		return canSubmitTCC;
	}
	
	public String getMensagem() {
		return "esta é a mensagem";
	}
	
	public TCC getTcc() {
		return tcc;
	}

	public void setTcc(TCC tcc) {
		this.tcc = tcc;
	}

	public boolean isAlunoEditBlock() {
		return alunoEditBlock;
	}

	public List<Departamento> getDepartamentos() {
		return departamentos;
	}

	public boolean getHasCoOrientador() {
		return hasCoOrientador;
	}

	@Command("setHasCoOrientador")
	@NotifyChange({ "hasCoOrientador", "tcc" })
	public void setHasCoOrientador() {
		hasCoOrientador = !hasCoOrientador;
		if (!hasCoOrientador)
			tcc.setCoOrientador(null);
	}

	public boolean getHasSubtitulo() {
		return hasSubtitulo;
	}

	@Command("setHasSubtitulo")
	@NotifyChange("hasSubtitulo")
	public void setHasSubtitulo() {
		hasSubtitulo = !hasSubtitulo;
		if (!hasSubtitulo)
			tcc.setSubNomeTCC(null);
	}

	public List<Usuario> getOrientadores() {
		return orientadores;
	}

	public boolean isOrientadorWindow() {
		return orientadorWindow;
	}

	public boolean isCanChangeMatricula() {
		return canChangeMatricula;
	}

	public boolean getCanEditUser() {
		return canEditUser;
	}

	public boolean isUserSecretaria() {
		return (getUsuario().getTipoUsuario().getIdTipoUsuario() == Usuario.SECRETARIA);
	}

	public Usuario getTempUser() {
		return tempUser;
	}

	public void setTempUser(Usuario tempUser) {
		this.tempUser = tempUser;
	}

	@NotifyChange({ "tcc", "alunoEditBlock" })
	@Command
	public void verifyAluno(@BindingParam("textBox") Textbox textb, @BindingParam("button") Button bt,
			@BindingParam("label") Label lbl) {
		if (bt.getLabel().equals("Verificar")) {
			lbl.setVisible(true);
			if (tcc.getAluno().getMatricula() != null && tcc.getAluno().getMatricula().trim().length() > 0) {
				Usuario aluno = new UsuarioBusiness().getByMatricula(tcc.getAluno().getMatricula().trim());
				if (aluno != null) {
					if (aluno.getTipoUsuario().getIdTipoUsuario() != Usuario.ALUNO
							|| getUsuario().getTipoUsuario().getIdTipoUsuario() != Usuario.ADMINISTRADOR
									&& aluno.getCurso().getIdCurso() != getUsuario().getCurso().getIdCurso()) {
						lbl.setValue("Usuário existe mas não é um aluno ou pertence a outro curso.");
						alunoEditBlock = true;
						alunoVerified = false;
					} else {
						tcc.setAluno(aluno);
						lbl.setValue("Usuário já cadastrado.");
						alunoEditBlock = true;
						alunoVerified = true;
						textb.setReadonly(true);
						bt.setLabel("Editar");
					}
				} else {
					if (getUsuario().getTipoUsuario().getIdTipoUsuario() != Usuario.ADMINISTRADOR) {
						alunoEditBlock = false;
						lbl.setValue("Usuário ainda não cadastrado.Faça o cadastro abaixo.");
						alunoVerified = true;
						textb.setReadonly(true);
						bt.setLabel("Editar");
					} else {
						alunoEditBlock = true;
						lbl.setValue("Usuário ainda não cadastrado.Cadastre ele no menu de Usuários.");
						alunoVerified = false;
					}
				}
			} else {
				alunoEditBlock = true;
				lbl.setValue("É necessário digitar a matrícula.");
				alunoVerified = false;
			}
		} else {
			alunoEditBlock = true;
			alunoVerified = false;
			lbl.setVisible(false);
			textb.setReadonly(false);
			textb.setAttribute("readonly", false);
			bt.setLabel("Verificar");
		}
	}

	@Command
	public void showTCC(@BindingParam("iframe") Iframe iframe) {
		this.iframe = iframe;

		InputStream is;
		if (tcc.getArquivoTCCFinal() != null)
			is = FileManager.getFileInputSream(tcc.getArquivoTCCFinal());
		else if (tcc.getArquivoTCCBanca() != null)
			is = FileManager.getFileInputSream(tcc.getArquivoTCCBanca());
		else
			is = FileManager.getFileInputSream("modelo.pdf");

		AMedia amedia = new AMedia(tcc.getNomeTCC() + ".pdf", "pdf", "application/pdf", is);
		iframe.setContent(amedia);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Command
	public void upload(@BindingParam("evt") UploadEvent evt) {
		System.out.println("Teste");
		String alerta1 = "Você está enviando a versão final do seu trabalho?";
		final String alerta2 = "Atenção, após submeter a versão final do seu trabalho e clicar em atualizar, ele não poderá mais ser alterado. Deseja continuar?";
		// TODO essa verificação seria de trabalho Final
		if (getUsuario().getTipoUsuario().getIdTipoUsuario() == Usuario.ALUNO) {
			if (tcc != null && tcc.getDataApresentacao() != null && tcc.isQuantidadeParticipacoesValidas()) {
				if (!tcc.isProjeto() && tcc.getDataApresentacao().before(new Date())) {
					Messagebox.show(alerta1, "Aviso Importante", Messagebox.YES | Messagebox.NO, Messagebox.EXCLAMATION,
							new org.zkoss.zk.ui.event.EventListener() {
								public void onEvent(Event evt) throws InterruptedException {
									if (evt.getName().equals("onYes")) {
										Messagebox.show(alerta2, "Aviso Importante", Messagebox.YES | Messagebox.NO,
												Messagebox.EXCLAMATION, new org.zkoss.zk.ui.event.EventListener() {

													public void onEvent(Event evt) throws InterruptedException {
														if (evt.getName().equals("onYes")) {
															trabFinal = true;

														} else {
															return;
														}

													}
												});
									} else
										return;
								}
							});
				}
			}
		}

		String fileName = evt.getMedia().getName();
		if (!FilenameUtils.getExtension(fileName).equals("pdf")) {
			Messagebox.show("Este não é um arquivo válido! Apenas PDF são aceitos.", "Formato inválido", Messagebox.OK,
					Messagebox.INFORMATION);
			tccFile = null;
			return;
		}

		pdf = new AMedia(tcc.getNomeTCC(), "pdf", "application/pdf", evt.getMedia().getByteData());
		tccFile = evt.getMedia().getStreamData();
		tccFileChanged = true;
		iframe.setContent(pdf);
		Messagebox.show("Arquivo enviado com sucesso.", "Confirmação", Messagebox.OK, Messagebox.INFORMATION,
				new org.zkoss.zk.ui.event.EventListener() {
					public void onEvent(Event evt) throws InterruptedException {
						if (evt.getName().equals("onOK")) {

						}
					}
				});
	}

	@Command
	public void extraUpload(@BindingParam("evt") UploadEvent evt) {
		extraFile = evt.getMedia().getStreamData();
		extraFileChanged = true;
		Messagebox.show("Arquivo enviado com sucesso.");
	}

	@Command
	public void selectedDepartamento(@BindingParam("dep") Comboitem combDep) {
		Departamento dep = (Departamento) combDep.getValue();
		tempUser = null;
		orientadores.clear();
		if (dep != null)
			orientadores = new UsuarioBusiness().getAllByDepartamento(dep);

		BindUtils.postNotifyChange(null, null, this, "tempUser");
		BindUtils.postNotifyChange(null, null, this, "orientadores");
	}

	// Editor Orientador

	@Command
	@NotifyChange("orientadorWindow")
	public void changeOrientador(@BindingParam("window") Window window) {
		orientadorWindow = true;
		window.doModal();
	}

	@Command
	@NotifyChange("orientadorWindow")
	public void changeCoOrientador(@BindingParam("window") Window window) {
		orientadorWindow = false;
		window.doModal();
	}

	@Command
	public void selectOrientacao(@BindingParam("window") Window window) {
		if (tempUser != null) {
			if (participacoesContains(tempUser)) {
				Messagebox.show(
						"Você escolheu um professor que já está incluído na Banca Examinadora. Se ele é seu Orientador, por favor retire-o da Banca antes.",
						"Inválido", Messagebox.OK, Messagebox.ERROR);
			} else if (orientadorWindow && tcc.getCoOrientador() != null
					&& tcc.getCoOrientador().getIdUsuario() == tempUser.getIdUsuario()) {
				Messagebox.show("Você escolheu um professor que já é seu Co-Orientador.", "Inválido", Messagebox.OK,
						Messagebox.ERROR);
			} else if (!orientadorWindow && tcc.getOrientador().getIdUsuario() == tempUser.getIdUsuario()) {
				Messagebox.show("Você escolheu um professor que já é seu Orientador.", "Inválido", Messagebox.OK,
						Messagebox.ERROR);
			} else {
				if (orientadorWindow)
					tcc.setOrientador(tempUser);
				else
					tcc.setCoOrientador(tempUser);
				BindUtils.postNotifyChange(null, null, this, "tcc");
			}
		} else
			Messagebox.show("Você não selecionou um professor.", "Erro", Messagebox.OK, Messagebox.ERROR);
		tempUser = null;
		BindUtils.postNotifyChange(null, null, this, "tempUser");
		window.setVisible(false);
	}

	private boolean participacoesContains(Usuario tempUser) {
		boolean find = false;

		for (Participacao p : tcc.getParticipacoes())
			if (p.getProfessor().getIdUsuario() == tempUser.getIdUsuario()) {
				find = true;
				break;
			}

		return find;
	}

	// Editor Banca
	@Command
	public void addToBanca() {
		if (tempUser != null) {
			if (!participacoesContains(tempUser) && tempUser.getIdUsuario() != tcc.getOrientador().getIdUsuario()
					&& (tcc.getCoOrientador() == null
							|| tempUser.getIdUsuario() != tcc.getCoOrientador().getIdUsuario())) {
				Participacao p = new Participacao();
				p.setProfessor(tempUser);
				p.setTcc(tcc);
				if (tempUser.getTitulacao() != null)
					p.setTitulacao(tempUser.getTitulacao());
				tcc.getParticipacoes().add(p);
				BindUtils.postNotifyChange(null, null, this, "tcc");
			} else {
				Messagebox.show("Esse professor já está na lista ou é o orientador/co-orientador do TCC", "Erro",
						Messagebox.OK, Messagebox.ERROR);
			}
		}
	}

	@Command
	public void removeFromBanca(@BindingParam("participacao") Participacao p) {
		tcc.getParticipacoes().remove(p);
		BindUtils.postNotifyChange(null, null, this, "tcc");
	}

	@Command
	public void submitBanca(@BindingParam("window") Window window) {
		orientadores.clear();
		orientadores.add(tcc.getOrientador());
		window.setVisible(false);
	}

	public boolean validaAutor(TCC tcc) {
		if (tcc.getAluno().getMatricula() == null || tcc.getAluno().getMatricula().isEmpty())
			return false;
		if (tcc.getAluno().getNomeUsuario() == null || tcc.getAluno().getNomeUsuario().isEmpty())
			return false;
		if (tcc.getAluno().getEmail() == null || tcc.getAluno().getEmail().isEmpty())
			return false;

		return true;
	}

	// Submit do TCC
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Command("submit")
	public void submit() {
		System.out.println("TeTESTETEEEE");
		int statusTCC = tcc.getStatus();
		int tipoUsuario = getUsuario().getTipoUsuario().getIdTipoUsuario();
		List<EnviadorEmailChain> email = new ArrayList<EnviadorEmailChain>();
		if (tccBusiness.validateTCC(tcc, statusTCC)) {
			atualizarArquivos();
			switch (statusTCC) {
			case TCC.PI:
				tcc.setStatus(TCC.PAA);
				email.add(new EnviadorEmailAvisoProjetoSubmetido());
				break;
			case TCC.PAA:
				System.out.println("Entrou no PAA");
				break;
			case TCC.PR:
				tcc.setStatus(TCC.PAA);
				email.add(new EnviadorEmailAvisoProjetoSubmetido());
				break;
			case TCC.TI:
				tcc.setStatus(TCC.TAAO);
				email.add(new EnviadorEmailChainTAAProfessor());
				break;
			case TCC.TAAO:
				break;
			case TCC.TRO:
				tcc.setStatus(TCC.TAAO);
				email.add(new EnviadorEmailAvisoTrabalhoFinalSubmetido());
				break;
			case TCC.TAAC:
				break;
			case TCC.TRC:
				break;
			case TCC.APROVADO:
				break;
			default:
				break;
			}

		} else {
			String errorMessage = "";
			for (String error : tccBusiness.getErrors())
				errorMessage += error;
			Messagebox.show(errorMessage, "Dados insuficientes / inválidos", Messagebox.OK, Messagebox.ERROR);
		}

		// TODO
		// Perguntar o que é isso
		System.out.println("\n\n" + new Date().toString());
		if (trabFinal) {
			tcc.setTrabFinal(true);
		}
		if (tipoUsuario == Usuario.SECRETARIA && (tcc.getArquivoTCCFinal() == null && !tccFileChanged)) {
			Messagebox.show("É necesário enviar o documento PDF.", "Erro", Messagebox.OK, Messagebox.ERROR);
			return;
		}
		if (tipoUsuario == Usuario.SECRETARIA && (!validaAutor(tcc))) {
			Messagebox.show("É necesário informar os dados do Autor.", "Erro", Messagebox.OK, Messagebox.ERROR);
			return;
		}

		if (tipoUsuario != Usuario.ALUNO && (tcc.getAluno() == null)) {
			Messagebox.show("Antes de enviar é necesário validar a matricula do aluno no botão de verificar.", "Erro",
					Messagebox.OK, Messagebox.ERROR);
			return;
		}

		tcc.setDataEnvioBanca(new Timestamp(new Date().getTime()));
		if (tccBusiness.validate(tcc)) {

			// TODO perguntar o que é isso
			if (!alunoEditBlock) {
				UsuarioBusiness usuarioBusiness = new UsuarioBusiness();
				if (usuarioBusiness.validate(tcc.getAluno(), null, false)) {
					tcc.getAluno().setSenha(usuarioBusiness.encripta(usuarioBusiness.generatePassword()));
					TipoUsuario aluno = new TipoUsuario();
					aluno.setIdTipoUsuario(Usuario.ALUNO);
					tcc.getAluno().setTipoUsuario(aluno);
					if (!usuarioBusiness.salvar(tcc.getAluno())) {
						Messagebox.show("Devido a um erro, o Autor não foi cadastrado.", "Erro", Messagebox.OK,
								Messagebox.ERROR);
						return;
					}
				} else {
					String errorMessage = "Aluno:\n";
					for (String error : usuarioBusiness.getErrors())
						errorMessage += error;
					Messagebox.show(errorMessage, "Dados insuficientes / inválidos", Messagebox.OK, Messagebox.ERROR);

					return;
				}
			}

			if (SessionManager.getAttribute("projeto") != null) {
				tcc.setProjeto(((boolean) SessionManager.getAttribute("projeto")));
				SessionManager.setAttribute("projeto", false);
			}
//	        tcc.setCalendarioSemestre(getCurrentCalendar(tcc.getAluno().getCurso()));
//	        TCC tccAux = tccBusiness.getCurrentTCCByAuthor(tcc.getAluno(), getCurrentCalendar(tcc.getAluno().getCurso()));
//	        if(tccAux!=null)
//	        if(tccAux.getIdTCC()!=tcc.getIdTCC())
//			{
//				Messagebox.show("Este usuário ja possui um trabalho iniciado neste período", "Erro",
//						Messagebox.OK, Messagebox.ERROR);
//					return;
//			}

			if (tccBusiness.saveOrEdit(tcc)) {
				String alerta;
				if (tcc.isProjeto())
					alerta = "Projeto salvo!";
				else
					alerta = "Trabalho salvo!";

				// TODO remover
//				
//				//ENVIA E-MAIL
//				if(tipoUsuario != Usuario.COORDENADOR && tipoUsuario != Usuario.SECRETARIA) {
//					enviadorEmail.enviarEmail(tcc, statusInicialTCC);
//				}

				Messagebox.show(alerta, "Confirmação", Messagebox.OK, Messagebox.EXCLAMATION,
						new org.zkoss.zk.ui.event.EventListener() {
							public void onEvent(Event evt) throws InterruptedException {
								if (evt.getName().equals("onOK")) {
									redirectHome();
								}
							}
						});

				if (!new ParticipacaoBusiness().updateList(tcc)) {
					Messagebox.show("Não foi possível salvar as alterações da Banca Examinadora.", "Erro",
							Messagebox.OK, Messagebox.ERROR);

					return;
				}

			} else {
				Messagebox.show("Devido a um erro, o trabalho não foi cadastrado.", "Erro", Messagebox.OK,
						Messagebox.ERROR);
			}

		} else {
			String errorMessage = "";
			for (String error : tccBusiness.getErrors())
				errorMessage += error;
			Messagebox.show(errorMessage, "Dados insuficientes / inválidos", Messagebox.OK, Messagebox.ERROR);
		}

	}

	// Update do TCC
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Command("updateTCC")
	public void updateTCC() {
		int statusTCC = tcc.getStatus();
		int tipoUsuario = getUsuario().getTipoUsuario().getIdTipoUsuario();
		if (tccBusiness.validateTCC(tcc, statusTCC)) {
			atualizarArquivos();
			if (tccBusiness.saveOrEdit(tcc)) {
				String alerta;
				if (tcc.isProjeto())
					alerta = "Projeto salvo!\n" + "Não se esqueça de submetê-lo quando estiver concluído";
				else
					alerta = "Trabalho salvo!\n" + "Não se esqueça de submetê-lo quando estiver concluído";

				Messagebox.show(alerta, "Confirmação", Messagebox.OK, Messagebox.EXCLAMATION,
						new org.zkoss.zk.ui.event.EventListener() {
							public void onEvent(Event evt) throws InterruptedException {
								if (evt.getName().equals("onOK")) {
									redirectHome();
								}
							}
						});

				if (!new ParticipacaoBusiness().updateList(tcc)) {
					Messagebox.show("Não foi possível salvar as alterações da Banca Examinadora.", "Erro",
							Messagebox.OK, Messagebox.ERROR);

					return;
				}

			} else {
				Messagebox.show("Devido a um erro, o trabalho não foi salvo.", "Erro", Messagebox.OK, Messagebox.ERROR);
			}

		} else {
			String errorMessage = "";
			for (String error : tccBusiness.getErrors())
				errorMessage += error;
			Messagebox.show(errorMessage, "Dados insuficientes / inválidos", Messagebox.OK, Messagebox.ERROR);
		}

		// TODO
		// Perguntar o que é isso
		System.out.println("\n\n" + new Date().toString());
		if (trabFinal) {
			tcc.setTrabFinal(true);
		}
		if (tipoUsuario == Usuario.SECRETARIA && (tcc.getArquivoTCCFinal() == null && !tccFileChanged)) {
			Messagebox.show("É necesário enviar o documento PDF.", "Erro", Messagebox.OK, Messagebox.ERROR);
			return;
		}
		if (tipoUsuario == Usuario.SECRETARIA && (!validaAutor(tcc))) {
			Messagebox.show("É necesário informar os dados do Autor.", "Erro", Messagebox.OK, Messagebox.ERROR);
			return;
		}

		if (tipoUsuario != Usuario.ALUNO && (tcc.getAluno() == null)) {
			Messagebox.show("Antes de enviar é necesário validar a matricula do aluno no botão de verificar.", "Erro",
					Messagebox.OK, Messagebox.ERROR);
			return;
		}

	}

	public boolean validateTCC(TCC tcc, int status) {

		return false;
	}

	// Verifica se o arquivo foi atualizado
	public void atualizarArquivos() {
		if (tccFileChanged && tccFile != null) {
			savePDF();
			tccFileChanged = false;
			try {
				tccFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			tccFile = null;
		} else if (getUsuario().getTipoUsuario().getIdTipoUsuario() != Usuario.SECRETARIA
				&& tcc.getArquivoTCCBanca() == null)
			Messagebox.show("Você não enviou o documento PDF. Lembre-se de enviá-lo depois.", "Aviso", Messagebox.OK,
					Messagebox.EXCLAMATION);

		if (extraFileChanged && extraFile != null) {
			saveExtraFile();
			extraFileChanged = false;
			try {
				extraFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			extraFile = null;
		}
	}

	@Command
	public void savePDF() {
		System.out.println("teste arquivo tcc banca");
		String newFileName = FileManager.saveFileInputSream(tccFile, "pdf");
		if (newFileName != null) {
			switch (getUsuario().getTipoUsuario().getIdTipoUsuario()) {
			case Usuario.SECRETARIA:
				FileManager.deleteFile(tcc.getArquivoTCCFinal());
				tcc.setArquivoTCCFinal(newFileName);
				break;
			// TODO Verificar se essas condições fazem sentido
			default:
				if (tcc.getArquivoTCCFinal() != null) {
					FileManager.deleteFile(tcc.getArquivoTCCFinal());
					tcc.setArquivoTCCFinal(newFileName);
				} else {
					FileManager.deleteFile(tcc.getArquivoTCCBanca());
					tcc.setArquivoTCCBanca(newFileName);
				}
				break;
			}
		}
	}

	@Command
	public void saveExtraFile() {
		String newFileName = FileManager.saveFileInputSream(extraFile, ".pdf");
		if (newFileName != null) {
			if (tcc.getArquivoExtraTCCFinal() != null) {
				FileManager.deleteFile(tcc.getArquivoExtraTCCFinal());
				tcc.setArquivoExtraTCCFinal(newFileName);
			} else {
				FileManager.deleteFile(tcc.getArquivoExtraTCCBanca());
				tcc.setArquivoExtraTCCBanca(newFileName);
			}
		}
	}

	/**
	 * Retorna false (para não desabilitar) se a data de aprensentação já tiver
	 * ocorrido
	 * 
	 * @return
	 */
	@Command
	public boolean exibirParticipou() {
		return false;
//		System.out.println("teste 2");
//		if(tcc != null && tcc.getDataApresentacao() != null) {
//			PrazoBusiness prazoB = new PrazoBusiness();
//			Prazo prazoDefesa = prazoB.getPrazoDataDefesaByCalendario(getCurrentCalendar());
//			int comparacao = tcc.getDataApresentacao().compareTo(prazoDefesa.getDataFinal());
//			// 0 para datas iguais, positivo para futuro, e negativo se estiver no passado
//			System.out.println("Teste");
//			if(comparacao <= 0) {
//				return true;
//				
//			}
//		}
//		System.out.println("teste 3");
//		return false;
	}

	public void verificarCanChangeParticipacao() {
		Timestamp dataApresentacao = tcc.getDataApresentacao();
		if (dataApresentacao != null) {
			// 0 se for igual, negativo se for antes
			int comparacao = tcc.getDataApresentacao().compareTo(new Date());
			if (comparacao <= 0) {
				canChangeParticipacao = true;
				return;
			}
		}
		canChangeParticipacao = false;
	}
	
	public void verificarCanSubmitTCC() {
		int status = tcc.getStatus();
		if(status == TCC.PAA || status == TCC.TAAC || status == TCC.TAAO)
			canSubmitTCC = false;
	}

	public boolean isProject() {
//		if(SessionManager.getAttribute("projeto") != null)
//			return (boolean) SessionManager.getAttribute("projeto");
//		else
		if (tcc != null)
			return tcc.isProjeto();
		return false;
	}

	/**
	 * Marca o membro da banca como suplente (ou remove)
	 */
	@Command
	public void onCheckSuplente(@BindingParam("membro") Participacao p, @BindingParam("checked") boolean checked) {
		p.setSuplente(checked);
	}

	/**
	 * Marca o membro se participou ou não da banca de defesa do tcc
	 */
	@Command
	public void onCheckParticipou(@BindingParam("membro") Participacao p, @BindingParam("checked") boolean checked) {
		p.setParticipou(checked);
	}

	public boolean isAlunoVerified() {
		return alunoVerified;
	}

	public void setAlunoVerified(boolean alunoVerified) {
		this.alunoVerified = alunoVerified;
	}

}
