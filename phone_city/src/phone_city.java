import java.io.*;
import java.util.Properties;
import java.util.Set;

/**
 * Created by michael on 13-12-6.
 */
public class phone_city {

    public static void main(String[] args) throws Exception {
        if (args != null) {
            for (String str : args) {
                System.out.println(str);
            }

            File file = new File(args[0]);
            if (file.exists()) {
                System.out.println("find file for parse >>>>");
                Properties p = new SortProperties();
                FileInputStream is = new FileInputStream(file);
                InputStreamReader r = new InputStreamReader(is, "utf-8");
                p.load(r);
                int count = 1;
                Set<String> list = p.stringPropertyNames();
                for (String number : list) {
                    System.out.println("line:" + (count++) + "  " + number + "=" + p.getProperty(number).trim());
                }
                FileOutputStream out = new FileOutputStream(new File("phone_city_map.txt"));
                OutputStreamWriter w = new OutputStreamWriter(out, "utf-8");
                p.store(w, null);

//                //save test
//                Properties p1 = new Properties();
//                p1.put("data1", "北京");
//                p1.put("data2", "北京-天安门");
//                FileOutputStream out = new FileOutputStream(new File("testsave.txt"));
//                OutputStreamWriter w = new OutputStreamWriter(out, "utf-8");
//                p1.store(w, null);
//                FileOutputStream out1 = new FileOutputStream(new File("testsave.xml"));
//                p1.storeToXML(out1, null, "utf-8");
            } else {
                System.out.println("can't find file >>>>");
            }
        }
    }

}
