package com.siat.cn.dai.algorithm;

import com.siat.cn.dai.base.Requests.Request;
import com.siat.cn.dai.base.Servers;

import java.math.BigDecimal;
import java.util.*;

public class Greedy {

    private BigDecimal lambda;
    private BigDecimal beta;

    private List<Request> request_list;

    private Map<String,BigDecimal> recentRequestTime;

    private Map<String, Servers.Server> serverMap;

    public Greedy(BigDecimal lambda, BigDecimal beta, List<Request> request_list){
        this.lambda = lambda;
        this.beta = beta;
        this.request_list = request_list;
        initRecentRequestTime();
    }

    public void initRecentRequestTime(){
        Map<String,BigDecimal> recentRequestTime = new HashMap<>();
        Map<String, Servers.Server> serverMap = new HashMap<>();
        for (Request i:this.request_list){
            recentRequestTime.put(i.getServerid(),new BigDecimal(0));
            serverMap.put(i.getServerid(),i.getServer());
        }
        this.serverMap = serverMap;
        this.recentRequestTime = recentRequestTime;
    }

    public void updateRecentRequestTime(Request i, String serverId){
        this.recentRequestTime.put(serverId,i.getTime());
    }

    public BigDecimal CachingCost(BigDecimal mu,BigDecimal delta_t){
        return mu.multiply(delta_t);
    }

    public BigDecimal TransferringCost(){
        return lambda;
    }

    public BigDecimal UploadingCost(){
        return beta;
    }

    public BigDecimal greedyForEachRequest(Request now){
        if (now.getTIndex() == 0){
            return UploadingCost();
        }
        HashMap<BigDecimal,String> calcs = new HashMap<>();
        for (Map.Entry<String,BigDecimal> entry: this.recentRequestTime.entrySet()){
            // i == j , u_i * delta_t(i,j)
            if (entry.getKey().equals(now.getServerid())){
                calcs.put(CachingCost(now.getServer().getCostUnit(),now.getTime().subtract(entry.getValue())),now.getServerid());
            }else{
                // u_i <= u_j ; lambda + u_i * delta_t ; update s_i
                if (now.getServer().getCostUnit().compareTo(this.serverMap.get(entry.getKey()).getCostUnit()) <= 0){
                    calcs.put(
                      TransferringCost().add(
                              CachingCost(now.getServer().getCostUnit(),now.getTime().subtract(entry.getValue()))
                      ),now.getServerid()
                    );
                }else{
                    // u_i > u_j ; lambda + u_j * delta_t ; update s_j
                    calcs.put(
                            TransferringCost().add(
                                    CachingCost(this.serverMap.get(entry.getKey()).getCostUnit(),now.getTime().subtract(entry.getValue()))
                            ),entry.getKey()
                    );
                }
            }
        }
        BigDecimal minCost = new BigDecimal(Double.MAX_VALUE);
        for(BigDecimal c:calcs.keySet()){
            minCost = minCost.min(c);
        }
        if (minCost.compareTo(UploadingCost()) <= 0) {
            // update the recent request time
            // occur on min transferring server and request server
            updateRecentRequestTime(now, calcs.get(minCost));
            updateRecentRequestTime(now, now.getServerid());
            return minCost;
        }else {
            // update the recent request time on request server
            updateRecentRequestTime(now, now.getServerid());
            return UploadingCost();
        }
    }

    public BigDecimal solution(){
        List<BigDecimal> test = new ArrayList<>();
        BigDecimal totalCost = new BigDecimal(0);
        for(Request i : this.request_list){
            totalCost = totalCost.add(greedyForEachRequest(i));
            test.add(totalCost);
        }
        return totalCost;
    }
}
