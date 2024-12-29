package handlers;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import br.com.gunthercloud.telegramapi.TelegramBot;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MessageHandler {
	
	private final TelegramBot bot;
	
	public MessageHandler(TelegramBot bot) {
		this.bot = bot;
	}

	public void sendAdvertisement(Update update) {

	}

	public void sendMessageAdmin(Update update, String message) {
		SendMessage msg = new SendMessage();
		msg.setChatId(bot.getIdAdmin());
		msg.setText(message);
		msg.enableMarkdown(true);

		try {
			bot.execute(msg);
		}
		catch (TelegramApiException e) {
			throw new RuntimeException(e);
		}
	}

	public void sendMessage(Update update, String message) {
		SendMessage msg = new SendMessage();
		msg.setChatId(update.getMessage().getChatId());
		msg.setText(message);
		msg.enableMarkdown(true);

		try {
			bot.execute(msg);
		}
		catch (TelegramApiException e) {
			throw new RuntimeException(e);
		}
	}
	public void sendMessageWithButton(Update update, String message, InlineKeyboardMarkup markup) {
		SendMessage msg = new SendMessage();
		msg.setChatId(update.getMessage().getChatId());
		msg.setText(message);
		msg.enableMarkdown(true);
		msg.setReplyMarkup(markup);

		try {
			bot.execute(msg);
		}
		catch (TelegramApiException e) {
			throw new RuntimeException(e);
		}
	}

	public void handleMessage(Update update) {
		Long ChatId = update.getMessage().getChatId();
		String text = update.getMessage().getText();

		if(!text.startsWith("/")) {
			SendMessage message = new SendMessage();
			message.setText(bot.INVALID_COMMAND);
			message.setChatId(ChatId);
			bot.executeMessage(message);
			return;
		}
	}
}
