package br.ufjf.tcc.business;

import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.model.Curso;
import br.ufjf.tcc.persistent.impl.CursoDAO;

public class CursoBusiness {
	
	private List<String> errors;
	private CursoDAO cursoDAO;

	public CursoBusiness() {
		this.errors = new ArrayList<String>();
		this.cursoDAO = new CursoDAO();
	}

	public List<String> getErrors() {
		return errors;
	}

	// valida√ß√£o dos formul√°rios
	public boolean validate(Curso curso, String oldCodigo) {
		errors.clear();

		validateCode(curso.getCodigoCurso(), oldCodigo);
		validateName(curso.getNomeCurso());

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

	// comunica√ß√£o com o CursoDAO
	public List<Curso> getAll() {
		return cursoDAO.getAll();
	}

	public List<Curso> buscar(String express√£o) {
		return cursoDAO.buscar(express√£o);
	}

	public boolean editar(Curso curso) {
		return cursoDAO.editar(curso);
	}

	public boolean salvar(Curso curso) {
		return cursoDAO.salvar(curso);
	}

	public boolean exclui(Curso curso) {
		errors.clear();
		if(new UsuarioBusiness().getAllByCurso(curso).size() > 0) {
			errors.add("O curso possui usu·rios cadastrados.");
			return false;
		}
		if (new TCCBusiness().getTCCsByCurso(curso).size() > 0) {
			errors.add("Existem TCCs relacionados a este curso.");
			return false;
		}
		return cursoDAO.exclui(curso);
	}

	public boolean jaExiste(String codigoCurso, String oldCodigo) {
		errors.clear();
		if (cursoDAO.jaExiste(codigoCurso, oldCodigo)){
			errors.add("J· existe um curso com este cÛdigo.\n");
			return true;
		}
		return false;
	}

	public Curso getCursoByCode(String codigo) {
		return cursoDAO.getCursoByCode(codigo);
	}

}
