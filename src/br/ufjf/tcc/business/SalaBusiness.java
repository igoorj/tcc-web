package br.ufjf.tcc.business;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import br.ufjf.tcc.model.Curso;
import br.ufjf.tcc.model.Participacao;
import br.ufjf.tcc.model.Sala;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.persistent.impl.SalaDAO;

public class SalaBusiness {
	
	private List<String> errors;
	private SalaDAO salaDAO;

	public SalaBusiness() {
		this.salaDAO = new SalaDAO();
		this.errors = new ArrayList<String>();
	}
	
	public List<String> getErrors() {
		return errors;
	}

	public boolean excluiLista(List<Participacao> participacoes)
	{
		return salaDAO.excluiLista(participacoes);
	}
	
	public List<Sala> getAll() {
		return salaDAO.getAll();
	}
	
	public boolean editar(Sala sala) {
		return salaDAO.editar(sala);
	}

	
	public Sala getSalaByTcc(TCC tcc) {
		return salaDAO.getSalaByTcc(tcc);
	}
	
	/**
	 * Seleciona todas as salas e filtra as que não tem curso 
	 * e as que têm o curso igual ao do parâmetro
	 * @param curso
	 * @return Lista de salas
	 */
	public List<Sala> getAllByCurso(Curso curso) {
		List<Sala> allSalas = salaDAO.getAll();
		List<Sala> aux = new ArrayList<Sala>();
		for(Iterator<Sala> i = allSalas.iterator(); i.hasNext();) {
			Sala sala = i.next();
			if(sala.getCurso() == null || sala.getCurso().getIdCurso() == curso.getIdCurso()) {
				aux.add(sala);
			}
		}
		return aux;
	}
	
	
	public boolean exclui(Sala	sala) {
		errors.clear();
		if (new TCCBusiness().getTCCsBySala(sala).size() > 0) {
			errors.add("Existem tccs cadastrados com essa sala.");
			return false;
		}
		return salaDAO.exclui(sala);
	}
	
	public boolean salvaOuEdita (Sala sala) {
		return salaDAO.salvaOuEdita(sala);
	}
	
	
	public boolean validate(Sala sala) {
		errors.clear();
		if(sala.getNomeSala() != null && sala.getNomeSala().trim() == "") {
			errors.add("É necessário informar o nome da sala.\n");
		}
		if(sala.isOnline() && sala.getCurso()==null) {
			errors.add("É necessário informar o curso da sala online.\n");
		}
		return errors.size() == 0;
	}
}
