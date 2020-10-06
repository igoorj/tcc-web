package br.ufjf.tcc.persistent.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;

import br.ufjf.tcc.model.Sala;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.persistent.GenericoDAO;

public class SalaDAO extends GenericoDAO {

	@SuppressWarnings("unchecked")
	public List<Sala> getAll() {
		List<Sala> results = null;

		try {
			Query query = getSession().createQuery("SELECT s FROM Sala AS s");
			results = (List<Sala>) query.list();
			getSession().close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return results;
	}

	@SuppressWarnings("unchecked")
	public Sala getSalaByTcc(TCC tcc) {
		Sala results = null;

		try {
			Query query = getSession().createQuery("SELECT s FROM Sala AS s " + "WHERE s = :sala");
			query.setParameter("sala", tcc.getSala());
			results = (Sala) query.uniqueResult();
			getSession().close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return results;
	}

}
