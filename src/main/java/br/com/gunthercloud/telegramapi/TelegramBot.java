package br.com.gunthercloud.telegramapi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.InputMismatchException;
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

@Component
public class TelegramBot extends TelegramLongPollingBot{
	
	private final String botName;
	private Long idAdmin;
	
	//Mensagens

	String STICKER = "CAACAgIAAxkBAAOpZ2m28lRy3l_xMgRuUSQlRvrbSqwAAncAA0tCIhGZCao8QXigcjYE";
	String INVALID_COMMAND = "Comando inválido, digite /start.";
	String INVALID_USER = "Por gentileza, registre um usuário em suas configurações!";
	String BAN_SUCCESSFUL = "Usuário banido com sucesso!";
	String BANNED_MESSAGE = "Você foi banido! entre em contato com o dono! \n\n@LucasBonny";
	String ALREADY_USER = "Usuário já está banido, caso deseje tirá-lo digite /unban!";
	String REGISTERED_USER = "Usuário novo registrado!";
	
	//Rotas
	String STARTED_USERS = "Users.txt";
	String BANNED_USERS = "BannedUsers.txt";
	
	//Comandos
	String BAN_COMMAND = "Por gentileza, informe:\n\n/ban (ID) (MOTIVO)";
	
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
			
			//Commands
			var command = update.getMessage().getText().toLowerCase();
			/*	
			 User Commands
			 * /start
			 * /id
			 * /suporte
			 * /ajuda
			 Admin Commands
			 * /ban
			 * /unban
			 * /banidos
			 * /ajuda
			 * */

			// User Commands
			if(command.startsWith("/start")) {
				try (BufferedReader br = new BufferedReader(new FileReader(STARTED_USERS))){
					String line = br.readLine();
					boolean exists = false;
					while(line != null) {
						String[] parts = line.split(" ");
						if(update.getMessage().getChatId() == (long) Long.parseLong(parts[1])) {
							exists = true;
						}
						line = br.readLine();
					}
					if(exists == false) {
						try (BufferedWriter bw = new BufferedWriter(new FileWriter(STARTED_USERS,true))){
							bw.write(update.getMessage().getChat().getUserName() + " " 
						+ update.getMessage().getChat().getId() + " " + LocalDateTime.now()
						.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
							sendMessageAdmin(update, REGISTERED_USER + "\n\n@" + update.getMessage().getChat().getUserName() + " `" 
									+ update.getMessage().getChat().getId() + "`");
							bw.newLine();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				menuList(update);
				logHandle(update);
				sendStartMessage(update);
				return;
			}
			if(command.startsWith("/id")) {
				logHandle(update);
				sendMessage(update, update.getMessage().getChat().getFirstName() + " seu id é: \n " + update.getMessage().getChatId());
				return;
			}
			
			// Admin Commands

			if(command.startsWith("/banidos")) {
				logHandle(update);
				
				//Leio linha por linha do arquivo
				try (BufferedReader br = new BufferedReader(new FileReader(BANNED_USERS))){
					String line = br.readLine();
					if(line == null) {
						sendMessage(update, "Não há usuários banidos!");
						return;
					}
					StringBuilder mensagem = new StringBuilder();
					mensagem.append("❌ Usuários Banidos ❌\n\n");
					
					while(line != null) {
						String[] parts = line.split(" ");
						
						//logica de busca
						try (BufferedReader br2 = new BufferedReader(new FileReader(STARTED_USERS))){
							String line2 = br2.readLine();
							while(line2 != null) {
								String[] users = line2.split(" ");
								if(parts[0].hashCode() == users[1].hashCode()) {
									if(parts[0].equals(users[1])) {
										mensagem.append("@" + users[0] +" ");
									}
								}
								line2 = br2.readLine();
							}
						}
						
						mensagem.append("`" + parts[0] +"` ");
						for(int i = 1; i < parts.length; i++) {
							
							mensagem.append(parts[i] + " ");
							if(i == parts.length -1) {
								mensagem.append("\n");
							}
						}
						line = br.readLine();
					}
					sendMessage(update, mensagem.toString());
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
			
			if(command.startsWith("/unban")) {
				logHandle(update);
				return;
			}
			
			if(command.startsWith("/ban")) {
				logHandle(update);
				
				if(!(update.getMessage().getChatId() == (long) idAdmin)) {
					sendMessage(update, INVALID_COMMAND);
					sendMessageAdmin(update,"O usuário @" + update.getMessage().getChat().getUserName() + "(`" + update.getMessage().getChatId() + "`) tentou usar o /ban!");
					return;
				}
				
				String[] parts = command.split(" ");
				;
				if(parts.length < 3) {
					sendMessage(update, BAN_COMMAND);
					return;
				}
				if(parts[1].matches(".*[a-zA-Z].*")) {
					sendMessage(update, "Insira somente numeros no ID!\n" + BAN_COMMAND);
					return;
				}
				
				try (BufferedReader br = new BufferedReader(new FileReader(BANNED_USERS))){
					File bannedFile = new File(BANNED_USERS);
					if(!bannedFile.exists()) {
						bannedFile.createNewFile();
					}
					String line = br.readLine();
					while(line != null) {
						String[] read = line.split(" ");
						if(read[0].equals(parts[1])) {
							sendMessage(update, ALREADY_USER);
							return;
						}
						line = br.readLine();
					}
				}
				catch(FileNotFoundException e) {
					e.printStackTrace();
				}
				catch(IOException e){
					e.printStackTrace();
				}
				
				try (BufferedWriter bw = new BufferedWriter(new FileWriter(BANNED_USERS,true))){
					for(int i = 1; i < parts.length; i++) {
						if(i < parts.length - 1) {
							 bw.write(parts[i] + " ");
						}
						else bw.write(parts[i]);
					}
					sendMessage(update, BAN_SUCCESSFUL);
					bw.newLine();
				}
				catch(IOException e){
					e.printStackTrace();
				}
				return;
			}
			else {
				sendMessage(update, INVALID_COMMAND);
			}
		}
		else if(update.hasCallbackQuery()) {
			var callback = update.getCallbackQuery();
			String callbackData = callback.getData();
			Long chatId = callback.getMessage().getChatId();
			handleButtonInteraction(chatId, callbackData);
		}
		
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
	private void logHandle(Update update) {
		//Log
		System.out.println(LocalDateTime.now()
				.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + " -> " 
			+ "@"+ update.getMessage().getChat().getUserName() +"(" + update.getMessage().getChatId() + ") > " 
			+ update.getMessage().getText());
	}
	
	private void menuList(Update update) {
		sendSticker(update, STICKER);
		sendMessage(update, "Olá " + update.getMessage().getChat().getFirstName() 
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
			e.printStackTrace();
		}
	}
	
	private void sendStartMessage(Update update) {
		SendMessage message = new SendMessage();
		message.setText("VICIADO");
		message.setChatId(update.getMessage().getChatId().toString());
		//Lista de botões - ArrayList
		InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
		List<List<InlineKeyboardButton>> rows = new ArrayList<>();
		
		//Botão 1
		List<InlineKeyboardButton> row1 = new ArrayList<>();
		InlineKeyboardButton button1 = new InlineKeyboardButton();
		button1.setText("Produto 1");
		button1.setCallbackData("option_1");
		
		//Botão 2
		List<InlineKeyboardButton> row2 = new ArrayList<>();
		InlineKeyboardButton button2 = new InlineKeyboardButton();
		button2.setText("Produto 2");
		button2.setCallbackData("option_2");
		
		InlineKeyboardButton button3 = new InlineKeyboardButton();
		button3.setCallbackData("option_3");
		button3.setText("Produto 3");
		
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
	private void sendMessage(Update update, String message) {
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
	private void sendMessageAdmin(Update update, String message) {
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

}
