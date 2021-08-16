package br.ufjf.tcc.business;

import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.model.Departamento;
import br.ufjf.tcc.persistent.impl.DepartamentoDAO;

public class DepartamentoBusiness {
	
	private List<String> errors;
	private DepartamentoDAO departamentoDAO;

	public DepartamentoBusiness() {
		this.errors = new ArrayList<String>();
		this.departamentoDAO = new DepartamentoDAO();
	}

	public List<String> getErrors() {
		return errors;
	}

	// valida√ß√£o dos formul√°rios
	public boolean validate(Departamento departamento, String oldCodigo) {
		errors.clear();

		validateCode(departamento.getCodigoDepartamento(), oldCodigo);
		validateName(departamento.getNomeDepartamento());

		return errors.size() == 0;
	}

	public void validateCode(String codigoCurso, String oldCodigo) {
		if (codigoCurso == null || codigoCurso.trim().length() == 0)
			errors.add("… necess·rio informar o cÛdigo do curso;\n");
		else
			jaExiste(codigoCurso, oldCodigo);
	}

	public void validateName(String nomeCurso) {
		if (nomeCurso == null || nomeCurso.trim().length() == 0)
			errors.add("… necess·rio informar o nome do curso;\n");
	}

	// comunica√ß√£o com o DepartamentoDAO
	public List<Departamento> getAll() {
		return departamentoDAO.getAll();
	}

	public List<Departamento> buscar(String express√£o) {
		return departamentoDAO.buscar(express√£o);
	}

	public boolean editar(Departamento departamento) {
		return departamentoDAO.editar(departamento);
	}

	public boolean salvar(Departamento departamento) {
		return departamentoDAO.salvar(departamento);
	}

	public boolean exclui(Departamento departamento) {
		errors.clear();
		if (new UsuarioBusiness().getAllByDepartamento(departamento).size() > 0) {
			errors.add("Existem professores cadastrados com este departamento.");
			return false;
		}
		return departamentoDAO.exclui(departamento);
	}
	
	public boolean jaExiste(String codigoDepartamento, String oldCodigo) {
		errors.clear();
		if (departamentoDAO.jaExiste(codigoDepartamento, oldCodigo)){
			errors.add("J· existe um departamento com este cÛdigo.\n");
			return true;
		}
		return false;
	}

}
