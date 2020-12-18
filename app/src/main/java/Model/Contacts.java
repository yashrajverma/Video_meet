package Model;

public class Contacts {
    String Profileimage,Username,Status,uid;

    public Contacts() {
    }

    public Contacts(String profileimage, String username, String status, String uid) {
        Profileimage = profileimage;
        Username = username;
        Status = status;
        this.uid = uid;
    }

    public String getProfileimage() {
        return Profileimage;
    }

    public void setProfileimage(String profileimage) {
        Profileimage = profileimage;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
