package start;


import entity.ThehubnewsPage;
import mappers.ThehubnewsMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import utils.SqlSessionUtil;
import utils.ThehubnewsUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;


public class StartThehubnewsLink {
    private ArrayList<ThehubnewsPage> list = new ArrayList<>();
    private ThehubnewsUtil util = new ThehubnewsUtil();
    private static Logger logger = Logger.getLogger(StartThehubnewsLink.class);
    private SqlSession sqlSession = SqlSessionUtil.getSqlSession();
    private ThehubnewsMapper thehubnewsMapper = sqlSession.getMapper(ThehubnewsMapper.class);

    /**
     *
     * @param page Thehubnews ajax頁數
     * @return 返回標題list ArrayList<ThehubnewsPage>
     */
    ArrayList<ThehubnewsPage> getThehubnewsLinks(int page) {
        list =
                util.getThehubnewsDivlist(
                        util.getThehubnewsDivTags(
                                util.getThehubnewsAjax(page)));
        return list;
    }

    /**
     *
     * @param page 將 ThehubnewsPage 存入資料庫 pagelink
     */
    void insertThehubnewsLink(ThehubnewsPage page) {
        thehubnewsMapper.addThehubnewsList(page);
    }



    public static void main(String[] args) throws Exception {
        System.out.println("請輸入讀取至幾頁");
        Scanner scanner = new Scanner(System.in);
        int lodaPage = scanner.nextInt();

        System.out.println("是否限定時效3天內 Y/N");
        scanner.nextLine();
        String YorN = scanner.next();

//        3天內連結數
        int twoDaysAgoCount = 0;
//        是否限定3天內
        if (YorN.equalsIgnoreCase("Y")) {
//            取得2天前0點long
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date today = simpleDateFormat.parse(
                    simpleDateFormat.format(new Date()));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            calendar.add(Calendar.DATE, -2);
            long twoDaysAgo = calendar.getTime().getTime();

//          更改日期成Thehubnews格式
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

//            依頁數跑迴圈
            for (int i = 0; i < lodaPage; i++) {
                StartThehubnewsLink start = new StartThehubnewsLink();
                ArrayList<ThehubnewsPage> linkList = start.getThehubnewsLinks(i + 1);
                long date = 0;
                for (ThehubnewsPage page : linkList) {
//                    取得該新聞日期long
                    date = simpleDateFormat.parse(page.getDate()).getTime();
//                    若日期小於2天前,不放入資料庫,跳出該次
                    if (date < twoDaysAgo) {
                        return;
                    }
//                    若日期為3天內,3天內連結數++
                    twoDaysAgoCount++;
                       try {
//                           存入資料庫,使用catch避免重複資料中斷程式
                           start.insertThehubnewsLink(page);

                       }catch (Exception e){
                           logger.error(e);
                       }
                }
            }
        } else {
//            取得2天前0點long
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date today = simpleDateFormat.parse(
                    simpleDateFormat.format(new Date()));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            calendar.add(Calendar.DATE, -2);
            long twoDaysAgo = calendar.getTime().getTime();

//          更改日期成Thehubnews格式
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

//          依頁數跑迴圈
            for (int i = 0; i < lodaPage; i++) {
                StartThehubnewsLink start = new StartThehubnewsLink();
                ArrayList<ThehubnewsPage> linkList = start.getThehubnewsLinks(i + 1);
                long date = 0;
                for (ThehubnewsPage page : linkList) {
//                    取得該新聞日期long
                    date = simpleDateFormat.parse(page.getDate()).getTime();
//                    若新聞日期為3天內,3天內連結數++
                    if (date > twoDaysAgo) {
                        twoDaysAgoCount++;
                    }
                    try {
//                        將所有取得資料放入資料庫,使用catch避免重複資料中斷程式
                        start.insertThehubnewsLink(page);
                    }catch (Exception e){
                        logger.error(e);
                    }

                }
            }
        }
//        logger info 資訊
        logger.info("三天內連結總數:" + twoDaysAgoCount);
        logger.info("End");
    }

}
