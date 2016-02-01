package net.neuraxis.FASS;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Extractor {
    private final String drugLinkSel;
    private final String atcSel;
    private final String substanceSel;
    private final String tradeNameSel;
    private final String formSel;
    private final String interactionsHeadingsSel;
    private final String interactionsSel;
    private final String sideEffectsHeadingsSel;
    private final String sideEffectsSel;

    public Extractor() throws IOException {
        final Properties properties = Main.properties;
        this.drugLinkSel = properties.getProperty("drug.info.page.link.sel");
        this.atcSel = properties.getProperty("atc.sel");
        this.substanceSel = properties.getProperty("substance.sel");
        this.tradeNameSel = properties.getProperty("tradename.sel");
        this.formSel = properties.getProperty("form.sel");
        this.interactionsHeadingsSel = properties.getProperty("interactions.headings.sel");
        this.interactionsSel = properties.getProperty("interactions.sel");
        this.sideEffectsHeadingsSel = properties.getProperty("side.effects.headings.sel");
        this.sideEffectsSel = properties.getProperty("side.effects.sel");
    }

    /**
     * Extracts a Drug object from a Document
     *
     * @param file - Path to html file name to extract from
     * @return drug - org.kliron.javaworks.drugBank.Drug
     */
    private Drug extract(final Path file) {
        final Drug drug = new Drug();
        try {
            final String filename = file.getFileName().toString();
            final String drugId = filename.substring(0, filename.length() - 5);   // minus ".html"
            final Document doc = Jsoup.parse(file.toAbsolutePath().toFile(), "UTF-8");

            /* Drug ID and ATC code must exist for all drugs */
            drug.setDrugId(drugId);
            drug.setAtc(doc.select(atcSel).text());
            drug.setSubstance(doc.select(substanceSel).text());
            drug.setForm(doc.select(formSel).text());
            drug.setTradeName(doc.select(tradeNameSel).first().ownText());

            /** Interactions and side-effects cannot be extracted by css because
             * 1) their unique identifier (id) is in a child of their preceding sibling and CSS has no "parent-of"
             * selector and
             * 2) their position in the text varies so we can't use nth-child and the likes.
             * We need to extract their positions programmatically. */
            /* Interactions */
            final Elements interactionHeadings = doc.select(interactionsHeadingsSel);
            int interactionDivOffset = 0;
            for (int i = 1; i < interactionHeadings.size(); i++) {
                final String content = interactionHeadings.get(i).select("a").attr("id");
                if (content.equals("interaction")) {
                    interactionDivOffset = i + 1;
                    break;
                }
            }
            final String iSel = interactionsSel + ":nth-of-type(" + interactionDivOffset + ")";
            drug.setInteractions(doc.select(iSel).text());

            /* Side-effects */
            final Elements sideEffectsHeadings = doc.select(sideEffectsHeadingsSel);
            int sideEffectsDivOffset = 0;
            for (int i = 1; i < sideEffectsHeadings.size(); i++) {
                final String content = sideEffectsHeadings.get(i).select("a").attr("id");
                if (content.equals("side-effects")) {
                    sideEffectsDivOffset = i + 1;
                    break;
                }
            }
            final String sSel = sideEffectsSel + ":nth-of-type(" + sideEffectsDivOffset + ")";
            String sideEffects = doc.select(sSel).text();

            /* Also extract text from side effects table */
            final String sideEffectsTableSel = sSel + " > table tr > td > p";
            sideEffects += doc.select(sideEffectsTableSel).text();
            drug.setSideEffects(sideEffects);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return drug;
    }

    /**
     * Reads fetched drug list files and extracts links to drug info pages.
     */
    public Set<String> extractLinks() {
        return Arrays.asList(Main.listsDir.toFile().list()).parallelStream()
                .map(f -> Main.listsDir.resolve(f))
                .filter(path -> path.getFileName().toString().matches("[ABCDEFGHIJKLMNOPQRSTUVWXYZÅÄÖ].html"))
                .flatMap(path -> {
                    Elements els = null;
                    try {
                        final Document doc = Jsoup.parse(path.toAbsolutePath().toFile(), "UTF-8");
                        els = doc.select(drugLinkSel);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    return els == null ? Stream.empty() : els.stream();
                })
                .map(el -> el.attr("href").replaceAll(".*?\\.(/product).*?(\\?.*)", "$1$2"))
                .collect(Collectors.toSet());
    }


    /**
     * Extracts drug information from fetched pages.
     */
    public void extractDrugs() throws JAXBException, IOException, XMLStreamException {
        try (final StreamXMLMarshaller<Drug> marshaller = new StreamXMLMarshaller<>(Drug.class)) {
            marshaller.open(Main.drugsXml);
            Arrays.asList(Main.infoDir.toFile().list()).parallelStream()
                    .map(f -> Main.infoDir.resolve(f))
                    .map(this::extract)
                    .forEachOrdered(drug -> {
                        try {
                            marshaller.write(drug);
                        } catch (JAXBException ex) {
                            ex.printStackTrace();
                        }
                    });

        }
    }
}