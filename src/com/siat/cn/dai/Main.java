package com.siat.cn.dai;

import com.siat.cn.dai.algorithm.DP;
import com.siat.cn.dai.algorithm.Greedy;
import com.siat.cn.dai.algorithm.Online;
import com.siat.cn.dai.base.Requests;
import com.siat.cn.dai.base.Servers;
import com.siat.cn.dai.algorithm.DPBAK;
import com.siat.cn.dai.utils.IO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main_t(final String[] args) {
        Requests rs = new Requests();
        Servers servers = new Servers();

        List<String> contents = new ArrayList<>();

        BigDecimal lambda;
        BigDecimal beta = new BigDecimal("0.1");

        Map<String, Servers.Server> servers_unit = servers.readServer("data/server_units/servers_unit_1_3.csv");
        while(beta.compareTo(new BigDecimal(4)) <= 0) {
        //while(beta.compareTo(new BigDecimal(0.1)) <= 0) {

            lambda = beta.divide(new BigDecimal(5));

            while((lambda.compareTo(beta.multiply(new BigDecimal(2))) <= 0 )&&(lambda.compareTo(new BigDecimal(2)) <= 0)) {

                System.out.println("start : "+lambda.toString()+" | "+beta.toString());

                // store results (costs and runtimes) into array
                List<Double> results = new ArrayList<>();
                results.add(lambda.doubleValue());
                results.add(beta.doubleValue());

                List<Requests.Request> ls = rs.readRequests("data/requests.txt", servers_unit);
                DPBAK dp_bak = new DPBAK(lambda, beta, ls);
                long start1 = System.currentTimeMillis();
                results.add(dp_bak.solution().doubleValue());
                results.add(Double.valueOf(System.currentTimeMillis() - start1));

                List<Requests.Request> ls2 = rs.readRequests("data/requests.txt", servers_unit);
                DP dp = new DP(lambda, beta, ls2);
                long start2 = System.currentTimeMillis();
                results.add(dp.solution().doubleValue());
                results.add(Double.valueOf(System.currentTimeMillis() - start2));

                List<Requests.Request> ls3 = rs.readRequests("data/requests.txt", servers_unit);
                Online online = new Online(lambda, beta, ls3, 4);
                long start3 = System.currentTimeMillis();
                results.add(online.solution().doubleValue());
                results.add(Double.valueOf(System.currentTimeMillis() - start3));

                List<Requests.Request> ls4 = rs.readRequests("data/requests.txt", servers_unit);
                Greedy greedy = new Greedy(lambda, beta, ls4);
                long start4 = System.currentTimeMillis();
                results.add(greedy.solution().doubleValue());
                results.add(Double.valueOf(System.currentTimeMillis() - start4));

                contents.add(String.format("%f,%f,%f,%f,%f,%f,%f,%f,%f,%f", results.toArray()));
                lambda = lambda.add(new BigDecimal("0.01"));
            }
            beta = beta.add(new BigDecimal("0.01"));
        }

        // cost_dp_bak(C & D), runtime_dp_bak(C & D), cost_dp(C & D & T & E), runtime_dp(C & D & T & E), cost_online, runtime_online, cost_greedy, runtime_greedy
        IO.writeFile(contents,"data/results.csv");
        System.out.println();
        //for (re)
    }



    public static void main(final String[] args) {
        Requests rs = new Requests();
        Servers servers = new Servers();

        List<String> contents = new ArrayList<>();

        BigDecimal lambda = new BigDecimal("1");
        BigDecimal beta = new BigDecimal("2");

        Map<String, Servers.Server> servers_unit = servers.readServer("data/server_units/servers_unit_1_3.csv");

        for(int samplesize = 100; samplesize <= 6000;samplesize += 100) {

            System.out.println("start : " + lambda.toString() + " | " + beta.toString());

            // store results (costs and runtimes) into array
            List<Double> results = new ArrayList<>();
            results.add(lambda.doubleValue());
            results.add(beta.doubleValue());

            List<Requests.Request> ls2 = rs.readRequestsWithSample("data/requests.txt", servers_unit,samplesize);
            DP dp = new DP(lambda, beta, ls2);
            long start2 = System.currentTimeMillis();
            results.add(dp.solution().doubleValue());
            results.add(Double.valueOf(System.currentTimeMillis() - start2));

            List<Requests.Request> ls4 = rs.readRequestsWithSample("data/requests.txt", servers_unit,samplesize);
            Greedy greedy = new Greedy(lambda, beta, ls4);
            long start4 = System.currentTimeMillis();
            results.add(greedy.solution().doubleValue());
            results.add(Double.valueOf(System.currentTimeMillis() - start4));

            results.add(samplesize+0.0);

            contents.add(String.format("%f,%f,%f,%f,%f,%f,%f", results.toArray()));
        }

        // cost_dp_bak(C & D), runtime_dp_bak(C & D), cost_dp(C & D & T & E), runtime_dp(C & D & T & E), cost_online, runtime_online, cost_greedy, runtime_greedy
        IO.writeFile(contents,"data/dp_greedy_comp_results.csv");
        System.out.println();
        //for (re)
    }

    public static String[] generateBetaArr(String clambda){
        String[] beta_arr = new String[3];
        beta_arr[0] = (Integer.valueOf(clambda) -1)+"";
        beta_arr[1] = (Integer.valueOf(clambda)*2 - 1)+"";
        beta_arr[2] = (Integer.valueOf(clambda)*2 + 1)+"";
        return beta_arr;
    }
    /**
     * test method
     * @param args
     */
    public static void main_t2(String[] args) {
        Requests rs = new Requests();
        Servers servers = new Servers();

        String[] lambda_arr = {"2","4","6"};

        List<String> contents = new ArrayList<>();

        for(String lambda_str : lambda_arr) {
            String[] beta_arr = generateBetaArr(lambda_str);
            for (String beta_str : beta_arr) {

                BigDecimal lambda = new BigDecimal(lambda_str);
                BigDecimal beta = new BigDecimal(beta_str);

                for(int low = 1;low<5;low++) {
                    for(int high = low;high < low+5;high++) {

                        Map<String, Servers.Server> servers_unit = servers.readServer(String.format("data/server_units/servers_unit_%d_%d.csv",low,high));

                        List<Double> results = new ArrayList<>();

                        results.add(lambda.doubleValue());
                        results.add(beta.doubleValue());
                        results.add(Double.valueOf(low));
                        results.add(Double.valueOf(high));

                        List<Requests.Request> ls = rs.readRequests("data/requests.txt", servers_unit);
                        DPBAK dp_bak = new DPBAK(lambda, beta, ls);
                        long start1 = System.currentTimeMillis();
                        results.add(dp_bak.solution().doubleValue());
                        results.add(Double.valueOf(System.currentTimeMillis() - start1));

                        List<Requests.Request> ls2 = rs.readRequests("data/requests.txt", servers_unit);
                        DP dp = new DP(lambda, beta, ls2);
                        long start2 = System.currentTimeMillis();
                        results.add(dp.solution().doubleValue());
                        results.add(Double.valueOf(System.currentTimeMillis() - start2));

                        List<Requests.Request> ls3 = rs.readRequests("data/requests.txt", servers_unit);
                        Online online = new Online(lambda, beta, ls3, 4);
                        long start3 = System.currentTimeMillis();
                        results.add(online.solution().doubleValue());
                        results.add(Double.valueOf(System.currentTimeMillis() - start3));

                        List<Requests.Request> ls4 = rs.readRequests("data/requests.txt", servers_unit);
                        Greedy greedy = new Greedy(lambda, beta, ls4);
                        long start4 = System.currentTimeMillis();
                        results.add(greedy.solution().doubleValue());
                        results.add(Double.valueOf(System.currentTimeMillis() - start4));

                        System.out.println(results);

                        contents.add(String.format("%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f,%f", results.toArray()));
                    }
                }
            }
        }

        IO.writeFile(contents,"data/results.csv");
    }
}
