package net.neuraxis.FASS;

import javax.xml.bind.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static javax.xml.stream.XMLStreamConstants.*;

public class StreamXMLUnmarshaller<T> implements AutoCloseable {
    private XMLStreamReader xmlIn;
    private final Class<T> klass;
    private final Unmarshaller unmarshaller;

    public StreamXMLUnmarshaller(Class<T> klass) throws JAXBException {
        this.klass = klass;
        final JAXBContext context = JAXBContext.newInstance(klass);
        this.unmarshaller = context.createUnmarshaller();
    }

    private void skip(final Integer... elements) throws XMLStreamException {
        int eventType = xmlIn.getEventType();
        final Set<Integer> ignoredTypes = new HashSet<>(Arrays.asList(elements));

        while(ignoredTypes.contains(eventType)) {
            eventType = xmlIn.next();
        }
    }

    public boolean hasNext() throws XMLStreamException {
        return xmlIn.hasNext();
    }

    public void open(final Path file) throws XMLStreamException, IOException {
        xmlIn = XMLInputFactory.newFactory().createXMLStreamReader(new BufferedInputStream(new FileInputStream(file.toAbsolutePath().toFile())));
        /* ignore headers */
        skip(START_DOCUMENT, DTD);
        /* ignore root element */
        xmlIn.nextTag();
        /* if there's no tag, ignore root element's end */
        skip(END_ELEMENT);
    }

    public T read() throws JAXBException, XMLStreamException {
        final T element = unmarshaller.unmarshal(xmlIn, klass).getValue();
        skip(CHARACTERS, END_ELEMENT);
        return element;
    }

    public void close() throws XMLStreamException {
        xmlIn.close();
    }
}