package org.asgraph;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tomek on 2017-01-14.
 */
public class AsGraph {


    private List<List<AsStat>> adjacencyList = new ArrayList<>();

    public Double[][] getAdjacencyArray(){
        Double[][] graphArray = new Double[adjacencyList.size()][adjacencyList.size()];
        AsStat neighbor;
        for (int i = 0; i < adjacencyList.size(); i++){
            // add self
            graphArray[i][i] = adjacencyList.get(i).get(0).getRttDistance();
            for ( int j = 1; j < adjacencyList.get(i).size(); j++){
                neighbor =  adjacencyList.get(i).get(j);
                int neighborPositionInGraph = getPositionInGraph(neighbor);
                graphArray[i][neighborPositionInGraph] = Math.abs(adjacencyList.get(neighborPositionInGraph).get(0).getRttDistance() - adjacencyList.get(i).get(0).getRttDistance());
                graphArray[neighborPositionInGraph][i] =  Math.abs((adjacencyList.get(neighborPositionInGraph).get(0).getRttDistance() - adjacencyList.get(i).get(0).getRttDistance()));
            }
        }
        return graphArray;
    }

    public Integer getAutonomousSystemNumber(int positionInArray){
        return adjacencyList.get(positionInArray).get(0).getAsNumber();
    }

    public Integer getGraphSize(){
        return adjacencyList.size();
    }



    public void asGraphTraceroute(List<String> ipAddresses){
        for(String ipAddress : ipAddresses){
            asGraphTraceroute(ipAddress);
        }
    }

    private void asGraphTraceroute(String ipAddress){
        String traceRouteResults = null;
        List<List<String>> parsedResults;
        try {
            traceRouteResults = NetworkDiagnostics.traceRoute(InetAddress.getByName(ipAddress));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        parsedResults = NetworkDiagnostics.getTraceRouteRows(traceRouteResults);
        List<AsStat> asResults = AsStat.getAsStatResults(parsedResults);

        addIntoGraph(asResults);
    }

    private int getPositionInGraph(AsStat asResult){
        List<AsStat> adjacent;
        for(int i = 0; i < adjacencyList.size();i++ ){
            adjacent = adjacencyList.get(i);
            if (adjacent.get(0).equals(asResult)){
                return i;
            }
        }
        throw new ArrayIndexOutOfBoundsException("No such as in graph");
    }

    private void addAdjacency(int asResult, int nextAsResult){
        for (AsStat adjacent : adjacencyList.get(asResult)){
            if (adjacent.equals(adjacencyList.get(nextAsResult).get(0))){
                return;
            }
        }
        adjacencyList.get(asResult).add(adjacencyList.get(nextAsResult).get(0));
        adjacencyList.get(nextAsResult).add(adjacencyList.get(asResult).get(0));

    }

    private void addIntoGraph(List<AsStat> asResults){
        AsStat asResult = null;
        AsStat nextAsResult = null;
        int asResultPositionInGraph = -1;
        int nextAsResultPositionInGraph = -1;
        for( int i = 0;i < asResults.size() - 1;i++ ){
            asResult = asResults.get(i);
            nextAsResult = asResults.get(i + 1);
            try {
                if (asResultPositionInGraph == -1) {
                    asResultPositionInGraph = getPositionInGraph(asResult);
                }
                // as already in graph
            }catch (ArrayIndexOutOfBoundsException e){
                // No as in graph yet
                List<AsStat> adjacent = new ArrayList<>();
                adjacent.add(asResult);
                adjacencyList.add(adjacent);
                asResultPositionInGraph = adjacencyList.size() - 1;
            }

            try {
                nextAsResultPositionInGraph = getPositionInGraph(nextAsResult);
            } catch (ArrayIndexOutOfBoundsException e){
                // No as in graph yet
                List<AsStat> adjacent = new ArrayList<>();
                adjacent.add(nextAsResult);
                adjacencyList.add(adjacent);
                nextAsResultPositionInGraph = adjacencyList.size() - 1;
            }
            addAdjacency(asResultPositionInGraph,nextAsResultPositionInGraph);
            asResultPositionInGraph = nextAsResultPositionInGraph;
        }
    }

    private static class AsStat{
        private Integer asNumber;
        private Double msRttDistance;

        public boolean equals(AsStat asStat){
            if (this.asNumber.equals(asStat.asNumber)){
                return true;
            }
            return false;
        }
        private static void asCompress(List<AsStat> asResults){
            int i = 0;
            while(i < asResults.size() - 1) {
                if (asResults.get(i).getAsNumber().equals(asResults.get(i + 1).getAsNumber())) {
                    asResults.remove(i);
                } else {
                    i++;
                }
            }
        }

        private static List<AsStat> getAsStatResults(List<List<String>> parsedResults){
            List<AsStat> asStatResults = new ArrayList<>();
            List<IpStat> ipStatResults;
            ipStatResults = IpStat.getIpStatResults(parsedResults);
            AsStat as = null;
            for (IpStat ipStatResult : ipStatResults){
                as = new AsStat();
                as.fillAsFromIpStat(ipStatResult);
                asStatResults.add(as);
            }
            asCompress(asStatResults);
            return asStatResults;
        }
        public Integer getAsNumber(){
            return asNumber;
        }

        public Double getRttDistance(){
            return msRttDistance;
        }

        public void fillAsFromIpStat(IpStat ipStat){
            this.msRttDistance = ipStat.getRtt();
            this.asNumber = findAsNumber(ipStat.getIpAddress());
        }

        private Integer findAsNumber(InetAddress ip){
            String response = sendGetRequest(ip.getHostAddress());
            //TODO - read 404: not found status
            JsonParser parser = new JsonParser();
            JsonObject obj = parser.parse(response).getAsJsonObject();
            String asn = obj.get("as").getAsJsonObject().get("asn").getAsString();


            return Integer.parseInt(asn);
        }

        private String sendGetRequest(String ip){
            String url = "http://api.moocher.io/as/ip/" + ip;
            String charset = "UTF-8";
            String jsonResponse = null;
            try {
                URLConnection connection = new URL(url).openConnection();
                connection.setRequestProperty("Accept-Charset", charset);
                InputStream response = connection.getInputStream();
                jsonResponse = StreamToStringConverter.convertStreamToString(response);
            } catch ( IOException e){
                e.printStackTrace();
                System.exit(-1);
            }
            return jsonResponse;
        }

    }


    public static void main (String[] args) {

        ArrayList<String> ipList = new ArrayList<String>();
        ipList.add("213.180.141.140");
        ipList.add("203.173.50.134");
        AsGraph asGraph = new AsGraph();
        asGraph.asGraphTraceroute(ipList);

        Double[][] graphArray = asGraph.getAdjacencyArray();

        for (int i = 0; i <asGraph.getGraphSize() ; i++){
            for ( int j = 0; j < asGraph.getGraphSize() ;j++){
                System.out.print(graphArray[i][j] + ", ");
                if (j == asGraph.getGraphSize() - 1){
                    System.out.println();
                }
            }
        }

       /* String traceRouteResults = null;
        List<List<String>> parsedResults;
        for(String ipAddress : ipList) {
            try {
                traceRouteResults = NetworkDiagnostics.traceRoute(InetAddress.getByName(ipAddress));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            parsedResults = NetworkDiagnostics.getTraceRouteRows(traceRouteResults);
            System.out.print(parsedResults);
            AsStat as = new AsStat();
            as.fillAsFromIpStat(IpStat.getIpStatResults(parsedResults).get(2));
            System.out.println(as.getAsNumber());
        }
        */

    }
}
