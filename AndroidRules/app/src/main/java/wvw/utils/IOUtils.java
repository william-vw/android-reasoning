package wvw.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by William on 15/03/2018.
 */

public class IOUtils {

    public static String read(InputStream in) throws IOException {
        StringBuilder buf = new StringBuilder();
        BufferedReader bin =
                new BufferedReader(new InputStreamReader(in, "UTF-8"));

        String str;
        while ((str = bin.readLine()) != null)
            buf.append(str);

        in.close();

        return buf.toString();
    }
}
