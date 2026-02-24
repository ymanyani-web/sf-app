package it.agrimontana.salesforce.ordine.builder;

import com.fasterxml.jackson.annotation.JsonInclude;
import it.agrimontana.salesforce.dto.order.lookup.SourceOrderItem;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmiwconRigheOrdine {
  private final String codiceProd;
  private final String prezzoLordo;
  private final String prezzoNetto;
  private final String sconto1;
  private final String sconto2;
  private final String sconto3;
  private final String sconto4;
  private final String sconto5;
  private final String sconto6;
  private final String sconto7;
  private final String sconto9;
  private final String flagSconto5;
  private final String flagSconto6;
  private final String flagSconto7;
  private final String flagSconto9;
  private final String qta;
  private final String tipoMovimento;
  private final String commentoInterno;
  private final String commentoEsterno;
  private final String tipoOfferta;
  private final String numeroOfferta;
  private final String numeroRigaOfferta;

  public SmiwconRigheOrdine(Builder builder) {
    this.codiceProd = builder.codiceProd;
    this.prezzoLordo = builder.prezzoLordo;
    this.prezzoNetto = builder.prezzoNetto;
    this.sconto1 = builder.sconto1;
    this.sconto2 = builder.sconto2;
    this.sconto3 = builder.sconto3;
    this.sconto4 = builder.sconto4;
    this.sconto5 = builder.sconto5;
    this.sconto6 = builder.sconto6;
    this.sconto7 = builder.sconto7;
    this.sconto9 = builder.sconto9;
    this.flagSconto5 = builder.flagSconto5;
    this.flagSconto6 = builder.flagSconto6;
    this.flagSconto7 = builder.flagSconto7;
    this.flagSconto9 = builder.flagSconto9;
    this.qta = builder.qta;
    this.tipoMovimento = builder.tipoMovimento;
    this.commentoInterno = builder.commentoInterno;
    this.commentoEsterno = builder.commentoEsterno;
    this.tipoOfferta = builder.tipoOfferta;
    this.numeroOfferta = builder.numeroOfferta;
    this.numeroRigaOfferta = builder.numeroRigaOfferta;
  }

  public static class Builder {
    private String codiceProd;
    private String prezzoLordo;
    private String prezzoNetto;
    private String sconto1;
    private String sconto2;
    private String sconto3;
    private String sconto4;
    private String sconto5;
    private String sconto6;
    private String sconto7;
    private String sconto9;
    private String flagSconto5;
    private String flagSconto6;
    private String flagSconto7;
    private String flagSconto9;
    private String qta;
    private String tipoMovimento;
    private String commentoInterno;
    private String commentoEsterno;
    private String tipoOfferta;
    private String numeroOfferta;
    private String numeroRigaOfferta;

    public Builder codiceProd(String codiceProd) {
      this.codiceProd = codiceProd;
      return this;
    }

    public Builder prezzoLordo(String unitprice) {
        this.prezzoLordo = unitprice;
        return this;
    }

    public Builder prezzoNetto1(String totalWithDiscount, String quantity) {
      double total = parseDouble(totalWithDiscount);
      double qty = parseDouble(quantity);
      if (qty != 0) {
        this.prezzoNetto = String.valueOf(total / qty);
      }
      return this;
    }

    public Builder prezzoNetto(String prezzo) {
      this.prezzoNetto = prezzo;
      return this;
    }

    private double parseDouble(String value) {
      if (value == null || value.trim().isEmpty()) {
        return 0.0;
      }
      try {
        return Double.parseDouble(value);
      } catch (NumberFormatException e) {
        return 0.0;
      }
    }

    public Builder sconto1(String sconto1) {
      this.sconto1 = sconto1;
      return this;
    }
    public Builder sconto2(String sconto2) {
      this.sconto2 = sconto2;
      return this;
    }

    public Builder sconto3(String sconto3) {
      this.sconto3 = sconto3;
      return this;
    }
    
    public Builder sconto4(String sconto4) {
      this.sconto4 = sconto4;
      return this;
    }

    public Builder sconto5(String sconto5) {
      this.sconto5 = sconto5;
      return this;
    }

    public Builder sconto6(String sconto6) {
      this.sconto6 = sconto6;
      return this;
    }

    public Builder sconto7(String sconto7) {
      this.sconto7 = sconto7;
      return this;
    }

    public Builder sconto9(String sconto9) {
      this.sconto9 = sconto9;
      return this;
    }

    public Builder flagSconto5(String flagSconto5) {
      this.flagSconto5 = flagSconto5;
      return this;
    }
    public Builder flagSconto6(String flagSconto6) {
      this.flagSconto6 = flagSconto6;
      return this;
    }

    public Builder flagSconto7(String flagSconto7) {
      this.flagSconto7 = flagSconto7;
      return this;
    }

    public Builder flagSconto9(String flagSconto9) {
      this.flagSconto9 = flagSconto9;
      return this;
    }

    public Builder qta(String qta) {
      this.qta = qta;
      return this;
    }

    public Builder tipoMovimento(String tipoMovimento) {
      if (tipoMovimento != null) {
        this.tipoMovimento = tipoMovimento.substring(0, 2).toUpperCase();
        return this;
      }

      this.tipoMovimento = null;
      return this;
    }

    public Builder commentoInterno(String commentoInterno) {
      this.commentoInterno = commentoInterno;
      return this;
    }

    public Builder commentoEsterno(String commentoEsterno) {
      this.commentoEsterno = commentoEsterno;
      return this;
    }

    public Builder tipoOfferta(String tipoOfferta) {
      this.tipoOfferta = tipoOfferta;
      return this;
    }

    public Builder numeroOfferta(SourceOrderItem numeroOfferta, String id) {
      if(numeroOfferta == null
          || numeroOfferta.getOrder() == null
          || numeroOfferta.getOrder().getERPKey__c() == null
      ) {
        throw new IllegalStateException(
            String.format("PreorderLineItem con Id %s non ha un SourceOrderItem__r.Order.ERPKey__c valido. Per ordini di tipo Richiamo (R), ogni riga deve avere un riferimento all'ordine sorgente.", id)
        );
      }

      String erpKey = numeroOfferta.getOrder().getERPKey__c();
      this.numeroOfferta = erpKey.contains("-") ? erpKey.substring(erpKey.indexOf("-") + 1) : erpKey;
      return this;
    }

    public Builder numeroRigaOfferta(String numeroRigaOfferta) {
      this.numeroRigaOfferta = numeroRigaOfferta;
      return this;
    }

    public SmiwconRigheOrdine build() {
      return new SmiwconRigheOrdine(this);
    }
  }

  @Override
  public String toString() {
    return "SmiwconRigheOrdine{" +
        "codiceProd='" + codiceProd + '\'' +
        ", prezzoLordo='" + prezzoLordo + '\'' +
        ", prezzoNetto='" + prezzoNetto + '\'' +
        ", sconto1='" + sconto1 + '\'' +
        ", sconto2='" + sconto2 + '\'' +
        ", sconto3='" + sconto3 + '\'' +
        ", sconto4='" + sconto4 + '\'' +
        ", sconto5='" + sconto5 + '\'' +
        ", flagSconto5='" + flagSconto5 + '\'' +
        ", qta='" + qta + '\'' +
        ", tipoMovimento='" + tipoMovimento + '\'' +
        ", commentoInterno='" + commentoInterno + '\'' +
        ", commentoEsterno='" + commentoEsterno + '\'' +
        ", tipoOfferta='" + tipoOfferta + '\'' +
        ", numeroOfferta='" + numeroOfferta + '\'' +
        ", numeroRigaOfferta='" + numeroRigaOfferta + '\'' +
        '}';
  }
}
