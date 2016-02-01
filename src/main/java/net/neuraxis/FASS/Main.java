package net.neuraxis.FASS;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Set;


public class Main {
    protected static Properties properties;
    protected static Properties secretProperties;
    private static Path workDir;
    private static Path dataDir;
    protected static Path listsDir;
    protected static Path infoDir;
    protected static Path outputDir;
    protected static Path drugsXml;


    private static void fetchList() throws FileNotFoundException, UnsupportedEncodingException {
        final Crawler crawler = new Crawler();
        final long startT = System.currentTimeMillis();
        int status = crawler.fetchDrugList();
        if (status != 0) {
            System.out.println("Crawler returned error status.");
        }
        final long endT = System.currentTimeMillis();
        System.out.printf("Fetch finished in %d ms\n", endT-startT);
    }

    private static void extractLinks() throws IOException {
        final Extractor extractor = new Extractor();
        final long startT = System.currentTimeMillis();
        final Set<String> links = extractor.extractLinks();
        final long endT = System.currentTimeMillis();
        for (final String link : links) {
            System.out.println(link);
        }
        System.out.printf("Extraction finished in %d ms\n", endT - startT);
    }

    private static void fetchDrugData() throws IOException {
        final Crawler crawler = new Crawler();
        final Extractor extractor = new Extractor();
        final long startT = System.currentTimeMillis();
        final Set<String> links = extractor.extractLinks();
        int status = crawler.fetchDrugData(links);
        final long endT = System.currentTimeMillis();
        if (status != 0) {
            System.out.println("Crawler returned error status.");
        }
        System.out.printf("Fetch finished in %d ms\n", endT-startT);
    }

    private static void extractDrugs() throws IOException, JAXBException, XMLStreamException {
        final Extractor extractor = new Extractor();
        final long startT = System.currentTimeMillis();
        extractor.extractDrugs();
        final long endT = System.currentTimeMillis();
        System.out.printf("Extraction finished in %d ms\n", endT - startT);
    }

    private static void seedDrugData() throws ClassNotFoundException, SQLException, JAXBException, IOException, XMLStreamException {
        Seeder seeder = new Seeder();
        final long startT = System.currentTimeMillis();
        seeder.seedDrugs();
        final long endT = System.currentTimeMillis();
        System.out.printf("Seeding finished in %d ms\n", endT - startT);
    }

    private static String getProp(final String key) {
        final String prop = System.getProperty(key);
        return prop != null ? prop : properties.getProperty(key);
    }


    public static void main(String[] args) throws JAXBException, IOException, ClassNotFoundException, SQLException, XMLStreamException {
        final String usage = "\nThis program needs a single argument.\n\n"
                + "fetchList :: Fetches all pages containing links to drug information pages from Fass.se in alphabetic order and saves to listsDir.\n"
                + "extractLinks :: For each page fetched in the above step, extracts links to drug info pages and prints to std out.\n"
                + "fetchDrugs :: For each file fetched from the above step, reads all links to drug information pages and fetches those pages.\n"
                + "extractDrugs :: For each fetched info sheet, extract drug info and save as xml to infoDir.\n"
                + "seed :: For each file fetched from the above step, reads all needed info and seeds to database.\n\n";

        final String configurableProperties = "Configurable properties:\n\n"
                + "work.dir\n"
                + "data.dir\n"
                + "info.dir\n"
                + "output.dir\n"
                + "lists.dir\n"
                + "info.dir\n"
                + "drugs.xml\n\n\n";

        if (args.length != 1) {
            System.err.println(usage);
            System.exit(-1);
        }

        try {
            properties = new Properties();
            final InputStream in = Crawler.class.getClassLoader().getResourceAsStream("config.properties");
            properties.load(in);

            workDir = Paths.get(getProp("work.dir"));
            dataDir = workDir.resolve(getProp("data.dir"));
            outputDir = workDir.resolve(getProp("output.dir"));
            listsDir = dataDir.resolve(getProp("lists.dir"));
            infoDir = dataDir.resolve(getProp("info.dir"));
            drugsXml = outputDir.resolve(getProp("drugs.xml"));

            secretProperties = new Properties();
            final InputStream sIn = Crawler.class.getClassLoader().getResourceAsStream("secret.properties");
            secretProperties.load(sIn);

            /* Assert destination directories exist and are writable */
            if (!Files.isDirectory(workDir) || !Files.isWritable(workDir)) {
                throw new AssertionError("lists directory " + listsDir + " does not exist or is not writable.");
            }
            if (!Files.isDirectory(dataDir) || !Files.isWritable(dataDir)) {
                throw new AssertionError("info directory " + infoDir + " does not exist or is not writable.");
            }
        } catch(AssertionError err) {
            System.err.println(err.getMessage());
            System.exit(-1);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }

        switch (args[0]) {
            case "fetchList":
                fetchList();
                break;
            case "extractLinks":
                extractLinks();
                break;
            case "fetchDrugs":
                fetchDrugData();
                break;
            case "extractDrugs":
                extractDrugs();
                break;
            case "seed":
                seedDrugData();
                break;
            default:
                System.err.println(usage);
                System.out.printf("Defaults:\n\nWork dir is %s\nData directory is %s\nLists directory is %s\nInfo directory is %s\n\n\n",
                        workDir, dataDir, listsDir, infoDir);
                System.out.println(configurableProperties);
        }
    }
}
