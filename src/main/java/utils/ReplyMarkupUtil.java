package utils;

import br.com.gunthercloud.telegramapi.TelegramBot;
import functions.Product;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class ReplyMarkupUtil {

    private final TelegramBot bot;
    private final Product product;

    public ReplyMarkupUtil(TelegramBot bot, Product product){
        this.bot = bot;
        this.product = product;
    }

    public void buttonConfirmation(SendMessage responseMessage){
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button1.setText("Sim");
        button2.setText("Não");
        button1.setCallbackData("2");
        button2.setCallbackData("2");
        row.add(button1);
        row.add(button2);
        rows.add(row);
        keyboardMarkup.setKeyboard(rows);
        responseMessage.setReplyMarkup(keyboardMarkup);
    }

    public void handleButtonInteraction(Long chatId, String callbackData) {

        String responseText = "null";
        for(String e : product.checkArchive()) {
            String[] parts = e.split(" /-/ ");
            StringBuilder msg = new StringBuilder();
            if (callbackData.equals(parts[4])) {
                msg.append("Você deseja comprar um(a) ").append(parts[1])
                        .append("\n\nValor: R$").append(parts[3].replace(".",","))
                        .append("\n\nDescrição: ").append(parts[2]);
                responseText = msg.toString();
                SendMessage responseMessage = new SendMessage();
                responseMessage.setChatId(chatId);
                responseMessage.setText(responseText);
                buttonConfirmation(responseMessage);
                bot.executeMessage(responseMessage);
                return;
            }
        }
        SendMessage responseMessage = new SendMessage();
        responseMessage.setChatId(chatId);
        responseMessage.setText(responseText);

        bot.executeMessage(responseMessage);
    }


}
