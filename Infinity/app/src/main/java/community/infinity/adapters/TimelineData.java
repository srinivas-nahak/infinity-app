package community.infinity.adapters;

/**
 * Created by Srinu on 01-03-2018.
 */

public class TimelineData {
    private String username,time,img_link,caption,private_post_stat,comment_disabled,
            share_disabled,download_disabled,likes_counter,comments_counter,short_book_content,society_name,owner_fullname,owner_profPic;

    public TimelineData(String username,String owner_fullname,String owner_profPic, String time, String img_link, String caption,String private_post_stat,
                        String comment_disabled,String share_disabled,String download_disabled,String likes_counter,
                        String comments_counter,String short_book_content,String society_name) {
        this.username = username;
        this.time = time;
        this.img_link = img_link;
        this.caption = caption;
        this.private_post_stat=private_post_stat;
        this.comment_disabled=comment_disabled;
        this.share_disabled=share_disabled;
        this.download_disabled=download_disabled;
        this.likes_counter=likes_counter;
        this.comments_counter=comments_counter;
        this.short_book_content=short_book_content;
        this.society_name=society_name;
        this.owner_fullname=owner_fullname;
        this.owner_profPic=owner_profPic;
    }

    public String getOwner_profPic() {
        return this.owner_profPic;
    }

    public String getOwner_fullname() {
        return this.owner_fullname;
    }

    public String getShort_book_content() {
        return this.short_book_content;
    }

    public String getSociety_name() {
        return this.society_name;
    }

    public String getDownload_disabled() {
        return this.download_disabled;
    }

    public void setDownload_disabled(String download_disabled) {
        this.download_disabled = download_disabled;
    }

    public String getLikes_counter() {
        return this.likes_counter;
    }

    public String getComments_counter() {
        return this.comments_counter;
    }

    public String getComment_disabled() {
        return this.comment_disabled;
    }

    public String getShare_disabled() {
        return this.share_disabled;
    }

    public void setPrivate_post_stat(String private_post_stat) {
        this.private_post_stat = private_post_stat;
    }

    public String getPrivate_post_stat() {
        return this.private_post_stat;
    }

    public String getUsername() {
        return username;
    }

    public String getTime() {
        return this.time;
    }

    public String getImg_link() {
        return this.img_link;
    }

    public String getCaption() {
        return this.caption;
    }
}
