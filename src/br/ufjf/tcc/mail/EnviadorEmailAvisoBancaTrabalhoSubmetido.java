package br.ufjf.tcc.mail;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import br.ufjf.tcc.model.Participacao;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;

//Email nÃºmero 11 do drive
public class EnviadorEmailAvisoBancaTrabalhoSubmetido extends EnviadorEmailChain {
	private String nomeAluno;
	private String nomeOrientador;
	private String nomeCurso;
	private String titulo;
	private Timestamp dataDefesa;
	private List<String> suplentes = new ArrayList<String>();
	private List<String> membros = new ArrayList<String>();
	private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

	public EnviadorEmailAvisoBancaTrabalhoSubmetido() {
		super(null);
	}

	protected EmailBuilder gerarEmail(TCC tcc, Participacao membro) {
		System.out.println("Teste EnviadorEmailAvisoBancaTrabalhoSubmetido");
		EmailBuilder emailBuilder = null;
//			List<Usuario> coordenadores = usuarioBusiness.getCoordenadoresByCurso(tcc.getAluno().getCurso());
//			String nomeCoordenador = coordenadores.get(0).getNomeUsuario();

		String dataApresentacaoString = formatter.format(this.dataDefesa);
		this.formatter.applyLocalizedPattern("HH:mm");
		String horaApresentacao = formatter.format(this.dataDefesa);

		String nomeMembro = membro.getProfessor().getNomeUsuario();

		emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Aviso TCC submetido - " + this.nomeAluno);
		emailBuilder.appendMensagem("Prezado <b>" + nomeMembro + "</b>, ").breakLine().breakLine();
		emailBuilder.appendMensagem("o Trabalho de Conclusão de Curso do(a) discente " + this.nomeAluno + ", ");
		emailBuilder.appendMensagem("com o título <b>" + this.titulo + "</b>, do qual você será membro da Banca ");
		emailBuilder.appendMensagem("Examinadora de defesa, se encontra disponível no Sistema de Monografias.");
		emailBuilder.breakLine().breakLine();
		emailBuilder
				.appendMensagem("A Defesa do TCC está marcada para dia <b>" + dataApresentacaoString + "</b>, às  <b>");
		emailBuilder
				.appendMensagem(horaApresentacao + "</b>, a ser realizado na(o) <b>" + tcc.getSalaDefesa() + "</b>.")
				.breakLine();

		emailBuilder.appendMensagem("Att.,").breakLine();
//				emailBuilder.appendMensagem(nomeCoordenador).breakLine();
		emailBuilder.appendMensagem("Coordenação do Curso de " + nomeCurso).breakLine();
		emailBuilder.appendLinkSistema();

		List<Usuario> destinatarios = new ArrayList<>();
		destinatarios.add(membro.getProfessor());
		inserirDestinatarios(destinatarios, emailBuilder);

		return emailBuilder;
	}

	@Override
	public void enviarEmail(TCC tcc, String status) {
		System.out.println("Entrou no enviar email");
		List<Participacao> participacoes = tcc.getParticipacoes();
		// Verifica quem prticipou e separa em suplentes e membros

		for (Iterator<Participacao> i = participacoes.iterator(); i.hasNext();) {
			Participacao p = i.next();
			if (p.isParticipou()) {
				if (p.isSuplente())
					this.suplentes.add(p.getProfessor().getNomeUsuario());
				else
					this.membros.add(p.getProfessor().getNomeUsuario());
			} else
				i.remove();
		}
		
		System.out.println("Passou do iterador");
		this.nomeAluno = tcc.getAluno().getNomeUsuario();
		this.nomeCurso = tcc.getAluno().getCurso().getNomeCurso();
		this.titulo = tcc.getNomeTCC();
		this.nomeOrientador = tcc.getOrientador().getNomeUsuario();
		this.dataDefesa = tcc.getDataApresentacao();

		for (Participacao p : participacoes) {
			EmailBuilder builder = gerarEmail(tcc, p);
			// Adicionar arquivo de carta de participaÃ§Ã£o
			Email email = new Email();
			email.enviar(builder);
		}
		System.out.println("Fim...");
	}

	@Override
	protected EmailBuilder gerarEmail(TCC tcc, String statusInicial) {
		return null;
	}

}
