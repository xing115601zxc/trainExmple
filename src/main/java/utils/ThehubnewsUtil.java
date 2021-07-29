package utils;


import entity.ThehubnewsPage;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThehubnewsUtil {

    /**
     *
     * @param page ajax頁數,從1開始++
     * @return 該頁ajax文章列表的div標籤
     */
    public String getThehubnewsAjax(int page) {
        try {
            String url = "https://www.thehubnews.net/archives/category/area";
            String html = Jsoup.connect(url).get().html();
            String td_atts=null,td_magic_token=null,block_type=null;
//            取得post data token
            Matcher matcher=Pattern.compile("block_tdi_71\\.atts = '([^}]+?})").matcher(html);
            if (matcher.find()) {td_atts = matcher.group(1);}
            matcher=Pattern.compile("block_tdi_71.block_type = \"([^\"]+?)\"").matcher(html);
            if (matcher.find()) {block_type = matcher.group(1);}
            matcher = Pattern.compile("var tdBlockNonce=\"([^\"]+)\";").matcher(html);
            if (matcher.find()) {td_magic_token = matcher.group(1);}

//            post data
            Map<String, String> data = new HashMap<>();
            data.put("action", "td_ajax_block");
            data.put("td_block_id", "tdi_71");
            data.put("td_atts", td_atts);
            data.put("td_current_page", page + "");
            data.put("block_type", block_type);
//            token會每日變動
            data.put("td_magic_token", td_magic_token);

//            取得ajax,解碼utf-8及html,return ajax
            String ajax = Jsoup.connect("https://www.thehubnews.net/wp-admin/admin-ajax.php")//?
                    .proxy("127.0.0.1", 8888).validateTLSCertificates(false)
                    .data(data).post().html();
            ajax= StringEscapeUtils.unescapeHtml4(StringEscapeUtils.unescapeJava(ajax));
            return ajax;
        } catch (Exception e) {

            e.printStackTrace();

        }
        return null;
    }

    /**
     *
     * @param ajax Thehubnews的ajax列表
     * @return 以標題分段的 String array
     */
    public String[] getThehubnewsDivTags(String ajax) {
//        將ajax依標題分段
        String[] divTags = ajax.split("<div class=\"\"tdb_module_loop\" td_module_wrap td-animation-stack\">");
        return divTags;
    }

    /**
     *
     * @param divTags 以標題分段的 String array
     * @return ThehubnewsPage物件的ArrayList
     */
    public ArrayList<ThehubnewsPage> getThehubnewsDivlist(String[] divTags) {
        ArrayList<ThehubnewsPage> list = new ArrayList<>();
        ThehubnewsPage page;
        for (String s : divTags) {
            page = new ThehubnewsPage();
//            解析url,num 存入ThehubnewsPage
            Matcher matcher = Pattern.compile("<h3 class=\"\"entry-title\"[\\S\\s]+?href=\"\"([\\S\\s]+?(\\d+))\"\"").matcher(s);
            if (matcher.find()) {
                page.setUrl(matcher.group(1));
                page.setId(Integer.parseInt(matcher.group(2)));
            } else continue;
//            解析title,date 存入ThehubnewsPage
            matcher = Pattern.compile("<h3 class=[\\S\\s]+?title=[^>]+?>([\\s\\S]+?)<").matcher(s);
            if (matcher.find()) page.setTitle(matcher.group(1));
            matcher = Pattern.compile("class=\"\"td-post-date\"\">[^\\]]+?>([^<]+)<").matcher(s);
            if (matcher.find()) page.setDate(matcher.group(1));

//          將ThehubnewsPage存入ArrayList
            list.add(page);
        }
        return list;
    }

    /**
     *
     * @param url Thehubnews新聞網址
     * @return 返回該頁面物件
     */
    public ThehubnewsPage getThehubnewsContent(String url) {
        ThehubnewsPage page = new ThehubnewsPage();
//        擷取url,id放入物件
        page.setUrl(url);
        page.setId(Integer.parseInt(url.replaceAll("https://www.thehubnews.net/archives/","")));
        String text=null;
        StringBuffer buffer=new StringBuffer();
        try {
//            擷取title,date放入物件
            String html = Jsoup.connect(url).get().html();
            Matcher matcher=Pattern.compile("<h1 class=\"entry-title\">([^<]+?)<").matcher(html);
            if(matcher.find())  {
                page.setTitle(matcher.group(1));
            }
            matcher=Pattern.compile("<h1[\\S\\s]+?datetime=\"([^\"]+)\"").matcher(html);
            if(matcher.find()){
                SimpleDateFormat simpleDateFormat =new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
                Date date = null;
                try {
                    date = simpleDateFormat.parse(matcher.group(1));
                    simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                page.setDate(simpleDateFormat.format(date));
            }
//            擷取content class
            matcher = Pattern.compile("(?s)<div class=\"td-post-content tagdiv-type\">([\\S\\s]+?)<div id=\"jp-relatedposts\"")
                    .matcher(html);
            if (matcher.find()){ text = matcher.toString();}

//            擷取<p>標籤內文,&nbsp;轉換空白
            matcher = Pattern.compile("(?s)<p[\\S\\s]*?>([\\S\\s]+?)</p>").matcher(text);
            while (matcher.find()){
                buffer =buffer.insert(buffer.length(),matcher.group(1)+"\n");}
            
            text =buffer.toString().replaceAll("&nbsp;"," ");

//            去除說圖文字、剩餘標籤
            text = text.replaceAll("▲.*\\(?（?圖?.*\\)?）?", "")
                    .replaceAll("(?s)<[^>]+?>", "");
//            將去除標籤的內文放入物件
            page.setText(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return page;
    }

}
