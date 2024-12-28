package br.com.gunthercloud.telegramapi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import handlers.CommandHandler;
import handlers.MessageHandler;

@Component
public class TelegramBot extends TelegramLongPollingBot{

	private final CommandHandler commandHandler = new CommandHandler(this);
	private final MessageHandler messageHandler = new MessageHandler(this);
	
	private final String botName;
	private Long idAdmin;
	
	//Mensagens

	public String STICKER = "CAACAgIAAxkBAAOpZ2m28lRy3l_xMgRuUSQlRvrbSqwAAncAA0tCIhGZCao8QXigcjYE";
	public String INVALID_COMMAND = "Comando inválido, digite /ajuda e veja os comandos.";
	public String INVALID_USER = "Por gentileza, registre um usuário em suas configurações!";
	public String BAN_SUCCESSFUL = "Usuário banido com sucesso!";
	public String BANNED_MESSAGE = "Você foi banido! entre em contato com o dono! \n\n@LucasBonny";
	public String ALREADY_USER = "Usuário já está banido, caso deseje tirá-lo digite /unban!";
	public String REGISTERED_USER = "Usuário novo registrado!";
	public String NOT_EXISTS_USER = "Esse usuário não está presente nessa lista!";
	
	//Rotas
	public String STARTED_USERS = "Users.txt";
	public String BANNED_USERS = "BannedUsers.txt";
	
	//Comandos
	public String BAN_COMMAND = "Por gentileza, informe:\n\n/ban (ID) (MOTIVO)";
	public String UNBAN_COMMAND = "Por gentileza, informe:\n\n/unban (ID)";
	
	public TelegramBot(String botName, String botToken, Long idAdmin) {
		super(botToken);
		this.idAdmin = idAdmin;
		this.botName = botName;
	}

	private void userNameInvalid(Update update) {
		SendMessage error = new SendMessage();
		error.setChatId(update.getMessage().getChatId());
		error.setText(INVALID_USER);
		try {
			execute(error);
		}
		catch(TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onUpdateReceived(Update update) {
		/////////MENSAGEM///////////
		if(update.hasMessage() && update.getMessage().hasText()) {
			//Username invalid
			if(update.getMessage().getChat().getUserName() == null) {
				System.out.println(LocalDateTime.now()
						.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + " -> " + "O usuário com o ID " + update.getMessage().getChat().getFirstName() 
						+ "(" + update.getMessage().getChatId() + ") tentou usar o bot sem usuário!");
				userNameInvalid(update);
				return;
			}
			//Username banned
			if(BannedUsers(update) == true) {
				return;
			}
			if(update.hasMessage() && update.getMessage().hasText()) {
				String text = update.getMessage().getText();				
				if(text.startsWith("/")) {					
					commandHandler.handleCommand(update);
				}
				else {
					messageHandler.handleMessage(update);
				}
			}
			
		}
		else if(update.hasCallbackQuery()) {
			var callback = update.getCallbackQuery();
			String callbackData = callback.getData();
			Long chatId = callback.getMessage().getChatId();
			handleButtonInteraction(chatId, callbackData);
		}
		
	}
	
	//	handleButton
	private List<InlineKeyboardButton> newButton(String text,String data) {
		InlineKeyboardButton button = new InlineKeyboardButton();
		button.setText(text);
		button.setCallbackData(data);

        return List.of(button);
	}
	private List<InlineKeyboardButton> newButton(String text) {
		
		InlineKeyboardButton button = new InlineKeyboardButton();
		button.setText(text);
		button.setCallbackData(text);

        return List.of(button);
	}
	
	private boolean BannedUsers(Update update) {
		try (BufferedReader br = new BufferedReader(new FileReader(BANNED_USERS))){
			String line = br.readLine();
			while(line != null) {
				String[] parts = line.split(" ");
				long id = Long.parseLong(parts[0]);
				if(update.hasMessage() && update.getMessage().getChatId() == id) {
					sendMessage(update, BANNED_MESSAGE);
					return true;
				}
				line = br.readLine();
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	

	public void logHandle(Update update) {
		//Log
		System.out.println(LocalDateTime.now()
				.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + " -> " 
			+ "@"+ update.getMessage().getChat().getUserName() +"(" + update.getMessage().getChatId() + ") > " 
			+ update.getMessage().getText());
	}
	
	public void menuList(Update update) {
		sendSticker(update, STICKER);
		SendMessage message = new SendMessage();
		message.setChatId(update.getMessage().getChatId());
		message.setText("Olá " + update.getMessage().getChat().getFirstName() 
				+ ", seja bem vindo ao bot de serviços automáticos!" + "\n\nDev: @LucasBonny");
		InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

		List<List<InlineKeyboardButton>> rows = new ArrayList<>();
		List<InlineKeyboardButton> row1 = new ArrayList<>();
		
		InlineKeyboardButton button = new InlineKeyboardButton();
		button.setText("VAI VENDO");
		button.setCallbackData("option_2");
		
		row1.add(button);
		
		rows.add(newButton("💻 Hospedagem de Sites", "option_2"));
		rows.add(newButton("👨‍💼 Conversor"));
		rows.add(newButton("🚴‍♂️ Suporte"));
		rows.add(row1);
		
		keyboardMarkup.setKeyboard(rows);
		
		message.setReplyMarkup(keyboardMarkup);
		
		executeMessage(message);
		
	}
	
	private void sendSticker(Update update, String sticker) {
		SendSticker stk = new SendSticker();
		stk.setChatId(update.getMessage().getChatId());
		stk.setSticker(new InputFile(sticker));
		
		try {
			execute(stk);
		}
		catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	
	
//	private void createButton(SendMessage message, Update update) {
//		
//	}
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
		e.printStackTrace();
	}
}
	public void sendMessage(Update update, String message) {
		SendMessage msg = new SendMessage();
		msg.setChatId(update.getMessage().getChatId());
		msg.setText(message);
		msg.enableMarkdown(true);
		
		try {
			execute(msg);
		}
		catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	public void sendMessageAdmin(Update update, String message) {
		SendMessage msg = new SendMessage();
		msg.setChatId(idAdmin);
		msg.setText(message);
		msg.enableMarkdown(true);
		
		try {
			execute(msg);
		}
		catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getBotUsername() {
		
		return this.botName;
	}

	public Long getIdAdmin() {
		return idAdmin;
	}

	public void executeMessage(SendMessage message) {
		try {
			execute(message);
		}
		catch(TelegramApiException e) {
			e.printStackTrace();
		}
		
	}

}
