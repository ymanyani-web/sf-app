package it.agrimontana.salesforce.ordine.builder;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmiwconOrdine {
  private final String tipoDocumento;
  private final String riferimentoCliente;
  private final String cliente;
  private final String pagamentoCli;
  private final String destinatario;
  private final String tipologiaVendita;
  private final String dataPrimaScadenza;
  private final String noPark;
  private final String agente;
  private final String pagamento;
  private final String listino;
  private final String commentoInterno;
  private final String commentoEsterno;
  private final List<SmiwconRigheOrdine> rows;

  private SmiwconOrdine(Builder builder) {
    this.tipoDocumento = builder.tipoDocumento;
    this.riferimentoCliente = builder.riferimentoCliente;
    this.cliente = builder.cliente;
    this.pagamentoCli = builder.pagamentoCli;
    this.destinatario = builder.destinatario;
    this.tipologiaVendita = builder.tipologiaVendita;
    this.dataPrimaScadenza = builder.dataPrimaScadenza;
    this.noPark = builder.noPark;
    this.agente = builder.agente;
    this.pagamento = builder.pagamento;
    this.listino = builder.listino;
    this.commentoInterno = builder.commentoInterno;
    this.commentoEsterno = builder.commentoEsterno;
    this.rows = builder.rows;
  }

  public static class Builder {
    private String tipoDocumento;
    private String riferimentoCliente;
    private String cliente;
    private String pagamentoCli;
    private String destinatario;
    private String tipologiaVendita;
    private String dataPrimaScadenza;
    private String noPark;
    private String agente;
    private String pagamento;
    private String listino;
    private String commentoInterno;
    private String commentoEsterno;
    private List<SmiwconRigheOrdine> rows;

    public Builder tipoDocumento(String tipoDocumento) {
      this.tipoDocumento = tipoDocumento;
      return this;
    }

    public Builder riferimentoCliente(String riferimentoCliente, String fallback) {
      if(riferimentoCliente == null || riferimentoCliente.trim().isEmpty()) {
        this.riferimentoCliente = fallback.length() > 20 ? fallback.substring(0,20) : fallback;
        return this;
      }
      this.riferimentoCliente = riferimentoCliente.length() > 20 ? riferimentoCliente.substring(0,20) : riferimentoCliente;
      return this;
    }

    public Builder cliente(String cliente) {
      this.cliente = cliente;
      return this;
    }

    public Builder pagamentoCli(String pagamentoPreordine, String pagamentoCliente) {
      if(pagamentoCliente == null || pagamentoCliente.trim().isEmpty()) {
        this.pagamentoCli = pagamentoPreordine;
        return this;
      }
      this.pagamentoCli = pagamentoCliente;
      return this;
    }

    public Builder destinatario(String addressType, String destinatario) {
      if (!"billing".equalsIgnoreCase(addressType)) {
        this.destinatario = destinatario;
        return this;
      }
      this.destinatario = null;
      return this;
    }

    public Builder tipologiaVendita(String tipologiaVendita) {
      if("labo".equalsIgnoreCase(tipologiaVendita)){
        this.tipologiaVendita = "LABO";
      } else if ("rtl".equalsIgnoreCase(tipologiaVendita)) {
        this.tipologiaVendita = "RTL";
      } else if ("gdo".equalsIgnoreCase(tipologiaVendita)){
        this.tipologiaVendita = "GDO";
      } else {
        this.tipologiaVendita = null;
      }
      return this;
    }

    public Builder tipologiaDiVendita(String tipologiaDiVendita) {
      if(tipologiaDiVendita == null || tipologiaDiVendita.trim().isEmpty()) {
        throw new IllegalArgumentException("Tipologia vendita preordine non definita");
      }

      if("labo1".equalsIgnoreCase(tipologiaDiVendita)) {
        this.tipologiaVendita = "LABO";
        return this;
      }

      if("rtl1".equalsIgnoreCase(tipologiaDiVendita)) {
        this.tipologiaVendita = "RTL";
        return this;
      }

      throw new IllegalArgumentException("Tipologia vendita preordine errata");
    }

    public Builder tipologiaDiVendita2(String tipologia) {
      this.tipologiaVendita = tipologia;
      return this;
    }

    public Builder dataPrimaScadenza(String dataPrimaScadenza) {
      if(dataPrimaScadenza != null) {
          this.dataPrimaScadenza = dataPrimaScadenza.replace("-","");
          return this;
      }

      this.dataPrimaScadenza = null;
      return this;
    }


    public Builder noPark(String noPark) {
      this.noPark = noPark;
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

    public Builder agente(String agente) {
      this.agente = agente;
      return this;
    }

    public Builder pagamento(String pagamentoPreordine, String pagamentoCliente) {
      if(pagamentoPreordine == null || pagamentoPreordine.trim().isEmpty()) {
        this.pagamento = pagamentoCliente;
        return this;
      }
      this.pagamento = pagamentoPreordine;
      return this;
    }

    public Builder rows(List<SmiwconRigheOrdine> rows) {
      this.rows = rows;
      return this;
    }

    public Builder listino(String listino) {
      this.listino = listino;
      return this;
    }

    public SmiwconOrdine build() {
      return new SmiwconOrdine(this);
    }
  }

  @Override
  public String toString() {
    return "SmiwconOrdine{" +
        "tipoDocumento='" + tipoDocumento + '\'' +
        ", riferimentoCliente='" + riferimentoCliente + '\'' +
        ", cliente='" + cliente + '\'' +
        ", pagamentoCli='" + pagamentoCli + '\'' +
        ", destinatario='" + destinatario + '\'' +
        ", tipologiaVendita='" + tipologiaVendita + '\'' +
        ", dataPrimaScadenza='" + dataPrimaScadenza + '\'' +
        ", noPark='" + noPark + '\'' +
        ", agente='" + agente + '\'' +
        ", pagamento='" + pagamento + '\'' +
        ", rows=" + rows +
        '}';
  }
}
