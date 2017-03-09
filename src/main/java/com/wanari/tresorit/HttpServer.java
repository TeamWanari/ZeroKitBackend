package com.wanari.tresorit;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.wanari.tresorit.request.*;
import com.wanari.tresorit.response.ResponseInitUserRegistration;

import java.io.IOException;
import java.util.List;

import static akka.http.javadsl.unmarshalling.StringUnmarshallers.STRING;

/**
 * Main class that starts the HTTP Server
 * Created by @jlprat on 2016/10/05
 */
public class HttpServer extends AllDirectives {

	private static final String ERROR_MSG = "There was an internal server error.";
	private static final String NO_USER = "There is no user with given username.";

	private String regSessionVerifier;
	private ObjectMapper objectMapper = new ObjectMapper();

	public static void main(String[] args) throws IOException {
		// boot up server using the route as defined below
		ActorSystem system = ActorSystem.create("routes");
		Config config = ConfigFactory.load();

		// HttpApp.bindRoute expects a route being provided by HttpApp.createRoute
		final HttpServer app = new HttpServer();

		final Http http = Http.get(system);
		final ActorMaterializer materializer = ActorMaterializer.create(system);

		final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materializer);
		http.bindAndHandle(routeFlow,
				ConnectHttp.toHost(config.getString("http.interface"), config.getInt("http.port")), materializer);
	}

	Route createRoute() {
		return route(
				listUsers(),
				listTresors(),
				listMembers(),
				initUserRegistration(),
				validateUser(),
				approveTresorCreation(),
				approveShare(),
				approveInvitationLinkCreation(),
				approveInvitationLinkAcception(),
				listUserNames(),
				getUserByUserName()
		);
	}

	private Route listUsers() {
		return pathPrefix("users", () -> get(() -> complete(TresoritService.administrativeCall("user/list-users"))));
	}

	private Route listUserNames() {
		return pathPrefix("usernames", () -> get(() -> {
			try {
				return complete(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(TresoritService.getUsers()));
			} catch(IOException e) {
				e.printStackTrace();
				return complete(StatusCodes.INTERNAL_SERVER_ERROR, ERROR_MSG);
			}
		}));
	}

	private Route listTresors() {
		return pathPrefix("tresors", () -> get(() -> complete(TresoritService.administrativeCall("tresor/list-tresors"))));
	}

	private Route getUserByUserName() {
		return pathPrefix("user", () -> get(() -> pathPrefix(STRING, userName -> {
					try {
						List<RequestValidateUser.RequestValidateUserDTO> users = TresoritService.getUsers();
						RequestValidateUser.RequestValidateUserDTO userTo = users.stream().filter(user -> userName.equals(user.getUserName())).findAny().orElse(null);
						return userTo == null ? complete(StatusCodes.NOT_FOUND, NO_USER) : complete(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(userTo));
					} catch(IOException e) {
						e.printStackTrace();
						return complete(StatusCodes.INTERNAL_SERVER_ERROR, ERROR_MSG);
					}
				}
		)));
	}

	private Route listMembers() {
		return pathPrefix("members", () -> get(() -> pathPrefix(STRING, tresorId ->
				complete(TresoritService.administrativeCall(String.format("tresor/list-members?tresorid=%s", tresorId)))
		)));
	}

	private Route initUserRegistration() {
		return pathPrefix("inituser", () -> post(() -> {
			try {
				String responseString = TresoritService.administrativeCall("user/init-user-registration", "");
				ResponseInitUserRegistration user = objectMapper.readValue(responseString, ResponseInitUserRegistration.class);
				regSessionVerifier = user.getRegSessionVerifier();
				return complete(objectMapper.writeValueAsString(ResponseInitUserRegistration.createDTOFromEntity(user)));
			} catch(IOException e) {
				e.printStackTrace();
				return complete(StatusCodes.INTERNAL_SERVER_ERROR, ERROR_MSG);
			}
		}));
	}

	private Route validateUser() {
		return pathPrefix("validate", () -> post(() -> entity(Unmarshaller.entityToString(), requestBody -> {
					try {
						RequestValidateUser user = objectMapper.readValue(requestBody, RequestValidateUser.class);
						user.setRegSessionVerifier(regSessionVerifier);
						TresoritService.saveUserIdToFile(user);
						return complete(TresoritService.administrativeCall("user/validate-user-registration", objectMapper.writeValueAsString(user)));
					} catch(IOException e) {
						e.printStackTrace();
						return complete(StatusCodes.INTERNAL_SERVER_ERROR, ERROR_MSG);
					}
				}))
		);
	}

	private Route approveTresorCreation() {
		return path(PathMatchers.segment("approve").slash("tresor"), () -> post(() -> entity(Unmarshaller.entityToString(), requestBody -> {
					try {
						RequestApproveTresorCreation body = objectMapper.readValue(requestBody, RequestApproveTresorCreation.class);
						return complete(TresoritService.administrativeCall("tresor/approve-tresor-creation", objectMapper.writeValueAsString(body)));
					} catch(IOException e) {
						e.printStackTrace();
						return complete(StatusCodes.INTERNAL_SERVER_ERROR, ERROR_MSG);
					}
				}))
		);
	}

	private Route approveShare() {
		return path(PathMatchers.segment("approve").slash("share"), () -> post(() -> entity(Unmarshaller.entityToString(), requestBody -> {
					try {
						RequestApproveShare body = objectMapper.readValue(requestBody, RequestApproveShare.class);
						return complete(TresoritService.administrativeCall("tresor/approve-share", objectMapper.writeValueAsString(body)));
					} catch(IOException e) {
						e.printStackTrace();
						return complete(StatusCodes.INTERNAL_SERVER_ERROR, ERROR_MSG);
					}
				}))
		);
	}

	private Route approveInvitationLinkCreation() {
		return path(PathMatchers.segment("approve").slash("invitation").slash("creation"), () -> post(() -> entity(Unmarshaller.entityToString(), requestBody -> {
					try {
						RequestApproveInvitationCreation body = objectMapper.readValue(requestBody, RequestApproveInvitationCreation.class);
						return complete(TresoritService.administrativeCall("tresor/approve-invitation-link-creation", objectMapper.writeValueAsString(body)));
					} catch(IOException e) {
						e.printStackTrace();
						return complete(StatusCodes.INTERNAL_SERVER_ERROR, ERROR_MSG);
					}
				}))
		);
	}

	private Route approveInvitationLinkAcception() {
		return path(PathMatchers.segment("approve").slash("invitation").slash("acception"), () -> post(() -> entity(Unmarshaller.entityToString(), requestBody -> {
					try {
						RequestApproveInvitationAcception body = objectMapper.readValue(requestBody, RequestApproveInvitationAcception.class);
						return complete(TresoritService.administrativeCall("tresor/approve-invitation-link-acception", objectMapper.writeValueAsString(body)));
					} catch(IOException e) {
						e.printStackTrace();
						return complete(StatusCodes.INTERNAL_SERVER_ERROR, ERROR_MSG);
					}
				}))
		);
	}
}
