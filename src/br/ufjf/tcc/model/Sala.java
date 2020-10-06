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
 * DTO da Tabela {@code Aviso} contém os atributos e relacionamentos da mesma.
 * 
 */
@Entity
@Table(name = "sala")
public class Sala implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Campo com ID da Sala. Relaciona com a coluna {@code idSala} do banco e
	 * é gerado por autoincrement do MySQL através das anotações
	 * {@code @GeneratedValue(generator = "increment")} e
	 * {@code @GenericGenerator(name = "increment", strategy = "increment")}
	 * 
	 */
	@Id
	@Column(name = "idSala", unique = true, nullable = false)
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	private int idSala;

	/**
	 * Campo com o nome da sala. Relaciona com a coluna {@code aviso} do banco
	 * através da anotação
	 * {@code @Column(name = "nome", length = 50, nullable = true)}.
	 */
	@Column(name = "nomeSala", length = 50, nullable = false)
	private String nomeSala;
	
	/**
	 * Campo que indica se sala é online. Relaciona com a coluna
	 * {@code online} do banco através da anotação
	 * {@code @Column(name = "online", nullable = true)}.
	 */
	@Column(name = "online", nullable = true)
	private boolean online;
	
	/**
	 * Relacionamento 1 para N entre Sala e TCC. Mapeada em {@link TCC} pela
	 * variável {@code sala} e retorno do tipo {@code LAZY} que indica que não
	 * será carregado automáticamente este dado quando retornarmos o
	 * {@link Sala} .
	 * 
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "sala")
	private List<TCC> tcc = new ArrayList<TCC>();

	public int getIdSala() {
		return idSala;
	}

	public void setIdSala(int idSala) {
		this.idSala = idSala;
	}

	public String getNomeSala() {
		return nomeSala;
	}

	public void setNomeSala(String nome) {
		this.nomeSala = nome;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public List<TCC> getTcc() {
		return tcc;
	}

	public void setTcc(List<TCC> tcc) {
		this.tcc = tcc;
	}

	
	
}
