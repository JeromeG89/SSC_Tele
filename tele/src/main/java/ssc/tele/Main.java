package ssc.tele;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        try {
            // Register the bot with the TelegramBots API
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            // Register your bot here
            
            botsApi.registerBot(new WorkScheduleBot());
            
            // Log success message to confirm the bot is running
            System.out.println("✅ Bot is running...");
        } catch (TelegramApiException e) {
            e.printStackTrace();
            System.out.println("❌ Bot failed to start.");
        }
    }
}
