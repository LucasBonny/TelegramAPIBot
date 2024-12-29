package functions;

import br.com.gunthercloud.telegramapi.TelegramBot;
import java.io.File;

import handlers.MessageHandler;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Product {

    private final TelegramBot bot;
    private final MessageHandler messageHandler;

    public Product(TelegramBot bot, MessageHandler messageHandler) {
        this.bot = bot;
        this.messageHandler = messageHandler;
    }

    public void productFinder(Update update,String msg) {
        if(checkArchive().isEmpty()) {
            messageHandler.sendMessageAdmin(update, "Não há produtos registrados ainda!\n\nDigite /criarprod e registre um novo.");
            return;
        }

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for(String e : checkArchive()) {
            String[] parts = e.split(" /-/ ");
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(parts[1]);
            button.setCallbackData(parts[4]);
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
            button.setText(parts[1]);
        }
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(rows);
        messageHandler.sendMessageWithButton(update, msg, keyboardMarkup);
    }
    private void createProduct(Update update) {
        //criar o produto

        //criar o callback

    }

    private void removeProduct(Update update) {

    }

    private void editProduct(Update update) {

    }

    private List<String> checkArchive(){
            File path = new File(bot.PRODUCTS_REGISTERED);
            if(!path.exists()){
                    try {

                        if (path.createNewFile()) {
                            System.out.println("Arquivo criado com sucesso!");
                        }
                    }
                    catch (IOException e) {
                                throw new RuntimeException("Erro ao criar o arquivo no diretório");
                    }
            }
        try (BufferedReader br = new BufferedReader(new FileReader(bot.PRODUCTS_REGISTERED))){
            List<String> lines = new ArrayList<>();
            String line;
            while((line = br.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
