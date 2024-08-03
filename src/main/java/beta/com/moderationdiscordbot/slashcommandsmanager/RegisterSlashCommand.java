package beta.com.moderationdiscordbot.slashcommandsmanager;

import beta.com.moderationdiscordbot.startup.Information;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
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

    public void registerCommands() {
        Object[][] commands = {
                {"ping", "A ping command"},
                {"mute", "Mute a user from the server",
                        new OptionData(OptionType.STRING, "username", "The username (mentionable) of the user to mute", true),
                        new OptionData(OptionType.STRING, "duration", "The mute duration (e.g., 7d, 12h)", false),
                        new OptionData(OptionType.STRING, "reason", "The reason for muting", false)},
                {"setlanguage", "Set the language of the bot",
                        new OptionData(OptionType.STRING, "language", "The language to set", true)},
                {"antispam", "AntiSpam Command",
                        new SubcommandData("messagelimit", "Set the anti-spam message limit")
                                .addOption(OptionType.INTEGER, "value", "The new message limit", true),
                        new SubcommandData("timelimit", "Set the anti-spam time limit")
                                .addOption(OptionType.INTEGER, "value", "The new time limit", true),
                        new SubcommandData("enable", "Set the anti-spam enable or false"),
                        new SubcommandData("punishment-type", "Set the punishment type for anti-spam")
                                .addOption(OptionType.STRING, "type", "The punishment type (warn/mute)", true)},
                {"ban", "Ban a user from the server",
                        new OptionData(OptionType.STRING, "username", "The username (mentionable) of the user to ban", true),
                        new OptionData(OptionType.STRING, "duration", "The ban duration (e.g., 7d, 12h)", false),
                        new OptionData(OptionType.STRING, "reason", "The reason for banning", false),
                        new OptionData(OptionType.INTEGER, "delete_history_message_duration", "The duration of deleting the message history", false)},
                {"modlog", "Set the modlog channel",
                        new OptionData(OptionType.CHANNEL, "channel", "The channel to set as the modlog channel", true)},
                {"antivirus", "AntiVirus Command"},
                {"unban", "Unban a user from the server",
                        new OptionData(OptionType.STRING, "username", "The username (mentionable) of the user to unban", true),
                        new OptionData(OptionType.STRING, "reason", "the reason for unbanning", false)},
                {"unmute", "Unmute a user from the server",
                        new OptionData(OptionType.STRING, "username", "The username (mentionable) of the user to unmute", true),
                        new OptionData(OptionType.STRING, "reason", "The reason for unmuting", false)},
                {"clear", "Clear the files",
                        new SubcommandData("files", "Clear the files")
                                .addOption(OptionType.INTEGER, "amount", "The amount of files to clear", true)
                                .addOption(OptionType.CHANNEL, "channel", "The channel to clear the files", true),
                        new SubcommandData("all", "Clear all the messages")
                                .addOption(OptionType.INTEGER, "amount", "The amount of messages to clear", true)
                                .addOption(OptionType.CHANNEL, "channel", "The channel to clear all the messages", true),
                        new SubcommandData("bots", "Clear all the bots messages")
                                .addOption(OptionType.INTEGER, "amount", "The amount of messages to clear", true)
                                .addOption(OptionType.CHANNEL, "channel", "The channel to clear all the bots messages", true),
                        new SubcommandData("content", "Clear the content")
                                .addOption(OptionType.STRING, "content", "The content to clear", true)
                                .addOption(OptionType.INTEGER, "amount", "The amount of messages to clear", true)
                                .addOption(OptionType.CHANNEL, "channel", "The channel to clear the content", true),
                        new SubcommandData("embeds", "Clear all the embeds")
                                .addOption(OptionType.INTEGER, "amount", "The amount of messages to clear", true)
                                .addOption(OptionType.CHANNEL, "channel", "The channel to clear all the embeds", true)},
                {"warn", "Warn a user from the server",
                        new OptionData(OptionType.STRING, "username", "The username (mentionable) of the user to warn", true),
                        new OptionData(OptionType.STRING, "reason", "The reason for warning", false)},
                {"unwarn", "Unwarn a user from the server",
                        new OptionData(OptionType.STRING, "username", "The username (mentionable) of the user to unwarn", true),
                        new OptionData(OptionType.STRING, "warningid", "The warning id to unwarn", true),
                        new OptionData(OptionType.STRING, "reason", "The reason for unwarning", false)},
                {"clearlogchannel", "Clear the log channel",
                        new OptionData(OptionType.CHANNEL, "channel", "The channel to clear the log channel", true)},
                {"kick", "Kick a user from the server",
                        new OptionData(OptionType.STRING, "username", "The username (mentionable) of the user to kick", true),
                        new OptionData(OptionType.STRING, "reason", "The reason for kicking", false)},
                {"warnlist", "List all the warns of a user",
                        new OptionData(OptionType.STRING, "username", "The username (mentionable) of the user to list the warns", true)},
                {"antiswear", "AntiSwear Command",
                        new SubcommandData("enable", "Enable the anti-swear system"),
                        new SubcommandData("disable", "Disable the anti-swear system"),
                        new SubcommandData("add", "Add a word to the anti-swear filter")
                                .addOption(OptionType.STRING, "word", "The word to add", true),
                        new SubcommandData("remove", "Add a word to the anti-swear filter")
                                .addOption(OptionType.STRING, "word", "The word to remove", true),
                        new SubcommandData("punishment-type", "Set the punishment type for anti-swear")
                                .addOption(OptionType.STRING, "type", "The punishment type (warn/mute)", true),
                        new SubcommandData("list", "List all the words in the anti-swear filter")},
                {"autopunish", "AutoPunish Command",
                        new SubcommandData("enable", "Enable the auto-punish system"),
                        new SubcommandData("disable", "Disable the auto-punish system")},
                {"channels", "Ban or unban a user from a specific channel",
                        new SubcommandData("ban", "Ban a user from a specific channel")
                                .addOption(OptionType.STRING, "username", "The username (mentionable) of the user to ban", true)
                                .addOption(OptionType.CHANNEL, "channel", "The channel to ban the user", true)
                                .addOption(OptionType.STRING, "duration", "The ban duration (e.g., 7d, 12h)", false)
                                .addOption(OptionType.STRING, "reason", "The reason for banning", false),
                        new SubcommandData("unban", "Unban a user from a specific channel")
                                .addOption(OptionType.STRING, "username", "The username (mentionable) of the user to unban", true)
                                .addOption(OptionType.CHANNEL, "channel", "The channel to unban the user", true)
                                .addOption(OptionType.STRING, "reason", "The reason for unbanning", false)},
                {"autorole", "AutoRole Command",
                        new OptionData(OptionType.ROLE, "role", "The role to set as the autorole", true)},
                {"setwarnkick", "Set the warn kick times",
                        new OptionData(OptionType.INTEGER, "times", "The new warn kick times", true)},
                {"voiceaction", "VoiceAction Command",
                        new SubcommandData("enable", "Enable the voice action system"),
                        new SubcommandData("disable", "Disable the voice action system"),
                        new SubcommandData("request", "Request a voice action"),
                        new SubcommandData("end","End a voice action")},
                {"verify", "Verify a user",
                        new SubcommandData("user", "Toggle the verification system")
                                .addOption(OptionType.STRING, "username", "The user to verify", true)
                                .addOption(OptionType.STRING, "level", "The level of the user", true)
                                .addOption(OptionType.STRING, "status", "The status of the user", true),
                        new SubcommandData("setrole", "Set the role to verify")
                                .addOption(OptionType.ROLE, "role", "The role to set", true),
                        new SubcommandData("toggle", "Toggle the verification system")
                                .addOption(OptionType.STRING, "action", "The action to perform (enable/disable)", true)},
                {"punishment-search", "Search for punishments",
                        new OptionData(OptionType.STRING, "type", "The type of punishment to search for (mute/ban/warn)", true),
                        new OptionData(OptionType.STRING, "predicate", "The search predicate (e.g., username, reason)", true),
                        new OptionData(OptionType.STRING, "value", "The value to search for", true)},
        };

        for (Object[] command : commands) {
            String name = (String) command[0];
            String description = (String) command[1];
            Object[] optionsAndSubcommands = new Object[command.length - 2];
            System.arraycopy(command, 2, optionsAndSubcommands, 0, optionsAndSubcommands.length);
            register(name, description, optionsAndSubcommands);
        }
    }
}