package br.ufjf.tcc.business;

import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.model.Curso;
import br.ufjf.tcc.model.Questionario;
import br.ufjf.tcc.model.Resposta;
import br.ufjf.tcc.persistent.impl.QuestionarioDAO;

public class QuestionarioBusiness {
	
	private List<String> errors;
	private QuestionarioDAO questionarioDAO;

	public QuestionarioBusiness() {
		this.errors = new ArrayList<String>();
		this.questionarioDAO = new QuestionarioDAO();
	}

	public List<String> getErrors() {
		return errors;
	}

	// validação dos formulários
	public boolean validate(Questionario questionary) {
		errors.clear();

		validateCurso(questionary.getCurso());

		return errors.size() == 0;
	}

	public void validateCurso(Curso curso) {
		if (curso == null)
			errors.add("� necess�rio selecionar um curso;\n");
	}

	// comunicação com o QuestionarioDAO
	public boolean save(Questionario questionario) {
		return questionarioDAO.salvar(questionario);
	}

	public Questionario getCurrentQuestionaryByCurso(Curso curso) {
		return questionarioDAO.getCurrentQuestionaryByCurso(curso);
	}

	public List<Questionario> getAllByCurso(Curso curso) {
		return questionarioDAO.getAllByCurso(curso);
	}

	public boolean isQuestionaryUsed(Questionario questionario) {
		List<Resposta> q = new RespostaBusiness()
				.getAnswersFromQuestionary(questionario);
		return q.size() > 0;
	}
	
}
