package com.siat.cn.dai.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class IO {
    public static List<String> readFileByLine(String strFile){
        try {
            File file = new File(strFile);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String strLine = null;
            List<String> res = new ArrayList<>();
            while(null != (strLine = bufferedReader.readLine())){
                res.add(strLine);
            }
            bufferedReader.close();
            return res;
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void writeFile(List<String> content,String strFile){
        try {
            File file = new File(strFile);
            if(!file.exists()){
                file.createNewFile();
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            for(String line: content) {
                    bufferedWriter.write(line+"\n");
            }
            bufferedWriter.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }


}
