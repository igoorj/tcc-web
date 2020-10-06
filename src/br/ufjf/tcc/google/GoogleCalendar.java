package br.ufjf.tcc.google;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
//import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.ConferenceSolutionKey;
import com.google.api.services.calendar.model.CreateConferenceRequest;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import br.ufjf.tcc.library.ConfHandler;
import br.ufjf.tcc.model.TCC;

public class GoogleCalendar {

	private Logger logger = Logger.getLogger(GoogleCalendar.class);

	private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "Documentos/bolsa_web/tcc-config/tokens/heder";

	// Refer to the Java quickstart on how to setup the environment:
	// https://developers.google.com/calendar/quickstart/java
	// Change the scope to CalendarScopes.CALENDAR and delete any stored
	// credentials.
	private static final String CREDENTIALS_FILE_PATH = ConfHandler.getConf("CREDENTIALS.PATH");
	private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
//	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	private HttpRequestInitializer getCredentials(final NetHttpTransport HTTP_TRANSPORT, TCC tcc) throws IOException {
		logger.debug("Obtendo credenciais...");
		String credentialsPath = CREDENTIALS_FILE_PATH + tcc.getAluno().getCurso().getCodigoCurso() + ".json";
//		String credentialsPath = CREDENTIALS_FILE_PATH;
		logger.debug("Caminho do arquivo de credenciais: " + credentialsPath);
		System.out.println("Caminho: " + credentialsPath);
		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath));
		credentials = credentials.createScoped(SCOPES);
		credentials.refreshIfExpired();
		HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
		return requestInitializer;
	}

	public boolean marcarDefesaTcc(TCC tcc) {
		logger.debug("Criando evento de defesa de tcc...");
		logger.debug("Id: " + tcc.getIdTCC());
		try {
			NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, tcc))
					.build();
			Event newEvent = buildEvent(tcc);
			String calendarId = "primary";
			try {
				service.events().get(calendarId, newEvent.getId()).execute();
				System.out.println("Update no evento");
				newEvent = service.events().update(calendarId, newEvent.getId(), newEvent).setConferenceDataVersion(0).execute();
			} catch (GoogleJsonResponseException e) {
				System.out.println("Insert");
				newEvent = service.events().insert(calendarId, newEvent).setConferenceDataVersion(0).execute();
			}
			logger.debug("Evento criado: " + newEvent.getHtmlLink());
			System.out.printf("Evento criado: %s\n", newEvent.getHtmlLink());
		} catch (GeneralSecurityException | IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	private Event buildEvent(TCC tcc) throws IOException {
		logger.debug("Construindo dados do evento...");
		Event newEvent = new Event().setSummary(tcc.getNomeTCC()).setLocation(tcc.getSala().getNomeSala())
				.setDescription(tcc.getResumoTCC());

		EventDateTime start = new EventDateTime().setDateTime(new DateTime(tcc.getDataApresentacao().getTime()))
				.setTimeZone("America/Sao_Paulo");
		newEvent.setStart(start);

		EventDateTime end = new EventDateTime()
				.setDateTime(new DateTime(tcc.getDataApresentacao().getTime() + 3 * 3600000))
				.setTimeZone("America/Sao_Paulo");
		newEvent.setEnd(end);

		System.out.println(new DateTime(tcc.getDataApresentacao().getTime()));
		System.out.println(new DateTime(tcc.getDataApresentacao().getTime() + 3 * 3600000));

		newEvent.setConferenceData(getConferenceData());

//		EventAttendee[] attendees = new EventAttendee[] {
//			    new EventAttendee().setEmail(tcc.getAluno().getEmail())
//			};
//		newEvent.setAttendees(Arrays.asList(attendees));
		newEvent.setReminders(new Event.Reminders().setUseDefault(false));
//		newEvent.setId("00006");
		newEvent.setId(formatTccId(tcc));
		return newEvent;
	}

	// TODO gerar string para requestId
	/*
	 * Retorna um ConferenceData, usado para criar uma conferência pro evento. Só
	 * funciona se o evento tiver a propriedade ConferenceDataVersion igual a 1
	 */
	private ConferenceData getConferenceData() {
		logger.debug("Construindo dados de conferência...");
		ConferenceSolutionKey conferenceSKey = new ConferenceSolutionKey();
		conferenceSKey.setType("hangoutsMeet");
		CreateConferenceRequest createConferenceReq = new CreateConferenceRequest();
		createConferenceReq.setRequestId("3whatisup3"); // ID generated by you
		createConferenceReq.setConferenceSolutionKey(conferenceSKey);
		ConferenceData conferenceData = new ConferenceData();
		conferenceData.setCreateRequest(createConferenceReq);

		return conferenceData;
	}
	
	public boolean getEvent(TCC tcc) {
		logger.debug("Criando evento de defesa de tcc...");
		logger.debug("Id: " + tcc.getIdTCC());
		try {
			NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, tcc))
					.build();
			String calendarId = "primary";
			Event newEvent = service.events().get(calendarId, formatTccId(tcc)).execute();
			logger.debug("Evento criado: " + newEvent.getHtmlLink());
			System.out.printf("Evento criado: %s\n", newEvent.getHtmlLink());
		} catch (GeneralSecurityException | IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * Transforma o id do tcc em uma string de pelo menos 5 caracteres, para ser
	 * aceito pelo google e identificar o evento do tcc
	 */
	private String formatTccId(TCC tcc) {
		String id = Integer.toString(tcc.getIdTCC());
		while (id.length() < 5) {
			id = "0" + id;
		}
		return id;
	}

	/**
	 * Creates an authorized Credential object.
	 * 
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException If the credentials.json file cannot be found.
	 */
//    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
//        // Load client secrets.
//        InputStream in = GoogleCalendar.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
//        if (in == null) {
//            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
//        }
//        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
//
//        // Build flow and trigger user authorization request.
//        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
//                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
//                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
//                .setAccessType("offline")
//                .build();
//        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
//        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
//    }

//	DateTime startDateTime = new DateTime(null);
//	EventDateTime start = new EventDateTime()
//	    .setDateTime(startDateTime)
//	    .setTimeZone("America/Los_Angeles");
//	DateTime endDateTime = new DateTime(null);
//	EventDateTime end = new EventDateTime()
//	    .setDateTime(endDateTime)
//	    .setTimeZone("America/Los_Angeles");
//	
//
//	String[] recurrence = new String[] {"RRULE:FREQ=DAILY;COUNT=2"};
//	event.setRecurrence(Arrays.asList(recurrence));
//
//	EventAttendee[] attendees = new EventAttendee[] {
//	    new EventAttendee().setEmail("lpage@example.com"),
//	    new EventAttendee().setEmail("sbrin@example.com"),
//	};
//	
//
//	EventReminder[] reminderOverrides = new EventReminder[] {
//	    new EventReminder().setMethod("email").setMinutes(24 * 60),
//	    new EventReminder().setMethod("popup").setMinutes(10),
//	};
//	Event.Reminders reminders = new Event.Reminders()
//	    .setUseDefault(false)
//	    .setOverrides(Arrays.asList(reminderOverrides));
//	event.setReminders(reminders);
//
//	String calendarId = "primary";
//	event = service.events().insert(calendarId, event).execute();
//	System.out.printf("Event created: %s\n", event.getHtmlLink());
}