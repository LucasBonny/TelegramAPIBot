package br.com.gunthercloud.telegramapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotConfig {
	@Bean
	public TelegramBot telegramBot(@Value("${bot.name}") String botName,
			@Value("${bot.token}") String botToken, @Value("${bot.admin}") Long idAdmin) {
		TelegramBot telegramBot = new TelegramBot(botName, botToken, idAdmin);
		try {
			var telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
			telegramBotsApi.registerBot(telegramBot);
		}
		catch(TelegramApiException e) {
			e.getStackTrace();
		}
		return telegramBot;
	}
}
