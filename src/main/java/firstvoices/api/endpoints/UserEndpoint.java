package firstvoices.api.endpoints;

import javax.inject.Inject;
import firstvoices.api.model.QueryBean;
import firstvoices.api.representations.*;
import firstvoices.api.representations.containers.Metadata;
import firstvoices.aws.JWTAuth;
import firstvoices.aws.UserContextStore;
import firstvoices.services.FirstVoicesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/v1/users")
@SecurityRequirements(
	{
		@SecurityRequirement(name = "oauth2", scopes = {"archives:public"})
	}
)
public class UserEndpoint {

	private static final Logger log = LoggerFactory.getLogger(UserEndpoint.class);

	private UserContextStore userContextStore;

	@Inject
	public UserEndpoint(UserContextStore userContextStore) {
		this.userContextStore = userContextStore;
	}

	@GET
	@Path("/current")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(
		description = "Get details for the currently-authenticated user",
		operationId = "GET CURRENT USER",
		responses = {
			@ApiResponse(
				description = "The current user",
				responseCode = "200",
				content = @Content(
					schema = @Schema(
						implementation = User.class
					)
				)

			)
		}
		,
		tags = {"Access", "User"}
	)
	@JWTAuth(requiredScopes = {"fvapi/communities:public"})
	public Response getCurrentUser(@BeanParam QueryBean query) {
		User u = this.userContextStore.getCurrentUser();
		return Response.ok(u).build();
	}

}
