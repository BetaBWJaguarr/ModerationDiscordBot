package beta.com.moderationdiscordbot.permissionsmanager;

import net.dv8tion.jda.api.entities.Member;

public class PermissionsManager {

    public boolean hasPermission(Member member, PermType permType) {
        return member.hasPermission(permType.getPermission());
    }
}