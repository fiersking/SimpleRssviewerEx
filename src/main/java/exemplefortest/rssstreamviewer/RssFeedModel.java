package exemplefortest.rssstreamviewer;

/**
 * Created by MoH on 06/01/2018.
 */

public class RssFeedModel {

    public String title;
    public String date;
    public String desc;
    public String link;
    public String Img;



    public RssFeedModel(String title, String date, String desc, String link,String Img) {
        this.title = title;
        this.date = date;
        this.desc= desc;
        this.link=link;
        this.Img=Img;
    }

    public String getTitle() {
        return title;
    }

    public String getdate() {
        return date;
    }

    public String getdesc() {
        return desc;
    }
    public String getlink() {
        return link;
    }

    public String getImg() {
        return Img;
    }

}