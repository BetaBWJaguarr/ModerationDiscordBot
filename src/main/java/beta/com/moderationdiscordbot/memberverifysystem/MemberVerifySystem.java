package beta.com.moderationdiscordbot.memberverifysystem;

import java.util.UUID;

public class MemberVerifySystem {
    private UUID id;
    private String username;
    private Integer level;
    private Status status;

    public MemberVerifySystem(UUID id, String username, Integer level, Status status) {
        this.id = id;
        this.username = username;
        this.level = level;
        this.status = status;
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
