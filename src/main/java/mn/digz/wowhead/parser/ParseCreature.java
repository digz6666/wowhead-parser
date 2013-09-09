package mn.digz.wowhead.parser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author MethoD
 */
public class ParseCreature {
    
    public void parseFromUrl(String url) throws Exception {
        Connection con = Jsoup.connect(url);
        Document doc = con.get();
        /*Elements npcTab = doc.select("div#tab-npcs");
        System.out.println("npcTab: " + npcTab);
        if(npcTab != null) {
            Elements npcLinks = npcTab.select("a[href]");
            for (int i = 0; i < npcLinks.size(); i++) {
                System.out.println(npcLinks.get(i));
            }
        }*/

        String[] lines = doc.data().split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if(line.startsWith("new Listview({template: 'npc', id: 'npcs'")) {
                /*line = line.replace("new Listview(", "").replace("}", "")
                        .replace("name: LANG.tab_npcs, ", "").replace("tabs: tabsRelated, ", "")
                        .replace("note: $WH.sprintf(LANG.lvnote_filterresults, '/npcs?filter=cr=6;crs=616;crv=0'), ", "");*/
                line = line.split("data: ")[1];
                line = line.replace(");", "");
                line = line.replace("undefined", "null");

                // parse
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
                mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
                
                exportToExcel(mapper.readValue(line, List.class));
                break;
            }
        }
        //System.out.println(doc.data());
    }
    
    public void parseFromFile(String path) throws Exception {
        //InputStream fis = new FileInputStream(path);
        FileReader fr = new FileReader(path);
        BufferedReader br = new BufferedReader(fr);
        
        String line;
        while( (line=br.readLine()) != null ) {
            if(line.startsWith("new Listview({template: 'npc', id: 'npcs'")) {
                // cleanup json
                line = line.split("data: ")[1];
                line = line.replace(");", "");
                line = line.replace("undefined", "null");

                // parse
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
                mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
                
                exportToExcel(mapper.readValue(line, List.class));
                break;
            }
        }
    }
    
    private void exportToExcel(List npcInfoList) throws Exception {
        // import to excel
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("NPC");
        for (int i = 0; i < npcInfoList.size(); i++) {
            Map npcInfo = (Map) npcInfoList.get(i);
            Row row = sheet.createRow(i);
            Cell idCell = row.createCell(0);
            idCell.setCellValue(npcInfo.get("id").toString());

            Cell nameCell = row.createCell(1);
            nameCell.setCellValue(npcInfo.get("name").toString());
        }

        FileOutputStream fos = new FileOutputStream("npc_list.xlsx");
        wb.write(fos);
    }
    
    public static void main(String[] args) {
        try {
            ParseCreature parseCreature = new ParseCreature();
            //parseCreature.parseFromUrl("http://www.wowhead.com/zone=616/mount-hyjal#npcs");
            parseCreature.parseFromFile("C:/Users/MethoD/Documents/NetBeansProjects/wowhead-parser/npc_json.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
