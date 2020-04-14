package firstvoices.api;

import com.google.inject.Guice;
import com.google.inject.Injector;
import firstvoices.api.endpoints.ArchiveEndpoint;
import firstvoices.api.endpoints.SharedEndpoint;
import firstvoices.api.endpoints.UserEndpoint;
import firstvoices.api.endpoints.VocabularyEndpoint;
import firstvoices.api.exceptions.ExceptionMappers;
import firstvoices.api.representations.Vocabulary;
import firstvoices.aws.JWTFilter;
import firstvoices.aws.LocalAuthFilter;
import firstvoices.aws.UserContextStore;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.*;
import io.swagger.v3.oas.annotations.servers.Server;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/")
@OpenAPIDefinition(
	info = @Info(
		title = "First Voices API",
		version = "0.0.3/prerelease",
		description = "First Voices API documentation",
		license = @License(name = "Unspecified License"),
		contact = @Contact(
			name = "First Peoples' Cultural Council",
			url = "http://www.fpcc.ca/",
			email = "info@fpcc.ca")
	),
	security = {
		@SecurityRequirement(name = "List Archives", scopes = {"archives:public"})
	},
	servers = {
		@Server(
			description = "Local Development Server",
			url = "http://localhost:8000/"
		)
	}
)
@SecurityScheme(name = "oauth2",
	type = SecuritySchemeType.OAUTH2,
	flows = @OAuthFlows(
		implicit = @OAuthFlow(authorizationUrl = "http://localhost:8888/auth",
			scopes = {
				@OAuthScope(name = "archives:public", description = "read public archive data"),
				@OAuthScope(name = "archives:recorder", description = "view unpublished content and submit new content"),
				@OAuthScope(name = "archives:admin", description = "administer archives and publish new content")
			}
		)
	)
)
public class JerseyApplication extends ResourceConfig {

	// run the application in a servlet container for local testing

	private static final Logger log = LoggerFactory.getLogger(JerseyApplication.class);

	public JerseyApplication() {
		Injector injector = Guice.createInjector(new FirstVoicesModule());

		register(CORSFilter.class);
		register(JacksonFeature.class);
		register(ExceptionMappers.class);
		register(ObjectMapperConfiguration.class);
		registerInstances(injector.getInstance(JWTFilter.class)); // or LocalAuthFilter to skip auth

		property(ServerProperties.TRACING, "ALL");
		property(ServerProperties.TRACING_THRESHOLD, "VERBOSE");
		property(ServerProperties.WADL_FEATURE_DISABLE, true);

		registerInstances(injector.getInstance(ArchiveEndpoint.class));
		registerInstances(injector.getInstance(SharedEndpoint.class));
		registerInstances(injector.getInstance(UserEndpoint.class));
		registerInstances(injector.getInstance(VocabularyEndpoint.class));

		log.info("startup");
	}

}
