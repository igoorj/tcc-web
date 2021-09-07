package br.ufjf.tcc.mail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.business.PrazoBusiness;
import br.ufjf.tcc.model.Participacao;
import br.ufjf.tcc.model.Prazo;
import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;

//Email número 06 do drive
public class EnviadorEmailAlertaDadosDeDefesa extends EnviadorEmailChain{

	public EnviadorEmailAlertaDadosDeDefesa() {
		super(null);
	}
	
	@Override
	protected EmailBuilder gerarEmail(TCC tcc, String statusInicial) {
		EmailBuilder emailBuilder = null;
		
//		UsuarioBusiness usuarioBusiness = new UsuarioBusiness();
//		List<Usuario> coordenadores = usuarioBusiness.getCoordenadoresByCurso(tcc.getAluno().getCurso());
//		String nomeCoordenador = coordenadores.get(0).getNomeUsuario();
		
		String nomeAluno = tcc.getAluno().getNomeUsuario();
		String nomeOrientador = tcc.getOrientador().getNomeUsuario();
		String nomeCurso = tcc.getAluno().getCurso().getNomeCurso();
		
		List<String> suplentes = new ArrayList<String>();
		List<String> membros = new ArrayList<String>();
		for(Participacao participacao : tcc.getParticipacoes()) {
			if(participacao.isSuplente())
				suplentes.add(participacao.getProfessor().getNomeUsuario());
			else
				membros.add(participacao.getProfessor().getNomeUsuario());
		}
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Prazo prazo = new PrazoBusiness().getPrazoByTipoAndCalendario(Prazo.ENTREGA_BANCA, tcc.getCalendarioSemestre());
		String prazoString = formatter.format(prazo.getDataFinal());
		
		emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Aviso de prazo de inclus�o dos dados da Defesa - "+nomeAluno);
		emailBuilder.appendMensagem("Prezados <b>" + nomeAluno + "</b> e <b>" + nomeOrientador + "</b>, ").breakLine();
		emailBuilder.appendMensagem("dentro de <b>2 dias (" + prazoString + ")</b> se encerra o prazo ");
		emailBuilder.appendMensagem("para incluir no Sistema de Monografias os dados  da Defesa ");
		emailBuilder.appendMensagem("do Trabalho de Conclus�o de Curso (TCC) e submeter ");
		emailBuilder.appendMensagem("a vers�o do TCC a ser avaliado pelos membros da Banca Examinadora. ");
		emailBuilder.breakLine().breakLine();
		
		emailBuilder.appendMensagem("<b>� preciso informar:</b> data, hora, local e ");
		emailBuilder.appendMensagem("nome dos membros que far�o parte da Banca Examinadora do TCC: ").breakLine(); 
		
		
		emailBuilder.appendMensagem("<ol>");
		emailBuilder.appendMensagem("<li>Orientador(a)</li>");
		emailBuilder.appendMensagem("<li>Coorientador(a) (se houver)</li>");
		emailBuilder.appendMensagem("<li>Membro 1</li>");
		emailBuilder.appendMensagem("<li>Membro 2</li>");
		emailBuilder.appendMensagem("<li>Suplente</li>");
		emailBuilder.appendMensagem("</ol>");
		emailBuilder.breakLine();
		emailBuilder.appendMensagem("� preciso preencher todas as informa��es no sistema ");
		emailBuilder.appendMensagem("para esta atividade se tornar completa, pois ainda ");
		emailBuilder.appendMensagem("n�o consta que o(a) discente realizou esta atividade completamente.").breakLine(); 
		emailBuilder.appendMensagem("Se essa tarefa n�o for cumprida dentro do prazo, n�o haver� ");
		emailBuilder.appendMensagem("como dar andamento das demais atividades, e dessa forma ");
		emailBuilder.appendMensagem("n�o ser� poss�vel gerar a documenta��o necess�ria para a Defesa do TCC.");
		emailBuilder.breakLine().breakLine();
		emailBuilder.appendMensagem("Att.,").breakLine();
//		emailBuilder.appendMensagem(nomeCoordenador).breakLine();
		emailBuilder.appendMensagem("Coordenador(a) do Curso de " + nomeCurso).breakLine();
		emailBuilder.appendLinkSistema();
		
		
		List<Usuario> destinatarios = new ArrayList<>();
		destinatarios.add(tcc.getAluno());
		destinatarios.add(tcc.getOrientador());
		inserirDestinatarios(destinatarios, emailBuilder);
	
		return emailBuilder;
		
	}

}
