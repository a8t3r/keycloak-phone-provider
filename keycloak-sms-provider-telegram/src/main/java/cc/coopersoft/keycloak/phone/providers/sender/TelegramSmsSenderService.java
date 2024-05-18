package cc.coopersoft.keycloak.phone.providers.sender;

import cc.coopersoft.keycloak.phone.Utils;
import cc.coopersoft.keycloak.phone.providers.exception.MessageSendException;
import cc.coopersoft.keycloak.phone.providers.spi.FullSmsSenderAbstractService;
import jakarta.ws.rs.BadRequestException;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class TelegramSmsSenderService extends FullSmsSenderAbstractService {

    private static final Logger logger = Logger.getLogger(TelegramSmsSenderService.class);

    public static final String CONFIG_APP_API_TOKEN = "token";

    private final KeycloakSession session;
    private final TelegramClient telegramClient;

    public TelegramSmsSenderService(Config.Scope config, KeycloakSession session) {
        super(session.getContext().getRealm().getDisplayName());
        this.session = session;
        this.telegramClient = new OkHttpTelegramClient(config.get(CONFIG_APP_API_TOKEN));
    }

    @Override
    public void sendMessage(String phoneNumber, String message) throws MessageSendException {
        String telegramChatId = Utils.findUserByPhone(session, session.getContext().getRealm(), phoneNumber)
            .flatMap(it -> it.getAttributeStream("telegramChatId").findFirst())
            .orElseThrow(() -> new BadRequestException("no chat id provided for phone number"));

        logger.info(String.format("To: %s >>> %s", phoneNumber, message));
        SendMessage sendMessage = new SendMessage(telegramChatId, "Verification code: " + message);
      try {
        telegramClient.execute(sendMessage);
      } catch (TelegramApiException e) {
        throw new MessageSendException(e.getMessage(), e);
      }
    }

    @Override
    public void close() {
    }
}
