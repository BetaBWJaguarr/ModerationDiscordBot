package beta.com.moderationdiscordbot.permissionsmanager;

import net.dv8tion.jda.api.entities.Member;

/**
 * This class provides utility methods to check permissions of Discord members.
 * It allows verification of whether a member has a specific permission type.
 *
 * Dependencies:
 * - Member: Represents a member (user) of a Discord guild.
 *
 * Usage:
 * Initialize PermissionsManager with a JDA instance to facilitate permission checks for Discord members.
 * Use the hasPermission() method to verify if a member possesses a specific permission type defined
 * in the PermType enum.
 */

public class PermissionsManager {

    public boolean hasPermission(Member member, PermType permType) {
        return member.hasPermission(permType.getPermission());
    }
}