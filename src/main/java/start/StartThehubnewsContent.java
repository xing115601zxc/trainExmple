package start;

import entity.ThehubnewsPage;
import mappers.ThehubnewsMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;
import utils.SqlSessionUtil;
import utils.ThehubnewsUtil;

import java.util.ArrayList;
import java.util.Scanner;

public class StartThehubnewsContent {
    private ThehubnewsUtil util = new ThehubnewsUtil();
    private final static Logger logger = Logger.getLogger(StartThehubnewsLink.class);
    private final SqlSession sqlSession = SqlSessionUtil.getSqlSession();
    private final ThehubnewsMapper thehubnewsMapper = sqlSession.getMapper(ThehubnewsMapper.class);
    /**
     *
     * @param datetime 讀取 datetime 以後的新連結
     * @return 返回該連結 ThehubnewsPage 物件
     */
    ArrayList<ThehubnewsPage> getThehubnewsLinks(String datetime) {
        return thehubnewsMapper.getPagelink(datetime);
    }

    /**
     *
     * @param page 將該 ThehubnewsPage 物件,存入 pagecontent
     */
    void insertThehubnewsContent(ThehubnewsPage page) {
        thehubnewsMapper.addThehubnewsContent(page);
    }

    /**
     *
     * @param page 以id判斷該物件是否存在
     * @return 存在返回 true
     */
    Boolean existThehubnewsContent(ThehubnewsPage page){
        return  thehubnewsMapper.existPagecontent(page.getId());
    }

    public static void main(String[] args) {
        System.out.println("請輸入讀取文章日期 yyyy-MM-dd");
        Scanner scanner = new Scanner(System.in);
        String datetime=scanner.next("\\d\\d\\d\\d-\\d+-\\d+");

//        pagecontent新增資料筆數
        int insertCount=0;

        StartThehubnewsContent start=new StartThehubnewsContent();
//        讀取大於時間戳的連結
        for (ThehubnewsPage page:start.getThehubnewsLinks(datetime)) {
//            將 ThehubnewsPage 連結,內容存入 pagecontent

//          若該id存在於資料庫,跳出該次
            if(start.existThehubnewsContent(page)){
                continue;
            }
            try {
//                分析內文，將物件匯入資料庫
                page=start.util.getThehubnewsContent(page.getUrl());
                start.insertThehubnewsContent(page);
//                pagecontent新增資料筆數++
                insertCount++;
            }catch (Exception e){
                logger.error(e);
            }
        }
//        logger info 資訊
        logger.info("datetime:"+datetime+"\t匯入資料庫筆數:"+insertCount);
        logger.info("end");
    }
}
