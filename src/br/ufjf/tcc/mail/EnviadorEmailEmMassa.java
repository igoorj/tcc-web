package br.ufjf.tcc.mail;

import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.business.CalendarioSemestreBusiness;
import br.ufjf.tcc.business.TCCBusiness;
import br.ufjf.tcc.business.UsuarioBusiness;
import br.ufjf.tcc.model.CalendarioSemestre;
import br.ufjf.tcc.model.Curso;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;

public class EnviadorEmailEmMassa extends EnviadorEmailChain {
	private boolean enviarParaAlunos = false;
	private boolean enviarParaOrientadores = false;
	private Curso curso = null;
	private CalendarioSemestre calendario = null;
	private boolean fromAdmin = false;
	private String titulo = "";
	private String corpoEmail = "";
	CalendarioSemestreBusiness cbBusiness = new CalendarioSemestreBusiness();
	private UsuarioBusiness ub = new UsuarioBusiness();
	private TCCBusiness tb = new TCCBusiness();
	
	public EnviadorEmailEmMassa() {
		super(null);
	}

	@Override
	protected EmailBuilder gerarEmail(TCC tcc, String statusInicial) {
		EmailBuilder emailBuilder = null;
		
		// Corpo do e-mail
		emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] " + titulo);
		emailBuilder.appendMensagem(this.corpoEmail).breakLine().breakLine();
		emailBuilder.appendLinkSistema();

		
		// Destinatários
		List<Usuario> destinatarios = new ArrayList<>();
		List<Usuario> alunos = new ArrayList<>();
		if(fromAdmin) {
			alunos = ub.getAlunosAtivos();
		} else {
			alunos = ub.getAlunosAtivosByCurso(curso);
		}
		
		for(Usuario aluno : alunos) {
			if(fromAdmin) {
				this.calendario = cbBusiness.getCurrentCalendarByCurso(aluno.getCurso());
			}
			TCC tccAluno = tb.getCurrentNotFinishedTCCByAuthor(aluno, calendario);
			if(tccAluno != null) {
				if(this.enviarParaOrientadores) {
					destinatarios.add(tccAluno.getOrientador());
				}
				if(this.enviarParaAlunos) {
					destinatarios.add(aluno);
				}
			}
		}
		
		inserirDestinatarios(destinatarios, emailBuilder);
	
		return emailBuilder;
		
	}


	public void setEnviarParaAlunos(boolean enviarParaAlunos) {
		this.enviarParaAlunos = enviarParaAlunos;
	}

	public void setEnviarParaOrientadores(boolean enviarParaOrientadores) {
		this.enviarParaOrientadores = enviarParaOrientadores;
	}
	
	public void setCorpoEmail(String corpoEmail){
		corpoEmail = corpoEmail.replaceAll("\n", "<br>");
		this.corpoEmail = corpoEmail;
	}
	
	public void setFromAdmin(boolean fromAdmin){
		this.fromAdmin= fromAdmin;
	}
	
	public void setCurso(Curso curso){
		this.curso = curso;
	}
	
	public void setCalendario(CalendarioSemestre calendario){
		this.calendario = calendario;
	}
	
	public void setTitulo(String titulo){
		this.titulo = titulo;
	}
	
	
	
	
}
