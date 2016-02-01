package net.neuraxis.FASS;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * (THIS CLASS IS NOT CURRENTLY USED, STREAM MARSHAL/UNMARSHALERS ARE USED INSTEAD)
 *
 To make Drug Lists marshallable/unmarshallable to/from XML/JSON just wrap the lists in this class.
 */

@XmlRootElement
@XmlSeeAlso(Drug.class)
public class Drugs extends ArrayList<Drug> {
    public Drugs() { super(); }

    public Drugs(Collection<? extends Drug> C) { super(C); }

    @XmlElement
    public List<Drug> getDrug() {
        return this;
    }

    public void setDrugs(List<Drug> drugs) {
        this.addAll(drugs);
    }
}