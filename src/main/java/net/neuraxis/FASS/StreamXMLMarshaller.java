package net.neuraxis.FASS;


import javax.xml.bind.*;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;


public class StreamXMLMarshaller<T> implements AutoCloseable {
    private XMLStreamWriter xmlOut;
    final private Marshaller marshaller;
    final private Class<T> klass;

    public StreamXMLMarshaller(final Class<T> klass) throws JAXBException {
        this.klass = klass;
        final JAXBContext context = JAXBContext.newInstance(klass);
        this.marshaller = context.createMarshaller();
        this.marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        /* pretty-printed XML is not working with streaming so this line is redundant */
        this.marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    }

    public void open(final Path file) throws XMLStreamException, IOException {
        xmlOut = XMLOutputFactory.newFactory().createXMLStreamWriter(new BufferedOutputStream(new FileOutputStream(file.toAbsolutePath().toFile())));
        xmlOut.writeStartDocument();
        xmlOut.writeStartElement("root");
    }

    public void write(final T t) throws JAXBException {
        final JAXBElement<T> element = new JAXBElement<>(QName.valueOf(klass.getSimpleName()), klass, t);
        marshaller.marshal(element, xmlOut);
    }

    public void close() throws XMLStreamException {
        xmlOut.writeEndDocument();
        xmlOut.close();
    }
}