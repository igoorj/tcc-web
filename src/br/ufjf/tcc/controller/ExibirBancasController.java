package br.ufjf.tcc.controller;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zhtml.Filedownload;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import br.ufjf.tcc.business.TCCBusiness;
import br.ufjf.tcc.library.FileManager;
import br.ufjf.tcc.library.SessionManager;
import br.ufjf.tcc.model.Participacao;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;
import br.ufjf.tcc.pdfHandle.Ata;
import br.ufjf.tcc.pdfHandle.AtaBanca;

public class ExibirBancasController extends CommonsController {

	private List<String> years;
	private String emptyMessage;
	private List<TCC> tccs = null, filterTccs = tccs, xmlTccs;
	private String filterString = "";
	private String filterYear = "Todos";
	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.S");
	private List<TCC> trabalhosMarcados ;
	private Ata ata;
	private int idTccModal = 0; // added
	
	@Init
	public void init() {
		int tipoUsuario = getUsuario().getTipoUsuario().getIdTipoUsuario();
		
		if(tipoUsuario != Usuario.COORDENADOR && tipoUsuario != Usuario.SECRETARIA) {
			redirectHome();
			return;
		}
		tccs = new TCCBusiness().getNotFinishedTCCsByCursoAndCalendar(getUsuario().getCurso(),getCurrentCalendar(getUsuario().getCurso()));
		
		trabalhosMarcados = new ArrayList<TCC>();
		
		TCCBusiness tccbusiness = new TCCBusiness();
		
		filterTccs = new ArrayList<TCC>();
		
		if (tccs != null) {
			for (TCC tcc:tccs) {
				if(tccbusiness.isTrabalhoAguardandoAprovacao(tcc)){
					filterTccs.add(tcc);
				}
			}
		}
		
		

		years = new ArrayList<String>();
		if (tccs != null && tccs.size() > 0) {
			for (TCC tcc : tccs) {
				if (tcc.getDataEnvioFinal() != null) {
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(tcc.getDataEnvioFinal().getTime());
					int year = cal.get(Calendar.YEAR);
					if (!years.contains("" + year))
						years.add("" + year);
				}
			}
			Collections.sort(years, Collections.reverseOrder());
		}
		years.add(0, "Todos");
		years.add(1, "Semestre Atual");
		this.filtra();
	}

	public String getEmptyMessage() {
		return emptyMessage;
	}

	public List<String> getYears() {
		return years;
	}
	@NotifyChange("filterTccs")
	public String getFilterYear() {
		return filterYear;
	}

	@NotifyChange("filterTccs")
	public void setFilterYear(String filterYear) {
		this.filterYear = filterYear;
	}

	public List<TCC> getFilterTccs() {
		return filterTccs;
	}

	public String getFilterString() {
		return filterString;
	}

	public void setFilterString(String filterString) {
		this.filterString = filterString;
	}

	public String getTccYear(@BindingParam("tcc") TCC tcc) {
		Calendar cal = Calendar.getInstance();
		if(tcc.getDataEnvioFinal()!=null)
		cal.setTimeInMillis(tcc.getDataEnvioFinal().getTime());
		
		return "" + cal.get(Calendar.YEAR);
	}

	@Command
	public void getEachTccYear(@BindingParam("tcc") TCC tcc,
			@BindingParam("lbl") Label lbl) {
		if (tcc.getDataEnvioFinal() != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(tcc.getDataEnvioFinal().getTime());
			lbl.setValue("" + cal.get(Calendar.YEAR));
		} else
			lbl.setValue("N„o finalizada");
	}

	public List<TCC> getXmlTccs() {
		return xmlTccs;
	}

	@NotifyChange({"filterTccs","filterYear"})
	@Command
	public void filtra() {
		
		
		String filter = filterString.toLowerCase().trim();
		if (tccs != null) {
			List<TCC> temp = new ArrayList<TCC>();
			
				for (TCC tcc : tccs) {
					
					if(tcc.getPalavrasChave()==null)
						tcc.setPalavrasChave("");
					if(tcc.getResumoTCC()==null)
						tcc.setResumoTCC("");
					if ((filterYear == "Todos" || filterYear.contains(getTccYear(tcc)) || filterYear=="Semestre Atual" )    
							&& (filter == "" || (tcc.getNomeTCC().toLowerCase()
									.contains(filter)
									|| tcc.getAluno().getNomeUsuario()
											.toLowerCase().contains(filter)
									|| tcc.getOrientador().getNomeUsuario()
											.toLowerCase().contains(filter)
									|| tcc.getPalavrasChave().toLowerCase()
											.contains(filter) || tcc.getResumoTCC()
									.toLowerCase().contains(filter)))){					
						temp.add(tcc);
						
					}
					
				}
			
			filterTccs = temp;
			Collections.sort(filterTccs,Collections.reverseOrder());
		} else {
			filterTccs = tccs;
		}

		emptyMessage = "N„o foram encontrados trabalhos cadastrados.";
		BindUtils.postNotifyChange(null, null, null, "emptyMessage");
	}

	@Command
	public void downloadPDF(@BindingParam("tcc") TCC tcc) {
		InputStream is = FileManager.getFileInputSream(tcc.getArquivoTCC());
		if (is != null)
			Filedownload.save(is, "application/pdf", tcc.getNomeTCC() + ".pdf");
		else
			Messagebox.show("O PDF n„o foi encontrado!", "Erro", Messagebox.OK,
					Messagebox.ERROR);
	}
	

	@Command
	public void downloadExtra(@BindingParam("tcc") TCC tcc) {
		if (tcc.getArquivoExtraTCC() != null && tcc.getArquivoExtraTCC() != "") {
			InputStream is = FileManager.getFileInputSream(tcc.getArquivoExtraTCC());
			if (is != null)
				Filedownload.save(is, "application/x-rar-compressed",
						tcc.getNomeTCC() + ".rar");
			else
				Messagebox.show("O RAR n„o foi encontrado!", "Erro",
						Messagebox.OK, Messagebox.ERROR);
		}
	}

	@Command
	public void novoTrabalho() //cadastrar TCC de forma definitiva
	{
		if(getCurrentCalendar(getUsuario().getCurso())!=null)
		{
		    SessionManager.setAttribute("projeto",false);
		    Executions.sendRedirect("/pages/editor.zul");
		}
		else
			Messagebox.show("… necessario cadastrar um calend·rio antes");
	}

	@Command
	public void novoAluno()//liberar para que o aluno de come√ßo a sua projeto
	{
		if(getCurrentCalendar(getUsuario().getCurso())!=null)
		{
		    SessionManager.setAttribute("projeto",true);
		    Executions.sendRedirect("/pages/editor.zul");
		}
		else
			Messagebox.show("… necessario cadastrar um calend·rio antes");
	}

	
	public boolean isProjetos()
	{
		if(SessionManager.getAttribute("trabalhos_semestre") != null)
			return (boolean) SessionManager.getAttribute("trabalhos_semestre");
		return false;
	}
	
	public boolean isSecretaria()
	{
		if(getUsuario().getTipoUsuario().getIdTipoUsuario()==Usuario.SECRETARIA)
			return true;
		return false;
	}
	
	
	@Command
	public void marcarTrabalho(@BindingParam("tcc") TCC tcc){
		int indiceTrabalho = trabalhoMarcado(tcc);
		if(indiceTrabalho==-1){
			trabalhosMarcados.add(tcc);
		}else{
			trabalhosMarcados.remove(indiceTrabalho);
		}
		
		for(TCC t:trabalhosMarcados){
			System.out.println(t.getNomeTCC());
		}
		
	}
	
	private int trabalhoMarcado(TCC t){
		for(int i=0;i<trabalhosMarcados.size();i++){
			if(trabalhosMarcados.get(i).getIdTCC()==t.getIdTCC())
				return i;
		}
		
		return -1;
	}

	public SimpleDateFormat getSdf() {
		return sdf;
	}

	public void setSdf(SimpleDateFormat sdf) {
		this.sdf = sdf;
	}
	
	
	// MÈtodo respons·vel por exibir dados da banca
	
	@SuppressWarnings({"unchecked","rawtypes"})
	@Command
	 public void exibirBancasApresentacao( @BindingParam("tcc") TCC tcc) {
		 
		 String mensagem = "\nAcademico(a): " + tcc.getAluno().getNomeUsuario();
		 mensagem += "\n\nMatricula: " + tcc.getAluno().getMatricula();
		 mensagem += "\n\nTitulo do Trabalho: " + tcc.getNomeTCC();
		 mensagem += "\n\nResumo do Trabalho: " + tcc.getResumoTCC();
		 mensagem += "\n\nData: " + tcc.getDataApresentacao();
		 mensagem += "\nHorario: " + tcc.getDataApresentacao().getHours() + ":" +tcc.getDataApresentacao().getMinutes() + ":" +tcc.getDataApresentacao().getSeconds() ;
		 mensagem += "\nSala: " + tcc.getSalaDefesa();
		 mensagem += "\nOrientador: " + tcc.getOrientador().getNomeUsuario();		 
		 
		 Messagebox.show(mensagem, "Visualizar Banca",
				 Messagebox.OK, Messagebox.INFORMATION);
		
	}	 
	
	
	@SuppressWarnings({"unchecked","rawtypes"})
	@Command
	public void gerarPDFBancas(){
		if(!trabalhosMarcados.isEmpty()){
			String mensagem = "A ata ser· gerada em uma nova janela. Verifique se o seu navegador permite a abertura de novas janelas";
			Messagebox.show(mensagem, "ConfirmaÁ„o", Messagebox.OK, Messagebox.INFORMATION,
					new org.zkoss.zk.ui.event.EventListener() {
						public void onEvent(Event evt) throws InterruptedException {
							if (evt.getName().equals("onOK")) {
								generate();

							}

						}
					});
		}else{
			Messagebox.show("… necess·rio marcar pelo menos um trabalho.");
		}
	}
	
	private void generate(){
		
		ata = new AtaBanca(null);
		
		ata.setTrabMarcados(trabalhosMarcados);
		ata.setIdAluno(getUsuario().getIdUsuario());
		try {
			ata.preencherPDF();
		} catch (Exception e) {
			System.out.println(e);
		}
		Executions.getCurrent().sendRedirect("/exibePdfBanca?id="+getUsuario().getIdUsuario(), "_blank");
		
		ata.deletarPDFsExibicaoBanca();
	}
	
		
}