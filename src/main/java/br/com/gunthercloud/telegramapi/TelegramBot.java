package br.com.gunthercloud.telegramapi;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
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
		if(update.hasMessage() && update.getMessage().hasText()) {
			SendMessage message = new SendMessage();
			message.setText("Hello World!");
			message.setChatId(update.getMessage().getChatId());
			
			try {
				execute(message);
			}
			catch(TelegramApiException e) {
				e.getStackTrace();
			}
		}
		
	}

	@Override
	public String getBotUsername() {
		
		return this.botName;
	}

}
