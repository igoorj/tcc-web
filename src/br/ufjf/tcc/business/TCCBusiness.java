package br.ufjf.tcc.business;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import br.ufjf.tcc.library.ConfHandler;
import br.ufjf.tcc.model.CalendarioSemestre;
import br.ufjf.tcc.model.Curso;
import br.ufjf.tcc.model.Participacao;
import br.ufjf.tcc.model.Prazo;
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
		validateName(tcc.getNomeTCC());
		validateResumo(tcc.getResumoTCC());
//		validateData(tcc.getDataApresentacao(), tcc);
		validateSala(tcc.getSalaDefesa(), tcc);
		validateBanca(tcc.getParticipacoes(), tcc);
		validateSuplente(tcc.getParticipacoes(), tcc);
		validatePalavraChave(tcc.getPalavrasChave());
		if (checkFile)
			validateArquivoBanca(tcc.getArquivoTCCBanca());

		return errors.size() == 0 ? false : true;
	}

	public boolean getMissing(TCC tcc) {
		return getMissing(tcc, false);
	}

	public boolean validate(TCC tcc) {
		errors.clear();

		validateName(tcc.getNomeTCC());
		if (errors.size() < 1)
			tcc.setNomeTCC(tcc.getNomeTCC().toUpperCase());
		validateOrientador(tcc.getOrientador());

		return errors.size() == 0 ? true : false;
	}
	public boolean validateTCC(TCC tcc, int status) {
		errors.clear();
		
		validateName(tcc.getNomeTCC());
		if (errors.size() < 1)
			tcc.setNomeTCC(tcc.getNomeTCC().toUpperCase());
		validateOrientador(tcc.getOrientador());
		
		return errors.size() == 0 ? true : false;
	}

	public void validateName(String nomeTCC) {
		if (nomeTCC == null || nomeTCC.trim().length() == 0)
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
	
	public void validateDataApresentacao(TCC tcc) {
		if(tcc != null) {
			Prazo prazoDefesa = new PrazoBusiness().getPrazoDataDefesaByCalendario(tcc.getCalendarioSemestre());
			if(prazoDefesa != null) {
				// 0 se for igual, negativo se for antes
//				int comparacao = tcc.getDataApresentacao().compareTo(prazoDefesa.getDataFinal());
//				if(comparacao > 0)
//					errors.add("A data da sua aprensentação está fora dos limites de prazo");
			} else {
				System.out.println("teste null");
			}
				
			
		}
		if (tcc != null)
			if (!tcc.isProjeto())
				errors.add("É necessário informar a data de apresentação\n");
	}

	public void validateSala(String sala, TCC tcc) {
		if (tcc != null)
			if ((sala == null || sala.trim().length() == 0) && !tcc.isProjeto())
				errors.add("É necessário informar a sala de apresentação\n");
	}

	public void validateBanca(List<Participacao> list, TCC tcc) {
		if (tcc != null)
			if ((list == null || list.size() < 3) && !tcc.isProjeto())
				errors.add("É necessário informar a banca. Mínimo de 3 participantes.\n");
	}

	public void validateSuplente(List<Participacao> list, TCC tcc) {
		if (tcc != null)
			if ((list == null || list.size() == 0 || !possuiSuplente(list)) && !tcc.isProjeto())
				errors.add("É necessário informar o suplente da banca.\n");
	}

	public void validateArquivoBanca(String arquivo) {
		if (arquivo == null || arquivo.trim().length() == 0)
			errors.add("É necessário fazer o upload do seu trabalho\n");
	}

	public void validatePalavraChave(String palavraschave) {
		if ((palavraschave == null || palavraschave.trim().length() == 0))
			errors.add("É necessário informar a as palavras chave\n");
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
		return tccDao.salvaOuEdita(tcc);
	}

	public boolean userHasTCC(Usuario user) {
		return tccDao.userHasTCC(user);
	}

	public List<TCC> getTCCsByCurso(Curso curso) {
		return tccDao.getTCCsByCurso(curso);
	}
	
	public List<TCC> getNotFinishedProjectsByCalendar(CalendarioSemestre currentCalendar){
		return tccDao.getNotFinishedProjectsByCalendar(currentCalendar);
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

	// TODO
	public List<TCC> getAllProjetosByCursoAndCalendar(Curso curso, CalendarioSemestre currentCalendar) {
//		return tccDao.getProjetosByCursoAndCalendar(curso, currentCalendar);
		return null;
	}
	
	public List<TCC> getProjetoAguardandoAprovacaoByCalendar(CalendarioSemestre currentCalendar) {
		return tccDao.getProjetoAguardandoAprovacaoByCalendar(currentCalendar);
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

	public boolean possuiSuplente(List<Participacao> participacoes) {
		for (Participacao p : participacoes) {
			if (p.getSuplente())
				return true;
		}

		return false;
	}
	
	

	public boolean isProjetoAguardandoAprovacao(TCC tcc) {
		if(tcc.isProjeto() && tcc.getStatus() == TCC.PAA)
			return true;
		
		if (tcc.isProjeto() && !(tcc.getPalavrasChave() == null || tcc.getPalavrasChave().trim().length() == 0)
				&& tcc.getArquivoTCCBanca() != null
				&& !(tcc.getResumoTCC() == null || tcc.getResumoTCC().trim().length() == 0)
				&& tcc.getOrientador() != null && tcc.getNomeTCC() != null)
			return true;
		return false;
	}

	public boolean isProjetoIncompleto(TCC tcc) {
		if(tcc.isProjeto() && tcc.getStatus() == TCC.PI)
			return true;
		if (tcc.isProjeto() && !isProjetoAguardandoAprovacao(tcc))
			return true;
		return false;
	}
	
	public boolean isProjetoReprovado(TCC tcc) {
		if(tcc.isProjeto() && tcc.getStatus() == TCC.PR)
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
		if(tcc.isProjeto() && tcc.getStatus() == TCC.TAAC)
			return true;
		return false;
	}
	
	public boolean isTrabalhoReprovado(TCC tcc) {
		if(tcc.isProjeto() && (tcc.getStatus() == TCC.TRC || tcc.getStatus() == TCC.TRO) )
			return true;
		return false;
	}

	public boolean isTrabalhoAguardandoAprovacao(TCC tcc) {

		if (!tcc.isProjeto() && !(tcc.getPalavrasChave() == null || tcc.getPalavrasChave().trim().length() == 0)
				&& tcc.getArquivoTCCBanca() != null
				&& !(tcc.getResumoTCC() == null || tcc.getResumoTCC().trim().length() == 0)
				&& tcc.getOrientador() != null && tcc.getNomeTCC() != null
				&& !(tcc.getSalaDefesa() == null || tcc.getSalaDefesa().trim().length() == 0)
				&& tcc.getDataApresentacao() != null && tcc.getParticipacoes() != null
				&& tcc.isQuantidadeParticipacoesValidas() && possuiSuplente(tcc.getParticipacoes()))
			return true;
		return false;
	}

	public boolean isTrabalhoIncompleto(TCC tcc) {
//		if (!tcc.isProjeto() && !isTrabalhoAguardandoAprovacao(tcc) && tcc.getArquivoTCCFinal() == null)
		if (!tcc.isProjeto() && tcc.getStatus() == TCC.TI)
			return true;
		return false;
	}

	public List<TCC> filtraProjetosIncompletos(List<TCC> projetos) {
		// iteração para remover elementos ao mesmo tempo que le o array
		for(Iterator<TCC> i = projetos.iterator(); i.hasNext();) {
			TCC tcc = i.next();
			if(!isProjetoIncompleto(tcc))
				i.remove();
		}
//		for (int i = 0; i < projetos.size(); i++)
//			if (!isProjetoIncompleto(projetos.get(i))) {
//				projetos.remove(i);
//				i--;
//			}
		return projetos;
	}

	public List<TCC> filtraProjetosAguardandoAprovacao(List<TCC> projetos) {
		for(Iterator<TCC> i = projetos.iterator(); i.hasNext();) {
			TCC tcc = i.next();
			if(!isProjetoAguardandoAprovacao(tcc))
				i.remove();
		}
//		for (int i = 0; i < projetos.size(); i++)
//			if (!isProjetoAguardandoAprovacao(projetos.get(i))) {
//				projetos.remove(i);
//				i--;
//			}
		return projetos;
	}
	
	public List<TCC> filtraProjetosReprovados(List<TCC> projetos) {
		for(Iterator<TCC> i = projetos.iterator(); i.hasNext();) {
			TCC tcc = i.next();
			if(!isProjetoReprovado(tcc))
				i.remove();
		}
		return projetos;
	}
	
	public List<TCC> filtraTrabalhosEnviadosParaBanca(List<TCC> projetos) {
		for(Iterator<TCC> i = projetos.iterator(); i.hasNext();) {
			TCC tcc = i.next();
			if(!isTrabalhoEnviadoParaBanca(tcc))
				i.remove();
		}
		return projetos;
	}
	
	public List<TCC> filtraTrabalhosAguardandoAprovacaoDeOrientador(List<TCC> projetos) {
		for(Iterator<TCC> i = projetos.iterator(); i.hasNext();) {
			TCC tcc = i.next();
			if(!isTrabalhoAguardandoAprovacaoDeOrientador(tcc))
				i.remove();
		}
		return projetos;
	}
	
	public List<TCC> filtraTrabalhosAguardandoAprovacaoDeCoordenador(List<TCC> projetos) {
		for(Iterator<TCC> i = projetos.iterator(); i.hasNext();) {
			TCC tcc = i.next();
			if(!isTrabalhoAguardandoAprovacaoDeCoordenador(tcc))
				i.remove();
		}
		return projetos;
	}
	
	public List<TCC> filtraTrabalhosReprovados(List<TCC> projetos) {
		for(Iterator<TCC> i = projetos.iterator(); i.hasNext();) {
			TCC tcc = i.next();
			if(!isTrabalhoReprovado(tcc))
				i.remove();
		}
		return projetos;
	}

	public List<TCC> filtraTrabalhosIncompletos(List<TCC> trabalhos) {
		for(Iterator<TCC> i = trabalhos.iterator(); i.hasNext();) {
			TCC tcc = i.next();
			if(!isTrabalhoIncompleto(tcc))
				i.remove();
		}
		return trabalhos;
	}

	public List<TCC> filtraTrabalhosAguardandoAprovacao(List<TCC> trabalhos) {
		for (int i = 0; i < trabalhos.size(); i++)
			if (!isTrabalhoAguardandoAprovacao(trabalhos.get(i))) {
				trabalhos.remove(i);
				i--;
			}
		return trabalhos;
	}

	public List<TCC> filtraTrabalhosFinalizados(List<TCC> trabalhos) {
		for (int i = 0; i < trabalhos.size(); i++)
			if (getStatusTCC(trabalhos.get(i)) != "Aprovado") {
				trabalhos.remove(i);
				i--;
			}
		return trabalhos;
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
	
/*	public String getStatusTCC(TCC tcc)
	{
		if(isProjetoAguardandoAprovacao(tcc))
			return "PAA";
		else
		if(isProjetoIncompleto(tcc))
			return "PI";
		else
		if(isTrabalhoAguardandoAprovacao(tcc))
			return "TAA";
		else
		if(isTrabalhoIncompleto(tcc))
			return "TI";
		else
			return "Aprovado";
	}*/

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

	public boolean excluitTCC(TCC tcc) {
		ParticipacaoBusiness PB = new ParticipacaoBusiness();
		PB.excluiLista(PB.getParticipacoesByTCC(tcc));

		File f;
		if (tcc.getArquivoTCCFinal() != null) {
			f = new File(ConfHandler.getConf("FILE.PATH") + tcc.getArquivoTCCFinal());
			if (f != null)
				f.delete();
		}
		if (tcc.getArquivoExtraTCCFinal() != null) {
			f = new File(ConfHandler.getConf("FILE.PATH") + tcc.getArquivoExtraTCCFinal());
			if (f != null)
				f.delete();
		}
		if (tcc.getArquivoExtraTCCBanca() != null) {
			f = new File(ConfHandler.getConf("FILE.PATH") + tcc.getArquivoExtraTCCBanca());
			if (f != null)
				f.delete();
		}
		if (tcc.getArquivoTCCBanca() != null) {
			f = new File(ConfHandler.getConf("FILE.PATH") + tcc.getArquivoTCCBanca());
			if (f != null)
				f.delete();
		}
		if (tcc.getArqExtraProjFinal() != null) {
			f = new File(ConfHandler.getConf("FILE.PATH") + tcc.getArqExtraProjFinal());
			if (f != null)
				f.delete();
		}
		if (tcc.getArqProjFinal() != null) {
			f = new File(ConfHandler.getConf("FILE.PATH") + tcc.getArqProjFinal());
			if (f != null)
				f.delete();
		}

		if ((new TCCDAO()).exclui(tcc))
			return true;
		return false;

	}

	public boolean isTarefasDentroDoPrazo(TCC tcc) {
		boolean tarefasDentroDoPrazo = true;

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
				if (!tcc.isEntregouDoc() || !tcc.isTrabFinal()) {
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
}
