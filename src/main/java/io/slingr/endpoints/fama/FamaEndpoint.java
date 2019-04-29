package io.slingr.endpoints.fama;

import io.slingr.endpoints.HttpEndpoint;
import io.slingr.endpoints.exceptions.EndpointException;
import io.slingr.endpoints.exceptions.ErrorCode;
import io.slingr.endpoints.framework.annotations.ApplicationLogger;
import io.slingr.endpoints.framework.annotations.EndpointProperty;
import io.slingr.endpoints.framework.annotations.EndpointWebService;
import io.slingr.endpoints.framework.annotations.SlingrEndpoint;
import io.slingr.endpoints.services.AppLogs;
import io.slingr.endpoints.services.HttpService;
import io.slingr.endpoints.services.IHttpExceptionConverter;
import io.slingr.endpoints.services.rest.RestClient;
import io.slingr.endpoints.services.rest.RestMethod;
import io.slingr.endpoints.utils.Json;
import io.slingr.endpoints.ws.exchange.WebServiceRequest;
import io.slingr.endpoints.ws.exchange.WebServiceResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * SLINGR Apps endpoint
 *
 * <p>Created by egonzalez on 08/08/17.
 */
@SlingrEndpoint(name = "fama", functionPrefix = "_")
public class FamaEndpoint extends HttpEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(FamaEndpoint.class);

    /**
     * List of REST methods enabled to process webhook events
     */
    private static final List<RestMethod> WEBHOOK_METHODS = Arrays.asList(RestMethod.PUT, RestMethod.POST);

    @ApplicationLogger
    private AppLogs appLogger;

    @EndpointProperty
    private String userName;

    @EndpointProperty
    private String userPassword;

    @EndpointProperty
    private String webhookToken;

    private FamaExceptionHandler handler = new FamaExceptionHandler();

    @Override
    public String getApiUri() {
        return "https://api.fama.io/api";
    }

    @Override
    public void endpointStarted() {
        try {
            doLogin();
        } catch(Exception e) {
            //do nothing
        }
        httpService().setupExceptionConverter(handler);
    }

    private void doLogin() {
        appLogger.info(String.format("Logging in with user [%s]", userName));
        Json response = RestClient.builder(getApiUri().concat("/oauth/token?grant_type=client_credentials"))
                .basicAuthentication(userName, userPassword).post();
        String token = !response.isEmpty("access_token") ? response.string("access_token") : null;
        httpService().setupBearerAuthenticationHeader(token);
        handler.retry = false;
        appLogger.info(String.format("Successfully logged in into with user [%s]", userName));
    }

    @EndpointWebService
    public WebServiceResponse webhookProcessor(WebServiceRequest request) {
        if(WEBHOOK_METHODS.contains(request.getMethod())){
            appLogger.info("Webhook received");
            final String sentToken = request.getHeader("token");
            if (StringUtils.isBlank(sentToken) || !sentToken.equals(webhookToken)) {
                appLogger.error(String.format("Webhook token [%s] is not valid", sentToken));
                return HttpService.defaultWebhookResponse(String.format("Token [%s] is not valid", sentToken), 401);
            }

            appLogger.info(String.format("WEBHOOK to process: %s", request.getJsonBody().toString()));
            events().send("webhook", request.getJsonBody());
        }
        return HttpService.defaultWebhookResponse();
    }

    /**
     * Converts the exceptions to permit to renew the token
     */
    class FamaExceptionHandler implements IHttpExceptionConverter {

        private boolean retry = false;

        @Override
        public EndpointException convertToEndpointException(Exception exception) {
            final EndpointException restException;
            if(exception instanceof EndpointException){
                restException = (EndpointException) exception;
            } else {
                restException = HttpService.defaultConvertToEndpointException(exception);
            }
            if(restException != null){
                if (restException.getAdditionalInfo() != null
                        && !restException.getAdditionalInfo().isEmpty("status")
                        && restException.getAdditionalInfo().integer("status") == 401) {
                    if (!retry) {
                        retry = true;
                        doLogin();
                        return EndpointException.retryable(ErrorCode.API, restException.getMessage());
                    } else {
                        retry = false;
                        return EndpointException.permanent(ErrorCode.API, "Cannot connect to Fama with configured user and password. Please contact your system administrator.");
                    }
                }
            }
            return restException;
        }
    }
}

