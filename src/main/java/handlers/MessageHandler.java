package handlers;

import org.telegram.telegrambots.meta.api.objects.Update;

import br.com.gunthercloud.telegramapi.TelegramBot;

public class MessageHandler {
	
	private final TelegramBot bot;
	
	public MessageHandler(TelegramBot bot) {
		this.bot = bot;
	}

	public void handleMessage(Update update) {
		
	}

	
}
