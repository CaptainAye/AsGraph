package org.asgraph;

import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Created by Tomek on 2017-01-14.
 */
public class StreamToStringConverter {
    public static String convertStreamToString(InputStream iStream){
        String result = null;
        try {
            result =  new Scanner(iStream, "UTF-8").useDelimiter("\\A").next();
        } catch (NoSuchElementException e){
            System.err.println("Reading finished");
        }
        return result;
    }
}
