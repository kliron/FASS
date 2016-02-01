package net.neuraxis.FASS;

import java.io.*;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import java.nio.file.Files;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.StandardCopyOption.*;

public class Crawler {
    private final int niceness;
    private final String drugListUrlFormat;
    private final String fassRoot;
    private final String idExtractorRegex;
    private final String userAgent;

    Crawler() throws FileNotFoundException, UnsupportedEncodingException {
        final Properties properties = Main.properties;
        this.niceness = Integer.parseInt(properties.getProperty("niceness"));
        this.drugListUrlFormat = properties.getProperty("fass.drug.list.url.format");
        this.fassRoot = properties.getProperty("fass.root.url");
        this.idExtractorRegex = properties.getProperty("id.extractor.regex");
        this.userAgent = properties.getProperty("user.agent");
    }

    /**
     * Fetches the pages that contain links to drug description pages.
     * @return 0 if operation was successful, -2 if any errors occured.
     */
    public int fetchDrugList() {
        final char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZÅÄÖ".toCharArray();
        int status = 0;
        for (char c : alphabet) {
            try {
                /* If a file named as this id exists already, skip */
                if (new File(Main.listsDir + "/" + c + ".html").exists()) {
                    System.out.println("File " + c + ".html exists. Skipping this letter.");
                    continue;
                }
                System.out.println(c + " ");
                final URL url = new URL(String.format(drugListUrlFormat, c));
                final HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
                con.setRequestProperty("User-Agent", userAgent);

                try (InputStream in = con.getInputStream()) {
                    Files.copy(in, Main.listsDir.resolve(c + ".html"), REPLACE_EXISTING);
                }

                Thread.sleep(niceness);
            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();
                status = -2;
            }
        }
        return status;
    }

    /**
     * Fetches the drug info pages.
     * @return 0 if operation was successful, -2 if any errors occured.
     */
    public int fetchDrugData(Set<String> links) {
        final Pattern idPattern = Pattern.compile(idExtractorRegex);
        int status = 0;
        for (final String link : links) {
            try {
                if (link == null) continue;

                final Matcher matcher = idPattern.matcher(link);
                if (!matcher.find()) {
                    System.out.println("WARNING: No id matched for link: " + link);
                    continue;
                }
                final String id = matcher.group(1);

                /* If a file named as this id exists already, skip */
                if (new File(Main.infoDir + "/" + id + ".html").exists()) {
                    System.out.println("INFO: File " + id + ".html exists. Skipping this id.");
                    continue;
                }
                System.out.println(id);

                final URL url = new URL(fassRoot + link);
                final HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
                con.setRequestProperty("User-Agent", userAgent);

                try (InputStream in = con.getInputStream()) {
                    Files.copy(in, Main.infoDir.resolve(id + ".html"), REPLACE_EXISTING);
                }

                Thread.sleep(niceness);
            } catch (Exception ex) {
                ex.printStackTrace();
                status = -2;
            }
        }
        return status;
    }
}
