package community.infinity.adapters;

/**
 * Created by Srinu on 17-03-2018.
 */

public class NotificationData {
    private String type,notif_sender_profPic,post_owner_username,post_owner_time,like_time,comment_time,liker_username,commenter_username,follower_username,notification_data,
    comment_owner_username,comment_owner_time,comment_liker_username,comment_like_time;

    public NotificationData(String type,String notif_sender_profPic, String post_owner_username, String post_owner_time, String like_time, String comment_time,
                            String liker_username, String commenter_username,String follower_username, String notification_data,
                            String comment_owner_username,String comment_owner_time,String comment_liker_username,String comment_like_time) {
        this.type = type;
        this.notif_sender_profPic=notif_sender_profPic;
        this.post_owner_username = post_owner_username;
        this.post_owner_time = post_owner_time;
        this.like_time = like_time;
        this.comment_time = comment_time;
        this.liker_username = liker_username;
        this.commenter_username = commenter_username;
        this.notification_data = notification_data;
        this.follower_username=follower_username;
        this.comment_owner_username=comment_owner_username;
        this.comment_owner_time=comment_owner_time;
        this.comment_liker_username=comment_liker_username;
        this.comment_like_time=comment_like_time;
    }

    public String getType() {
        return type;
    }

    public String getNotif_sender_profPic() {
        return notif_sender_profPic;
    }

    public String getPost_owner_time() {
        return this.post_owner_time;
    }

    public String getLike_time() {
        return this.like_time;
    }

    public String getComment_time() {
        return this.comment_time;
    }

    public String getComment_owner_time() {
        return comment_owner_time;
    }

    public String getComment_like_time() {
        return comment_like_time;
    }

    public String getPost_owner_username() {
        return post_owner_username;
    }

    public String getLiker_username() {
        return liker_username;
    }

    public String getCommenter_username() {
        return commenter_username;
    }

    public String getFollower_username() {
        return follower_username;
    }

    public String getComment_owner_username() {
        return comment_owner_username;
    }

    public String getComment_liker_username() {
        return comment_liker_username;
    }

    public String getNotification_data() {
        return this.notification_data;
    }
}
