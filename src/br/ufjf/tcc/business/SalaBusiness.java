package br.ufjf.tcc.business;

import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.model.Departamento;
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
	
	
	public boolean exclui(Sala	sala) {
		errors.clear();
		if (new TCCBusiness().getTCCsBySala(sala).size() > 0) {
			errors.add("Existem tccs cadastrados com essa sala.");
			return false;
		}
		return salaDAO.exclui(sala);
	}
	
	public boolean salvar (Sala sala) {
		return salaDAO.salvar(sala);
	}
	
	
	public boolean validate(Sala sala) {
		return true;
	}
}
