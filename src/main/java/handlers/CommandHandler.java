package handlers;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import br.com.gunthercloud.telegramapi.TelegramBot;

public class CommandHandler {
	
	private final TelegramBot bot;
	
	public CommandHandler(TelegramBot bot) {
		this.bot = bot;
	}
	//Commands
	/*	
	 User Commands
	 * /start
	 * /id
	 * /suporte
	 * /ajuda
	 Admin Commands
	 * /ban
	 * /unban
	 * /deletartudo
	 * /verid
	 * /anunciar
	 * /usuarios
	 * /licenca
	 * /banidos
	 * /ajuda
	 * */
	
	public void handleCommand(Update update) {
		String command = update.getMessage().getText();
		Long ChatId = update.getMessage().getChatId();
		
		switch(command.toLowerCase()) {
			case "/start": {
				try (BufferedReader br = new BufferedReader(new FileReader(bot.STARTED_USERS))){
					String line = br.readLine();
					boolean exists = false;
					while(line != null) {
						String[] parts = line.split(" ");
						if(ChatId == Long.parseLong(parts[1])) {
							exists = true;
						}
						line = br.readLine();
					}
					if(!exists) {
						try (BufferedWriter bw = new BufferedWriter(new FileWriter(bot.STARTED_USERS,true))){
							bw.write(update.getMessage().getChat().getUserName() + " " 
						+ update.getMessage().getChat().getId() + " " + LocalDateTime.now()
						.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
							bot.sendMessageAdmin(update, bot.REGISTERED_USER + "\n\n@" + update.getMessage().getChat().getUserName() + " `" 
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
				bot.menuList(update);
				bot.logHandle(update);
				break;
			}
			case "/ajuda": {
				bot.logHandle(update);
				bot.executeMessage(helpCommand(update));
				break;
			}
			case "/verid": {
				bot.logHandle(update);
				if(isAdmin(update)) break;
				String[] parts = update.getMessage().getText().split(" ");
				
				if(parts.length < 2) {
					bot.sendMessage(update, "Por gentileza, informe:\n\n/verid (ID)");
					break;
				}
				
				try (BufferedReader br = new BufferedReader(new FileReader(bot.STARTED_USERS))){
					String line;
					while((line = br.readLine()) != null) {
						String[] user = line.split(" ");
						if(parts[1].equals(user[1])) {
							bot.sendMessage(update, "Usuário: @" + user[0] + "\nID: `" + user[1] + "`\nRegistrado: \n" + user[2] + " " + user[3]);
							break;
						}
					}
                    bot.sendMessage(update, "Esse ID não existe! digite /usuarios para ver os usuários registrados!");
                    break;
                }
				catch(IOException e) {
					e.printStackTrace();
				}
				break;
			}
			case "/id": {
				bot.logHandle(update);
				bot.sendMessage(update, update.getMessage().getChat().getFirstName() + " seu id é: \n " + update.getMessage().getChatId());
			break;
			}
			case "/banidos": {
				bot.logHandle(update);
				if(isAdmin(update)) break;
				//Leio linha por linha do arquivo
				try (BufferedReader br = new BufferedReader(new FileReader(bot.BANNED_USERS))){
					String line = br.readLine();
					if(line == null) {
						bot.sendMessage(update, "Não há usuários banidos!");
						break;
					}
					StringBuilder mensagem = new StringBuilder();
					mensagem.append("❌ Usuários Banidos ❌\n\n");
					
					while(line != null) {
						String[] parts = line.split(" ");
						
						//logica de busca
						try (BufferedReader br2 = new BufferedReader(new FileReader(bot.STARTED_USERS))){
							String line2 = br2.readLine();
							while(line2 != null) {
								String[] users = line2.split(" ");
								if(parts[0].hashCode() == users[1].hashCode()) {
									if(parts[0].equals(users[1])) {
										mensagem.append("@").append(users[0]).append(" ");
									}
								}
								line2 = br2.readLine();
							}
						}
						
						mensagem.append("`").append(parts[0]).append("` ");
						for(int i = 1; i < parts.length; i++) {
							
							mensagem.append(parts[i]).append(" ");
							if(i == parts.length -1) {
								mensagem.append("\n");
							}
						}
						line = br.readLine();
					}
					bot.sendMessage(update, mensagem.toString());
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
			case "/unban" : {

				bot.logHandle(update);
				if(isAdmin(update)) break;
				String[] parts = command.split(" ");

				if(parts.length < 2 || parts[1].matches(".*[a-zA-Z].*")) {
					String msg = parts.length < 2 ? bot.UNBAN_COMMAND : "Insira somente numeros no ID!\n" + bot.UNBAN_COMMAND;
					bot.sendMessage(update, msg);
					break;
				}

				String userIdToUnban = parts[1];
				boolean exists = false;

				try(BufferedReader br = new BufferedReader(new FileReader(bot.BANNED_USERS));
					BufferedWriter bw = new BufferedWriter(new FileWriter(bot.BANNED_USERS + "_temp"))){
					String line;
					while((line = br.readLine()) != null) {
						String[] val = line.split(" ");
						if(val[0].equals(userIdToUnban)) {
							exists = true;
						}
						else {
							bw.write(line);
							bw.newLine();
						}
					}
					if(!exists) {
						bot.sendMessage(update, bot.NOT_EXISTS_USER);
						break;
					}
				} catch (IOException e) {
					bot.sendMessage(update, "Erro ao processar a lista de banidos!");
					e.printStackTrace();
					break;
				}
                System.out.println("Teste 1");
                File originalFile = new File(bot.BANNED_USERS);
                File tempFile = new File(bot.BANNED_USERS + "_temp");
                if(originalFile.delete() && tempFile.renameTo(originalFile)) {
                    bot.sendMessage(update, "Usuário desbanido com sucesso!");
                }
                else {
                    bot.sendMessage(update, "Erro ao atualizar a lista de banidos!");
                }
            }
			case "/ban" : {
				bot.logHandle(update);
				if(isAdmin(update)) break;

				String[] parts = command.split(" ");
				if(parts.length < 3) {
					bot.sendMessage(update, bot.BAN_COMMAND);
					break;
				}
				if(parts[1].matches(".*[a-zA-Z].*")) {
					bot.sendMessage(update, "Insira somente numeros no ID!\n" + bot.BAN_COMMAND);
					break;
				}
				if(bot.getIdAdmin() == Long.parseLong(parts[1])) {
					bot.sendMessage(update, "Você não pode banir esse usuário!");
					break;
				}

				try (BufferedReader br = new BufferedReader(new FileReader(bot.BANNED_USERS))){
					File bannedFile = new File(bot.BANNED_USERS);
					String line = br.readLine();
					while(line != null) {
						String[] read = line.split(" ");
						if(read[0].equals(parts[1])) {
							bot.sendMessage(update, bot.ALREADY_USER);
							break;
						}
						line = br.readLine();
					}
				}
				catch(IOException e){
					e.printStackTrace();
				}

				try (BufferedWriter bw = new BufferedWriter(new FileWriter(bot.BANNED_USERS,true))){
					for(int i = 1; i < parts.length; i++) {
						if(i < parts.length - 1) {
							 bw.write(parts[i] + " ");
						}
						else bw.write(parts[i]);
					}
					bot.sendMessage(update, bot.BAN_SUCCESSFUL);
					bw.newLine();
				}
				catch(IOException e){
					e.printStackTrace();
				}

			}
			case "/usuarios" : {
				bot.logHandle(update);
				if (isAdmin(update)) break;
				SendMessage message = new SendMessage();
				message.setChatId(update.getMessage().getChatId());
				message.enableMarkdown(true);
				List<String> list = readFile(bot.STARTED_USERS);
				StringBuilder msg = new StringBuilder();
				msg.append("🧾 Usuários registrados 🧾\n\n");
				for(String e : list){
					String[] parts = e.split(" ");
					msg.append("@").append(parts[0])
							.append(" `").append(parts[1]).append("` ")
							.append(parts[2]).append(" ").append(parts[3])
							.append("\n");
				}
				message.setText(msg.toString());
				bot.executeMessage(message);
				break;
			}
			default:
				SendMessage message = new SendMessage();
				message.setChatId(update.getMessage().getChatId());
				message.setText(bot.INVALID_COMMAND);
				bot.executeMessage(message);
				break;
		}
		
	}

	private StringBuilder commands(Update update) {
		StringBuilder msg = new StringBuilder();
		msg.append("💬 Comandos de Ajuda 💬\n\n");

		if(update.getMessage().getChatId() == (long) bot.getIdAdmin()) {
			msg.append("/ban - Banir um usuário\n");
			msg.append("/banidos - Ver banidos\n");
			msg.append("/ajuda - Ver comandos admin\n");
			msg.append("/unban - Desbanir um usuário\n");
			msg.append("/deletartudo - Limpar tudo no bot\n");
			msg.append("/licenca - Ver informações da licença\n");
			msg.append("/usuarios - Ver usuários registrados\n");

		}
		else {
			msg.append("/start - Iniciar o bot\n");
			msg.append("/id - Ver id\n");
			msg.append("/suporte - Iniciar o bot\n");
			msg.append("/ajuda - Ver comandos \n");
		}
		return msg;
	}
	private SendMessage helpCommand(Update update) {
		SendMessage message = new SendMessage();
		message.setText(commands(update).toString());
		message.setChatId(update.getMessage().getChatId());
		return message;
	}

	public boolean isAdmin(Update update) {
		String[] parseCommand = update.getMessage().getText().split(" ");
		if(update.getMessage().getChatId() == (long) bot.getIdAdmin()) {
			return false;
		}
		bot.sendMessage(update, bot.INVALID_COMMAND);
		bot.sendMessageAdmin(update,"O usuário @" + update.getMessage().getChat().getUserName() + "(`" + update.getMessage().getChatId() + "`) tentou usar o comando " + parseCommand[0]);
		return true;
	}
	public ArrayList<String> readFile(String dir) {
		try(BufferedReader br = new BufferedReader(new FileReader(dir))) {
			String line;
			ArrayList<String> res = new ArrayList<>();
			while((line = br.readLine()) != null){
				res.add(line);
			}
			return res;
		} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
	
}
