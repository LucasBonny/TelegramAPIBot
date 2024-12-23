package br.com.gunthercloud.telegramapi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegramBot extends TelegramLongPollingBot{
	
	private final String botName;
	
	public TelegramBot(String botName, String botToken) {
		super(botToken);
		this.botName = botName;
	}

	@Override
	public void onUpdateReceived(Update update) {
		/////////MENSAGEM///////////
		if(update.hasMessage() && update.getMessage().hasText()) {
			
			//Metodo /start
			if(update.getMessage().getText().equalsIgnoreCase("/start")) {
				System.out.println(update.getMessage());
				sendStartMessage(update.getMessage().getChatId());

				//Log
				System.out.println(LocalDateTime.now()
						.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) 
						+ " -> " + update.getMessage().getChatId() + " > " 
						+ update.getMessage().getText());
				
			}
			else {
				sendMessageBot("Comando inválido, digite /start.", update.getMessage().getChatId());
			}
		}
		else if(update.hasCallbackQuery()) {
			var callback = update.getCallbackQuery();
			String callbackData = callback.getData();
			Long chatId = callback.getMessage().getChatId();
			handleButtonInteraction(chatId, callbackData);
		}
		
		////////ARQUIVO//////////
		if(update.hasMessage() && update.getMessage().hasDocument()) {
			var chatId = update.getMessage().getChatId();
			try {
				execute(new SendMessage(chatId.toString(), "Você enviou um arquivo!"));
			} catch (TelegramApiException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		
	}
	
	private void sendStartMessage(Long chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId.toString());
		message.setText("Olá seja bem vindo ao chatBOT!");
		
		//Lista de botões - ArrayList
		InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rows = new ArrayList<>();
		
		//Botão 1
		List<InlineKeyboardButton> row1 = new ArrayList<>();
		InlineKeyboardButton button1 = new InlineKeyboardButton();
		button1.setText("Ajuda");
		button1.setCallbackData("option_1");
		
		//Botão 2
		List<InlineKeyboardButton> row2 = new ArrayList<>();
		InlineKeyboardButton button2 = new InlineKeyboardButton();
		button2.setText("Fala ae");
		button2.setCallbackData("option_2");
		
		InlineKeyboardButton button3 = new InlineKeyboardButton();
		button3.setCallbackData("option_3");
		button3.setText("Nada aki");
		
		row1.add(button1);
		row2.add(button2);
		row2.add(button3);
		
		rows.add(row1);
		rows.add(row2);
		
		keyboardMarkup.setKeyboard(rows);
		
		message.setReplyMarkup(keyboardMarkup);
		
		try {
			execute(message);
		}
		catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	private void sendMessageBot(String msg, Long chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId.toString());
		message.setText(msg);
		message.enableMarkdown(true);
		
		try {
			execute(message);
		}
		catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	
	
	private void handleButtonInteraction(Long chatId, String callbackData) {
		String responseText = switch(callbackData) {
			case "option_1" -> "Teste 1";
			case "option_2" -> "Teste 2";
			default -> "Teste 3";
		};
		
		SendMessage responseMessage = new SendMessage();
		responseMessage.setChatId(chatId);
		responseMessage.setText(responseText);
		
		try {
			execute(responseMessage);
		}
		catch (TelegramApiException e) {
			e.getStackTrace();
		}
	}

	@Override
	public String getBotUsername() {
		
		return this.botName;
	}

}
