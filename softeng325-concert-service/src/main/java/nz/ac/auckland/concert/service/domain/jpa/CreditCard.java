package nz.ac.auckland.concert.service.domain.jpa;

import nz.ac.auckland.concert.common.dto.CreditCardDTO;

import javax.persistence.*;
import java.time.LocalDate;

@Embeddable
public class CreditCard {

    public enum Type {Visa, Master};

    @Enumerated
    private CreditCardDTO.Type _type;

    private String _name;

    private String _number;

    private LocalDate _expiryDate;

    @Enumerated
    public CreditCardDTO.Type getType() {
        return _type;
    }

    public CreditCard(CreditCardDTO.Type _type, String _name, String _number, LocalDate _expiryDate) {
        this._type = _type;
        this._name = _name;
        this._number = _number;
        this._expiryDate = _expiryDate;
    }

    public CreditCard() {}

    public void setType(CreditCardDTO.Type type) {
        this._type = type;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public String getNumber() {
        return _number;
    }

    public void setNumber(String number) {
        this._number = number;
    }

    public LocalDate getExpiryDate() {
        return _expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this._expiryDate = expiryDate;
    }
}
