package com.siat.cn.dai.base;

import com.siat.cn.dai.utils.IO;

import java.math.BigDecimal;
import java.util.*;

public class Requests {
    public class Request{
        String serverid;
        BigDecimal time;
        Servers.Server server;
        Request priorRequest = null; 
        List<Request> feedSet = new ArrayList<>();
        int tindex; //  sort all requests by time
        public Request(String time, Servers.Server server){
            this.server = server;
            this.serverid = server.sid;
            this.time = new BigDecimal(time);
        }

        public BigDecimal getTime() {
            return time;
        }

        public Servers.Server getServer(){
            return server;
        }

        public String getServerid() {
            return serverid;
        }

        public int getTIndex(){
            return this.tindex;
        }

        public void setTIndex(int tindex){
            this.tindex = tindex;
        }

        public List<Request> getFeedSet(){
            return this.feedSet;
        }

        public Request getPriorRequest(){
            return this.priorRequest;
        }

        public void setPriorRequest(Request r){
            this.priorRequest = r;
        }

        public void addFeedSet(Request r){
            this.feedSet.add(r);
        }
    }

    public List<Request> readRequests(String requestfile, Map<String, Servers.Server> serverMap){
        List<String> lines = IO.readFileByLine(requestfile);
        List<Request> lr = new ArrayList<>();
        for(String line : lines){
            String[] ls = line.split(",");
            lr.add(new Request(ls[1],serverMap.get(ls[0])));
        }
        lr.sort(new Comparator<Request>() {
            @Override
            public int compare(Requests.Request o0, Requests.Request o2) {
                return o0.getTime().compareTo(o2.getTime());
            }
        });
        int tindex = 0;
        for(Request i:lr){
            i.setTIndex(tindex);
            tindex++;
        }
        return lr;
    }

    public List<Request> readRequestsWithSample(String requestfile, Map<String, Servers.Server> serverMap,int samplesize){
        List<String> lines = IO.readFileByLine(requestfile);
        List<Request> olr = new ArrayList<>();
        for(String line : lines){
            String[] ls = line.split(",");
            olr.add(new Request(ls[1],serverMap.get(ls[0])));
        }
        List<Request> lr = sample(olr,samplesize);

        lr.sort(new Comparator<Request>() {
            @Override
            public int compare(Requests.Request o0, Requests.Request o2) {
                return o0.getTime().compareTo(o2.getTime());
            }
        });
        int tindex = 0;
        for(Request i:lr){
            i.setTIndex(tindex);
            tindex++;
        }
        return lr;
    }

    public List<Request> sample(List<Request> ls, int samplesize){
        Collections.shuffle(ls);
        return ls.subList(0,samplesize);
    }
}
