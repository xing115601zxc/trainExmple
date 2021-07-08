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
    private ArrayList<ThehubnewsPage> list = new ArrayList<>();
    private ThehubnewsUtil util = new ThehubnewsUtil();
    private static Logger logger = Logger.getLogger(StartThehubnewsLink.class);
    private SqlSession sqlSession = SqlSessionUtil.getSqlSession();
    private ThehubnewsMapper thehubnewsMapper = sqlSession.getMapper(ThehubnewsMapper.class);

    /**
     *
     * @param limit id 降冪排列,使用 limit 讀取 limit 數量的新連結
     * @return 返回該連結 ThehubnewsPage 物件
     */
    ArrayList<ThehubnewsPage> getThehubnewsLinks(int limit) {
        return thehubnewsMapper.getPagelink(limit);
    }

    /**
     *
     * @param page 將該 ThehubnewsPage 物件,存入 pagecontent
     */
    void insertThehubnewsContent(ThehubnewsPage page) {
        page.setText(
                util.getThehubnewsContent(page.getUrl())
        );
        thehubnewsMapper.addThehubnewsContent(page);
    }

    public static void main(String[] args) {
        System.out.println("請輸入讀取文章limit");
        Scanner scanner = new Scanner(System.in);
        int limit=scanner.nextInt();

//        pagecontent新增資料筆數
        int insertCount=0;

        StartThehubnewsContent start=new StartThehubnewsContent();
//        讀取 limit 數量的連結
        for (ThehubnewsPage page:start.getThehubnewsLinks(limit)) {
//            將 ThehubnewsPage 連結,內容存入 pagecontent,使用catch避免資料重複而中斷程式
            try {
                start.insertThehubnewsContent(page);
//                pagecontent新增資料筆數++
                insertCount++;
            }catch (Exception e){
                logger.error(e);
            }
        }
//        logger info 資訊
        logger.info("limit:"+limit+"\t匯入資料庫筆數:"+insertCount);
        logger.info("end");
    }
}
