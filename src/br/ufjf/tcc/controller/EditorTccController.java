
package br.ufjf.tcc.controller;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.AMedia;
import org.zkoss.zhtml.Filedownload;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Iframe;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import br.ufjf.tcc.business.DepartamentoBusiness;
import br.ufjf.tcc.business.ParticipacaoBusiness;
import br.ufjf.tcc.business.SalaBusiness;
import br.ufjf.tcc.business.TCCBusiness;
import br.ufjf.tcc.business.UsuarioBusiness;
import br.ufjf.tcc.library.FileManager;
import br.ufjf.tcc.mail.EnviadorEmailAvisoProjetoSubmetido;
import br.ufjf.tcc.mail.EnviadorEmailAvisoTrabalhoFinalAprovadoPorOrientador;
import br.ufjf.tcc.mail.EnviadorEmailAvisoTrabalhoFinalSubmetido;
import br.ufjf.tcc.mail.EnviadorEmailChain;
import br.ufjf.tcc.mail.EnviadorEmailChainTAAProfessor;
import br.ufjf.tcc.mail.EnviadorEmailDatasCalendarioAluno;
import br.ufjf.tcc.mail.EnviadorEmailDatasCalendarioOrientador;
import br.ufjf.tcc.mail.EnviadorEmailInformesDadosDefesa;
import br.ufjf.tcc.mail.EnviadorEmailProjetoCriado;
import br.ufjf.tcc.model.CalendarioSemestre;
import br.ufjf.tcc.model.Curso;
import br.ufjf.tcc.model.Departamento;
import br.ufjf.tcc.model.Participacao;
import br.ufjf.tcc.model.Sala;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.TipoUsuario;
import br.ufjf.tcc.model.Usuario;

public class EditorTccController extends CommonsController {

	private TCCBusiness tccBusiness = new TCCBusiness();
	private Usuario tempUser = null;
	private Sala tempSala = null;
	private Timestamp tempDataApresentacao;
	private TCC tcc = null;
	private String statusInicialTCC = "";
	private String tempLinkSala = "";
	private Iframe iframe;
	private InputStream tccFile = null, extraFile = null, docFile = null;
	private AMedia pdf = null;
	private List<Departamento> departamentos;
	private List<Sala> salas;
	private List<Usuario> orientadores = new ArrayList<Usuario>();
	private boolean canChangeOrientacao = false, alunoEditBlock = true, canChangeMatricula = false, canEditUser = false,
			alunoVerified = false, tccFileChanged = false, extraFileChanged = false, docFileChanged = false, hasSubtitulo = false,
			canChangeParticipacao = false, canChangeBanca = false, hasCoOrientador = false, orientadorWindow = true, trabFinal = false,
			canSubmitTCC = true, canSubmitDocs = false, tccAtrasado = false, canUpdateTCC = false, alterouOrientador = false;
	
	private Logger logger = Logger.getLogger(EditorTccController.class);

	@Init
	public void init() {
		if (!getUsuario().isAtivo())
			redirectHome();
		String tccId = Executions.getCurrent().getParameter("id");
		int tipoUsuario = getUsuario().getTipoUsuario().getIdTipoUsuario();
		Curso cursoOrigem = getUsuario().getCurso(); //add
		int idCursoOrigem = cursoOrigem.getIdCurso(); // add
		
		switch (tipoUsuario) {
		case Usuario.ALUNO:
			TCC tempTCC = tccBusiness.getCurrentNotFinishedTCCByAuthor(getUsuario(), getCurrentCalendar());
			// Cria novo projeto
			if (tempTCC == null) {
				tempTCC = createTCC(getUsuario());
			}
			tcc = tempTCC;
			statusInicialTCC = tcc.getStatusTCC();
			
			canChangeOrientacao = true;
			verificarAtrasado();
			verificarCanChangeParticipacao();
			verificarCanSubmitTCC();
			verificarCanSubmitDocs();
			verificarCanChangeBanca();
			break;

		case Usuario.ADMINISTRADOR:

		case Usuario.COORDENADOR:
			canChangeParticipacao = true;
			canChangeBanca = true;
			canChangeMatricula = true;
			canChangeOrientacao = true;
			canSubmitTCC = false;
			canUpdateTCC = true;
			canSubmitDocs = true;
			
		case Usuario.SECRETARIA:
			canEditUser = true;
			canChangeOrientacao = true;
			canSubmitDocs = true;
			canUpdateTCC = true; // added
			
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
			if (tcc == null || !canEdit() && !(tipoUsuario == Usuario.SECRETARIA)) {
				redirectHome();
				verificarAtrasado();
			} /*else if(!(tipoUsuario == Usuario.SECRETARIA && idCursoOrigem == 5)) {  // Tratamento forçado
					verificarAtrasado();
			 }*/
		}
		
		if (tcc != null) {
			tempSala = tcc.getSala();
			tempLinkSala = tcc.getLinkSala();
			tempDataApresentacao = tcc.getDataApresentacao();
			hasCoOrientador = (tcc.getCoOrientador() != null);
			hasSubtitulo = (tcc.getSubNomeTCC() != null);
			tccBusiness.preenchebanca(tcc);
		}
		departamentos = (new DepartamentoBusiness()).getAll();
		salas = (new SalaBusiness()).getAllByCurso(tcc.getAluno().getCurso());
	}

	private boolean canEdit() {
		return (tcc.getOrientador() == null || tcc.getOrientador().getIdUsuario() == getUsuario().getIdUsuario()
				|| getUsuario().getTipoUsuario().getIdTipoUsuario() == Usuario.ADMINISTRADOR
				|| (getUsuario().getTipoUsuario().getIdTipoUsuario() == Usuario.COORDENADOR
						|| (getUsuario().getTipoUsuario().getIdTipoUsuario() == Usuario.SECRETARIA
								&& tcc.getDataEnvioFinal() != null)
								&& getUsuario().getCurso().getIdCurso() == tcc.getAluno().getCurso().getIdCurso()));
	}
	
	public boolean canSubmitDocs() {
		return canSubmitDocs;
	}

	public boolean isCanChangeOrientacao() {
		return canChangeOrientacao;
	}
	
	public boolean isCanChangeParticipacao() {
		return canChangeParticipacao;
	}
	
	public boolean isCanChangeBanca() {
		return canChangeBanca;
	}
	
	public boolean isCanSubmitTCC() {
		return canSubmitTCC;
	}
	
	public boolean isCanUpdateTCC() {
		return canUpdateTCC;
	}
	
	public String getMensagem() {
		if(tccAtrasado) {
			String projeto = tcc.isProjeto() ? "projeto" : "trabalho";
			return "Você perdeu a data para envio do " + projeto;
		}
		else if(tcc.getStatus() == TCC.TEPB) {
			return "Esperando data de apresentação";
		}
		return "Seu trabalho está sob avaliação.";
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
	public List<Sala> getSalas() {
		return salas;
	}

	public boolean getHasCoOrientador() {
		return hasCoOrientador;
	}

	@Command("setHasCoOrientador")
	@NotifyChange({ "hasCoOrientador", "tcc" })
	public void setHasCoOrientador() {
		hasCoOrientador = !hasCoOrientador;
		if (!hasCoOrientador) {
			tcc.setParticipacoes(tccBusiness.removeParticipacao(tcc, tcc.getCoOrientador()));
			tcc.setCoOrientador(null);
		}
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
	
	public Sala getTempSala() {
		return tempSala;
	}
	
	public Timestamp getTempDataApresentacao() {
		return tempDataApresentacao;
	}
	
	public String getTempLinkSala() {
		return tempLinkSala;
	}
	
	/*
	 * Se for orientador alterando, permite alterar em qualquer momento
	 * Se for o aluno, verifica se a defesa jÃ¡ foi marcada
	 */
	public void setTempSala(Sala tempSala) {
		int tipoUsuario = getUsuario().getTipoUsuario().getIdTipoUsuario();
		if(tcc.getStatus() >= TCC.TEPB && tipoUsuario != Usuario.COORDENADOR) {
			Messagebox.show("Não é possível alterar a sala depois de ter marcado a defesa", "Operação inválida", Messagebox.OK,
					Messagebox.ERROR);
			this.tempSala= tcc.getSala();
			return;
		}
		tcc.setSala(tempSala);
		this.tempSala = tempSala;
		BindUtils.postNotifyChange(null, null, this, "tcc");
	}
	
	/*
	 * Se for orientador alterando, permite alterar em qualquer momento
	 * Se for o aluno, verifica se a defesa jÃ¡ foi marcada
	 */
	public void setTempLinkSala(String tempLinkSala) {
		System.out.println("Teste tempLinkSala: " + tempLinkSala);
		int tipoUsuario = getUsuario().getTipoUsuario().getIdTipoUsuario();
		if(tcc.getStatus() >= TCC.TEPB && tipoUsuario != Usuario.COORDENADOR) {
			Messagebox.show("Não é possível alterar o link da sala depois de ter marcado a defesa", "Operação inválida", Messagebox.OK,
					Messagebox.ERROR);
			this.tempLinkSala= tcc.getLinkSala();
			return;
		}
		tcc.setLinkSala(tempLinkSala);
		this.tempLinkSala = tempLinkSala;
	}
	
	/*
	 * Se for orientador alterando, permite alterar em qualquer momento
	 * Se for o aluno, verifica se a data estÃ¡ dentro do prazo de apresentaÃ§Ã£o
	 * e se ele jÃ¡ nÃ£o marcou a defesa
	 */
	public void setTempDataApresentacao(Timestamp tempDataApresentacao) {
		int tipoUsuario = getUsuario().getTipoUsuario().getIdTipoUsuario();
		if(tipoUsuario == Usuario.COORDENADOR) {
			tcc.setDataApresentacao(tempDataApresentacao);
			this.tempDataApresentacao = tempDataApresentacao;
			return;
		}
		if(!tccBusiness.validateDataApresentacao(tempDataApresentacao, tcc.getCalendarioSemestre())) {
			Messagebox.show("A data informada está fora do prazo de apresentação", "Data inválida", Messagebox.OK,
					Messagebox.ERROR);
			tcc.setDataApresentacao(null);
		}
		if(tcc.getStatus() >= TCC.TEPB) {
			Messagebox.show("Não é possível alterar a data de apresentação depois de ter marcado a defesa", "Operação inválida", Messagebox.OK,
					Messagebox.ERROR);
			tempDataApresentacao = tcc.getDataApresentacao();
			return;
		}
		tcc.setDataApresentacao(tempDataApresentacao);
		this.tempDataApresentacao = tempDataApresentacao;
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
		InputStream is = null;
		if (tcc.getArquivoTCC() != null)
			is = FileManager.getFileInputSream(tcc.getArquivoTCC());
		else
			is = FileManager.getFileInputSream("modelo.pdf");
		if(is != null) {
			final AMedia amedia = new AMedia(tcc.getNomeTCC() + ".pdf", "pdf",
					"application/pdf", is);
			iframe.setContent(amedia);
		}
	}

//	TODO passar a usar status, status final?
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Command
	public void upload(@BindingParam("evt") UploadEvent evt) {
		String alerta1 = "Você está enviando a versão final do seu trabalho?";
		final String alerta2 = "Atenção, após submeter a versão final do seu trabalho e clicar em atualizar, ele não poderá mais ser alterado. Deseja continuar?";
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
					Messagebox.ERROR);
			tccFile = null;
			return;
		}

		pdf = new AMedia(tcc.getNomeTCC(), "pdf", "application/pdf", evt.getMedia().getByteData());
		tccFile = evt.getMedia().getStreamData();
		tccFileChanged = true;
		iframe.setContent(pdf);
		logger.info("Upload de arquivo: " + evt.getMedia().getName());
		Messagebox.show("Arquivo enviado com sucesso.", "Confirmação", Messagebox.OK, Messagebox.INFORMATION);
	}
	@Command
	public void extraUpload(@BindingParam("evt") UploadEvent evt) {
		String fileName = evt.getMedia().getName();
		if (!FilenameUtils.getExtension(fileName).equals("zip")) {
			Messagebox.show("Este não é um arquivo válido! Apenas ZIP são aceitos.", "Formato inválido", Messagebox.OK,
					Messagebox.ERROR);
			extraFile = null;
			return;
		}
		extraFile = evt.getMedia().getStreamData();
		extraFileChanged = true;
		Messagebox.show("Arquivo enviado com sucesso.", "Confirmação", Messagebox.OK, Messagebox.INFORMATION);
	}
	@Command
	public void uploadDocumentacao(@BindingParam("evt") UploadEvent evt) {
		String fileName = evt.getMedia().getName();
		if (!FilenameUtils.getExtension(fileName).equals("pdf")) {
			Messagebox.show("Este não é um arquivo válido! Apenas PDF são aceitos.", "Formato inválido", Messagebox.OK,
					Messagebox.ERROR);
			docFile = null;
			return;
		}
		docFile = evt.getMedia().getStreamData();
		docFileChanged = true;
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
	
	@Command
	public void downloadDocumentacao() {
		InputStream is = null;
		
		if (tcc.getArquivoDocumentacao() != null) 
			is = FileManager.getFileInputSream(tcc.getArquivoDocumentacao());
		
		if (is != null)
			Filedownload.save(is, "application/x-rar-compressed",
					tcc.getNomeTCC() + "_documentacao.pdf");
		else
			Messagebox.show("O PDF não foi encontrado!", "Erro",
					Messagebox.OK, Messagebox.ERROR);
	}
	
	@Command
	public void downloadArquivoExtra() {
		InputStream is = null;
		
		if (tcc.getArquivoExtraTCC() != null) 
			is = FileManager.getFileInputSream(tcc.getArquivoExtraTCC());
		
		if (is != null)
			Filedownload.save(is, "application/x-rar-compressed",
					tcc.getNomeTCC() + "_arquivoExtra.zip");
		else
			Messagebox.show("O ZIP não foi encontrado!", "Erro",
					Messagebox.OK, Messagebox.ERROR);
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
				int tipoMembro = 0;
				if (orientadorWindow) {
					tcc.setParticipacoes(tccBusiness.removeParticipacao(tcc, tcc.getOrientador()));
					tcc.setOrientador(tempUser);
					tcc.getAluno().setOrientador(tempUser);
					alterouOrientador = true;
					tipoMembro = Participacao.ORIENTADOR;
				}
				else {
					tcc.setParticipacoes(tccBusiness.removeParticipacao(tcc, tcc.getCoOrientador()));
					tcc.setCoOrientador(tempUser);
					tipoMembro = Participacao.COORIENTADOR;
				}
				tccBusiness.addParticipacao(tcc, tempUser, tipoMembro, true);
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
			if (p.getTipo() == Participacao.BANCA && p.getProfessor().getIdUsuario() == tempUser.getIdUsuario()) {
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
				tccBusiness.addParticipacao(tcc, tempUser, Participacao.BANCA, true);
				BindUtils.postNotifyChange(null, null, this, "tcc");
			} else {
				Messagebox.show("Esse professor já está na lista ou é o orientador/co-orientador do TCC", "Erro",
						Messagebox.OK, Messagebox.ERROR);
			}
			tempUser = null;
		}
	}

	@Command
	public void removeFromBanca(@BindingParam("participacao") Participacao p) {
		if(p.getTipo() == Participacao.BANCA) {
			tcc.getParticipacoes().remove(p);
			BindUtils.postNotifyChange(null, null, this, "tcc");
		}
	}

	@Command
	public void submitBanca(@BindingParam("window") Window window) {
		orientadores.clear();
		orientadores.add(tcc.getOrientador());
		window.setVisible(false);
	}

	public boolean validaAutor(TCC tcc) {
		System.out.println(tcc.getAluno());
	
		if (tcc.getAluno().getMatricula() == null || tcc.getAluno().getMatricula().isEmpty())
			return false;
		if (tcc.getAluno().getNomeUsuario() == null || tcc.getAluno().getNomeUsuario().isEmpty())
			return false;
		if (tcc.getAluno().getEmail() == null || tcc.getAluno().getEmail().isEmpty())
			return false;
	
		
		return true;
	}

	// Submit do TCC
	private void submit() {
		int statusTCC = tcc.getStatus();
		int tipoUsuario = getUsuario().getTipoUsuario().getIdTipoUsuario();
		int idCursoUsuario = getUsuario().getCurso().getIdCurso();
		
		List<EnviadorEmailChain> emails = new ArrayList<EnviadorEmailChain>();
		atualizarArquivos();
		if (tipoUsuario == Usuario.SECRETARIA && (tcc.getArquivoTCC() == null && !tccFileChanged)) {
			Messagebox.show("É necessário enviar o documento PDF.", "Erro", Messagebox.OK, Messagebox.ERROR);
			return;
		}
		if (tipoUsuario == Usuario.SECRETARIA && (!validaAutor(tcc))) {
			Messagebox.show("É necessário informar os dados do Autor.", "Erro", Messagebox.OK, Messagebox.ERROR);
			return;
		}
		
		if(tipoUsuario == Usuario.SECRETARIA && (tcc.getAluno() == null)) {
			Messagebox.show("Aluno sem tipo identificado!",  "Erro", Messagebox.OK, Messagebox.ERROR);
			return;
		}

		/*
		 * Essa condição adicionad é falsa porque na verdade já existe uma instância de aluno associado ao tcc
		 * cadastrado pela secretaria*/
		if (tipoUsuario != Usuario.ALUNO && (tcc.getAluno() == null)) { 
			Messagebox.show("Antes de enviar é necessário validar a matrícula do aluno no botão de verificar.", "Erro",
					Messagebox.OK, Messagebox.ERROR);
			return;
		}
		if (!alunoEditBlock) {
			
			/*
			 * Tratamento Adicionado para evitar erro no cadastro de novos tccs pela secretaria
			 * */
			if (tipoUsuario == Usuario.SECRETARIA && idCursoUsuario == 5) {
				
				Usuario alunoCadastrado = tcc.getAluno();
				TipoUsuario tipoAluno = new TipoUsuario();
				tipoAluno.setIdTipoUsuario(Usuario.ALUNO);
				alunoCadastrado.setTipoUsuario(tipoAluno);
				tcc.setAluno(alunoCadastrado);	
			}
			
			updateTCCUser();
		}
		
		if (tccBusiness.validateTCC(tcc, statusTCC)) {
			switch (statusTCC) {
			case TCC.PI:			
				/*
				 * Este if verifica se o usuário é da Secretaria de Licenciatura em Computação. Caso for, não
				 * enviar email para o coordenador do curso solicitando a aprovação final do tcc. Para este curso
				 * esta a avaliação não é necessária
				 * */
				tcc.setStatus(TCC.PAA);
				
				if (tipoUsuario == Usuario.SECRETARIA && idCursoUsuario == 5) {
					break;
				}
				
				emails.add(new EnviadorEmailAvisoProjetoSubmetido());
				break;
			case TCC.PAA:
				return;
			case TCC.PR:
				tcc.setStatus(TCC.PAA);
				emails.add(new EnviadorEmailAvisoProjetoSubmetido());
				break;
			case TCC.TI:
				tcc.setDataEnvioBanca(new Timestamp(new Date().getTime()));
				tcc.setStatus(TCC.TEPB);
				emails.add(new EnviadorEmailInformesDadosDefesa());
				emails.add(new EnviadorEmailChainTAAProfessor());
//				tccBusiness.marcarTcc(tcc);
				break;
			case TCC.TEPB:
				tcc.setStatus(TCC.TAAO);
				emails.add(new EnviadorEmailAvisoTrabalhoFinalSubmetido());
				break;
			case TCC.TAAO:
				return;
			case TCC.TRO:
				tcc.setStatus(TCC.TAAO);
				emails.add(new EnviadorEmailAvisoTrabalhoFinalSubmetido());
				break;
			case TCC.TAAC:
				return;
			case TCC.TRC:
				tcc.setStatus(TCC.TAAC);
				emails.add(new EnviadorEmailAvisoTrabalhoFinalAprovadoPorOrientador());
				break;
			case TCC.APROVADO:
				return;
			default:
				return;
			}
			
			if (tccBusiness.saveOrEdit(tcc)) {
				String alerta;
				if (tcc.isProjeto())
					alerta = "Projeto salvo!";
				else
					alerta = "Trabalho salvo!";

				for(EnviadorEmailChain email : emails) {
					email.enviarEmail(tcc, null);
				}

				Messagebox.show(alerta, "Confirmação", Messagebox.OK, Messagebox.INFORMATION,
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
		int tipoUsuario = getUsuario().getTipoUsuario().getIdTipoUsuario();
		int idCursoUsuario = getUsuario().getCurso().getIdCurso();
		
		atualizarArquivos();
		
		if (tipoUsuario == Usuario.SECRETARIA && (tcc.getArquivoTCC() == null && !tccFileChanged)) {
			Messagebox.show("É necessário enviar o documento PDF.", "Erro", Messagebox.OK, Messagebox.ERROR);
			return;
		}
		if (tipoUsuario == Usuario.SECRETARIA && (!validaAutor(tcc))) {
			Messagebox.show("É necessário informar os dados do Autor.", "Erro", Messagebox.OK, Messagebox.ERROR);
			return;
		}
		
		if (tipoUsuario != Usuario.ALUNO && (tcc.getAluno() == null)) {
			Messagebox.show("Antes de enviar é necessário validar a matricula do aluno no botão de verificar.", "Erro",
					Messagebox.OK, Messagebox.ERROR);
			return;
		}
		
		// Edita usuario
		if (!alunoEditBlock) {
			
			/*
			 * Define um usuario do tipo aluno na hora de cadastrar um novo TCC
			 * */
			
			if(tipoUsuario == Usuario.SECRETARIA && idCursoUsuario == 5 ) {
				Usuario alunoCadastrado = tcc.getAluno();
				TipoUsuario tipoAluno = new TipoUsuario();
				tipoAluno.setIdTipoUsuario(Usuario.ALUNO);
				alunoCadastrado.setTipoUsuario(tipoAluno);
				tcc.setAluno(alunoCadastrado);				
			}

			updateTCCUser();
		}
		if(alterouOrientador) {
			UsuarioBusiness ub = new UsuarioBusiness();
			Usuario aluno = tcc.getAluno();
			if (!ub.editar(aluno)) {
				Messagebox.show("Devido a um erro, o Aluno não foi salvo.", "Erro", Messagebox.OK,
						Messagebox.ERROR);
			}
		}
		
		if (tccBusiness.saveOrEdit(tcc)) {
			if (!new ParticipacaoBusiness().updateList(tcc)) {
				Messagebox.show("Não foi possível salvar as alterações da Banca Examinadora.", "Erro",
						Messagebox.OK, Messagebox.ERROR);
				
				return;
			}
			
			String alerta = tcc.isProjeto() ? "Projeto salvo!\n" : "Trabalho salvo!\n";
			if (tipoUsuario == Usuario.ALUNO) {
				alerta  = alerta + "Não se esqueça de submetê-lo quando estiver concluído.";
			}
			
			Messagebox.show(alerta, "Confirmação", Messagebox.OK, Messagebox.EXCLAMATION,
					new org.zkoss.zk.ui.event.EventListener() {
						public void onEvent(Event evt) throws InterruptedException {
							if (evt.getName().equals("onOK")) {
								redirectHome();
							}
						}
					});
		} else {
			Messagebox.show("Devido a um erro, o trabalho não foi salvo.", "Erro", Messagebox.OK, Messagebox.ERROR);
		}


	}
	
	// Atualiza as informaÃ§Ãµes de usuÃ¡rio (coordenadores podem alterar na tela de ediÃ§Ã£o de tcc do aluno)
	private void updateTCCUser() {
		Usuario aluno = tcc.getAluno();
		UsuarioBusiness usuarioBusiness = new UsuarioBusiness();
		if (usuarioBusiness.validate(aluno, null, false)) {
			aluno.setSenha(usuarioBusiness.encripta(usuarioBusiness.generatePassword()));
			TipoUsuario tipoAluno = new TipoUsuario();
			tipoAluno.setIdTipoUsuario(Usuario.ALUNO);
			aluno.setTipoUsuario(tipoAluno);
			if (!usuarioBusiness.salvar(aluno)) {
				Messagebox.show("Devido a um erro, o Autor não foi cadastrado.", "Erro", Messagebox.OK,
						Messagebox.ERROR);
			}
		} else {
			String errorMessage = "Aluno:\n";
			for (String error : usuarioBusiness.getErrors())
				errorMessage += error;
			Messagebox.show(errorMessage, "Dados insuficientes / inválidos", Messagebox.OK, Messagebox.ERROR);
		}
	}

	// Verifica se o arquivo foi atualizado
	public void atualizarArquivos() {
		logger.info("Atualizando arquivos...");
		if (tccFileChanged && tccFile != null) {
			savePDF();
			tccFileChanged = false;
			try {
				tccFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			tccFile = null;
		} 

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
		if (docFileChanged && docFile != null) {
			saveDocFile();
			docFileChanged = false;
			try {
				docFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			docFile = null;
		}
	}

	public void savePDF() {
		logger.info("Salvando arquivo do tcc...");
		String newFileName = FileManager.saveFileInputSream(tccFile, "pdf");
		if (newFileName != null) {
			FileManager.deleteFile(tcc.getArquivoTCC());
			tcc.setArquivoTCC(newFileName);
		}
	}

	public void saveExtraFile() {
		logger.info("Salvando arquivo extra...");
		String newFileName = FileManager.saveFileInputSream(extraFile, "zip");
		if (newFileName != null) {
			FileManager.deleteFile(tcc.getArquivoExtraTCC());
			tcc.setArquivoExtraTCC(newFileName);
		}
	}
	
	public void saveDocFile() {
		logger.info("Salvando arquivo de documentação...");
		String newFileName = FileManager.saveFileInputSream(docFile, "pdf");
		if (newFileName != null) {
			if (tcc.getArquivoDocumentacao() != null) {
				FileManager.deleteFile(tcc.getArquivoDocumentacao());
			} 
			tcc.setArquivoDocumentacao(newFileName);
		}
	}

	
	public boolean exibirBaixarDocumentacao() {
		if(tcc != null) {
			if(tcc.getArquivoDocumentacao() != null) {
				return true;
			}
		}
		return false;
	}
	
	public boolean exibirBaixarArquivoExtra() {
		if(tcc != null) {
			if(tcc.getArquivoExtraTCC() != null) {
				return true;
			}
		}
		return false;
	}
	
	
	@Command
	public void validarSala( ) {
		if(tcc.getStatus() >= TCC.TEPB) {
			Messagebox.show("Não é possível alterar a sala depois de ter marcado a defesa", "Operação inválida", Messagebox.OK,
					Messagebox.ERROR);
			tempSala = tcc.getSala();
			return;
		}
		
		tcc.setSala(tempSala);
	}

	public void verificarCanChangeParticipacao() {
//		if(verificarJaApresentou()) 
		if(verificarJaApresentou() && tcc.getStatus() >= TCC.TEPB) 
			canChangeParticipacao = true;
	}
	
	public void verificarCanChangeBanca() {
		if(tcc.getStatus() == TCC.TI) 
			canChangeBanca = true;
	}
	
	public void verificarAtrasado() {
		if(tccBusiness.isTrabalhoAtrasado(tcc))
			tccAtrasado = true;
	}
	
	
	public void verificarCanSubmitTCC() {
		int status = tcc.getStatus();
		if(tccAtrasado) {
			canSubmitTCC = false;
			if(tccBusiness.isTccReprovado(tcc) && !tccBusiness.isTccReprovadoAtrasado(tcc)) {
				canSubmitTCC = true;
			}
		}
		if(status == TCC.PAA || status == TCC.TAAC || status == TCC.TAAO)
			canSubmitTCC = false;
		else if(status == TCC.TEPB && !verificarJaApresentou()) {
			canSubmitTCC = false;
		}
		canUpdateTCC = canSubmitTCC;
//		if(status == TCC.TEPB) {
//			if(!verificarJaApresentou()) {
//				canSubmitTCC = false;
//			}
//		}
//		else if(status == TCC.PAA || status == TCC.TAAC || status == TCC.TAAO)
//			canSubmitTCC = false;
	}
	
	public void verificarCanSubmitDocs() {
		if(verificarJaApresentou()){
			int status = tcc.getStatus();
			if((status == TCC.TEPB || status == TCC.TRC || status == TCC.TRO))
				canSubmitDocs = true;
		}
	}
	
	private boolean verificarJaApresentou() {
		Timestamp dataApresentacao = tcc.getDataApresentacao();
		if (dataApresentacao != null) {
			// 0 se for igual, negativo se for antes
			int comparacao = tcc.getDataApresentacao().compareTo(new Date());
			if (comparacao <= 0) {
				return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Command("submit")
	public void confirmarSubmit() {
		Messagebox.show("Tem certeza que deseja submeter seu trabalho?.", "Confirmação", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event evt) throws InterruptedException {
					if (evt.getName().equals("onYes")) {
						submit();
					}
				}
		});
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
	@NotifyChange({ "tcc" })
	public void onCheckSuplente(@BindingParam("membro") Participacao p, @BindingParam("checked") boolean checked) {
		if(p.getTipo() != Participacao.BANCA) {
			Messagebox.show("Não é possível marcar o orientador/coorientador como suplente", "Operação inválida", Messagebox.OK,
					Messagebox.ERROR);
			return;
		}
		int tipoUsuario = getUsuario().getTipoUsuario().getIdTipoUsuario();
		if(tipoUsuario == Usuario.ALUNO) {
//			if(verificarJaApresentou() && tcc.getStatus() > TCC.TI) {
			if(tcc.getStatus() > TCC.TI) {
				Messagebox.show("Não é possível alterar suplente depois da apresentação.", "Operação inválida", Messagebox.OK,
						Messagebox.ERROR);
				return;
			}
		}
		if(checked) {
			p.setParticipou(false);
		}
		p.setSuplente(checked);
	}

	/**
	 * Marca o membro se participou ou nÃ£o da banca de defesa do tcc
	 */
	@Command
	@NotifyChange({ "tcc" })
	public void onCheckParticipou(@BindingParam("membro") Participacao p, @BindingParam("checked") boolean checked) {
//		int tipoUsuario = getUsuario().getTipoUsuario().getIdTipoUsuario();
//		if(tipoUsuario == Usuario.ALUNO) {
//			if(tcc.getStatus() > TCC.TEPB) {
//				Messagebox.show("NÃ£o Ã© possÃ­vel alterar quem participou depois de ter submetido o trabalho.", "OperaÃ§Ã£o invÃ¡lida", Messagebox.OK,
//						Messagebox.ERROR);
//				return;
//			}
//		}
		p.setParticipou(checked);
	}

	public boolean isAlunoVerified() {
		return alunoVerified;
	}

	public void setAlunoVerified(boolean alunoVerified) {
		this.alunoVerified = alunoVerified;
	}
	
	/*
	 * Cria um novo tcc para o usuario passado como parÃ¢metro para o calendÃ¡rio
	 * atual, e envia os emails das datas para ele e seu orientador
	 */
	private TCC createTCC(Usuario user) {
		TCC newTcc = new TCC();
		newTcc.setAluno(user);
		newTcc.setCalendarioSemestre(getCurrentCalendar(user.getCurso()));
		newTcc.setProjeto(true);
		newTcc.setOrientador(user.getOrientador());
		if(new TCCBusiness().save(newTcc)) {
			// Envio de email para aluno e orientador
			EnviadorEmailChain email = new EnviadorEmailDatasCalendarioAluno();
			email.enviarEmail(newTcc, null);
			email = new EnviadorEmailDatasCalendarioOrientador();
			email.enviarEmail(newTcc, null);
		}
		return newTcc;
	}
	
}
