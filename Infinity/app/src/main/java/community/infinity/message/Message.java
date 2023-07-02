package community.infinity.message;


/**
 * Created by Srinu on 17-12-2017.
 */

public class Message {
    private String message,who,username,time,img_link,msg_post_owner_username,
            msg_post_owner_time,msg_type, msg_post_img_link,msg_post_type,msg_post_multi;

    public Message(String message,String who,String username,String time,String img_link,
                   String msg_post_owner_username,String msg_post_owner_time,
                   String msg_post_img_link,String msg_post_type,String msg_post_multi,String msg_type) {
        this.message = message;
        this.who=who;
        this.username=username;
        this.time=time;
        this.img_link=img_link;
        this.msg_post_owner_username=msg_post_owner_username;
        this.msg_post_owner_time=msg_post_owner_time;
        this.msg_post_img_link=msg_post_img_link;
        this.msg_post_type=msg_post_type;
        this.msg_post_multi=msg_post_multi;
        this.msg_type=msg_type;
    }

    public String getMsg_post_owner_username() {
        return msg_post_owner_username;
    }

    public String getMsg_post_img_link() {
        return msg_post_img_link;
    }

    public String getMsg_post_type() {
        return msg_post_type;
    }

    public String getMsg_post_multi() {
        return msg_post_multi;
    }

    public String getMsg_post_owner_time() {
        return msg_post_owner_time;
    }

    public String getMsgType() {
        return msg_type;
    }

    public String getImgLink() {
        return img_link;
    }

    public String getMessage() {
        return message;
    }

    public String getWho() {
        return who;
    }

    public String getUsername() {
        return username;
    }
    public String getTime() {
        return time;
    }
}
