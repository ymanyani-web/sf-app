package it.agrimontana.salesforce.service.database;

import static it.agrimontana.salesforce.Utils.trimString;
import static it.agrimontana.salesforce.Utils.convertAs400DateToIso;
import com.ibm.as400.access.AS400;
import it.agrimontana.salesforce.dto.*;
import it.agrimontana.salesforce.dto.order.SalesforceOrder;
import it.agrimontana.salesforce.dto.order.SalesforceOrderLines;
import it.agrimontana.salesforce.monitoring.MonitoringDAO;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jboss.logging.Logger;

@ApplicationScoped
public class As400Service {
    @Inject
    MonitoringDAO monitoringDAO;

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
        return withAs400Call("As400Service.pingDb", "SYSIBM.SYSDUMMY1:ping", () -> {
            try (Connection conn = dataSource.getConnection();
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM SYSIBM.SYSDUMMY1")) {

                if (rs.next())
                    return "DB AS400 raggiungibile: OK";
                return "DB AS400 NON risponde come previsto";
            } catch (Exception e) {
                return "Errore ping DB AS400: " + e.getMessage();
            }
        });
    }

    public List<FlatSalesforceAccount> getAccounts(String cursor, int limit) {

        return withAs400Call(
                "As400Service.getAccounts",
                "CGANA01J",
                () -> {

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
                            stmt.setNull(1, Types.VARCHAR);
                            stmt.setNull(2, Types.VARCHAR);
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

                                accounts.add(account);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Errore durante l'esecuzione della query getAccounts", e);
                    }

                    return accounts;
                });
    }

    public List<SalesforceProduct> getProducts() {
        return withAs400Call(
                "As400Service.getProducts",
                "MGART00F",
                () -> {
                    List<SalesforceProduct> listProducts = new ArrayList<>();

                    String sql = "SELECT CDARMA, DSARMA, UMBAMA, TPSTMA, CDALMA, FLMPMA, FLPEMA, TMAPMA FROM MGART00F LIMIT 200";

                    try (Connection conn = dataSource.getConnection();
                            Statement stmt = conn.createStatement();
                            ResultSet rs = stmt.executeQuery(sql)) {

                        while (rs.next()) {
                            SalesforceProduct product = new SalesforceProduct();
                            product.setProductCode(trimString(rs.getString("CDARMA")));
                            product.setDescription(trimString(rs.getString("DSARMA")));
                            product.setName(product.getDescription());
                            product.setQuantityUnitOfMeasure(trimString(rs.getString("UMBAMA")));
                            product.setStockKeepingUnit(trimString(rs.getString("CDALMA")));
                            listProducts.add(product);
                        }
                    } catch (Exception e) {
                        logger.error("Errore durante getProducts", e);
                    }

                    return listProducts;
                });
    }

    public SalesforceAccount getAccount(String code) {
        return withAs400Call(
                "As400Service.getAccount",
                "CGANA01J",
                () -> {
                    SalesforceAccount account = new SalesforceAccount();

                    String sql = "SELECT DSCOCP AS NAME, CONTCA AS CUSTOMER_CODE, CONTCA AS ERP_KEY, " +
                            "PIVACA AS VAT_CODE, CDFICA AS FISCAL_CODE, INELCA AS EMAIL, NTELCA AS PHONE, " +
                            "DEBACA AS BANK_NAME, IBANCA AS IBAN, CCABCA AS CAB, CABICA AS ABI " +
                            "FROM CGANA01J " +
                            "WHERE CONTCA = ?";

                    try (Connection conn = dataSource.getConnection();
                            PreparedStatement stmt = conn.prepareStatement(sql)) {

                        logger.info("getAccount: code " + code);

                        if (code == null)
                            stmt.setNull(1, Types.VARCHAR);
                        else
                            stmt.setString(1, code);

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
                                account.setSdiCode__c(getSDI(code)); // already wrapped once you wrap getSDI()
                                account.setAddresses(getSubAddresses(code)); // already wrapped once you wrap
                                                                             // getSubAddresses()
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Errore durante getAccount", e);
                    }

                    return account;
                });
    }

    public List<SalesforceAddresses> getSubAddresses(String code) {
        return withAs400Call(
                "As400Service.getSubAddresses",
                "CGANA01J",
                () -> {
                    List<SalesforceAddresses> addresses = new ArrayList<>();

                    String sql = "SELECT CONTCA AS ERP_KEY, DSULCP, INDICA AS STREET, " +
                            "LOCACA AS CITY, PROVCA AS PROVINCE, " +
                            "CAPOCA AS POSTAL_CODE, CISOCA AS STATE_CODE " +
                            "FROM CGANA01J " +
                            "WHERE CAGRCA = ?";

                    try (Connection conn = dataSource.getConnection();
                            PreparedStatement stmt = conn.prepareStatement(sql)) {

                        logger.info("getSubAddresses: code " + code);

                        if (code == null)
                            stmt.setNull(1, Types.VARCHAR);
                        else
                            stmt.setString(1, code);

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

                                addresses.add(ads);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Errore durante getSubAddresses", e);
                    }

                    return addresses;
                });
    }

    public String getSDI(String code) {
        return withAs400Call(
                "As400Service.getSDI",
                "FTPAE55F",
                () -> {
                    String sdi = "";
                    String sql = "SELECT CODIPA FROM FTPAE55F WHERE CODCLI IN (?)";

                    try (Connection conn = dataSource.getConnection();
                            PreparedStatement stmt = conn.prepareStatement(sql)) {

                        stmt.setString(1, code);

                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next())
                                sdi = trimString(rs.getString("CODIPA"));
                        }
                    } catch (Exception e) {
                        logger.error("Errore getSDI", e);
                    }
                    return sdi;
                });
    }

    public PriceBook getPricebook(String code) {
        return withAs400Call(
                "As400Service.getPricebook",
                "MGLIS00F",
                () -> {
                    PriceBook pricebook = new PriceBook();
                    String sql = "SELECT * FROM MGLIS00F WHERE CLISML = (?)";

                    try (Connection conn = dataSource.getConnection();
                            PreparedStatement stmt = conn.prepareStatement(sql)) {

                        stmt.setString(1, code);

                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                pricebook.setPriceBookEntries(getPricebookEntryies(code)); // wrap below
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Errore getPricebook", e);
                    }

                    return pricebook;
                });
    }

    private List<PriceBookEntry> getPricebookEntryies(String code) {
        return withAs400Call(
                "As400Service.getPricebookEntryies",
                "MGLIS01F",
                () -> {
                    List<PriceBookEntry> list = new ArrayList<>();
                    String sql = "SELECT CDARML, PRZLML FROM MGLIS01F WHERE DTLVML = '0' AND CLISML = (?)";

                    try (Connection conn = dataSource.getConnection();
                            PreparedStatement pstmt = conn.prepareStatement(sql)) {

                        pstmt.setString(1, code);

                        try (ResultSet rs = pstmt.executeQuery()) {
                            while (rs.next()) {
                                PriceBookEntry p = new PriceBookEntry();
                                p.setProduct2Id(rs.getString("CDARML"));
                                p.setUnitPrice(new BigDecimal(rs.getString("PRZLML"))
                                        .setScale(2, RoundingMode.HALF_UP)
                                        .toPlainString());
                                list.add(p);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Errore getPricebookEntryies", e);
                    }

                    return list;
                });
    }

    public List<SalesforceOrderLines> getPreorderOrderLines(String orderId, String lettera) {
        return withAs400Call("As400Service.getPreorderOrderLines", "OCMOV01F/OCMOV00F", () -> {
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
        });
    }

    private String tiporiga(Integer riga) {
        return String.format("%02d", riga);
    }

    public SalesforceOrder getPreorderOrder(String lettera, String numero) {

        return withAs400Call("As400Service.getPreorderOrder", "OCMOV00F/OCMOV01F", () -> {
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
                                convertAs400DateToIso(rs.getString("DATAORDINE")));
                        order.setDataUltimaConsegna__c(
                                convertAs400DateToIso(rs.getString("DATAORDINE")));
                        order.setName(rs.getString("NUMEROORDINE"));

                    }
                }

            } catch (Exception e) {
                logger.error("Errore durante l'esecuzione della query getPreorderOrder", e);
            }

            return order;
        });
    }

    public String findAccountRecord(String partitaIva) {
        return withAs400Call("As400Service.findAccountRecord", "CGANA03J", () -> {
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
        });
    }

    public List<Odps> getOdps(int limit) {

        return withAs400Call("As400Service.getOdps", "PMORD00F", () -> {
        List<Odps> odpsList = new ArrayList<>();

        String sql = "SELECT O.ORPRPO AS odp " +
                "FROM PMORD00F O " +
                "ORDER BY O.ORPRPO DESC " +
                "FETCH FIRST ? ROWS ONLY";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            logger.info("getOdps: limit=" + limit);

            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Odps odp = new Odps();
                    odp.setOdp(trimString(rs.getString("odp")));
                    odpsList.add(odp);
                }
            }

        } catch (Exception e) {
            logger.error("Errore durante l'esecuzione della query getOdps", e);
        }

        return odpsList;
        });
    }

    private interface SqlWork<T> {
        T run() throws Exception;
    }

    private <T> T withAs400Call(String operation, String target, SqlWork<T> work) {
        UUID reqId = it.agrimontana.salesforce.monitoring.MonitoringContext.requestId();
        String corr = it.agrimontana.salesforce.monitoring.MonitoringContext.correlationId();

        long callId = -1;
        System.out.println("[withAs400Call] op=" + operation
        + " target=" + target
        + " reqId=" + reqId
        + " corr=" + corr
        + " user=" + it.agrimontana.salesforce.monitoring.MonitoringContext.username());
        try {
            callId = monitoringDAO.startExternalCall(
                    reqId,
                    corr,
                    "AS400",
                    operation,
                    target,
                    "SQL",
                    null);

            T res = work.run();

            monitoringDAO.finishExternalCall(
                    callId,
                    0, // SQL => no HTTP status; 0 is ok, or null
                    true,
                    null,
                    null,
                    null,
                    null);
            return res;

        } catch (Exception e) {
            try {
                if (callId != -1) {
                    monitoringDAO.finishExternalCall(
                            callId,
                            0,
                            false,
                            null,
                            null,
                            e,
                            null);
                }

                // Optional: also register error signature + event (useful for Errors page)
                // monitoringDAO.upsertErrorSignatureAndInsertEvent(...)

            } catch (Exception ignore) {
                // never break business flow because monitoring failed
            }

            // Keep your existing behavior (either return message or throw)
            throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException(e);
        }
    }
}
