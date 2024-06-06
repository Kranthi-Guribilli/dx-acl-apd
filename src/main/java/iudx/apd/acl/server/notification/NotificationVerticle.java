package iudx.apd.acl.server.notification;

import static iudx.apd.acl.server.apiserver.util.Constants.APD_URL;
import static iudx.apd.acl.server.common.Constants.NOTIFICATION_SERVICE_ADDRESS;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.serviceproxy.ServiceBinder;
import iudx.apd.acl.server.authentication.AuthClient;
import iudx.apd.acl.server.policy.CatalogueClient;
import iudx.apd.acl.server.policy.PostgresService;

public class NotificationVerticle extends AbstractVerticle {
  private DeleteNotification deleteNotification;
  private UpdateNotification updateNotification;
  private GetNotification getNotification;
  private PostgresService postgresService;
  private NotificationServiceImpl notificationService;
  private CreateNotification createNotification;
  private CatalogueClient catalogueClient;
  private EmailNotification emailNotification;
  private GetDelegateEmailIds getDelegateEmailIds;
  private WebClient webClient;
  private WebClientOptions webClientOptions;
  private AuthClient authClient;

  @Override
  public void start() {
    postgresService = new PostgresService(config(), vertx);
    catalogueClient = new CatalogueClient(config());
    webClientOptions = new WebClientOptions();
    webClientOptions.setTrustAll(false).setVerifyHost(true).setSsl(true);
    webClient = WebClient.create(vertx, webClientOptions);
    getDelegateEmailIds = new GetDelegateEmailIds(config(), webClient);

    authClient = new AuthClient(config(), webClient);

    emailNotification = new EmailNotification(vertx, config(), getDelegateEmailIds);
    String apdURL = config().getString(APD_URL);
    createNotification =
        new CreateNotification(postgresService, catalogueClient, emailNotification, authClient, apdURL);
    deleteNotification = new DeleteNotification(postgresService);
    updateNotification = new UpdateNotification(postgresService);
    getNotification = new GetNotification(postgresService);
    notificationService =
        new NotificationServiceImpl(
            deleteNotification, updateNotification, getNotification, createNotification);
    new ServiceBinder(vertx)
        .setAddress(NOTIFICATION_SERVICE_ADDRESS)
        .register(NotificationService.class, notificationService);
  }
}
