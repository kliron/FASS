package net.neuraxis.FASS;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="drug")
public class Drug {
    private String drugId;    // FASS nplId field
    private String atc;
    private String substance;
    private String tradeName;
    private String form;
    private String interactions;
    private String sideEffects;

    public Drug(String drugId, String atc, String substance, String tradeName, String form, String interactions, String sideEffects) {
        this.drugId = drugId;
        this.atc = atc;
        this.substance = substance;
        this.tradeName = tradeName;
        this.form = form;
        this.interactions = interactions;
        this.sideEffects = sideEffects;
    }

    public Drug() {}

    public String getDrugId() {
        return drugId;
    }

    public void setDrugId(String drugId) {
        this.drugId = drugId;
    }

    public String getAtc() {
        return atc;
    }

    public void setAtc(String atc) {
        this.atc = atc;
    }

    public String getSubstance() {
        return substance;
    }

    public void setSubstance(String substance) {
        this.substance = substance;
    }

    public String getTradeName() {
        return tradeName;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getInteractions() {
        return interactions;
    }

    public void setInteractions(String interactions) {
        this.interactions = interactions;
    }

    public String getSideEffects() {
        return sideEffects;
    }

    public void setSideEffects(String sideEffects) {
        this.sideEffects = sideEffects;
    }
}
