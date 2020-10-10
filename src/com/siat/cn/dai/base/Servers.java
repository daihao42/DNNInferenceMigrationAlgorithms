package com.siat.cn.dai.base;

import com.siat.cn.dai.utils.IO;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Servers {
    public class Server{
        public String sid;
        // unit caching cost
        public BigDecimal cost_unit;
        public Server(String sid,String cu){
            this.sid = sid;
            this.cost_unit = new BigDecimal(cu);
        }
        public BigDecimal getCostUnit(){
            return this.cost_unit;
        }
    }

    public Map<String,Server> readServer(String serverfile){
        List<String> lines = IO.readFileByLine(serverfile);
        Map<String, Server> lr = new HashMap<>();
        for(String line : lines){
            String[] ls = line.split(",");
            lr.put(ls[0],new Servers.Server(ls[0],ls[1]));
        }
        return lr;
    }
}
