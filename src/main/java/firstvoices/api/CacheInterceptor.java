package firstvoices.api;

import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.protocol.CommandArgs;
import okio.Timeout;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.tools.ant.filters.StringInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;

public class CacheInterceptor implements MethodInterceptor {

	public static class CacheUnavailableException extends Exception {
		public CacheUnavailableException(Throwable cause) {
			super(cause);
		}
	}

	private static final Logger log = LoggerFactory.getLogger(CacheInterceptor.class);
	private RedisClient redisClient;
	private MessageDigest hasher = MessageDigest.getInstance("SHA1");

	CacheInterceptor(String redisURL) throws NoSuchAlgorithmException, CacheUnavailableException {
		try {
			this.redisClient = RedisClient.create(redisURL);
			TimeoutOptions to = TimeoutOptions
				.builder()
				.fixedTimeout(Duration.ofSeconds(2))
				.connectionTimeout().fixedTimeout(Duration.ofSeconds(2))
				.build();

			ClientOptions clientOptions = ClientOptions
				.builder()
				.timeoutOptions(to)
				.build();

			redisClient.setOptions(clientOptions);
		} catch (Exception e) {
			throw new CacheUnavailableException(e);
		}
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {

		Cached annotation = invocation.getMethod().getAnnotation(Cached.class);
		String key = annotation.value();
		if (invocation.getArguments().length > 0) {
			hasher.reset();
			for (Object o : invocation.getArguments()) {
				hasher.update(o.toString().getBytes(StandardCharsets.UTF_8));
			}
			key = key + ":" + Base64.getEncoder().encodeToString(hasher.digest());
		}

		boolean executed =false;
		Object result = null;

		try (StatefulRedisConnection<String, Object> connection = redisClient.connect(
			new JDKSerializerCodec()
		)) {
			RedisCommands<String, Object> cmds = connection.sync();
			if (cmds.exists(key) > 0) {
				log.trace("returning cached result");
				return cmds.get(key);
			}

			log.trace("invoking and caching result");
			result = invocation.proceed();
			executed = true;

			try {
				cmds.set(key, result);
			} catch (RedisException e) {
				log.warn("Cache set failed", e);
			}
			return result;

		} catch (RedisException e) {
			log.warn("Cache retrieval failed unrecoverably", e);
			if (executed) {
				return result;
			} else {
				log.info("proceeding with invocation");
				return invocation.proceed();
			}
		}
	}
}
