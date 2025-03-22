package ssc.tele;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class WorkScheduleBot extends TelegramLongPollingBot {
    // Step 3.1: HashMap to store working dates with the number of employees
    private static final String JSON_FILE = "data.json";
    private JsonObject workDataJson; //Storing WorkDates
    private JsonObject workDates; //Storing employees assigned and details workDates <: workDataJson
    private final Gson gson;

    public WorkScheduleBot() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();  // Initialize gson first
        this.workDataJson = loadJsonData("workDataTest.json"); // Now load JSON after gson is ready
        if (workDataJson.has("workDates")) {
            this.workDates = workDataJson.getAsJsonObject("workDates"); // Get all work date details
        } else {
            this.workDates = new JsonObject();
            this.workDataJson.add("workDates", this.workDates);
        }
    }

    private JsonObject loadJsonData(String filePath) {
        JsonObject data;
        try (FileReader reader = new FileReader(filePath)) {
            data = JsonParser.parseReader(reader).getAsJsonObject();
            System.out.println("✅ JSON data loaded successfully.");
            return data;
        } catch (IOException e) {
            System.out.println("⚠️ JSON file not found. Initializing empty JSON.");
            data = new JsonObject();  // Create empty JSON if file doesn't exist
            data.add("users", gson.toJsonTree(new ArrayList<>()));
            return data;
        }
    }

    private void saveJsonData(String filePath, JsonObject data) {
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(data, writer);
            System.out.println("✅ JSON data saved successfully.");
        } catch (IOException e) {
            System.out.println("❌ Failed to save JSON data.");
            e.printStackTrace();
        }
    }
    
    @Override
    public String getBotUsername() {
        return "SSC_AssistantBot";  // Replace with your bot's username
    }

    @Override
    public String getBotToken() {
        return "7557997630:AAFofMAbnB-FtFO0NrW6RGoOlI2ZaiYWZV8";  // Replace with your bot's token
    }

    public void handleSetWork(Update update) {
        Message message = update.getMessage();  // Get the message text
        String messageText = message.getText();
        long chatId = update.getMessage().getChatId();  // Get the chat ID
        Long userId = message.getFrom().getId();
        String username = message.getFrom().getUserName();

        String[] parts = messageText.split(" ");
        JsonObject newWork = new JsonObject();
        if (parts.length == 3) {
            String dateString = parts[1];
            Date date = parseDate(dateString);
            if (date == null) {
                sendMessage(chatId, "Invalid format. Use: /setworkdate <yyyy-mm-dd> <number_of_employees>");
                return;
            }
            newWork.addProperty("createdId", userId);
            newWork.addProperty("createdName", username);
            int employeesCount = Integer.parseInt(parts[2]);  // Parse the number of employees
            JsonObject workSlots = new JsonObject();
            newWork.add("workSlots" ,workSlots);

            for (int i = 1; i <= employeesCount; i++) { //TODO handle time, duration and pay
                JsonObject slot = new JsonObject();
                slot.addProperty("time", "5pm");
                slot.addProperty("duration", "2hrs");
                slot.addProperty("pay", "$24");
                workSlots.add("WorkSlot " + String.valueOf(i), slot);
            }
            // Add or update the work date in the Json
            this.workDates.add(dateString, newWork);
            sendMessage(chatId, "Work date " + dateString + " has been set with " + employeesCount + " employees.");
        } else {
            sendMessage(chatId, "Invalid format. Use: /setworkdate <date> <number_of_employees>");
        }
    }
    
    public void handleSignUp(Update update) {
        Message message = update.getMessage();  // Get the message text
        String messageText = message.getText();
        long chatId = update.getMessage().getChatId();  // Get the chat ID
        Long userId = message.getFrom().getId();
        String username = message.getFrom().getUserName();

        String[] parts = messageText.split(" ");
        String slot = parts[2];

        // Extract the date from the message and sign up the user
        String dateString = parts[1];
        Date date = parseDate(dateString);
        
        if (date == null) {
            sendMessage(chatId, "Invalid format. Use: /singup <yyyy-mm-dd> <work_slot>"); 
            return;
        } else if (!this.workDates.has(dateString)) {
            sendMessage(chatId, "Invalid Date, work date does not exist please contact god. ");
            return;
        }
        JsonObject workDetails = this.workDates.getAsJsonObject(dateString);
        JsonObject slots = workDetails.getAsJsonObject("workSlots");
        
        if (!slots.has("WorkSlot " + slot)) {
            sendMessage(chatId, "Invalid Slot, slot does not exist");
            return;
        }

        JsonObject slotDetails = slots.getAsJsonObject("WorkSlot " + slot);

        if (slotDetails.has("assignedId")) {
            sendMessage(chatId, "Invalid Slot, slot is booked");
            return;
        } else {
            slotDetails.addProperty("assignedId", userId);
            slotDetails.addProperty("assignedName", username);
            sendMessage(chatId, "Signed up for " + dateString + " Slot " + slot);
            return;
        }

    }

    // Step 3.2: Handle incoming updates (commands)
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();  // Get the message text
            String messageText = message.getText();
            long chatId = update.getMessage().getChatId();  // Get the chat ID
            Long userId = message.getFrom().getId();
            String username = message.getFrom().getUserName();

            if (messageText.startsWith("/setworkdate")) {
                handleSetWork(update);
            } else if (messageText.startsWith("/signup ")) {
                handleSignUp(update);
            }
             //else if (messageText.equals("/monthly_summary")) {
            //     // Step 5.1: Display a summary of work dates and number of employees signed up
            //     StringBuilder summary = new StringBuilder("Monthly Summary:\n");
            //     if (workingDates.isEmpty()) {
            //         summary.append("No work dates set yet.");
            //     } else {
            //         for (Map.Entry<String, Integer> entry : workingDates.entrySet()) {
            //             summary.append("Date: ").append(entry.getKey())
            //                    .append(", Employees: ").append(entry.getValue()).append("\n");
            //         }
            //     }
            //     sendMessage(chatId, summary.toString());
            // }
        }
        saveJsonData("workDataTest.json", workDataJson); 
    }
    private Date parseDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
        try {
            // Parse the string into a Date object
            Date date = dateFormat.parse(dateString);
            return date;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    // Method to send a message to the user
    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);  // Sending the message to the user
        } catch (TelegramApiException e) {
            e.printStackTrace();  // Handle any exceptions during message sending
        }
    }
}
