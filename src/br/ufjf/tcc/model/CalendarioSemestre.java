package br.ufjf.tcc.model;
import java.util.function.Function;
import java.util.function.Predicate;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * DTO da Tabela {@code CalendarioSemestre} contém os atributos e
 * relacionamentos da mesma.
 * 
 */
@Entity
@Table(name = "calendarioSemestre")
public class CalendarioSemestre implements Serializable {
	public static final int PRAZO_ENVIO_TCC = 90;

	private static final long serialVersionUID = 1L;

	/**
	 * Campo com ID do CalendarioSemestre. Relaciona com a coluna
	 * {@code idCalendarioSemestre} do banco e é gerado por autoincrement do
	 * MySQL através das anotações
	 * {@code @GeneratedValue(generator = "increment")} e
	 * {@code @GenericGenerator(name = "increment", strategy = "increment")}
	 * 
	 */
	@Id
	@Column(name = "idCalendarioSemestre", unique = true, nullable = false)
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	private int idCalendarioSemestre;

	/**
	 * Campo com a data do final do semestre. Relaciona com a coluna
	 * {@code finalSemestre} do banco através da anotação
	 * {@code @Column(name = "finalSemestre", nullable = false)}.
	 */
	@Column(name = "finalSemestre", nullable = false)
	private Date finalSemestre;

	/**
	 * Campo com a nome do calendário(Ex: '2013/1'). Relaciona com a coluna
	 * {@code nomeCalendarioSemestre} do banco através da anotação
	 * {@code @Column(name = "nomeCalendarioSemestre", nullable = false)}.
	 */
	@Column(name = "nomeCalendarioSemestre", nullable = false)
	private String nomeCalendarioSemestre;

	/**
	 * Relacionamento N para 1 entre CalendarioSemestre e Curso. Mapeando
	 * {@link Curso} na variável {@code curso} e retorno do tipo {@code LAZY}
	 * que indica que não será carregado automáticamente este dado quando
	 * retornarmos o {@link CalendarioSemestre}.
	 * 
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idCurso", nullable = false)
	private Curso curso;

	/**
	 * Relacionamento 1 para N entre CalendarioSemestre e Questionario. Mapeada
	 * em {@link Questionario} pela variável {@code calendarioSemestre} e
	 * retorno do tipo {@code LAZY} que indica que não será carregado
	 * automáticamente este dado quando retornarmos o {@link CalendarioSemestre}
	 * .
	 * 
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "calendarioSemestre")
	private List<Questionario> questionarios = new ArrayList<Questionario>();
	
	/**
	 * Relacionamento 1 para N entre CalendarioSemestre e Prazo. Mapeada em
	 * {@link Prazo} pela variável {@code calendarioSemestre} e retorno do tipo
	 * {@code LAZY} que indica que não será carregado automáticamente este dado
	 * quando retornarmos o {@link CalendarioSemestre}.
	 * 
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "calendarioSemestre")
	private List<Prazo> prazos = new ArrayList<Prazo>();

	public int getIdCalendarioSemestre() {
		return idCalendarioSemestre;
	}

	public void setIdCalendarioSemestre(int idCalendarioSemestre) {
		this.idCalendarioSemestre = idCalendarioSemestre;
	}

	public Date getFinalSemestre() {
		return finalSemestre;
	}

	public void setFinalSemestre(Date finalSemestre) {
		// Definindo o hor�rio do final do semestre para 23h59
		this.finalSemestre = finalSemestre;
		this.finalSemestre.setHours(23);
		this.finalSemestre.setMinutes(59);
	}

	public String getNomeCalendarioSemestre() {
		return nomeCalendarioSemestre;
	}

	public void setNomeCalendarioSemestre(String nomeCalendarioSemestre) {
		this.nomeCalendarioSemestre = nomeCalendarioSemestre;
	}

	public Curso getCurso() {
		return curso;
	}

	public void setCurso(Curso curso) {
		this.curso = curso;
	}

	public List<Questionario> getQuestionarios() {
		return questionarios;
	}

	public void setQuestionarios(List<Questionario> questionarios) {
		this.questionarios = questionarios;
	}

	public List<Prazo> getPrazos() {
		return prazos;
	}

	public void setPrazos(List<Prazo> prazos) {
		this.prazos = prazos;
		// Definindo o hor�rio final dos prazos para 23h59
		if(this.prazos != null) {
			
			for(Prazo prazo: this.prazos) {
				prazo.getDataFinal().setHours(23);
				prazo.getDataFinal().setMinutes(59);
			}
		}
	}

	@Override
	public String toString() {
		return "CalendarioSemestre [finalSemestre=" + finalSemestre + ", prazos=" + prazos + "]";
	}
	
}
