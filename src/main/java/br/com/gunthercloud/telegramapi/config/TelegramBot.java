package br.com.gunthercloud.telegramapi.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import br.com.gunthercloud.telegramapi.functions.Payment;
import br.com.gunthercloud.telegramapi.functions.Product;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import br.com.gunthercloud.telegramapi.handlers.CommandHandler;
import br.com.gunthercloud.telegramapi.handlers.MessageHandler;
import br.com.gunthercloud.telegramapi.utils.ReplyMarkupUtil;

@Component
public class TelegramBot extends TelegramLongPollingBot{

	private final MessageHandler messageHandler = new MessageHandler(this);
	private final Payment payment = new Payment(this);
	private final Product product = new Product(this, messageHandler);
	private final ReplyMarkupUtil replyMarkupUtil = new ReplyMarkupUtil(this, product, payment);
	private final CommandHandler commandHandler = new CommandHandler(this, product, messageHandler);

	private final String botName;
	private final Long idAdmin;

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
	public String PRODUCTS_REGISTERED = "Products.txt";

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
			throw new RuntimeException(e);
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
			if(BannedUsers(update)) {
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
		if(update.hasCallbackQuery()) {
			var callback = update.getCallbackQuery();
			String callbackData = callback.getData();
			Long chatId = callback.getMessage().getChatId();
			replyMarkupUtil.handleButtonInteraction(chatId, callbackData);
		}

	}

	private boolean BannedUsers(Update update) {
		try (BufferedReader br = new BufferedReader(new FileReader(BANNED_USERS))){
			String line = br.readLine();
			while(line != null) {
				String[] parts = line.split(" ");
				long id = Long.parseLong(parts[0]);
				if(update.hasMessage() && update.getMessage().getChatId() == id) {
					messageHandler.sendMessage(update, BANNED_MESSAGE);
					return true;
				}
				line = br.readLine();
			}
		}
		catch(IOException e) {
			throw new RuntimeException(e);
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
		product.productFinder(update,"Olá " + update.getMessage().getChat().getFirstName()
				+ ", seja bem vindo ao bot de serviços automáticos!" + "\n\nDev: @LucasBonny");
	}

	private void sendSticker(Update update, String sticker) {
		SendSticker stk = new SendSticker();
		stk.setChatId(update.getMessage().getChatId());
		stk.setSticker(new InputFile(sticker));

		try {
			execute(stk);
		}
		catch (TelegramApiException e) {
			throw new RuntimeException(e);
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
			throw new RuntimeException(e);
		}

	}
	public void executeMessage(SendPhoto message) {
		try {
			execute(message);
		}
		catch(TelegramApiException e) {
			throw new RuntimeException(e);
		}

	}

}
