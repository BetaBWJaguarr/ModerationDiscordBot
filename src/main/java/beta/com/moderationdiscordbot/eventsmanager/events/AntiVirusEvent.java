package beta.com.moderationdiscordbot.eventsmanager.events;

import beta.com.moderationdiscordbot.antivirus.AntiVirusManager;
import beta.com.moderationdiscordbot.databasemanager.ServerSettings.ServerSettings;
import beta.com.moderationdiscordbot.langmanager.LanguageManager;
import beta.com.moderationdiscordbot.slashcommandsmanager.commands.moderationcommands.AntiVirusCommand;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AntiVirusEvent extends ListenerAdapter {

    private final AntiVirusManager antiVirusManager;
    private final LanguageManager languageManager;
    private final AntiVirusCommand antiVirusCommand;
    private final ServerSettings serverSettings;

    public AntiVirusEvent(AntiVirusCommand antiVirusCommand,LanguageManager languageManager, ServerSettings serverSettings) {
        this.antiVirusCommand = antiVirusCommand;
        this.antiVirusManager = new AntiVirusManager();
        this.languageManager = languageManager;
        this.serverSettings = serverSettings;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild()) {
            return;
        }

        String serverId = event.getGuild().getId();

        if (!antiVirusCommand.isAntiVirusEnabled(serverId)) {
            return;
        }

        event.getMessage().getAttachments().forEach(attachment -> {
            try {
                File tempFile = downloadAttachment(attachment);
                if (isVirus(tempFile)) {
                    event.getMessage().delete().queue(deletedMessage -> {
                        String message = languageManager.getMessage("events.virus.detected", serverSettings.getLanguage(serverId));
                        event.getChannel().sendMessage(message).queue();
                    }, error -> {
                        String message = languageManager.getMessage("events.virus.error", serverSettings.getLanguage(serverId));
                        event.getChannel().sendMessage(message).queue();
                    });
                }
                tempFile.delete();
            } catch (IOException e) {
                System.err.println("Error checking attachment: " + e.getMessage());
            }
        });
    }

    private File downloadAttachment(Attachment attachment) throws IOException {
        File tempFile = File.createTempFile("attachment-", ".tmp");
        tempFile.deleteOnExit();
        try (InputStream in = new URL(attachment.getUrl()).openStream()) {
            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile;
    }

    private boolean isVirus(File file) {
        return antiVirusManager.isFileInfected(file);
    }
}
