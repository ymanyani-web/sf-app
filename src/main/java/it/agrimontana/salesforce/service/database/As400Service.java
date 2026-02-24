package it.agrimontana.salesforce.service.database;

import static it.agrimontana.salesforce.Utils.trimString;
import static it.agrimontana.salesforce.Utils.convertAs400DateToIso;
import com.ibm.as400.access.AS400;
import it.agrimontana.salesforce.dto.*;
import it.agrimontana.salesforce.dto.order.SalesforceOrder;
import it.agrimontana.salesforce.dto.order.SalesforceOrderLines;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
public class As400Service {
    private static final Logger logger = Logger.getLogger(As400Service.class);
    @Resource(lookup = "java:/jdbc/AS400DS")
    private DataSource dataSource;

    public String testConnection(String server, String user, String password) {
        try {
            AS400 as400 = new AS400(server, user, password);
            as400.validateSignon();
            return "Connessione AS400 riuscita!";
        } catch (Exception e) {
            return "Errore di connessione AS400: " + e.getMessage();
        }
    }

    public String pingDb() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM SYSIBM.SYSDUMMY1")) {
            if (rs.next()) {
                return "DB AS400 raggiungibile: OK";
            } else {
                return "DB AS400 NON risponde come previsto";
            }
        } catch (Exception e) {
            return "Errore ping DB AS400: " + e.getMessage();
        }
    }

    public List<FlatSalesforceAccount> getAccounts(String cursor, int limit) {

        List<FlatSalesforceAccount> accounts = new ArrayList<>();

        String sql = "SELECT DISTINCT(DSCOCP), CDDTCA, NTELCA, SWEBCA, NFAXCA, " +
            "INDICA, LOCACA, PROVCA, CAPOCA, CISOCA, DTMNCA, DT01CA " +
            "FROM CGANA01J " +
            "WHERE (? IS NULL OR DSCOCP > ?) " +
            "ORDER BY DSCOCP ASC " +
            "FETCH FIRST ? ROWS ONLY ";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            logger.info("getAccounts: limit=" + limit + " cursor=" + cursor);

            if (cursor == null) {
                stmt.setNull(1, java.sql.Types.VARCHAR);
                stmt.setNull(2, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(1, cursor);
                stmt.setString(2, cursor);
            }

            stmt.setInt(3, limit);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    FlatSalesforceAccount account = new FlatSalesforceAccount();
                    account.setName(trimString(rs.getString("DSCOCP")));
                    account.setAccountNumber(trimString(rs.getString("CDDTCA")));
                    account.setPhone(trimString(rs.getString("NTELCA")));
                    account.setWebsite(trimString(rs.getString("SWEBCA")));
                    account.setFax(trimString(rs.getString("NFAXCA")));

                    account.setBillingStreet(trimString(rs.getString("INDICA")));
                    account.setBillingCity(trimString(rs.getString("LOCACA")));
                    account.setBillingState(trimString(rs.getString("PROVCA")));
                    account.setBillingPostalCode(trimString(rs.getString("CAPOCA")));
                    account.setBillingCountry(trimString(rs.getString("CISOCA")));

                    account.setShippingStreet(trimString(rs.getString("INDICA")));
                    account.setShippingCity(trimString(rs.getString("LOCACA")));
                    account.setShippingState(trimString(rs.getString("PROVCA")));
                    account.setShippingPostalCode(trimString(rs.getString("CAPOCA")));
                    account.setShippingCountry(trimString(rs.getString("CISOCA")));

                    // Date handling (se necessario)
                    // account.setCreatedDate(parseDate(rs.getString("DT01CA")));
                    // account.setLastModifiedDate(parseDate(rs.getString("DTMNCA")));

                    accounts.add(account);
                }
            }

        } catch (Exception e) {
            logger.error("Errore durante l'esecuzione della query getAccounts", e);
        }

        return accounts;
    }

    public List<SalesforceProduct> getProducts() {
        List<SalesforceProduct> listProducts = new ArrayList<>();

        String sql = "SELECT CDARMA, DSARMA, UMBAMA, TPSTMA, CDALMA, FLMPMA, FLPEMA, TMAPMA FROM MGART00F LIMIT 200 ";

        try (
             Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

             while (rs.next()) {
                    SalesforceProduct product = new SalesforceProduct();
                    product.setProductCode(trimString(rs.getString("CDARMA")));
                    product.setDescription(trimString(rs.getString("DSARMA")));
                    product.setName(product.getDescription());
                    product.setQuantityUnitOfMeasure(trimString(rs.getString("UMBAMA")));
                    //product.setType(trimString(rs.getString("TPSTMA")));
                    product.setStockKeepingUnit(trimString(rs.getString("CDALMA")));
                    //product.setQuantityScheduleEnabled("S".equalsIgnoreCase(rs.getString("FLMPMA")));
                    //product.setCanUseRevenueSchedule("".equalsIgnoreCase(rs.getString("FLPEMA")));
                    //product.setQuantityInstallmentPeriod(rs.getString("TMAPMA"));
                    logger.info("getProducts: " + product.toString() + "");
                    listProducts.add(product);
            }
            return listProducts;

        } catch (Exception e) {
            e.printStackTrace();
         }
        return listProducts;
    }

    public SalesforceAccount getAccount(String code) {

        SalesforceAccount account = new SalesforceAccount();

        String sql = "SELECT DSCOCP AS NAME, CONTCA AS CUSTOMER_CODE, CONTCA AS ERP_KEY, " +
            "PIVACA AS VAT_CODE, CDFICA AS FISCAL_CODE, INELCA AS EMAIL, NTELCA AS PHONE, " +
            "DEBACA AS BANK_NAME, IBANCA AS IBAN, CCABCA AS CAB, CABICA AS ABI " +
            "FROM CGANA01J " +
            "WHERE CONTCA = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            logger.info("getAccount: code " + code);

            if (code == null) {
                stmt.setNull(1, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(1, code);
            }

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    account.setName(trimString(rs.getString("NAME")));
                    account.setCustomerCode__c(trimString(rs.getString("CUSTOMER_CODE")));
                    account.setERPKey__c(trimString(rs.getString("ERP_KEY")));
                    account.setFiscalCode__c(trimString(rs.getString("FISCAL_CODE")));
                    account.setVatNumber__c(trimString(rs.getString("VAT_CODE")));
                    account.setEmail(trimString(rs.getString("EMAIL")));
                    account.setPhone(trimString(rs.getString("PHONE")));
                    account.setBankName__c(trimString(rs.getString("BANK_NAME")));
                    account.setABI__c(trimString(rs.getString("ABI")));
                    account.setCAB__C(trimString(rs.getString("CAB")));
                    account.setIBAN__c(trimString(rs.getString("IBAN")));

                    account.setAreaNielsen__c("");
                    account.setPreferredPaymentMethod__c("");
                    account.setSdiCode__c(getSDI(code));
                    account.setAddresses(getSubAddresses(code));
                }
            }

        } catch (Exception e) {
            logger.error("Errore durante l'esecuzione della query getAccount", e);
        }

        return account;
    }

    public List<SalesforceAddresses> getSubAddresses(String code) {

        List<SalesforceAddresses> addresses = new ArrayList<>();

        String sql = "SELECT CONTCA AS ERP_KEY, DSULCP, INDICA AS STREET, " +
            "LOCACA AS CITY, PROVCA AS PROVINCE, " +
            "CAPOCA AS POSTAL_CODE, CISOCA AS STATE_CODE " +
            "FROM CGANA01J " +
            "WHERE CAGRCA = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            logger.info("getSubAddresses: code " + code);

            if (code == null) {
                stmt.setNull(1, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(1, code);
            }

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    SalesforceAddresses ads = new SalesforceAddresses();
                    SalesforceAddress ad = new SalesforceAddress();

                    ad.setCity(trimString(rs.getString("CITY")));
                    ad.setCountryCode(trimString(rs.getString("STATE_CODE")));
                    ad.setPostalCode(trimString(rs.getString("POSTAL_CODE")));
                    ad.setStreet(trimString(rs.getString("STREET")));
                    ad.setStateCode(trimString(rs.getString("PROVINCE")));

                    ads.setAddress(ad);
                    ads.setERPKey__c(trimString(rs.getString("ERP_KEY")));
                    ads.setName(rs.getString("DSULCP") + " " + ad.getStreet());
                    ads.setLabel(ads.getName());

                    logger.info("getSubAddresses: address name " + ads.getName());
                    logger.info("getSubAddresses: ads " + ads.toString()); addresses.add(ads);

                    addresses.add(ads);
                }
            }

        } catch (Exception e) {
            logger.error("Errore durante l'esecuzione della query getSubAddresses", e);
        }

        return addresses;
    }

    public String getSDI(String code) {
        String sdi = "";
        String sql = "SELECT CODIPA FROM FTPAE55F WHERE CODCLI IN (?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            logger.debug("getSDI: code " + code);
            stmt.setString(1, code);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    sdi = trimString(rs.getString("CODIPA"));
                    logger.debug("getSDI: sdi " + sdi);
                }
            }

        } catch (Exception e) {
            logger.error("Errore durante l'esecuzione della query getSDI per il codice: " + code, e);
        }

        return sdi;
    }

    public PriceBook getPricebook(String code) {
        PriceBook pricebook = new PriceBook();
        String sql = "SELECT * FROM MGLIS00F WHERE CLISML = (?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            logger.debug("getPricebook: code " + code);
            stmt.setString(1, code);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    pricebook.setPriceBookEntries(getPricebookEntryies(code));
                }
            }

        } catch (Exception e) {
            logger.error("Errore durante l'esecuzione della query getPricebook per il codice: " + code, e);
        }

        return pricebook;
    }

    private List<PriceBookEntry> getPricebookEntryies(String code) {
        List<PriceBookEntry> listPricebookEntry = new ArrayList<>();

        String sql = "SELECT CDARML, PRZLML FROM MGLIS01F WHERE DTLVML = '0' AND CLISML = (?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, code);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PriceBookEntry pricebookEntry = new PriceBookEntry();
                    pricebookEntry.setProduct2Id(rs.getString("CDARML")); // TEXT
                    pricebookEntry.setUnitPrice(new BigDecimal(rs.getString("PRZLML"))
                        .setScale(2, RoundingMode.HALF_UP)
                        .toPlainString()); // CURRENCY

                    logger.info("getPricebookEntryies:" + pricebookEntry.toString());
                    listPricebookEntry.add(pricebookEntry);
                }
            }

        } catch (Exception e) {
            logger.error("Errore durante l'esecuzione della query getPricebookEntryies per il codice: " + code, e);
        }

        return listPricebookEntry;
    }

    public List<SalesforceOrderLines> getPreorderOrderLines(String orderId, String lettera) {

        String sql = "SELECT " +
            "R.NROROO NumeroOrdine, " +
            "R.CDAROO CodiceArticolo, " +
            "R.QTOROO QuantitaOrdinata, " +
            "R.PRZUOO PrezzoUnitario," +
            "R.PZNEOO PrezzoNetto, " +
            "R.PZNLOO PrezzoListino, " +
            "R.PZNEOO PrezzoNetto, " +
            "R.VLNMOO ValoreTotaleRiga," +
            "R.SCPMOO Sconto1, " +
            "R.TANOOO Sconto2, " +
            "R.SCCAOO Sconto3, " +
            "R.SCN1OO Sconto4, " +
            "R.PESOOO PesoNetto, " +
            "R.PSLOOO PesoLordo, " +
            "R.VOUNOO VolumeUnitario, " +
            "T.CLISOO CodiceListino, " +
            "T.TDOCOO TipoRigo, " +
            "R.TIMOOO TIPOMOVIMENTO, " +
            "R.LOTMOO LOTTO, " +
            "R.NCOLOO COLLI, " +
            "R.NRBLOO BOLLA," +
            "R.NRRGOO NUMERORIGA " +
            "FROM OCMOV01F R " +
            "INNER JOIN OCMOV00F T " +
            "ON R.NROROO = T.NROROO " +
            "AND R.TDOCOO = T.TDOCOO " +
            "WHERE R.NROROO = ? " +
            "AND R.TDOCOO = ?";

        List<SalesforceOrderLines> allOrderLines = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, orderId);
            pstmt.setString(2, lettera);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    SalesforceOrderLines orderLines = new SalesforceOrderLines();
                    orderLines.setOrderId(orderId);
                    orderLines.setProduct2Id(rs.getString("CODICEARTICOLO"));
                    orderLines.setPricebookEntryId(rs.getString("CODICELISTINO"));
                    orderLines.setQuantity(rs.getString("QUANTITAORDINATA"));
                    orderLines.setUnitPrice(rs.getString("PREZZOLISTINO"));
                    orderLines.setTipoRigo__c(tiporiga(Integer.valueOf(rs.getString("TIPOMOVIMENTO"))));
                    orderLines.setLotto__c(rs.getString("LOTTO"));
                    orderLines.setCollo__c(rs.getString("COLLI"));
                    orderLines.setNumeroRigo__c(rs.getString("NUMERORIGA"));
                    orderLines.setDiscount1__c(rs.getString("SCONTO1"));
                    orderLines.setDiscount2__c(rs.getString("SCONTO2"));
                    orderLines.setDiscount3__c(rs.getString("SCONTO3"));
                    orderLines.setDiscount4__c(rs.getString("SCONTO4"));

                    allOrderLines.add(orderLines);
                }
            }

        } catch (Exception e) {
            logger.error("Errore durante l'esecuzione della query getPreorderOrderLines", e);
            throw new RuntimeException("Errore durante l'esecuzione della query getPreorderOrderLines", e);
        }

        return allOrderLines;
    }

    private String tiporiga(Integer riga) {
        return String.format("%02d", riga);
    }

    public SalesforceOrder getPreorderOrder(String lettera, String numero) {

        String sql = "SELECT " +
            "T.NROROO AS NumeroOrdine, " +
            "T.CDCFOO AS CodiceCliente, " +
            "T.CLISOO AS CodiceListino, " +
            "T.DTOROO AS DataOrdine, " +
            "R.NROFOO AS NumeroOrdinePrenotazione " +
            "FROM OCMOV00F T " +
            "INNER JOIN OCMOV01F R " +
            "ON T.NROROO = R.NROROO " +
            "AND T.TDOCOO = R.TDOCOO " +
            "WHERE T.NROAOO = ? " +
            "AND T.TDOCOO = ? ";

        SalesforceOrder order = new SalesforceOrder();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, numero);
            pstmt.setString(2, lettera);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    order.setAccountId(rs.getString("CODICECLIENTE"));
                    order.setPricebook2Id(rs.getString("CODICELISTINO"));
                    order.setEffectiveDate(
                        convertAs400DateToIso(rs.getString("DATAORDINE"))
                    );
                    order.setDataUltimaConsegna__c(
                        convertAs400DateToIso(rs.getString("DATAORDINE"))
                    );
                    order.setName(rs.getString("NUMEROORDINE"));

                }
            }

        } catch (Exception e) {
            logger.error("Errore durante l'esecuzione della query getPreorderOrder", e);
        }

        return order;
    }

    public String findAccountRecord(String partitaIva) {

        String sql = "SELECT CONTCA FROM CGANA03J WHERE PIVACA = ? AND CONTCA NOT LIKE '%DE%' ";
        String id = null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, partitaIva);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    id = rs.getString("CONTCA");
                    if (rs.next()) {
                        logger.debugf("Trovati più account per partita IVA {}", partitaIva);
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Errore durante l'esecuzione della query findAccountRecord", e);
        }
        return id;
    }

}
