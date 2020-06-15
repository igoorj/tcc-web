package br.ufjf.tcc.persistent.impl;

import java.util.List;

import org.hibernate.Query;

import br.ufjf.tcc.model.CalendarioSemestre;
import br.ufjf.tcc.model.Prazo;
import br.ufjf.tcc.persistent.GenericoDAO;

public class PrazoDAO extends GenericoDAO {

	@SuppressWarnings("unchecked")
	public List<Prazo> getPrazosByCalendario(
			CalendarioSemestre calendarioSemestre) {
		List<Prazo> prazos = null;
		try {
			Query query = getSession()
					.createQuery( " SELECT p FROM Prazo AS p "
								+ " WHERE p.calendarioSemestre = :calendarioSemestre"
								+ " ORDER BY p.dataFinal");
			
			query.setParameter("calendarioSemestre", calendarioSemestre);
			prazos = query.list();

			getSession().close();
			return prazos;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return prazos;
	}
	
	public Prazo getPrazoByTipoAndCalendario(int tipo, CalendarioSemestre calendario) {
		Prazo prazo = null;
		try {
			Query query = getSession()
					.createQuery( " SELECT p FROM Prazo AS p"
								+ " WHERE p.calendarioSemestre = :calendarioSemestre "
								+ " AND p.tipo = :tipo"
								+ " ORDER BY p.dataFinal");
			
			query.setParameter("calendarioSemestre", calendario);
			query.setParameter("tipo", tipo);
			prazo = (Prazo) query.uniqueResult();

			getSession().close();
			return prazo;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return prazo;
	}
	

}
