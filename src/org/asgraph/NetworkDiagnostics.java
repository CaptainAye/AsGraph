package org.asgraph;

import jdk.nashorn.internal.runtime.regexp.joni.Regex;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Tomek on 2017-01-14.
 */
public class NetworkDiagnostics {

    private static final String SPACE = " ";
    private static final String NEW_LINE = "\r\n";
    private static final String OS = System.getProperty("os.name").toLowerCase();




    private static List<String> getParsedRow(String tracerouteRow){
        String tmpRow = tracerouteRow.trim();
        String[] rowArray = tmpRow.split(SPACE);
        List<String> resultList = new ArrayList<>();

        for (int i = 0; i < rowArray.length ; i++){
            String currentField = rowArray[i];
            currentField = currentField.trim();
            if (!RegexCheck.checkWhitespace(currentField) && !RegexCheck.checkMS(currentField)){
                resultList.add(currentField);
            }
        }
        return resultList;
    }

    private static List<List<String>> removeFalseRows(List<String> results){
        List<String> tmpRowElements = null;
        List<List<String>> resultRows = new ArrayList<>();
        String tmpRow = null;
        int size = 0;
        for(Iterator<String> iter = results.iterator();iter.hasNext();){
            tmpRow = iter.next();
            tmpRowElements = getParsedRow(tmpRow);
            size = tmpRowElements.size();
            if (size > 0){
                // make sure that first row is number, las row is ip and that ip is public (prevents json fail)
                if (RegexCheck.checkInteger(tmpRowElements.get(0)) && RegexCheck.checkIp(tmpRowElements.get(size - 1)) && !RegexCheck.checkPrivateIp(tmpRowElements.get(size - 1))){
                    resultRows.add(tmpRowElements);
                }
            }

        }
        return resultRows;
    }

    public static List<List<String>> getTraceRouteRows(String results){
        List<List<String>> resultRows = null;
        List<String> tracerouteRows = new ArrayList<>(Arrays.asList(results.split(NEW_LINE)));
        resultRows = removeFalseRows(tracerouteRows);
        return resultRows;
    }
    public static String traceRoute(InetAddress address){
        String route = "";
        try {
            Process traceRt;
            if(OS.contains("win")) traceRt = Runtime.getRuntime().exec("tracert -d " + address.getHostAddress());
            else traceRt = Runtime.getRuntime().exec("traceroute " + address.getHostAddress());
            route = StreamToStringConverter.convertStreamToString(traceRt.getInputStream());
            String errors = StreamToStringConverter.convertStreamToString(traceRt.getErrorStream());
            if(errors != "") ;//java.util.logging.Logger.error(errors);
        }
        catch (IOException e) {
            ;//Logger.error("error while performing trace route command", e);
            e.printStackTrace();
        }

        return route;
    }




}
