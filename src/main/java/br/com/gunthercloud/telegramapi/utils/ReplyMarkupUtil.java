package br.com.gunthercloud.telegramapi.utils;

import br.com.gunthercloud.telegramapi.config.TelegramBot;
import br.com.gunthercloud.telegramapi.functions.Payment;
import br.com.gunthercloud.telegramapi.functions.Product;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class ReplyMarkupUtil {

    private final TelegramBot bot;
    private final Product product;
    private final Payment payment;

    public ReplyMarkupUtil(TelegramBot bot, Product product, Payment payment){
        this.bot = bot;
        this.product = product;
        this.payment = payment;
    }

    public void buttonConfirmation(SendMessage responseMessage){
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button1.setText("Sim");
        button2.setText("Não");
        button1.setCallbackData("paymentConfirmation");
        button2.setCallbackData("backToHome");
        row.add(button1);
        row.add(button2);
        rows.add(row);
        keyboardMarkup.setKeyboard(rows);
        responseMessage.setReplyMarkup(keyboardMarkup);
    }

    public String[] checkProduct(String callbackData){
        for(String e : product.checkArchive()) {
            String[] parts = e.split(" /-/ ");
            StringBuilder msg = new StringBuilder();
            if (callbackData.equals(parts[4])) {
                return parts;
            }
        }
        return null;
    }
    public void handleButtonInteraction(Long chatId, String callbackData) {

        String responseText = "null";
        SendMessage responseMessage = new SendMessage();
        responseMessage.setChatId(chatId);
        String[] parts = checkProduct(callbackData);
        if(callbackData.equals("backToHome")){
            responseMessage.setText("Compra cancelada! Digite /start para rever os produtos.");
        }
        if(callbackData.equals("paymentConfirmation")){
            responseMessage = payment.handlePaymentButton(parts);
            responseMessage.setChatId(chatId);
        }
        if(parts != null){
            if (callbackData.equals(parts[4])) {
                responseText = "Você deseja comprar um(a) " + parts[1] +
                        "\n\nValor: R$" + parts[3].replace(".", ",") +
                        "\n\nDescrição: " + parts[2];
                responseMessage.setText(responseText);
                buttonConfirmation(responseMessage);
            }
        }
        bot.executeMessage(responseMessage);
    }

}
