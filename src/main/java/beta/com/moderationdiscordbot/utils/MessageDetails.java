package beta.com.moderationdiscordbot.utils;

public class MessageDetails {
    private int count;
    private long time;

    public MessageDetails(int count, long time) {
        this.count = count;
        this.time = time;
    }

    public int getCount() {
        return count;
    }

    public void incrementCount() {
        this.count++;
    }

    public void resetCount() {
        this.count = 0;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}