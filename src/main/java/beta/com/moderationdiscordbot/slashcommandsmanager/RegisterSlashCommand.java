package beta.com.moderationdiscordbot.slashcommandsmanager;

import beta.com.moderationdiscordbot.startup.Information;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

/**
 * This class manages the registration of slash commands for a Discord bot.
 * It utilizes JDA (Java Discord API) to create or update slash commands with specified names,
 * descriptions, options, and subcommands. Upon successful registration, it increments the count
 * of registered commands in the provided Information instance.
 *
 * Dependencies:
 * - JDA: Provides methods for interacting with the Discord API to manage slash commands.
 * - Information: Tracks bot-related statistics, such as the number of registered commands.
 *
 * Usage:
 * Initialize RegisterSlashCommand with a JDA instance and an Information instance to facilitate
 * the registration of slash commands. Use the register() method to specify the command's name,
 * description, and any additional options or subcommands. Commands are created or updated
 * asynchronously via the Discord API, and the Information instance is updated to reflect the
 * addition of each registered command.
 *
 * Example:
 * <pre>{@code
 * // Initialize JDA and Information instances
 * JDA jda = ...; // Initialize JDA instance
 * Information information = ...; // Initialize Information instance
 *
 * // Create RegisterSlashCommand instance
 * RegisterSlashCommand commandRegistrar = new RegisterSlashCommand(jda, information);
 *
 * // Define slash command details
 * String commandName = "example";
 * String commandDescription = "Example command description";
 * OptionData option = new OptionData(OptionType.STRING, "example_option", "Example option description").setRequired(true);
 *
 * // Register the slash command
 * commandRegistrar.register(commandName, commandDescription, option);
 * }</pre>
 */


public class RegisterSlashCommand {
    private JDA jda;
    private Information information;

    public RegisterSlashCommand(JDA jda, Information information) {
        this.jda = jda;
        this.information = information;
    }

    public RegisterSlashCommand register(String commandName, String commandDescription, Object... optionsAndSubcommands) {
        CommandCreateAction commandAction = jda.upsertCommand(commandName, commandDescription);

        for (Object obj : optionsAndSubcommands) {
            if (obj instanceof OptionData) {
                commandAction = commandAction.addOptions((OptionData) obj);
            } else if (obj instanceof SubcommandData) {
                commandAction = commandAction.addSubcommands((SubcommandData) obj);
            }
        }

        commandAction.queue();
        information.incrementCommands();
        return this;
    }
}