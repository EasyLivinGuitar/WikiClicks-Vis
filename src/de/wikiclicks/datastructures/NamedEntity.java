package de.wikiclicks.datastructures;

import java.io.Serializable;

public class NamedEntity implements Serializable {
    private String namedEntity;
    private Double hotnessScore;

    private static final long serialVersionUID = -1711389721610250462L;

    public NamedEntity(String namedEntity) {
        this.namedEntity = namedEntity;

        hotnessScore = 1.0;
    }

    public NamedEntity(String namedEntity, Double hotnessScore) {
        this.namedEntity = namedEntity;
        this.hotnessScore = hotnessScore;
    }

    public String getNamedEntity() {
        return namedEntity;
    }

    public Double getHotnessScore() {
        return hotnessScore;
    }

    public NamedEntity merge(NamedEntity entity){
        if(this.namedEntity.equals(entity.namedEntity)){
            this.hotnessScore += entity.hotnessScore;
        }

        return this;
    }

    @Override
    public boolean equals(Object other){
        if(!(other instanceof NamedEntity)){
            return false;
        }

        return namedEntity.equals(((NamedEntity)other).getNamedEntity());
    }

    @Override
    public String toString(){
        return namedEntity+": "+hotnessScore;
    }
}
