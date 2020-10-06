package br.ufjf.tcc.business;

import java.util.List;

import br.ufjf.tcc.model.Participacao;
import br.ufjf.tcc.model.Sala;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.persistent.impl.SalaDAO;

public class SalaBusiness {
	
	private SalaDAO salaDAO;

	public SalaBusiness() {
		this.salaDAO = new SalaDAO();
	}

	public boolean excluiLista(List<Participacao> participacoes)
	{
		return salaDAO.excluiLista(participacoes);
	}
	
	public List<Sala> getAll() {
		return salaDAO.getAll();
	}
	
	public Sala getSalaByTcc(TCC tcc) {
		return salaDAO.getSalaByTcc(tcc);
		
	}
}
