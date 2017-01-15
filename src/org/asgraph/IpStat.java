package org.asgraph;

import jdk.nashorn.internal.runtime.regexp.joni.Regex;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tomek on 2017-01-14.
 */
public class IpStat {
    private final Double rtt;
    private final InetAddress ipAddress;

    public Double getRtt(){
        return rtt;
    }

    public InetAddress getIpAddress(){
        return ipAddress;
    }

    public IpStat(Double rtt, InetAddress ipAddress){
        this.rtt = rtt;
        this.ipAddress = ipAddress;
    }

    private static IpStat getIpStat(List<String> tracerouteResult){
        int size = tracerouteResult.size();
        int rttSum = 0;
        int sumNo = 0;
        String currentField;
        if(size != 5){
            throw new RuntimeException("Wrong number of columns in traceroute results");
        }

        for (int i = 1; i< size - 2; i++){
            currentField = tracerouteResult.get(i);
            if (RegexCheck.checkInteger(currentField)){
                rttSum += Integer.parseInt(currentField);
                sumNo ++;
            }
        }
        InetAddress ip = null;
        try{
            ip = InetAddress.getByName(tracerouteResult.get(size - 1));
        } catch (UnknownHostException e){
            e.printStackTrace();
            System.exit(-1);
        }

        return new IpStat((double) rttSum/sumNo, ip);
    }

    public static List<IpStat> getIpStatResults(List<List<String>> tracerouteResults){
        List<IpStat> parsedResults = new ArrayList<>();
        IpStat statRow;
        for (List<String> result : tracerouteResults){
            statRow = getIpStat(result);
            // make sure that checked address is public one (preserves json fail)
            if (!RegexCheck.checkPrivateIp(statRow.getIpAddress().getHostAddress())) {
                parsedResults.add(getIpStat(result));
            }
        }
        return parsedResults;
    }
}
