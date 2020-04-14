package firstvoices.aws;

import firstvoices.api.representations.User;
import io.swagger.v3.core.filter.AbstractSpecFilter;
import io.swagger.v3.oas.models.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.*;

@Singleton
public class GroovyFilter extends AbstractSpecFilter {
	private static final Logger log = LoggerFactory.getLogger(GroovyFilter.class);

	private final List<String> GROOVY_BLACKLIST = Arrays.asList(
		"AnnotationNode",
		"ASTNode",
		"BlockStatement",
		"BytecodeProcessor",
		"CachedClass",
		"CachedConstructor",
		"CachedField",
		"CachedMethod",
		"CallSiteClassLoader",
		"ClassInfo",
		"ClassLoaderForClassArtifacts",
		"ClassNode",
		"CompilationCustomizer",
		"CompileUnit",
		"CompilerConfiguration",
		"ConstructedOuterNestedClassNode",
		"ConstructorAccessor",
		"ConstructorNode",
		"ErrorCollector",
		"ExpandoMetaClass",
		"Expression",
		"FieldNode",
		"GenericsType",
		"GroovyClassLoader",
		"GroovyResourceLoader",
		"ImportNode",
		"InnerClassNode",
		"ListHashMap",
		"MetaClass",
		"MetaClassCreationHandle",
		"MetaClassRegistry",
		"MetaClassRegistryChangeEventListener",
		"MetaMethod",
		"MetaProperty",
		"MethodAccessor",
		"MethodNode",
		"MixinNode",
		"ModuleNode",
		"PackageNode",
		"Parameter",
		"ParameterTypes",
		"ParserPluginFactory",
		"PropertyNode",
		"ReaderSource",
		"Reduction",
		"SourceUnit",
		"Statement",
		"Token",
		"Variable",
		"VariableScope"
	);

	@Override
	public Optional<Schema> filterSchemaProperty(Schema property, Schema schema, String propName, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
		if ("metaClass".equals(schema.getName())) {
			return Optional.empty();
		}
		return super.filterSchemaProperty(property, schema, propName, params, cookies, headers);
	}

	@Override
	public Optional<Schema> filterSchema(Schema schema, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
		if (GROOVY_BLACKLIST.contains(schema.getName())) {
			return Optional.empty();
		}
		return super.filterSchema(schema, params, cookies, headers);
	}
}
