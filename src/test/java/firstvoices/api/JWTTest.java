package firstvoices.api;

import com.google.inject.Guice;
import com.google.inject.Injector;
import firstvoices.api.endpoints.ArchiveEndpoint;
import firstvoices.api.endpoints.SharedEndpoint;
import firstvoices.api.endpoints.UserEndpoint;
import firstvoices.api.endpoints.VocabularyEndpoint;
import firstvoices.api.exceptions.ExceptionMappers;
import firstvoices.aws.JWTFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class JWTTest extends JerseyTest {

	@Override
	protected Application configure() {
		enable(TestProperties.LOG_TRAFFIC);
		enable(TestProperties.DUMP_ENTITY);
		Injector injector = Guice.createInjector(new FirstVoicesModule());

		ResourceConfig rc = new ResourceConfig();
		rc.register(ExceptionMappers.class);
		rc.registerInstances(JWTFilter.class);
		rc.registerInstances(injector.getInstance(UserEndpoint.class));

		return rc;
	}

	@Test
	public void testTokenDecodeFailure() {
		final String TOKEN = "invalid_token";
		final Response response = target("/v1/users/current")
			.request(MediaType.APPLICATION_JSON_TYPE)
			.header("Authorization", "Bearer " + TOKEN)
			.get();
		assertNotEquals(200, response.getStatus());
	}

}
