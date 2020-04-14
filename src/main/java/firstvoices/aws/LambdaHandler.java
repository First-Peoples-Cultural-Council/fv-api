package firstvoices.aws;

import com.amazonaws.serverless.proxy.jersey.JerseyLambdaContainerHandler;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.inject.Guice;
import com.google.inject.Injector;
import firstvoices.api.CORSFilter;
import firstvoices.api.FirstVoicesModule;
import firstvoices.api.endpoints.ArchiveEndpoint;
import firstvoices.api.endpoints.SharedEndpoint;
import firstvoices.api.endpoints.UserEndpoint;
import firstvoices.api.endpoints.VocabularyEndpoint;
import firstvoices.api.exceptions.ExceptionMappers;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LambdaHandler implements RequestStreamHandler {

	// to handle requests in AWS Lambda API Gateway Proxy Environment

	private static final Logger log = LoggerFactory.getLogger(LambdaHandler.class);

	private static final ResourceConfig jerseyApplication;

	static {

		Injector injector = Guice.createInjector(new FirstVoicesModule());

		jerseyApplication =
			new ResourceConfig()
				.registerInstances(injector.getInstance(ArchiveEndpoint.class))
				.registerInstances(injector.getInstance(SharedEndpoint.class))
				.registerInstances(injector.getInstance(UserEndpoint.class))
				.registerInstances(injector.getInstance(VocabularyEndpoint.class))
				.register(CORSFilter.class)
				.register(ExceptionMappers.class)
				.register(JacksonFeature.class)
				.registerInstances(injector.getInstance(JWTFilter.class))
				.property(ServerProperties.WADL_FEATURE_DISABLE, true);
	}

	private static final JerseyLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler
		= JerseyLambdaContainerHandler.getAwsProxyHandler(jerseyApplication);

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
		throws IOException {
		handler.stripBasePath("/demo"); //@todo check for stage var to strip custom bases
		handler.proxyStream(inputStream, outputStream, context);
	}
}
