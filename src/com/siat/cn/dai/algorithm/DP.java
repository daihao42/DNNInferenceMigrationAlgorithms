package com.siat.cn.dai.algorithm;

import com.siat.cn.dai.base.Requests.Request;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DP {

    private BigDecimal lambda;
    private BigDecimal beta;

    private List<BigDecimal> costBoundB;
    private List<BigDecimal> semiOptD = new ArrayList<>();
    private List<BigDecimal> semiOptT = new ArrayList<>();
    private List<BigDecimal> semiOptE = new ArrayList<>();
    private List<BigDecimal> optC = new ArrayList<>();
    private List<Request> request_list;
    private BigDecimal minCostUnit;
    private String minCostServerID;

    public DP(BigDecimal lambda, BigDecimal beta, List<Request> request_list){
        this.lambda = lambda;
        this.beta = beta;
        this.request_list = request_list;
        this.costBoundB = this.calc_CostBoundB(request_list);
        findMinCostUnit();
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

    public BigDecimal getLambda() {
        return lambda;
    }

    public BigDecimal getBeta() {
        return beta;
    }

    public List<BigDecimal> getCostBoundB() {
        return costBoundB;
    }

    public List<BigDecimal> getSemiOptD() {
        return semiOptD;
    }

    public List<BigDecimal> getOptC() {
        return optC;
    }

    public void findMinCostUnit(){
        BigDecimal minCostUnit = new BigDecimal(Double.MAX_VALUE);
        String minCostServerId = null;
        for(Request r:this.request_list){
            if (r.getServer().getCostUnit().compareTo(minCostUnit) < 0){
                minCostUnit = r.getServer().getCostUnit();
                minCostServerId = r.getServerid();
            }
        }
        this.minCostServerID = minCostServerId;
        this.minCostUnit = minCostUnit;
    }

    /**
     * lower bound B for each request
     * @param requests
     * @return
     */
    private List<BigDecimal> calc_CostBoundB(List<Request> requests){
        List<BigDecimal> res = new ArrayList<>();
        BigDecimal sum = new BigDecimal(0);
        res.add(sum);
        Map<String, Request> lastReq = new HashMap<String, Request>();
        for(Request i : requests){
            // refer to feedset and prior
            for(Map.Entry<String,Request>entry:lastReq.entrySet()){
                    if(entry.getKey() == i.getServerid()){
                        i.setPriorRequest(entry.getValue());
                    }else{
                        i.addFeedSet(entry.getValue());
                    }
            }

            // add caching cost or transferring cost
            if(lastReq.get(i.getServerid()) == null){
                sum = sum.add(this.TransferringCost().min(UploadingCost()));
                res.add(sum);
            }
            else {
                BigDecimal caching_cost = CachingCost(i.getServer().cost_unit,i.getTime().subtract(lastReq.get(i.getServerid()).getTime()));
                sum = sum.add(caching_cost.compareTo(TransferringCost().min(UploadingCost())) < 0 ? caching_cost:TransferringCost().min(UploadingCost()));
                res.add(sum);
            }
            lastReq.put(i.getServerid(),i);
        }
        return res;
    }

    /**
     * find cover index set in Def.9
     * @param pi_j feed set item "j"
     * @param pi_i solutiong request "i"
     * @return
     */
    private List<Request> getCoverSet(Request pi_j,Request pi_i){
        List<Request> res = new ArrayList<>();
        for(Request i:pi_i.getFeedSet()){
            if(i.getPriorRequest() != null){
                if((i.getTime().compareTo(pi_j.getTime()) >= 0) & (i.getPriorRequest().getTime().compareTo(pi_j.getTime()) < 0 )){
                    res.add(i);
                }
            }
        }
        return res;
    }

    public BigDecimal calc_SemiOptD(Request now){
        if((now.getTIndex() == 0) || (now.getPriorRequest() == null)){
            this.semiOptD.add(new BigDecimal(Double.MAX_VALUE));
            return new BigDecimal(Double.MAX_VALUE);
        }
        List<BigDecimal> calcs = new ArrayList<>();
        // C(p(i)) + mu * delta_t(i,p(i)) + B(p(i),i-1)
        calcs.add(this.optC.get(now.getPriorRequest().getTIndex())
                  .add(this.CachingCost(now.getServer().getCostUnit(), now.getTime().subtract(now.getPriorRequest().getTime())))
                  .add(this.costBoundB.get(now.getTIndex() - 1).subtract(this.costBoundB.get(now.getPriorRequest().getTIndex())))
                );
        List<Request> pi_list = this.getCoverSet(now.getPriorRequest(), now);
        for(Request j : pi_list){
            calcs.add(this.semiOptD.get(j.getTIndex())
                        .add(this.CachingCost(now.getServer().getCostUnit(), now.getTime().subtract(now.getPriorRequest().getTime())))
                        .add(this.costBoundB.get(now.getTIndex() - 1).subtract(this.costBoundB.get(j.getTIndex())))
                    );
        }
        BigDecimal minCost = new BigDecimal(Double.MAX_VALUE);
        for(BigDecimal c:calcs){
            minCost = minCost.min(c);
        }
        this.semiOptD.add(minCost);
        return minCost;
    }

    public BigDecimal calc_SemiOptT(Request now){
        if((now.getTIndex() == 0) || (now.getFeedSet().size() == 0)){
            this.semiOptT.add(new BigDecimal(Double.MAX_VALUE));
            return new BigDecimal(Double.MAX_VALUE);
        }
        List<BigDecimal> calcs = new ArrayList<>();
        // min (lambda + C(j) + mu * delta_t + B(j,i-1) , lambda + min(D(k) + mu * delta_t + B(k,i-1)))
        for(Request j: now.getFeedSet()){
            // lambda + C(j) + min(mu_i,mu_j) * delta_t(i,j) + B(j,i-1)
            calcs.add(this.TransferringCost()
                    .add(this.optC.get(j.getTIndex()))
                    .add(this.CachingCost(now.getServer().getCostUnit().min(j.getServer().getCostUnit()),now.getTime().subtract(j.getTime())))
                    .add(this.costBoundB.get(now.getTIndex() - 1).subtract(this.costBoundB.get(j.getTIndex())))
            );
            // lambda + D(k) + mu * delta_t(i,j) + B(k,i-1)
            List<Request> pi_list = this.getCoverSet(j, now);
            for(Request k:pi_list){
                calcs.add(this.TransferringCost()
                        .add(this.semiOptD.get(k.getTIndex()))
                        .add(this.CachingCost(now.getServer().getCostUnit().min(j.getServer().getCostUnit()), now.getTime().subtract(j.getTime())))
                        .add(this.costBoundB.get(now.getTIndex() - 1).subtract(this.costBoundB.get(k.getTIndex())))
                );
            }
        }
        BigDecimal minCost = new BigDecimal(Double.MAX_VALUE);
        for(BigDecimal c:calcs){
            minCost = minCost.min(c);
        }
        this.semiOptT.add(minCost);
        return minCost;
    }

    public BigDecimal calc_SemiOptE(Request now){
        if((now.getTIndex() == 0) || (now.getFeedSet().size() == 0)){
            this.semiOptE.add(new BigDecimal(Double.MAX_VALUE));
            return new BigDecimal(Double.MAX_VALUE);
        }
        List<BigDecimal> calcs = new ArrayList<>();
        List<Request> iter = new ArrayList<>();
        iter.addAll(now.getFeedSet());
        if (now.getPriorRequest() != null) {
            iter.add(now.getPriorRequest());
        }
        for(Request j: iter){
            if((!j.getServerid().equals(this.minCostServerID)) && (!now.getServerid().equals(this.minCostServerID))) {
                // min(D(j),T(j)) + 2lambda + u_min * delta_t(j,i) + B(j,i-1)
                calcs.add(this.semiOptD.get(j.getTIndex()).min(this.semiOptT.get(j.getTIndex()))
                        .add(this.TransferringCost().multiply(new BigDecimal(2)))
                        .add(this.CachingCost(this.minCostUnit, now.getTime().subtract(j.getTime())))
                        .add(this.costBoundB.get(now.getTIndex() - 1).subtract(this.costBoundB.get(j.getTIndex())))
                );
                // E(j) + lambda + u_min * delta_t(j,i) + B(j,i-1)
                calcs.add(this.semiOptE.get(j.getTIndex())
                        .add(this.TransferringCost())
                        .add(this.CachingCost(this.minCostUnit, now.getTime().subtract(j.getTime())))
                        .add(this.costBoundB.get(now.getTIndex() - 1).subtract(this.costBoundB.get(j.getTIndex())))
                );
            } else if ((!now.getServerid().equals(j.getServerid())) && (j.getServerid().equals(this.minCostServerID))){
                // min(D(j),T(j),E(j)) + lambda + u_min * delta_t(j,i) + B(j,i-1)
                calcs.add(this.semiOptD.get(j.getTIndex()).min(this.semiOptT.get(j.getTIndex())).min(this.semiOptE.get(j.getTIndex()))
                        .add(this.TransferringCost())
                        .add(this.CachingCost(this.minCostUnit, now.getTime().subtract(j.getTime())))
                        .add(this.costBoundB.get(now.getTIndex() - 1).subtract(this.costBoundB.get(j.getTIndex())))
                );
            } else if ((!now.getServerid().equals(j.getServerid())) && (now.getServerid().equals(this.minCostServerID))){
                // min(D(j),T(j)) + lambda + u_min * delta_t(j,i) + B(j,i-1)
                calcs.add(this.semiOptD.get(j.getTIndex()).min(this.semiOptT.get(j.getTIndex()))
                        .add(this.TransferringCost())
                        .add(this.CachingCost(this.minCostUnit, now.getTime().subtract(j.getTime())))
                        .add(this.costBoundB.get(now.getTIndex() - 1).subtract(this.costBoundB.get(j.getTIndex())))
                );
                // E(j) + u_min * delta_t(j,i) + B(j,i-1)
                calcs.add(this.semiOptE.get(j.getTIndex())
                        .add(this.CachingCost(this.minCostUnit, now.getTime().subtract(j.getTime())))
                        .add(this.costBoundB.get(now.getTIndex() - 1).subtract(this.costBoundB.get(j.getTIndex())))
                );
            } else {
                // E(j) + u_min * delta_t(j,i) + B(j,i-1)
                calcs.add(this.semiOptE.get(j.getTIndex())
                        .add(this.CachingCost(this.minCostUnit, now.getTime().subtract(j.getTime())))
                        .add(this.costBoundB.get(now.getTIndex() - 1).subtract(this.costBoundB.get(j.getTIndex())))
                );
            }
        }
        BigDecimal minCost = new BigDecimal(Double.MAX_VALUE);
        for(BigDecimal c:calcs){
            minCost = minCost.min(c);
        }
        this.semiOptE.add(minCost);
        return minCost;
    }

    public BigDecimal calc_OptC(Request now){
        if(now.getTIndex() == 0){
            this.optC.add(this.UploadingCost());
            return this.UploadingCost();
        }
        List<BigDecimal> calcs = new ArrayList<>();
        // D(i)
        calcs.add(this.semiOptD.get(now.getTIndex()));
        // T(i)
        calcs.add(this.semiOptT.get(now.getTIndex()));
        // E(i)
        calcs.add(this.semiOptE.get(now.getTIndex()));
        // C(i - 1) + beta
        calcs.add(this.optC.get(now.getTIndex() - 1).add(this.UploadingCost()));

        BigDecimal minCost = new BigDecimal(Double.MAX_VALUE);
        for(BigDecimal c:calcs){
            minCost = minCost.min(c);
        }
        this.optC.add(minCost);
        return minCost;
    }

    public BigDecimal solution(){
        for(Request i : this.request_list){
            this.calc_SemiOptD(i);
            this.calc_SemiOptT(i);
            this.calc_SemiOptE(i);
            this.calc_OptC(i);
        }
        return this.optC.get(this.optC.size() - 1);
    }
}
