package br.ufjf.tcc.pdfHandle;

import java.io.IOException;
import java.net.MalformedURLException;

import com.lowagie.text.BadElementException;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.BaseFont;

import br.ufjf.tcc.model.TCC;

public class AtaCCoorientador extends Ata {
	
	public AtaCCoorientador(TCC tcc){
		super(tcc);
	}

	protected String getPathTemplate() throws Exception {
		return PASTA_COM_TEMPLATE_ATAS + TEMPLATE_COM_COORIENTADOR + tcc.getAluno().getCurso().getCodigoCurso() + EXTENSAO_PDF;
	}
	
	// PREENCHE AS PAGINAS QUE CONTEM INFORMAÇÕES
	@Override
	public void preencherPDF() throws Exception {

		iniciarParametros();
		over = stamper.getOverContent(leitor.getNumberOfPages()-1);
		over.beginText();
		over.setFontAndSize(bfTextoSimples, 12);
		inserirTabelaDinamica();
		inserirDadosPrimeiraPagina();
		inserirDadosSegundaPagina();
		fecharFluxos();

		preencherFichasAvaliacaoIndividual();

		UniaoPDF.unirPDFsFichaAvaliacaoIndividual(qtAvaliador, idAluno);
		UniaoPDF.unirFichaAvaliacaoFinalComFichaAvaliacaoIndividual(idAluno);

	}

	private void inserirDadosSegundaPagina() throws IOException, DocumentException {
		over = stamper.getOverContent(leitor.getNumberOfPages());
		
		over.beginText();
		over.setFontAndSize(bfTextoSimples, 12);

		// PRIMEIRAS INFORMACOES
		form.setField("dia2", dia);
		
		form.setField("mes2", getMesPeloNumero(mes));
		
		form.setField("ano2", ano);
		
		form.setField("hora1", hora);		
		
		form.setField("sala1",sala);
		
		form.setField("orientador2", tcc.getOrientador().getNomeUsuario());

		// ALUNO
		form.setField("nomeAluno2", tcc.getAluno().getNomeUsuario());

		// TCC ENTITULADO
		form.setField("titulo1_2", DivisorString.dividirTitulo(tcc.getNomeTCC())[0]);

		form.setField("titulo2_2", DivisorString.dividirTitulo(tcc.getNomeTCC())[1]);
		
		form.setField("titulo2_3", DivisorString.dividirTitulo(tcc.getNomeTCC())[2]);

		// EXAMINADORES
		
		form.setField("orientador3", "1. "+tcc.getOrientador().getNomeUsuario());

		form.setField("coorientador2", "2. "+tcc.getCoOrientador().getNomeUsuario());
	
		
		form.setField("avaliador1_2", "3. "+avaliadores[2]);
		
		form.setField("avaliador2_2", "4. "+avaliadores[3]);
		
		form.setField("nomeAvaliador1_3", "(" + avaliadores[2] + ")");

		form.setField("nomeAvaliador2_3", "(" + avaliadores[3] + ")"); 
		
		
			
		if (qtAvaliador >= 5) {

				form.setField("avaliador3_2", "5. "+avaliadores[4]);

			// ASSINATURA
			over.setTextMatrix(118, 128);
			over.showText("__________________________");
			over.setTextMatrix(159, 115);
			over.showText("(Avaliador 03)");

			if (qtAvaliador >= 6) {
					form.setField("avaliador4_2", "6. "+avaliadores[5]);

				// ASSINATURA
				over.setTextMatrix(358, 128);
				over.showText("__________________________");
				over.setTextMatrix(399, 115);
				over.showText("(Avaliador 04)");
			}
		}
	}

	private void inserirDadosPrimeiraPagina() throws DocumentException, IOException {
		bfTextoSimples = BaseFont.createFont(BaseFont.TIMES_ROMAN,
				BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);

		form.setField("nomeAluno", tcc.getAluno().getNomeUsuario());
		
		form.setField("titulo1", DivisorString.dividirTitulo(tcc.getNomeTCC())[0]);
		
		form.setField("titulo2", DivisorString.dividirTitulo(tcc.getNomeTCC())[1]);
		
		form.setField("titulo3", DivisorString.dividirTitulo(tcc.getNomeTCC())[2]);
		
		form.setField("subtitulo", tcc.getSubNomeTCC());
		
		form.setField("orientador1", tcc.getOrientador().getNomeUsuario());
		
		form.setField("coorientador1", tcc.getCoOrientador().getNomeUsuario());

		form.setField("avaliador1", avaliadores[2]);
		
		form.setField("avaliador2", avaliadores[3]);
		
		form.setField("nomeAvaliador1", avaliadores[2]);
		
		form.setField("nomeAvaliador2", avaliadores[3]);
		
		form.setField("nomeAvaliador1_2", "(" + avaliadores[2] + ")");
		
		form.setField("nomeAvaliador2_2", "(" + avaliadores[3] + ")");
		
		
		if (qtAvaliador >= 5) {
			form.setField("campoAvaliador3", "Avaliador 3:");

			over.setFontAndSize(bfTextoSimples, 12);
			form.setField("avaliador3", avaliadores[4]);

			// ASSINATURA
			over.setTextMatrix(113, 125);
			over.showText("__________________________");
			over.setTextMatrix(154, 112);
			over.showText("(Avaliador 03)");

		}
		if (qtAvaliador >= 6) {
			form.setField("campoAvaliador4", "Avaliador 4:");

			form.setField("avaliador4", avaliadores[5]);

			// ASSINATURA
			over.setTextMatrix(357, 125);
			over.showText("__________________________");
			over.setTextMatrix(398, 112);
			over.showText("(Avaliador 04)");

		}

		// DATA SEGUNDA PAGINA
		form.setField("dia1", dia);
		form.setField("mes1", getMesPeloNumero(mes));
		form.setField("ano1", ano);
	}

	private void inserirTabelaDinamica()
			throws BadElementException, MalformedURLException, IOException, DocumentException {
		
		Image tabela = null;
		
		if (qtAvaliador == 5) {
			tabela = Image.getInstance(PASTA_COM_TEMPLATE_ATAS + "tablec3.png");
			tabela.setAbsolutePosition(98, 417);
			over.addImage(tabela);
			over.setTextMatrix(102, 424);
			over.showText("Avaliador(a) 3");
		}
		
		if (qtAvaliador == 6) {
			tabela = Image.getInstance(PASTA_COM_TEMPLATE_ATAS + "tablec4.png");
			tabela.setAbsolutePosition(98, 397);
			over.addImage(tabela);
			over.setTextMatrix(102, 424);
			over.showText("Avaliador(a) 3");
			over.setTextMatrix(102, 404);
			over.showText("Avaliador(a) 4");
		}
		
	}
	
}
