package firstvoices.api;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.matcher.Matchers;
import firstvoices.aws.JWKSKeyResolver;
import firstvoices.services.FirstVoicesService;
import firstvoices.services.NuxeoFirstVoicesServiceImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.security.NoSuchAlgorithmException;

public class FirstVoicesModule extends AbstractModule {

	private static String getEnv(String key, String fallback, boolean raiseException) {
		final String v = System.getenv(key);
		if (v == null) {
			if (raiseException) {
				throw new RuntimeException("" + key + " is required in the environment");
			} else {
				return fallback;
			}
		}
		return v;
	}

	private static String getEnv(String key, String fallback) {
		return getEnv(key, fallback, false);
	}

	private static final Logger log = LoggerFactory.getLogger(FirstVoicesModule.class);

	@Override
	protected void configure() {
		bind(FirstVoicesService.class).to(NuxeoFirstVoicesServiceImplementation.class);
		try {
			String redisURL = getEnv("REDIS_URL", null);
			if (redisURL != null) {
				log.info("setting up redis cache");
				bindInterceptor(Matchers.any(), Matchers.annotatedWith(Cached.class), new CacheInterceptor(redisURL));
			}
		} catch (NoSuchAlgorithmException | CacheInterceptor.CacheUnavailableException e) {
			log.warn("Caching unavailable", e);
		}
		bindConstant().annotatedWith(NuxeoHost.class).to(getEnv("NUXEO_HOST", "http://127.0.0.01/"));
		bindConstant().annotatedWith(NuxeoUsername.class).to(getEnv("NUXEO_USERNAME", ""));
		bindConstant().annotatedWith(NuxeoPassword.class).to(getEnv("NUXEO_PASSWORD", ""));
		bindConstant().annotatedWith(JWKSUrl.class).to(getEnv("JWKS_URL", "http://127.0.0.1:4000/"));
	}

	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@BindingAnnotation
	public static @interface NuxeoHost {
	}

	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@BindingAnnotation
	public static @interface NuxeoUsername {
	}

	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@BindingAnnotation
	public static @interface NuxeoPassword {
	}

	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@BindingAnnotation
	public static @interface JWKSUrl {
	}
}
