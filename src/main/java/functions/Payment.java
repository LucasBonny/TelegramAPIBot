package functions;

import br.com.gunthercloud.telegramapi.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class Payment {

    private final TelegramBot bot;

    public Payment(TelegramBot bot) {
        this.bot = bot;
    }

    public SendMessage handlePaymentButton(String[] values) {
        SendMessage message = new SendMessage();
        message.setText("TESTE");
        return message;
    }
}
