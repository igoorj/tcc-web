package br.ufjf.tcc.business;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.joda.time.DateTime;

import br.ufjf.tcc.google.GoogleCalendar;
import br.ufjf.tcc.library.ConfHandler;
import br.ufjf.tcc.model.CalendarioSemestre;
import br.ufjf.tcc.model.Curso;
import br.ufjf.tcc.model.Participacao;
import br.ufjf.tcc.model.Prazo;
import br.ufjf.tcc.model.Sala;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;
import br.ufjf.tcc.persistent.impl.TCCDAO;

public class TCCBusiness {

	private List<String> errors;
	private TCCDAO tccDao;

	public TCCBusiness() {
		this.tccDao = new TCCDAO();
		this.errors = new ArrayList<String>();
	}

	public List<String> getErrors() {
		return errors;
	}

	
	public boolean getMissing(TCC tcc, boolean checkFile) {
		errors.clear();

		validateOrientador(tcc.getOrientador());
		validateName(tcc);
		validateResumo(tcc.getResumoTCC());
//		validateApresentacao(tcc);
		validateDataApresentacao(tcc.getDataApresentacao(), tcc.getCalendarioSemestre());
		validateSalaApresentacao(tcc);
		validateBanca(tcc.getParticipacoes());
		validatePalavraChave(tcc.getPalavrasChave());
		if (checkFile)
			validateArquivo(tcc.getArquivoTCC());
		//System.out.println(errors.size());
		System.out.println("\nErros:\n");
		errors.stream().forEach(System.out::println);
		return errors.size() == 0 ? false : true;
	}

	public boolean getMissing(TCC tcc) {
		return getMissing(tcc, false);
	}

	public boolean validate(TCC tcc) {
		errors.clear();

		validateName(tcc);
		if (errors.size() < 1)
			tcc.setNomeTCC(tcc.getNomeTCC().toUpperCase());
		validateOrientador(tcc.getOrientador());

		return errors.size() == 0 ? true : false;
	}
	public boolean validateTCC(TCC tcc, int status) {
		errors.clear();
		switch (status) {
		case TCC.PI:
			validateProjeto(tcc);
			break;
		case TCC.PAA:
			break;
		case TCC.PR:
			break;
		case TCC.TI:
			validateDadosDeDefesa(tcc);
			break;
		case TCC.TEPB:
			validateTrabalho(tcc);
			break;
		case TCC.TAAO:
			break;
		case TCC.TRO:
			tcc.setStatus(TCC.TAAO);
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
		return errors.size() == 0;
	}
	
	public void validateProjeto(TCC tcc) {
		validateName(tcc);
		validateResumo(tcc.getResumoTCC());
		validatePalavraChave(tcc.getPalavrasChave());
		validateOrientador(tcc.getOrientador());
		validateArquivo(tcc.getArquivoTCC());
	}
	
	public void validateDadosDeDefesa(TCC tcc) {
		validateProjeto(tcc);
		validateApresentacao(tcc);
		validateBanca(tcc.getParticipacoes());
	}
	
	/*
	 * Valida os campos necessÃ¡rios do trabalho
	 */
	public void validateTrabalho(TCC tcc) {
		// Necessita dos mesmo campos de projeto
		validateDadosDeDefesa(tcc);
		validateArquivoDocumentacao(tcc.getArquivoDocumentacao());
		validateParticipacao(tcc.getParticipacoes());
	}

	public void validateName(TCC tcc) {
		String nome = tcc.getNomeTCC();
		if(nome != null) {
			nome = nome.trim().toUpperCase();
			if(nome.length() == 0)
				errors.add("É necessário informar o nome do seu Trabalho\n");
			return;
		}
		errors.add("É necessário informar o nome do seu Trabalho\n");
	}

	public void validateOrientador(Usuario orientador) {
		if (orientador == null)
			errors.add("É necessário informar o orientador\n");
	}

	public void validateResumo(String resumo) {
		if (resumo == null || resumo.trim().length() == 0)
			errors.add("É necessário informar o resumo do TCC\n");
	}
	
	/*
	 * Verifica se a data de apresentaÃ§Ã£o estÃ¡ dentro do prazo da defesa
	 */
	public boolean validateApresentacao(TCC tcc) {
		if(tcc != null) {
			validateDataApresentacao(tcc.getDataApresentacao(), tcc.getCalendarioSemestre());
			validateSalaApresentacao(tcc);
			return true;
		}
		errors.add("É necessário informar o tcc.\n");
		return false;
	}
	
	/*
	 * Verifica se a data de apresentaÃ§Ã£o estÃ¡ dentro do prazo da defesa
	 */
	public boolean validateDataApresentacao(Timestamp data, CalendarioSemestre calendario) {
		if(data != null && calendario != null) {
			Prazo prazoDefesa = new PrazoBusiness().getPrazoByTipoAndCalendario(Prazo.DEFESA, calendario);
			if(prazoDefesa == null) {
				System.out.println("Não existe prazo pra defesa\n");
				return false;
			}
			// 0 se for igual, negativo se for antes
			int comparacao = data.compareTo(prazoDefesa.getDataFinal());
			if(comparacao > 0) {
				System.out.println("Erro na comparacao\n");
				errors.add("A data da sua apresentação está fora dos limites de prazo\n");
				return false;
			}
			return true;
		}
		System.out.println("Passou tudo\n");
		errors.add("É necessário informar o a data de apresentação e o calendário.\n");
		return false;
	}
	/*
	 * Verifica se a sala estÃ¡ em uso no horÃ¡rio da apresentaÃ§Ã£o
	 */
	public boolean validateSalaApresentacao(TCC tcc) {
		SalaBusiness salaBusiness = new SalaBusiness();
		if(tcc.getSala() == null) {
			errors.add("É necessário informar a sala de apresentação. \n");
			return false;
		}
		if(salaBusiness.getSalaByTcc(tcc).isOnline()) {
			if(tcc.getLinkSala() == null || tcc.getLinkSala().trim().equals("")) {
				errors.add("É necessário informar o link da sala online.\n");
				return false;
			}
			return true;
		}
		if(tcc.getDataApresentacao() == null) {
			return false;
		}
		List<TCC> tccs = tccDao.getTCCByDataApresentacao(new Date(tcc.getDataApresentacao().getTime()));
		System.out.println("Encontrados: " + tccs.size());
		for(TCC tccIt : tccs) {
			if(tccIt.getIdTCC() != tcc.getIdTCC() && tccIt.getSala().getIdSala() == tcc.getSala().getIdSala()
					&& !tccIt.getSala().isOnline()) {
				errors.add("Não existe uma defesa marcada para essa hora nessa sala\n");
				return false;
			}
		}
		return true;
	}

	public void validateSala(String sala, TCC tcc) {
		if (tcc != null)
			if ((sala == null || sala.trim().length() == 0))
				errors.add("É necessário informar a sala de apresentação\n");
	}

	public void validateBanca(List<Participacao> list) {
		if (list == null) {
			errors.add("É necessário informar a banca. Mínimo de 3 participantes.\n");
			return;
		}
		boolean hasSuplente = false;
		int membrosBanca = 0;
		for(Participacao p : list) {
			if(p.isSuplente())
				hasSuplente = true;
			if(p.getTipo() == Participacao.BANCA)
				membrosBanca++;
		}
		if(membrosBanca < 3) {
			errors.add("É necessário informar a banca. Mínimo de 3 participantes.\n");
		}
		if(!hasSuplente) {
			errors.add("É necessário informar o suplente da banca.\n");
		}
	}
	
	
	public void validateArquivo(String arquivo) {
		if (arquivo == null || arquivo.trim().length() == 0)
			errors.add("É necessário fazer o upload do seu trabalho\n");
	}
	
	public void validateArquivoDocumentacao(String arquivo) {
		if (arquivo == null || arquivo.trim().length() == 0)
			errors.add("É necessário fazer o upload da documentação\n");
	}
	
	public void validateParticipacao(List<Participacao> participacoes) {
		if(participacoes != null) {
			boolean alguemParticipou = false;
			for(Participacao participacao : participacoes) {
				if(participacao.getTipo() == Participacao.BANCA && participacao.isParticipou()) {
					alguemParticipou = true;
					return;
				}
			}
			if(!alguemParticipou) {
				errors.add("É necessário informar quais professores participaram da defesa\n");
			}
		}
	}
	
	public List<Participacao> removeParticipacao(TCC tcc, Usuario user) {
		if(user == null) {
			return tcc.getParticipacoes();
		}
		List<Participacao> aux = new ArrayList<Participacao>();
		for(Iterator<Participacao> i = tcc.getParticipacoes().iterator(); i.hasNext();) {
			Participacao part = i.next();
			System.out.println("participacao: " + part.getProfessor().getNomeUsuario());
			if(part.getProfessor().getIdUsuario() != user.getIdUsuario()) {
				aux.add(part);
			}
		}
		return aux;
	}

	public void validatePalavraChave(String palavraschave) {
		if ((palavraschave == null || palavraschave.trim().length() == 0))
			errors.add("É necessário informar as palavras chave\n");
	}

	public List<TCC> getAll() {
		return tccDao.getAll();
	}

	public boolean save(TCC tcc) {
		return tccDao.salvar(tcc);
	}

	public boolean saveList(List<TCC> tccs) {
		return tccDao.salvarLista(tccs);
	}

	public boolean edit(TCC tcc) {
		return tccDao.editar(tcc);
	}

	public boolean saveOrEdit(TCC tcc) {
		if(tcc.getSala() == null || !tcc.getSala().isOnline()) {
			tcc.setLinkSala(null);
		}
		return tccDao.salvaOuEdita(tcc);
	}

	public boolean userHasTCC(Usuario user) {
		return tccDao.userHasTCC(user);
	}

	public List<TCC> getTCCsByCurso(Curso curso) {
		return tccDao.getTCCsByCurso(curso);
	}
	
	public List<TCC> getTCCsBySala(Sala sala) {
		return tccDao.getTCCsBySala(sala);
	}
	
	public TCC getCurrentTCCByAuthor(Usuario user, CalendarioSemestre currentCalendar) {
		if (currentCalendar != null)
			return tccDao.getCurrentTCCByAuthor(user, currentCalendar);
		else
			return null;
	}

	public List<TCC> getTCCByCursoAndCalendar(Curso curso, CalendarioSemestre currentCalendar) {
		if (currentCalendar != null)
			return tccDao.getTCCByCursoAndCalendar(curso, currentCalendar);
		else
			return null;
	}

	public TCC getTCCById(int id) {
		return tccDao.getTCCById(id);
	}

	public TCC getTCCByCertificadoDigital(String certificadoDigital) {
		return tccDao.getTCCByCertificadoDigital(certificadoDigital);
	}

	public List<TCC> getTCCsByOrientador(Usuario user) {
		return tccDao.getTCCsByOrientador(user);
	}

	public List<TCC> getTCCsByUserParticipacao(Usuario user) {
		return tccDao.getTCCsByUserParticipacao(user);
	}

	public List<TCC> getFinishedTCCsByCurso(Curso curso) {
		return tccDao.getFinishedTCCsByCurso(curso);
	}

	public List<TCC> getAllFinishedTCCs() {
		return tccDao.getAllFinishedTCCs();
	}

	public List<TCC> getAllFinishedTCCsBy(Curso curso, String palavra, String year, int firstResult, int maxResult) {
		return tccDao.getAllFinishedTCCsBy(curso, palavra, year, firstResult, maxResult);
	}

	public List<Integer> getAllYears() {
		return tccDao.getAllYears();
	}

	public Integer getQuantidadeTCCs() {
		return tccDao.getQuantidadeTCCs();
	}

	public List<TCC> getNewest(int quantidade) {
		return tccDao.getNewest(quantidade);
	}

	public List<TCC> getNotFinishedTCCsByCursoAndCalendar(Curso curso, CalendarioSemestre currentCalendar) {
		if (currentCalendar != null)
			return tccDao.getNotFinishedTCCsByCursoAndCalendar(curso, currentCalendar);
		else
			return null;
	}

	public List<TCC> getNotFinishedTCCsAndProjectsByCursoAndCalendar(Curso curso, CalendarioSemestre currentCalendar) {
		if (currentCalendar != null)
			return tccDao.getNotFinishedTCCsAndProjectsByCursoAndCalendar(curso, currentCalendar);
		else
			return null;
	}

	public List<TCC> getProjetosByCalendar(CalendarioSemestre currentCalendar) {
		if (currentCalendar != null)
			return tccDao.getProjetosByCalendar(currentCalendar);
		else
			return null;
	}

	public TCC getCurrentNotFinishedTCCByAuthor(Usuario user, CalendarioSemestre currentCalendar) {
		if (currentCalendar != null)
			return tccDao.getCurrentNotFinishedTCCByAuthor(user, currentCalendar);
		else
			return null;
	}

	public List<TCC> getNotFinishedTCCsByCurso(Curso curso) {
		return tccDao.getNotFinishedTCCsByCurso(curso);
	}

	public List<TCC> getNotFinishedTCCsAndProjectsByCurso(Curso curso) {

		return tccDao.getNotFinishedTCCsAndProjectsByCurso(curso);
	}

	public List<TCC> getProjetosByCurso(Curso curso) {
		return tccDao.getProjetosByCurso(curso);
	}

	public List<TCC> getAllProjetosByCurso(Curso curso) {
		return tccDao.getAllProjetosByCurso(curso);
	}


	public List<TCC> getAllTrabalhosByCurso(Curso curso) {
		return tccDao.getAllTrabalhosByCurso(curso);
	}
	

	public List<TCC> getTrabalhosByCalendar(CalendarioSemestre currentCalendar) {
		return tccDao.getTrabalhosByCalendar(currentCalendar);
	}

	public List<TCC> getAllTrabalhosAndProjetosByCurso(Curso curso) {
		return tccDao.getAllTrabalhosAndProjetosByCurso(curso);
	}

	public List<TCC> getAllTrabalhosBancaMarcada(Curso curso, CalendarioSemestre currentCalendar) {
		return tccDao.getAllTrabalhosBancaMarcada(curso, currentCalendar);
	}

	public List<TCC> getTrabalhosAndProjetosByCalendar(CalendarioSemestre currentCalendar) {
		return tccDao.getTrabalhosAndProjetosByCalendar(currentCalendar);
	}
	
	public void addParticipacao(TCC tcc, Usuario usuario, int tipo, boolean participou) {
		if(tcc == null || usuario == null) return;
		System.out.println("Adicionando na banca: " + tipo);
		Participacao p = new Participacao();
		p.setProfessor(usuario);
		p.setTcc(tcc);
		p.setTipo(tipo);
		p.setParticipou(participou);
		if (usuario.getTitulacao() != null)
			p.setTitulacao(usuario.getTitulacao());
		tcc.getParticipacoes().add(p);
	}

	
	public void preenchebanca(TCC tcc) {
		List<Participacao> participacoes= tcc.getParticipacoes();
		boolean hasOrientador = false;
		boolean hasCoOrientador = false;
		for(Participacao p : participacoes) {
			if(!hasOrientador && p.getTipo() == Participacao.ORIENTADOR)
				hasOrientador = true;
			if(!hasCoOrientador && p.getTipo() == Participacao.ORIENTADOR)
				hasCoOrientador = true;
		}
		if(!hasOrientador) {
			addParticipacao(tcc, tcc.getOrientador(), Participacao.ORIENTADOR, true);
		}
		if(!hasCoOrientador) {
			addParticipacao(tcc, tcc.getCoOrientador(), Participacao.COORIENTADOR, true);
		}
	}
	
	
	public boolean isTrabalhoAtrasado(TCC tcc) {
		if(tcc == null)
			return false;
		int status = tcc.getStatus();
		if(tcc == null || status == TCC.APROVADO)
			return false; 
		
		CalendarioSemestre calendario = new CalendarioSemestreBusiness().getCalendarByTCC(tcc);
		List <Prazo> prazos = calendario.getPrazos();
		
		Date agora = new Date();
		for (Prazo prazo : prazos) {
						
			switch (prazo.getTipo()) {
			
			case Prazo.PRAZO_PROJETO:
				if(prazo.getDataFinal().before(agora) && tcc.isProjeto()) {
					
					String informaHoraAtual = "";
					informaHoraAtual += "\nData: "+ agora.getDate();
					informaHoraAtual += "\nHora: "+ agora.getHours() + "h" +agora.getMinutes();
					
					System.out.println(informaHoraAtual);
					return true;				
				}
				break;
			case Prazo.ENTREGA_BANCA:
				if(prazo.getDataFinal().before(new Date()) && status < TCC.TEPB)
					return true;		
				break;
				
			case Prazo.ENTREGA_FINAL:
				if(prazo.getDataFinal().before(new Date()) && status < TCC.APROVADO)
					return true;
				break;

			default:
				break;
			}
		} 
		return false;
	}
	
	/*
	 * Verifica se o tcc estÃ¡ reprovado, mas dentro de 7 dias
	 * apÃ³s o tÃ©rmino do prazo.
	 */
	public boolean isTccReprovadoAtrasado(TCC tcc) {
		if(tcc == null || tcc.getStatus() == TCC.APROVADO)
			return false;
		CalendarioSemestre calendario = new CalendarioSemestreBusiness().getCalendarByTCC(tcc);
		List <Prazo> prazos = calendario.getPrazos();
		Calendar hoje = Calendar.getInstance();
		for (Prazo prazo : prazos) {
			switch (prazo.getTipo()) {
			case Prazo.PRAZO_PROJETO:
				hoje.add(Calendar.DAY_OF_MONTH, -7);
				if(prazo.getDataFinal().before(hoje.getTime()) && isProjetoReprovado(tcc))
					return true;
				break;
			case Prazo.ENTREGA_FINAL:
				hoje = Calendar.getInstance();
				hoje.add(Calendar.DAY_OF_MONTH, -2);
				if(prazo.getDataFinal().before(hoje.getTime()) && isTrabalhoReprovado(tcc)) {
					return true;
				}
				break;

			default:
				break;
			}
		}
		return false;
	}

	public boolean isProjetoAguardandoAprovacao(TCC tcc) {
		if(tcc.isProjeto() && tcc.getStatus() == TCC.PAA)
			return true;
		return false;
	}

	public boolean isProjetoIncompleto(TCC tcc) {
		if(tcc.isProjeto() && tcc.getStatus() == TCC.PI) {
			return true;
		}
		return false;
	}
	
	public boolean isProjetoReprovado(TCC tcc) {
		if(tcc.isProjeto() && tcc.getStatus() == TCC.PR)
			return true;
		return false;
	}
	
	public boolean isTccReprovado(TCC tcc) {
		if(isTrabalhoReprovado(tcc) || isProjetoReprovado(tcc))
			return true;
		return false;
	}
	
	public boolean isTrabalhoEnviadoParaBanca(TCC tcc) {
		if(!tcc.isProjeto() && tcc.getStatus() == TCC.TEPB)
			return true;
		return false;
	}
	
	public boolean isTrabalhoAguardandoAprovacaoDeOrientador(TCC tcc) {
		if(!tcc.isProjeto() && tcc.getStatus() == TCC.TAAO)
			return true;
		return false;
	}
	
	public boolean isTrabalhoAguardandoAprovacaoDeCoordenador(TCC tcc) {
		if(!tcc.isProjeto() && tcc.getStatus() == TCC.TAAC)
			return true;
		return false;
	}
	
	public boolean isTrabalhoReprovado(TCC tcc) {
		if(!tcc.isProjeto() && (tcc.getStatus() == TCC.TRC || tcc.getStatus() == TCC.TRO) )
			return true;
		return false;
	}

	// TODO remover
	public boolean isTrabalhoAguardandoAprovacao(TCC tcc) {
		int status = tcc.getStatus();
		if(status == TCC.TEPB || status == TCC.TAAC || status == TCC.TAAO || status == TCC.TRC || status == TCC.TRO)
			return true;
		return false;
	}

	public boolean isTrabalhoIncompleto(TCC tcc) {
		if (!tcc.isProjeto() && tcc.getStatus() == TCC.TI)
			return true;
		return false;
	}
	
	public boolean isTrabalhoAprovado(TCC tcc) {
		if (!tcc.isProjeto() && tcc.getStatus() == TCC.APROVADO)
			return true;
		return false;
	}

	public List<TCC> filtraProjetosIncompletos(List<TCC> tccs) {
		List<TCC> aux = new ArrayList<TCC>();
		// iteraÃ§Ã£o para remover elementos ao mesmo tempo que le o array
		for(Iterator<TCC> i = tccs.iterator(); i.hasNext();) {
			TCC tcc = i.next();
			if(isProjetoIncompleto(tcc)) {
				aux.add(tcc);
			}
		}
		return aux;
	}

	public List<TCC> filtraProjetosAguardandoAprovacao(List<TCC> tccs) {
		List<TCC> aux = new ArrayList<TCC>();
		// iteraÃ§Ã£o para remover elementos ao mesmo tempo que le o array
		for(Iterator<TCC> i = tccs.iterator(); i.hasNext();) {
			TCC tcc = i.next();
			if(isProjetoAguardandoAprovacao(tcc)) {
				aux.add(tcc);
			}
		}
		return aux;
	}
	
	public List<TCC> filtraProjetosReprovados(List<TCC> tccs) {
		List<TCC> aux = new ArrayList<TCC>();
		for(Iterator<TCC> i = tccs.iterator(); i.hasNext();) {
			TCC tcc = i.next();
			if(isProjetoReprovado(tcc))
				aux.add(tcc);
		}
		return aux;
	}

	public List<TCC> filtraTrabalhosEnviadosParaBanca(List<TCC> tccs) {
		List<TCC> aux = new ArrayList<TCC>();
		for(Iterator<TCC> i = tccs.iterator(); i.hasNext();) {
			TCC tcc = i.next();
			if(isTrabalhoEnviadoParaBanca(tcc))
				aux.add(tcc);
		}
		return aux;
	}
	
	public List<TCC> filtraTrabalhosAguardandoAprovacaoDeOrientador(List<TCC> tccs) {
		List<TCC> aux = new ArrayList<TCC>();
		for(Iterator<TCC> i = tccs.iterator(); i.hasNext();) {
			TCC tcc = i.next();
			if(isTrabalhoAguardandoAprovacaoDeOrientador(tcc))
				aux.add(tcc);
		}
		return aux;
	}

	public List<TCC> filtraTrabalhosAguardandoAprovacaoDeCoordenador(List<TCC> tccs) {
		List<TCC> aux = new ArrayList<TCC>();
		for(Iterator<TCC> i = tccs.iterator(); i.hasNext();) {
			TCC tcc = i.next();
			if(isTrabalhoAguardandoAprovacaoDeCoordenador(tcc))
				aux.add(tcc);
		}
		return aux;
	}
	
	public List<TCC> filtraTrabalhosReprovados(List<TCC> tccs) {
		List<TCC> aux = new ArrayList<TCC>();
		for(Iterator<TCC> i = tccs.iterator(); i.hasNext();) {
			TCC tcc = i.next();
			if(isTrabalhoReprovado(tcc))
				aux.add(tcc);
		}
		return aux;
	}

	public List<TCC> filtraTrabalhosIncompletos(List<TCC> tccs) {
		List<TCC> aux = new ArrayList<TCC>();
		for(Iterator<TCC> i = tccs.iterator(); i.hasNext();) {
			TCC tcc = i.next();
			if(isTrabalhoIncompleto(tcc))
				aux.add(tcc);
		}
		return aux;
	}


	public List<TCC> filtraTrabalhosFinalizados(List<TCC> tccs) {
		List<TCC> aux = new ArrayList<TCC>();
		for(Iterator<TCC> i = tccs.iterator(); i.hasNext();) {
			TCC tcc = i.next();
			if(isTrabalhoAprovado(tcc))
				aux.add(tcc);
		}
		return aux;
	}

	public String getStatusTCC(TCC tcc) {
		switch (tcc.getStatus()) {
			case TCC.PI:
				return "PI";
			case TCC.PR:
				return "PR";
			case TCC.PAA:
				return "PAA";
			case TCC.TI:
				return "TI";
			case TCC.TEPB:
				return "TEPB";
			case TCC.TRO:
				return "TRO";
			case TCC.TAAO:
				return "TAAO";
			case TCC.TRC:
				return "TRC";
			case TCC.TAAC:
				return "TAAC";
			case TCC.APROVADO:
				return "Aprovado";
			default:
				return "";
		}
	}
	
	public String getStatusCorridoTCC(TCC tcc) {
		switch (tcc.getStatus()) {
		case TCC.PI:
			return "Projeto incompleto";
		case TCC.PR:
			return "Projeto reprovado";
		case TCC.PAA:
			return "Projeto aguardando aprovação";
		case TCC.TI:
			return "Trabalho incompleto";
		case TCC.TEPB:
			return "Trabalho enviado para banca";
		case TCC.TRO:
			return "Trabalho reprovado por orientador";
		case TCC.TAAO:
			return "Trabalho aguardando aprovação de orientador";
		case TCC.TAAC:
			return "Trabalho aguardando aprovação de coordenador";
		case TCC.TRC:
			return "Trabalho reprovado por coordenador";
		case TCC.APROVADO:
			return "Aprovado";
		default:
			return "";
		}
	}

	public boolean excluirTCC(TCC tcc) {
		ParticipacaoBusiness PB = new ParticipacaoBusiness();
		PB.excluiLista(PB.getParticipacoesByTCC(tcc));
		Usuario aluno = tcc.getAluno();
		if(aluno.isAtivo()) {
			UsuarioBusiness ub = new UsuarioBusiness();
			aluno.setAtivo(false);
			ub.editar(aluno);
		}

		File f;
		if (tcc.getArquivoTCC() != null) {
			f = new File(ConfHandler.getConf("FILE.PATH") + tcc.getArquivoTCC());
			if (f != null)
				f.delete();
		}
		if (tcc.getArquivoExtraTCC() != null) {
			f = new File(ConfHandler.getConf("FILE.PATH") + tcc.getArquivoExtraTCC());
			if (f != null)
				f.delete();
		}
		if (tcc.getArquivoDocumentacao() != null) {
			f = new File(ConfHandler.getConf("FILE.PATH") + tcc.getArquivoDocumentacao());
			if (f != null)
				f.delete();
		}
		

		if ((new TCCDAO()).exclui(tcc))
			return true;
		return false;

	}

	public boolean isTarefasDentroDoPrazo(TCC tcc) {
		boolean tarefasDentroDoPrazo = true;
		
		if(isTrabalhoAprovado(tcc))
			return true;

		DateTime diaHoje = new DateTime(new Date());
		int ultimoPrazoPassado = -1; // caso nenhum prazo tenha passado

		Curso curso = tcc.getAluno().getCurso();
		CalendarioSemestre calendarioAtual = new CalendarioSemestreBusiness().getCurrentCalendarByCurso(curso);

		if (calendarioAtual != null) {
			List<Prazo> prazos = calendarioAtual.getPrazos();

			for (int i = prazos.size() - 1; i >= 0; i--) {
				if (diaHoje.isAfter(new DateTime(prazos.get(i).getDataFinal()))) {
					ultimoPrazoPassado = i;
					break;
				}
			}

			switch (ultimoPrazoPassado) {
			case Prazo.PRAZO_PROJETO:
				if (isProjetoIncompleto(tcc)) {
					tarefasDentroDoPrazo = false;
				}
				break;

			case Prazo.ENTREGA_BANCA:
				if (isProjetoIncompleto(tcc) || isProjetoAguardandoAprovacao(tcc)) {
					tarefasDentroDoPrazo = false;
				}
				break;

			case Prazo.DEFESA:
				if (isProjetoIncompleto(tcc) || isProjetoAguardandoAprovacao(tcc)) {
					tarefasDentroDoPrazo = false;
				}
				break;

			case Prazo.ENTREGA_FINAL:
				if (!tcc.isEntregouDocumentacao() || !tcc.isTrabFinal()) {
					tarefasDentroDoPrazo = false;
				}
				break;

			case Prazo.FIM_SEMESTRE:
				if (!tcc.isTrabFinal()) {
					tarefasDentroDoPrazo = false;
				}
				break;

			default:
				tarefasDentroDoPrazo = true;
			}
		}

		return tarefasDentroDoPrazo;
	}
	
	public void marcarTcc(TCC tcc) {
		GoogleCalendar calendar = new GoogleCalendar();
		calendar.marcarDefesaTcc(tcc);
	}
}
