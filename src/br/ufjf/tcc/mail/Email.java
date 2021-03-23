package br.ufjf.tcc.mail;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import br.ufjf.tcc.library.ConfHandler;

public class Email {

	private Session session;
	private Message message;
	private DataSource dataSource;
	private BodyPart bodyPart;
	private Multipart multipart;
	private Logger logger = Logger.getLogger(Email.class);

	public Email() {
		if (session == null) {
			Properties propriedades = EmailProperties.getPropriedades();
			AutenticacaoEmail autenticacao = new AutenticacaoEmail();
			session = Session.getInstance(propriedades, autenticacao);
			session.setDebug(true);
		}
		this.message = new MimeMessage(session);
		this.bodyPart = new MimeBodyPart();
		this.multipart = new MimeMultipart();
	}

	public void enviar(EmailBuilder builder) throws RuntimeException {
		if(builder==null)
			return;
		try {
			logger.info("Enviando email: " + builder.getTitulo());
			System.out.println("Enviando email: " + builder.getTitulo());
			message.setFrom(new InternetAddress(ConfHandler.getConf("MAIL.FROM"))); // Remetente

			Address[] toUser = InternetAddress.parse(builder.getDestinatarios()); // Destinatário(s)
			message.setRecipients(Message.RecipientType.TO, toUser);
			
			message.setSubject(builder.getTitulo()); // Assunto
			
			// Body do email
			if(builder.isHtmlFormat()) {
				bodyPart.setContent(builder.getMensagem(), "text/html; charset=UTF-8");
			}
			else {
				bodyPart.setText(builder.getMensagem());
			}
			multipart.addBodyPart(bodyPart);
			
			
			// Arquivo em anexo
			if(builder.getCaminhoArquivo() != null) {
				bodyPart = new MimeBodyPart();
				this.dataSource = new FileDataSource(builder.getCaminhoArquivo());
				bodyPart.setDataHandler(new DataHandler(this.dataSource));
				bodyPart.setFileName("CartaDeParticipacao.pdf");
				multipart.addBodyPart(bodyPart);
			}
			
			message.setContent(multipart);
			Transport.send(message); // Método para enviar a mensagem criada
			System.out.println("Email enviado com sucesso!!\n\n");
			logger.info("Email enviado com sucesso");

		} catch (MessagingException e) {
			logger.error("Erro ao enviar email");
			logger.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}

}
