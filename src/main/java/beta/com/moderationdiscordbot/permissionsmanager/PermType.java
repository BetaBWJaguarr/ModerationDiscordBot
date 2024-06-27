package beta.com.moderationdiscordbot.permissionsmanager;

import net.dv8tion.jda.api.Permission;

/**
 * This enum represents different types of permissions available in Discord.
 * Each permission type corresponds to a specific Discord Permission.
 *
 * Dependencies:
 * - Permission: Represents a permission in the Discord API.
 *
 * Usage:
 * Use PermType to specify different types of permissions required for operations in a Discord guild.
 * Each PermType value corresponds to a Discord Permission, allowing easy and type-safe permission checks.
 */

public enum PermType {
    CREATE_INSTANT_INVITE(Permission.CREATE_INSTANT_INVITE),
    KICK_MEMBERS(Permission.KICK_MEMBERS),
    BAN_MEMBERS(Permission.BAN_MEMBERS),
    ADMINISTRATOR(Permission.ADMINISTRATOR),
    MANAGE_CHANNEL(Permission.MANAGE_CHANNEL),
    MANAGE_SERVER(Permission.MANAGE_SERVER),
    MESSAGE_ADD_REACTION(Permission.MESSAGE_ADD_REACTION),
    VIEW_AUDIT_LOGS(Permission.VIEW_AUDIT_LOGS),
    PRIORITY_SPEAKER(Permission.PRIORITY_SPEAKER),
    VIEW_CHANNEL(Permission.VIEW_CHANNEL),
    MESSAGE_SEND(Permission.MESSAGE_SEND),
    MESSAGE_TTS(Permission.MESSAGE_TTS),
    MESSAGE_MANAGE(Permission.MESSAGE_MANAGE),
    MESSAGE_EMBED_LINKS(Permission.MESSAGE_EMBED_LINKS),
    MESSAGE_ATTACH_FILES(Permission.MESSAGE_ATTACH_FILES),
    MESSAGE_HISTORY(Permission.MESSAGE_HISTORY),
    MESSAGE_MENTION_EVERYONE(Permission.MESSAGE_MENTION_EVERYONE),
    MESSAGE_EXT_EMOJI(Permission.MESSAGE_EXT_EMOJI),
    VIEW_GUILD_INSIGHTS(Permission.VIEW_GUILD_INSIGHTS),
    VOICE_CONNECT(Permission.VOICE_CONNECT),
    VOICE_SPEAK(Permission.VOICE_SPEAK),
    VOICE_MUTE_OTHERS(Permission.VOICE_MUTE_OTHERS),
    VOICE_DEAF_OTHERS(Permission.VOICE_DEAF_OTHERS),
    VOICE_MOVE_OTHERS(Permission.VOICE_MOVE_OTHERS),
    VOICE_USE_VAD(Permission.VOICE_USE_VAD),
    NICKNAME_CHANGE(Permission.NICKNAME_CHANGE),
    NICKNAME_MANAGE(Permission.NICKNAME_MANAGE),
    MANAGE_ROLES(Permission.MANAGE_ROLES),
    MANAGE_PERMISSIONS(Permission.MANAGE_PERMISSIONS),
    MANAGE_WEBHOOKS(Permission.MANAGE_WEBHOOKS);

    private final Permission permission;

    PermType(Permission permission) {
        this.permission = permission;
    }

    public Permission getPermission() {
        return this.permission;
    }
}