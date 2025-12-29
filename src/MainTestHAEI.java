/**
 * @author Xuanwei Zhang
 * @date 2025/12/026
 * @description: MAEI Algorithm
 * */

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

public class MainTestHAEI {

    public static void main(String arg[]) throws IOException {

        String fileName1 = "DBUtility";
        String fileName2 = "stock";
        String input1 = fileToPath(fileName1+ ".txt");
        String input2 = fileToPath(fileName2+ ".txt");

        //threshold : minimum average-efficiency
        double min_AE = 20;

        //result set output file location
        String output = "MAEI/Result/" + fileName1 + "_" + min_AE +"output.txt";
        File ouputFile = new File(output);
        ouputFile.getParentFile().mkdirs();

        //MAEI algorithm
        AlgoHAEI haeim = new AlgoHAEI();
        haeim.runAlgorithm(input1, input2, output, min_AE);
        haeim.printStats();
    }

    /**
     * This method read a file. It is originally copied from AlgoAgrawalFim
     * @param filename the filename
     * @return the String as a list of lines
     */
    public static String fileToPath(String filename) throws UnsupportedEncodingException {
        URL url = MainTestHAEI.class.getResource(filename);
        return java.net.URLDecoder.decode(url.getPath(),"UTF-8");
    }

}