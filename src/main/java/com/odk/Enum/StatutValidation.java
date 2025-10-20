package com.odk.Enum;

public enum StatutValidation {
    Validé("VALIDE"),
    En_Attente("En_Attente"),
    Rejeté("REJETER")
    ;

    private final String value;

    StatutValidation(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
