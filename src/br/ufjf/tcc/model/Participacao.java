package br.ufjf.tcc.model;

import java.io.Serializable;
import java.util.ArrayList;
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
 * DTO da Tabela {@code Participacao} contém os atributos e relacionamentos da
 * mesma.
 * 
 */
@Entity
@Table(name = "participacao")
public class Participacao implements Serializable {

	private static final long serialVersionUID = 1L;
	/*
	 * BANCA	- Membro da banca
	 * ORIENTADOR	- Orientador do tcc
	 * COORIENTADOR	- Coorientador do tcc
	 */
	public static final int BANCA = 0, ORIENTADOR = 1, COORIENTADOR = 2;

	/**
	 * Campo com ID da Participacao. Relaciona com a coluna
	 * {@code idParticipacao} do banco e é gerado por autoincrement do MySQL
	 * através das anotações {@code @GeneratedValue(generator = "increment")} e
	 * {@code @GenericGenerator(name = "increment", strategy = "increment")}
	 * 
	 */
	@Id
	@Column(name = "idParticipacao", unique = true, nullable = false)
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	private int idParticipacao;

	/**
	 * Campo com a titulacao do professor. Relaciona com a coluna
	 * {@code titulacao} do banco através da anotação
	 * {@code @Column(name = "titulacao", length = 45}.
	 */
	@Column(name = "titulacao", length = 45)
	private String titulacao = null;

	/**
	 * Relacionamento 1 para N entre Participacao e Resposta. Mapeada em
	 * {@link Resposta} pela variável {@code participacao} e retorno do tipo
	 * {@code LAZY} que indica que não será carregado automáticamente este dado
	 * quando retornarmos a {@link Pariticipacao} .
	 * 
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "participacao")
	private List<Resposta> respostas = new ArrayList<Resposta>();

	/**
	 * Relacionamento N para 1 entre Participacao e Usuario. Mapeando
	 * {@link Usuario} na variável {@code professor} e retorno do tipo
	 * {@code LAZY} que indica que não será carregado automáticamente este dado
	 * quando retornarmos a {@link Participacao}.
	 * 
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idUsuario", nullable = false)
	private Usuario professor;

	/**
	 * Relacionamento N para 1 entre Participacao e TCC. Mapeando {@link TCC} na
	 * variável {@code tecc} e retorno do tipo {@code LAZY} que indica que não
	 * será carregado automáticamente este dado quando retornarmos o
	 * {@link Participacao}.
	 * 
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idTCC", nullable = false)
	private TCC tcc;
	
	@Column(name = "suplente", length = 4)
	private boolean suplente;
	
	/**
	 * Campo preenchido pelo aluno para indicar se o membro estava presente
	 * no dia da defesa do tcc
	 */
	@Column(name = "participou", nullable = false)
	private boolean participou;
	
	/**
	 * Campo para indicar qual o tipo de paritipacao
	 * para facilitar regras de negocio
	 */
	@Column(name = "tipo", nullable = false)
	private int tipo;

	public int getIdParticipacao() {
		return idParticipacao;
	}

	public void setIdParticipacao(int idParticipacao) {
		this.idParticipacao = idParticipacao;
	}

	public String getTitulacao() {
		return titulacao;
	}

	public void setTitulacao(String titulacao) {
		this.titulacao = titulacao;
	}

	public List<Resposta> getRespostas() {
		return respostas;
	}

	public void setRespostas(List<Resposta> respostas) {
		this.respostas = respostas;
	}

	public Usuario getProfessor() {
		return professor;
	}

	public void setProfessor(Usuario professor) {
		this.professor = professor;
	}

	public TCC getTcc() {
		return tcc;
	}

	public void setTcc(TCC tcc) {
		this.tcc = tcc;
	}

	public boolean getSuplente() {
		return suplente;
	}

	public void setSuplente(boolean suplente) {
		this.suplente = suplente;
	}
	
	public boolean isSuplente() {
		return getSuplente();
	}

	public boolean isParticipou() {
		return participou;
	}

	public void setParticipou(boolean participou) {
		this.participou = participou;
	}

	public int getTipo() {
		return tipo;
	}
	
	public String getTipoCorrido() {
		String tipoCorrido = "";
		switch (this.tipo) {
			case Participacao.BANCA:
				tipoCorrido = "Banca";
				break;
			case Participacao.ORIENTADOR:
				tipoCorrido = "Orientador";
				break;
			case Participacao.COORIENTADOR:
				tipoCorrido = "Coorientador";
				break;
			default:
				break;
		}
		return tipoCorrido;
	}

	public void setTipo(int tipo) {
		this.tipo = tipo;
	}
	
	

}
