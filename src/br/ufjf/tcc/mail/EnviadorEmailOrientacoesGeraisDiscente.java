package br.ufjf.tcc.mail;

import java.util.ArrayList;
import java.util.List;

import br.ufjf.tcc.model.TCC;
import br.ufjf.tcc.model.Usuario;


// Email de número 09 do drive
public class EnviadorEmailOrientacoesGeraisDiscente extends EnviadorEmailChain{

	public EnviadorEmailOrientacoesGeraisDiscente() {
		super(null);
	}
	
	@Override
	protected EmailBuilder gerarEmail(TCC tcc, String statusInicial) {
		EmailBuilder emailBuilder = null;
		
//		UsuarioBusiness usuarioBusiness = new UsuarioBusiness();
//		List<Usuario> coordenadores = usuarioBusiness.getCoordenadoresByCurso(tcc.getAluno().getCurso());
//		String nomeCoordenador = coordenadores.get(0).getNomeUsuario();
		
		
		String nomeCurso = tcc.getAluno().getCurso().getNomeCurso();

		emailBuilder = new EmailBuilder(true).comTitulo("[TCC-WEB] Orienta��es Gerais");
		emailBuilder.appendMensagem("ORIENTA��ES GERAIS PARA AVALIA��O DOS TRABALHOS DE CONCLUS�O DE CURSO").breakLine(); 
		emailBuilder.appendMensagem("Instru��es para defesa de TCC com o t�tulo <t�tulo do trabalho> : ").breakLine(); 
		
		emailBuilder.appendMensagem("1. O(a) discente ser� avaliado em duas modalidades - avalia��o da apresenta��o oral ");
		emailBuilder.appendMensagem("e an�lise do trabalho escrito - por uma Banca Examinadora composta por ao ");
		emailBuilder.appendMensagem("menos dois membros que n�o participaram do trabalho (nem orientador(a) e nem co-orientador(a)), ");
		emailBuilder.appendMensagem("que atribuir�o, individualmente, nota ao trabalho nos documentos destinados � avalia��o;").breakLine();
		
		emailBuilder.appendMensagem("2. No trabalho escrito, cada membro deve avaliar: organiza��o sequencial, argumenta��o, ");
		emailBuilder.appendMensagem("profundidade do tema, relev�ncia e contribui��o acad�mica da pesquisa ou sistema ");
		emailBuilder.appendMensagem("desenvolvido, corre��o gramatical, clareza, apresenta��o est�tica e adequa��o aos ");
		emailBuilder.appendMensagem("aspectos formais e �s normas da ABNT;").breakLine();
		
		emailBuilder.appendMensagem("3. Na apresenta��o oral, cada membro deve avaliar: dom�nio do conte�do, organiza��o da ");
		emailBuilder.appendMensagem("apresenta��o, habilidades de comunica��o e express�o, capacidade de argumenta��o, uso ");
		emailBuilder.appendMensagem("dos recursos audiovisuais, corre��o gramatical e apresenta��o est�tica do trabalho;").breakLine();
		
		emailBuilder.appendMensagem("4. Recomenda-se que a defesa do TCC siga a seguinte sequ�ncia:").breakLine();
		emailBuilder.appendMensagem("a) O(a) discente tem 35 (trinta e cinco minutos) para apresenta��o oral do trabalho;").breakLine();
		emailBuilder.appendMensagem("b) Logo ap�s, o trabalho � arguido pelos membros da banca examinadora.").breakLine();
		
		emailBuilder.appendMensagem("5. A nota de cada examinador (totalizando valores de 0 (zero) a 100 (cem) ) ser� a soma ");
		emailBuilder.appendMensagem("da nota do trabalho escrito (com valores de 0 (zero) a 70 (setenta) ) com a nota da ");
		emailBuilder.appendMensagem("apresenta��o oral (com valor de 0 (zero) a 30 (trinta)).").breakLine();
		
		emailBuilder.appendMensagem("6. A avalia��o ser� documentada em uma Ficha de Avalia��o Final e Fichas Individuais de ");
		emailBuilder.appendMensagem("cada membro da banca, onde devem constar as notas que cada examinador ");
		emailBuilder.appendMensagem("atribuiu ao aluno (vide documento em anexo).").breakLine();
		
		emailBuilder.appendMensagem("7. Ao t�rmino da defesa, o(a) discente ou o(a) orientador(a) dever� entregar a Ata de Defesa ");
		emailBuilder.appendMensagem("devidamente assinada e as Fichas de Avalia��o (final e individuais) � Coordena��o do Curso.\n");
		
		emailBuilder.appendMensagem("8. O(a) discente tem o prazo m�ximo de 7 (sete) dias corridos ap�s a defesa, ");
		emailBuilder.appendMensagem("desde que n�o ultrapasse o �ltimo dia letivo do semestre, para submeter a ");
		emailBuilder.appendMensagem("vers�o final do Trabalho de Conclus�o de Curso no Sistema de Monografias, ");
		emailBuilder.appendMensagem("com as corre��es sugeridas pela banca.").breakLine();
		
		emailBuilder.appendMensagem("9. O(a) orientador(a) tem o prazo m�ximo de 2 (dois) dias ap�s a ");
		emailBuilder.appendMensagem("submiss�o da vers�o final do TCC, desde que n�o ultrapasse o �ltimo ");
		emailBuilder.appendMensagem("dia letivo do semestre, para verificar se a vers�o final cont�m as corre��es ");
		emailBuilder.appendMensagem("sugeridas pela Banca Examinadora. Se estiver tudo correto o(a) orientador(a) ");
		emailBuilder.appendMensagem("deve Aprovar essa vers�o final no Sistema de Monografias e lan�ar ");
		emailBuilder.appendMensagem("o resultado: Aprovado no SIGA. ").breakLine();
		
		emailBuilder.appendMensagem("10. Caso o(a) orientador(a) n�o concorde com a vers�o final ");
		emailBuilder.appendMensagem("submetida pelo(a) discente, o(a) mesmo(a) deve Reprovar a ");
		emailBuilder.appendMensagem("Vers�o Final informando o(s) motivo(s). O(a) discente tem o prazo ");
		emailBuilder.appendMensagem("m�ximo de 2 (dois) dias, desde que n�o ultrapasse o �ltimo dia letivo ");
		emailBuilder.appendMensagem("do semestre, para corrigir o TCC e submeter a vers�o corrigida no ");
		emailBuilder.appendMensagem("sistema para ser avaliada.").breakLine();
		
		emailBuilder.appendMensagem("11. Uma vez Aprovada a vers�o final do TCC pelo(a) orientador(a) ");
		emailBuilder.appendMensagem("e entregue toda a documenta��o de Defesa do TCC, a Coordena��o do ");
		emailBuilder.appendMensagem("Curso deve avaliar se a formata��o do TCC no sistema atende aos ");
		emailBuilder.appendMensagem("padr�es de monografia estabelecidos no PPC do curso. Se estiver ");
		emailBuilder.appendMensagem("tudo correto, a Coordena��o Aprova o TCC e o torna p�blico para ");
		emailBuilder.appendMensagem("leitura no Sistema de Monografias. Caso contr�rio, a Coordena��o ");
		emailBuilder.appendMensagem("Reprova o TCC informando o(s) motivo(s) e o(a) discente ter� o prazo ");
		emailBuilder.appendMensagem("m�ximo de 2 (dois) dias, desde que n�o ultrapasse o �ltimo dia letivo ");
		emailBuilder.appendMensagem("do semestre, para corrigir a formata��o do TCC e submeter a vers�o ");
		emailBuilder.appendMensagem("corrigida no sistema para ser avaliada.").breakLine(); 
		
		emailBuilder.appendMensagem("12. A identifica��o de qualquer tipo de pl�gio ou a n�o ado��o ");
		emailBuilder.appendMensagem("do padr�o de monografia disponibilizado no PPC do curso resulta ");
		emailBuilder.appendMensagem("em Reprova��o do Trabalho de Conclus�o do Curso com nota 0 (zero).").breakLine();
		
		emailBuilder.appendMensagem("Att.,").breakLine();
		//emailBuilder.appendMensagem(nomeCoordenador).breakLine();
		emailBuilder.appendMensagem("Coordenador(a) do Curso de " + nomeCurso).breakLine();
		emailBuilder.appendLinkSistema();
		
		List<Usuario> destinatarios = new ArrayList<>();
		destinatarios.add(tcc.getAluno());
		inserirDestinatarios(destinatarios, emailBuilder);
	
		return emailBuilder;
		
	}
	
}
