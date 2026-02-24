-- VIEWS COMPLETA PER -> anagrafica comuni
CGACO40L
SELECT * FROM P0180DAT.CGACO40L anagrafica FETCH FIRST 10 ROWS ONLY

-- VIEWS COMPLETA PER -> anagrafico cliente/fornitore, note, piano dei conti
CGANA01J
SELECT * FROM P0180DAT.CGANA01J cliente_fornitore FETCH FIRST 10 ROWS ONLY

-- VIEWS COMPLETA PER -> fatturato
SELECT * FROM P0180DAT.CGFAT01L FETCH FIRST 10 ROWS ONLY

-- VIEWS COMPLETA PER -> movimenti contabilita, dati commerciali aggiuntivi cli_for
SELECT * FROM P0180DAT.CGMOV02J FETCH FIRST 10 ROWS ONLY

-- VIEWS COMPLETA PER -> piano dei conti, anagrafico clienti fornitori e note
SELECT * FROM P0180DAT.CGPCO02L FETCH FIRST 10 ROWS ONLY

-- VIEWS COMPLETA PER -> file testate fatturazione, file righe fatturazione, anagrafica articoli - dati commerciali
SELECT * FROM P0180DAT.FTMOV06J FETCH FIRST 10 ROWS ONLY

-- VIEWS COMPLETA PER -> file testate ordini fornitori, file testate ordini fornitori estensione, file righe ordini fornitori
SELECT * FROM P0180DAT.GABKMA0J FETCH FIRST 10 ROWS ONLY

-- VIEWS COMPLETA PER -> righe offerte clienti dati aggiuntivi, file righe offerte clienti
SELECT * FROM P0180DAT.OFMOV02J FETCH FIRST 10 ROWS ONLY 

-- VIEWS COMPLETA PER -> file anagrafico di lotti
SELECT * FROM P0180DAT.RLAN103L FETCH FIRST 10 ROWS ONLY

-- VIEWS COMPLETA PER -> testata spedizioni documenti
SELECT * FROM P0180DAT.SDDOC00L FETCH FIRST 10 ROWS ONLY

-- VIEWS COMPLETA PER -> dettaglio ordini per scelta spedizione, file righe ordini clienti, righe ordini clienti dati aggiuntivi, File Anagrafico Articoli Dati Aggiuntivi, Anagrafica Articoli / Dati Commerciali, File Anagrafico Articoli Dati Vini
-- sembra escludere alcuni campi comunque da capire
SELECT * FROM P0180DAT.SPBKR00J FETCH FIRST 10 ROWS ONLY

-- VIEWS COMPLETA PER -> File Testate Listini Clienti / Prezzari Fornitori
SELECT * FROM P0180DAT.MGLIS00L FETCH FIRST 10 ROWS ONLY

-- VIEWS COMPLETA PER -> File Dettaglio Listini Clienti / Prezzari Fornit.
SELECT * FROM P0180DAT.MGLIS01F FETCH FIRST 10 ROWS ONLY

-- VIEWS COMPLETA PER -> usato per il web contiene: File Dettaglio Listini Clienti / Prezzari Fornit, File Anagrafico Articoli, 
SELECT * FROM P0180DAT.WBCATEBGJ FETCH FIRST 10 ROWS ONLY


-- VIEWS COMPLETA PER -> file testate ordini clienti, file testate ordini clienti estensione, file righe ordini clienti
OCMOVA0J
SELECT * FROM P0180DAT.OCMOVA0J ordine WHERE ordine.NROROO = '1601155' -- tabella presente

-- VIEWS COMPLETA PER -> file testate fatturazione, file testate fatturazione estensione, file righe fatturazione
FTBKMA0J
SELECT * FROM P0180DAT.FTBKMA0J FETCH FIRST 10 ROWS ONLY
SELECT * FROM P0180DAT.FTBKMA0J fattura WHERE ordine.NROROO = '1601155' -- tabella presente

-- VIEWS COMPLETA PER -> File work per stampa listini Dettaglio
SELECT * FROM P0180DAT.MGLIS03W FETCH FIRST 10 ROWS ONLY --VIEW VUOTA


-- TABELLE PER File Anagrafico Listini Promoz.Comm. Testate
SELECT * FROM P0180DAT.MGLPC00F FETCH FIRST 10 ROWS ONLY

-- TABELLE PER File Testate Ordini Clienti estensione
OCBKAV801
SELECT * FROM P0180DAT.OCBKAV801 FETCH FIRST 10 ROWS ONLY -- tabella presente

OCBKMA0F
SELECT * FROM P0180DAT.OCBKMA0F FETCH FIRST 10 ROWS ONLY -- tabella presente

OCMOVAV801
SELECT * FROM P0180DAT.OCMOVAV801 FETCH FIRST 10 ROWS ONLY -- tabella presente

OCMOVA0F
SELECT * FROM P0180DAT.OCMOVA0F FETCH FIRST 10 ROWS ONLY -- tabella presente



-- TABELLE PER File Testate Ordini Clienti
OCBKM00F
SELECT * FROM P0180DAT.OCBKM00F FETCH FIRST 10 ROWS ONLY -- tabella vuota

OCMOV_1205
SELECT * FROM P0180DAT.OCMOV_1205 FETCH FIRST 10 ROWS ONLY -- tabella presente

OCMOV00F
SELECT * FROM P0180DAT.OCMOV00F FETCH FIRST 10 ROWS ONLY -- tabella presente STO USANDO QUESTA

OCMOV00WW
SELECT * FROM P0180DAT.OCMOV00WW FETCH FIRST 10 ROWS ONLY -- tabella vuota



-- TABELLE PER File Testate Ordini Clienti per I.R
OCMOVR00F
SELECT * FROM P0180DAT.OCMOVR00F FETCH FIRST 10 ROWS ONLY -- tabella vuota



-- TABELLE PER File Testate Ordini Clienti estensione per I.R.
OCMOVRA0F
SELECT * FROM P0180DAT.OCMOVRA0F FETCH FIRST 10 ROWS ONLY -- tabella vuota

OCMOVRA801
SELECT * FROM P0180DAT.OCMOVRA801 FETCH FIRST 10 ROWS ONLY -- tabella vuota



-- TABELLE PER File Testate Fatturazione
FTMOV00F
SELECT * FROM P0180DAT.FTMOV00F FETCH FIRST 10 ROWS ONLY -- tabella presente



-- TABELLE PER File Righe Fatturazione
FTMOV01F
SELECT * FROM P0180DAT.FTMOV01F FETCH FIRST 10 ROWS ONLY -- tabella presente

FTBKM01F
SELECT * FROM P0180DAT.FTBKM01F FETCH FIRST 10 ROWS ONLY -- tabella presente


-- cliente/fornitore
SELECT 
	cliente.CABSCA AS Codice_CAB_Banca_Sconto, 
	cliente.CSPECA AS Codice_Cl_For_Sped, 
	cliente.CCORCA AS Codice_Cl_For_Corrispondente, 
	cliente.CDCSCA AS Cod_Destinazione, 
	cliente.TPLICA AS Tipo_Listino
FROM P0180DAT.CGANA01J cliente 
	FETCH FIRST 10 ROWS ONLY

-- testa ordini
SELECT 
	testa_ordine.CDDTOO AS Codice_ditta,
	testa_ordine.CDCFOO AS Codice_cliente_fornitore,
	testa_ordine.CSPEOO AS Codice_c_f_spedizioni,
	testa_ordine.CDPAOO AS Codice_pagamento,
	testa_ordine.CLISOO AS Codice_listino,
	testa_ordine.CDAGOO AS Codice_agente,
	testa_ordine.CDIMOO AS Codice_imballo,
	testa_ordine.CDCNOO AS Codice_consegna,
	testa_ordine.CDSPOO AS Codice_spedizione,
	testa_ordine.CDDEOO AS Codice_magazzino,
	testa_ordine.CDCSOO AS Codice_destinazione
FROM P0180DAT.OCMOV00F testa_ordine
FETCH FIRST 10 ROWS ONLY	
	


-- clienti/fornitori legami capogruppi (cosa vorrà dire?)
SELECT
	legame_gruppo.CONTLG AS giroconoto_utente, 
	legame_gruppo.DIVILG AS divisione, 
	legame_gruppo.CAGRLG AS codice_capogruppo, 
	legame_gruppo.DTINLG AS data_inizio, 
	legame_gruppo.DTFILG AS data_fine, 
FROM P0180DAT.CGALG00F legame_gruppo FETCH FIRST 10 ROWS ONLY


-- anagrafica clienti/fornitore e note
SELECT 
	anagrafica.SWEBCA AS indirizzo_web, 
	anagrafica.NOTECA AS note, 
	anagrafica.CSPECA AS codice_cli_for_spedizione, 
	anagrafica.CDCSCA AS codice_destinazione, 
	anagrafica.COAQCA AS conto_acquisti, 
	anagrafica.CCORCA AS codice_cli_for_corrispondente, 
	anagrafica.CNCOCA AS numero_conto_corrente, 
	anagrafica.CAGRCA AS gr_conto_sott_capo_gruppo
FROM P0180DAT.CGANA00F anagrafica 
FETCH FIRST 10 ROWS ONLY


-- trova righe ordine del cliente
SELECT  
	anag_cliente.PROFCA AS utente,
	anag_cliente.CONTCA AS gr_conto_sottoconto,
	anag_cliente.SIGLCP AS sigla,
	anag_cliente.INDICA AS indirizzo,
	ordini_riga.CDC1FM AS OR_codice_cliente_fornitore,
	ordini_riga.INDIFM AS OR_indirizzo,
	ordini_riga.LOCAFM AS OR_localita,
	ordini_riga.NRORFM AS OR_numero_ordine,
	ordini_riga.NRGOFM AS OR_numero_riga,
	ordini_riga.CDARFM AS OR_codice_articolo
FROM P0180DAT.CGANA01J anag_cliente 
	INNER JOIN P0180DAT.FTBKM01J ordini_riga
	ON anag_cliente.CONTCA = ordini_riga.CDC1FM
FETCH FIRST 10 ROWS ONLY



-- TROVA ARTICOLI CHE FANNO PARTE DI RIGHE ORDINE E AGGIUNGI CLIENTE
SELECT
    anag_cliente.PROFCA AS utente,
    anag_cliente.CONTCA AS gr_conto_sottoconto,
    anag_cliente.SIGLCP AS sigla,
    anag_cliente.INDICA AS indirizzo,
    ordini_riga.CDC1FM AS OR_codice_cliente_fornitore,
    ordini_riga.INDIFM AS OR_indirizzo,
    ordini_riga.LOCAFM AS OR_localita,
    ordini_riga.NRORFM AS OR_numero_ordine,
    ordini_riga.NRGOFM AS OR_numero_riga,
    ordini_riga.CDARFM AS OR_codice_articolo,
    articoli.DSARMA AS AR_descrizione_articolo
FROM P0180DAT.CGANA01J anag_cliente
INNER JOIN P0180DAT.FTBKM01J ordini_riga
    ON anag_cliente.CONTCA = ordini_riga.CDC1FM
INNER JOIN P0180DAT.MGART00F articoli
    ON articoli.CDARMA = ordini_riga.CDARFM
FETCH FIRST 10 ROWS ONLY;


-- TROVA ORDINE CON TUTTE LE RIGHE DELL'ORDINE
SELECT 
	ordine.NROROO AS or_numero_ordine,
	righe_ordine.NRORFM AS rg_numero_ordine,
	ordine.NRRGOO AS or_numero_riga_ordine,
	ordine.CDCFOO AS or_ordine_codice_cliente,
	ordine.TDOCOO AS or_tipo_documento,
	righe_ordine.NRGOFM AS rg_numero_riga,
	righe_ordine.TDOCFM AS rg_tipo_documento,
	ordine.CLISOO AS or_codice_listino,
	righe_ordine.CLISFM AS rg_codice_listino,
	righe_ordine.NRBOFM AS rg_numero_bolla,
	righe_ordine.RIGAFM AS rg_numero_riga_fm,
	ordine.DTOROO AS or_data_ordine,
	ordine.CSPEOO AS or_codice_cli_for_spedizioni,
	ordine.CDPAOO AS or_codice_pagamento,
	ordine.CDAGOO AS or_codice_agente,
	righe_ordine.DSARFM AS rg_codice_desc_articolo,	
	righe_ordine.TPORFM AS rg_tipo_documento,
	righe_ordine.CDCOFM AS rg_codice_commessa,
	righe_ordine.DTBOFM AS rg_data_bolla,
	righe_ordine.LOTMFM AS rg_lotto_partita,
	righe_ordine.NRDFFM AS rg_numero_docu_fattura,
	righe_ordine.CTGMFM AS rg_codice_estensione,
	righe_ordine.INDIFM AS rg_indirizzo,
	righe_ordine.CAPDFM AS rg_cap_destinazione,
	righe_ordine.LOCAFM AS rg_localita,
	righe_ordine.PROVFM AS rg_provincia
FROM P0180DAT.OCMOV00F ordine
	INNER JOIN P0180DAT.FTBKM01J righe_ordine
		ON ordine.NROROO = righe_ordine.NRORFM
WHERE ordine.NROROO = '9920001'


-- TROVA SINGOLO ORDINE CON RIGHE
SELECT * FROM P0180DAT.OCMOV00F ordine
	INNER JOIN P0180DAT.FTBKM01J righe_ordine
		ON ordine.NROROO = righe_ordine.NRORFM
WHERE ordine.NROROO = '9920001' AND ordine.TDOCOO = 'E'


-- testata offerta clienti con cliente
SELECT 
	testa_offerta_clienti.TDOCOF AS or_tipo_documento,
	testa_offerta_clienti.NROROF AS or_numero_offerta,
	testa_offerta_clienti.NRRGOF AS or_numero_riga,
	testa_offerta_clienti.CDCFOF AS or_codice_cli_for,
	testa_offerta_clienti.INDIOF AS or_indirizzo,
	cliente.INDICA AS cliente_indirizzo,
	testa_offerta_clienti.CAPOF AS or_cap_destinazione,
	cliente.CAPOCA AS cliente_cap,
	testa_offerta_clienti.LOCAOF AS or_localita,
	cliente.LOCACA AS cliente_localita,
	testa_offerta_clienti.DTOROF AS or_data_offerta,
	testa_offerta_clienti.CSPEOF AS or_codice_cli_for_spedizione,
	testa_offerta_clienti.CDPAOF AS or_codice_pagamento,
	testa_offerta_clienti.SCCLOF AS or_sconto_cli_for,
	testa_offerta_clienti.SCPROF AS or_sconto_partita,
	testa_offerta_clienti.SCPAOF AS or_sconto_pagamento,
	testa_offerta_clienti.CLISOF AS or_codice_listino,
	testa_offerta_clienti.CLI2OF AS or_codice_listino_rif_2,
	testa_offerta_clienti.CDIMOF AS or_codice_imballo,
	testa_offerta_clienti.CDCNOF AS or_codice_consegna,
FROM P0180DAT.OFMOV00F testa_offerta_clienti 
	INNER JOIN P0180DAT.CGANA01J cliente 
	ON testa_offerta_clienti.CDCFOF = cliente.CONTCA
WHERE cliente.CONTCA = '0220684355'
FETCH FIRST 10 ROWS ONLY








SELECT 
    cj.CDDTCA as COD_DITTA_CLIENTI,
    m.CDDTMA as COD_DITTA_ARTICOLI,
    w.CDDTOO as COD_DITTA_ORDINI,
    cj.CONTCA as COD_CLIENTE,
    cj.DSCOCP as RAGIONE_SOCIALE,
    m.CDARMA as COD_ARTICOLO,
    m.DSARMA as DESCRIZIONE_ARTICOLO
FROM P0180DAT.CGANA01J cj
INNER JOIN P0180DAT.MGART00F m 
    ON m.CDDTMA = cj.CDDTCA
LEFT JOIN P0180DAT.WBORIPYL w 
    ON w.CDDTOO = cj.CDDTCA
WHERE cj.CDDTCA = m.CDDTMA 
    AND (w.CDDTOO IS NULL OR w.CDDTOO = cj.CDDTCA)
FETCH FIRST 5 ROWS ONLY;



SELECT
    CLIENTE.*,
    ANAG_SEDE_AMMINISTRATIVA.*
FROM P0180DAT.CGANA01J AS CLIENTE
    INNER JOIN P0180DAT.CGANA01W AS ANAG_SEDE_AMMINISTRATIVA
        ON CLIENTE.CDDTCA = ANAG_SEDE_AMMINISTRATIVA.CDDTCB
    WHERE CLIENTE.CLFOCP = 'C'
    FETCH FIRST 4 ROWS ONLY
    
    
SELECT
    CLIENTE.*,
    CISO_PIVA.*
FROM P0180DAT.CGANA01J AS CLIENTE
    INNER JOIN CGANA02J AS CISO_PIVA
        ON CLIENTE.CAGRCA = CISO_PIVA.CONTCA
    WHERE CLIENTE.CLFOCP = 'C'
    FETCH FIRST 4 ROWS ONLY
    
    
    

SELECT * FROM P0180DAT.CGACO00F cf FETCH FIRST 5 ROWS ONLY -- ANAGRAFICA COMUNI
SELECT * FROM P0180DAT.CGADE00F cf FETCH FIRST 5 ROWS ONLY -- ANAGRAGICA DESTINATARI VUOTO

SELECT *
FROM (
	SELECT * FROM SPTES00F
	UNION ALL
	SELECT * FROM SPBKT00F
) FETCH FIRST 5 ROWS ONLY


SELECT * FROM CGANA01J FETCH FIRST 10 ROWS ONLY

SELECT * FROM CGANA02F FETCH FIRST 10 ROWS ONLY


SELECT * FROM CGANA01J 
	INNER JOIN CGANA02F 
	ON CGANA01J.CONTCA = CGANA02F.CONTAC
	WHERE CGANA01J.CLFOCP = 'C'
FETCH FIRST 10 ROWS ONLY


SELECT * FROM CGACC01L FETCH FIRST 10 ROWS ONLY

SELECT * FROM CSTAB FETCH FIRST 10 ROWS ONLY
	
	
SELECT * FROM CGANA01J cliente
	INNER JOIN CGANA03F cdfi ON cliente.CONTCA = cdfi.CONTAD
	FETCH FIRST 5 ROWS ONLY
	
	


	







-- dati aggiuntivi ordini cliente (NO LVLCHK)
SELECT  
	dati_aggiuntivi.NRORO8 AS numero_ordine,
	dati_aggiuntivi.NRRGO8 AS numero_riga,
	dati_aggiuntivi.NSRGO8 AS numero_sotto_riga,
	dati_aggiuntivi.PRGRO8 AS prezzo_grado_val_base,
	dati_aggiuntivi.GRMDO8 AS grado_medio_prev,
	dati_aggiuntivi.GRECO8 AS grado_minimo
FROM P0180DAT.GAMOV08FOC dati_aggiuntivi FETCH FIRST 10 ROWS ONLY -- TABELLA PRESENTE



SELECT * FROM P0180DAT.CGANA01J cj 
WHERE cj.CONTCA = "0220157756"

SELECT * FROM P0180DAT.FTBKM01J fk FETCH FIRST 10 ROWS ONLY


-- trova l'ordine del cliente
SELECT * FROM P0180DAT.CGANA01J cj FETCH FIRST 10 ROWS ONLY
SELECT * FROM P0180DAT.FTBKM01J fk WHERE fk.cdc1fm = 0220005067 FETCH FIRST 10 ROWS ONLY












-- File Testate Offerte Clienti
SELECT * FROM P0180DAT.OFMOV00F FETCH FIRST 10 ROWS ONLY --TABELLA PRESENTE




SELECT * FROM P0180DAT.CGANA01J cliente WHERE cliente.CONTCA = '0220684355' FETCH FIRST 10 ROWS ONLY

SELECT * FROM P0180DAT.CGANA01J cliente FETCH FIRST 10 ROWS ONLY



-- TROVA ORDINE PRINCIPALE CON ASSOCIATO LE RIGHE ORDINE E ARTICOLI
SELECT * FROM P0180DAT.MGART00F INNER JOIN P0180DAT.FTBKM01J ON 

SELECT * FROM P0180DAT.WBOTEP0F FETCH FIRST 10 ROWS ONLY -- TABELLA FASULLA

SELECT * FROM P0180DAT.OCBKAV801 ordine_estensione FETCH FIRST 10 ROWS ONLY

SELECT * FROM P0180DAT.OCMOV00F ordine WHERE ordine.NROROO = '9500001'
SELECT * FROM P0180DAT.OCMOV00F ordine FETCH FIRST 100 ROWS ONLY





--FETCH FIRST 10 ROWS ONLY



	


-- riferimento ordine fattura elettronica (ANCHE LA TABELLA FTXCO00F STESSA ROBA)
SELECT * FROM CXXCO00F FETCH FIRST 10 ROWS ONLY

-- TROVA ARTICOLI TUTTI 
SELECT * FROM P0180DAT.MGART00F

-- TROVA ARTICOLI CHE APPARTENGONO A RIGHE ORDINE
SELECT * FROM P0180DAT.MGART00F articoli INNER JOIN P0180DAT.FTBKM01J ordini_riga ON articoli.CDARMA = ordini_riga.CDARFM FETCH FIRST 10 ROWS ONLY


-- tutti gli ordini
SELECT * FROM OCMOV00F FETCH FIRST 10 ROWS ONLY -- TABELLA PRESENTE

SELECT * FROM OCMOV00F 
	INNER JOIN 

	
SELECT * FROM P0180DAT.OCSKT00F FETCH FIRST 10 ROWS ONLY -- TABELLA VUOTA
SELECT * FROM P0180DAT.OCSKT01F FETCH FIRST 10 ROWS ONLY -- TABELLA VUOTA
SELECT * FROM P0180DAT.OCSKT01F1 FETCH FIRST 10 ROWS ONLY -- TABELLA VUOTA

SELECT * FROM OCBKM01F FETCH FIRST 10 ROWS ONLY -- TABELLA VUOTA

-- Lancio produzione - Testate ordine cliente
SELECT * FROM PKCOM01F FETCH FIRST 10 ROWS ONLY -- TABELLA VUOTA

-- File Esplosione Ordine di Produzione
SELECT * FROM PMESP00F FETCH FIRST 10 ROWS ONLY -- TABELLA VUOTA

-- Fido clienti
SELECT * FROM CGFID00F FETCH FIRST 10 ROWS ONLY --TABELLA PRESENTE

-- Fido Work Clienti
SELECT * FROM CGFID00W FETCH FIRST 10 ROWS ONLY --TABELLA VUOTA



--  Anagrafico Clienti/Fornitori e Note
SELECT * FROM CGANA_0105 FETCH FIRST 10 ROWS ONLY --TABELLA PRESENTE

-- Anagrafico Clienti/Fornitori e Note
SELECT * FROM CGANAX0F FETCH FIRST 10 ROWS ONLY --TABELLA PRESENTE

-- Anagrafico Clienti/Fornitori e Note
SELECT * FROM CGANA00F FETCH FIRST 10 ROWS ONLY --TABELLA PRESENTE

-- ?

-- Anagrafico Clienti/Fornitori e Note-File di Lavoro
SELECT * FROM CGANA00W FETCH FIRST 10 ROWS ONLY -- TABELLA PRESENTE


-- Anagrafico Proposte Clienti/Fornitori
SELECT * FROM CGANP00F FETCH FIRST 10 ROWS ONLY -- TABELLA PRESENTE


-- tre query per Anagrafico Indicatori Analisi Rating Clienti
SELECT * FROM CGARCX0F FETCH FIRST 10 ROWS ONLY -- TABELLA PRESENTE
SELECT * FROM CGARC0V801 FETCH FIRST 10 ROWS ONLY -- TABELLA VUOTA
SELECT * FROM CGARC00F FETCH FIRST 10 ROWS ONLY -- TABELLA VUOTA


-- Anagrafico CLIENTI (Unicode) SCALA->GALILEO
SELECT * FROM CGCLI00G FETCH FIRST 10 ROWS ONLY -- TABELLA VUOTA

-- Elenchi Clienti e Fornitori COMPLETO
SELECT * FROM CGFEL00F FETCH FIRST 10 ROWS ONLY -- TABELLA VUOTA

-- Elenchi Clienti e Fornitori - 0 TESTA
SELECT * FROM CGFE000F FETCH FIRST 10 ROWS ONLY --TABELLA VUOTA

-- Elenchi Clienti e Fornitori - 1 DETTAG. CLIENTI
SELECT * FROM CGFE100F FETCH FIRST 10 ROWS ONLY --TABELLA VUOTA 

-- Anagrafico Proposte di codifica Clienti/Fornitori
SELECT * FROM CGANP00W FETCH FIRST 10 ROWS ONLY -- TABELLA VUOTA

-- entrata ordini clienti
SELECT * FROM P0180DAT.FFFLU01L FETCH FIRST 10 ROWS ONLY -- TABELLA VUOTA

-- fatturazioni clienti (NO LVLCHK)
SELECT * FROM P0180DAT.FTMOV11FMG FETCH FIRST 10 ROWS ONLY -- TABELLA VUOTA

-- File Informazioni/Segnalazioni Clienti
SELECT * FROM ASNOT00F FETCH FIRST 10 ROWS ONLY -- TABELLA VUOTA

-- File Comodo Fatturazione Clienti (la 01W vuota pure)
SELECT * FROM AVFAT00W FETCH FIRST 10 ROWS ONLY -- TABELLA VUOTA

-- Anagrafico Aggiuntivo Clienti per Cauzioni
SELECT * FROM CAANA00F FETCH FIRST 10 ROWS ONLY -- TABELLA VUOTA

-- Statistiche Ordini Clienti per esportazione su PC
SELECT * FROM CEOPC00F FETCH FIRST 10 ROWS ONLY -- TABELLA VUOTA






-- viste
SELECT * FROM P0180DAT.FFFLU01L FETCH FIRST 10 ROWS ONLY -- VIEW vuota
SELECT * FROM P0180DAT.CAANA01L FETCH FIRST 10 ROWS ONLY -- VIEW vuota 
SELECT * FROM P0180DAT.CAANA02L FETCH FIRST 10 ROWS ONLY -- VIEW vuota
SELECT * FROM P0180DAT.CAMOV01L FETCH FIRST 10 ROWS ONLY -- VIEW vuota
SELECT * FROM P0180DAT.CAMOV02L FETCH FIRST 10 ROWS ONLY -- VIEW vuota
SELECT * FROM AGM80DAT.CAMOV03L FETCH FIRST 10 ROWS ONLY -- VIEW vuota
SELECT * FROM AGM80DAT.CAMOV04L FETCH FIRST 10 ROWS ONLY -- VIEW vuota
SELECT * FROM AGM80DAT.CAMOV05L FETCH FIRST 10 ROWS ONLY -- VIEW vuota
SELECT * FROM AGM80DAT.CAMOV06L FETCH FIRST 10 ROWS ONLY -- VIEW vuota
SELECT * FROM AGM80DAT.CEPPC00L FETCH FIRST 10 ROWS ONLY -- VIEW vuota
SELECT * FROM AGM80DAT.CEPPC00L FETCH FIRST 10 ROWS ONLY -- VIEW vuota
SELECT * FROM P0180DAT.CEPPC01L FETCH FIRST 10 ROWS ONLY -- VIEW VUOTA
SELECT * FROM P0180DAT.CEPPF00L FETCH FIRST 10 ROWS ONLY -- VIEW VUOTA
SELECT * FROM P0180DAT.CEPPF01L FETCH FIRST 10 ROWS ONLY -- VIEW VUOTA
SELECT * FROM P0180DAT.CGACC01L FETCH FIRST 10 ROWS ONLY -- VIEW VUOTA
SELECT * FROM P0180DAT.CGACC02L FETCH FIRST 10 ROWS ONLY -- VIEW VUOTA
SELECT * FROM P0180DAT.CGANPW1L FETCH FIRST 10 ROWS ONLY -- VIEW VUOTA
SELECT * FROM P0180DAT.CGAHB01L FETCH FIRST 10 ROWS ONLY -- VIEW VUOTA
SELECT * FROM P0180DAT.CGAHB02L FETCH FIRST 10 ROWS ONLY -- VIEW VUOTA
SELECT * FROM P0180DAT.CGALA01L FETCH FIRST 10 ROWS ONLY -- VIEW VUOTA
SELECT * FROM P0180DAT.CGALP01L FETCH FIRST 10 ROWS ONLY -- VIEW VUOTA
SELECT * FROM P0180DAT.CGARC01L FETCH FIRST 10 ROWS ONLY -- VIEW VUOTA
SELECT * FROM P0180DAT.CGAZA01L FETCH FIRST 10 ROWS ONLY -- VIEW VUOTA
SELECT * FROM P0180DAT.OCBKMA0J FETCH FIRST 10 ROWS ONLY -- VIEW VUOTA



SELECT * FROM P0180DAT.CGACO40L FETCH FIRST 10 ROWS ONLY -- VIEW PRESENTE
SELECT * FROM P0180DAT.CGALG01L FETCH FIRST 10 ROWS ONLY -- VIEW PRESENTE
SELECT * FROM P0180DAT.CGALG02L FETCH FIRST 10 ROWS ONLY -- VIEW PRESENTE
SELECT * FROM P0180DAT.CGANA01J FETCH FIRST 10 ROWS ONLY -- VIEW PRESENTE
SELECT * FROM P0180DAT.CGANA01L FETCH FIRST 10 ROWS ONLY -- VIEW PRESENTE
SELECT * FROM P0180DAT.CGANA02J FETCH FIRST 10 ROWS ONLY -- VIEW PRESENTE
SELECT * FROM P0180DAT.CGANA02L FETCH FIRST 10 ROWS ONLY -- VIEW PRESENTE
SELECT * FROM P0180DAT.CGANA03J FETCH FIRST 10 ROWS ONLY -- VIEW PRESENTE
SELECT * FROM P0180DAT.CGANA03L FETCH FIRST 10 ROWS ONLY -- VIEW PRESENTE
SELECT * FROM P0180DAT.CGANA04L FETCH FIRST 10 ROWS ONLY -- VIEW PRESENTE
SELECT * FROM P0180DAT.CGANA05L FETCH FIRST 10 ROWS ONLY -- VIEW PRESENTE
SELECT * FROM P0180DAT.CGANA06L FETCH FIRST 10 ROWS ONLY -- VIEW PRESENTE
SELECT * FROM P0180DAT.CGANR01L FETCH FIRST 10 ROWS ONLY -- VIEW PRESENTE
SELECT * FROM P0180DAT.CGANR02L FETCH FIRST 10 ROWS ONLY -- VIEW PRESENTE
SELECT * FROM P0180DAT.CGANR03L FETCH FIRST 10 ROWS ONLY -- VIEW PRESENTE
SELECT * FROM P0180DAT.CGANR04L FETCH FIRST 10 ROWS ONLY -- VIEW PRESENTE



SELECT
	testa_ordine.CDCFOO
FROM P0180DAT.OCMOV00F testa_ordine
	INNER JOIN CGANA01J clienti
	ON testa_ordine.CDCFOO = clienti.CSPECA
FETCH FIRST 10 ROWS ONLY
	




SELECT * FROM P0180DAT.CGALG00F FETCH FIRST 10 ROWS ONLY

	
	
SELECT 
	cliente.CABSCA,cliente.CSPECA, cliente.CCORCA, cliente.CDCSCA, testa_ordine_cliente.CDCAOO, testa_ordine_cliente.CDCFOO, testa_ordine_cliente.FLINOO FROM OCMOV00F testa_ordine_cliente
	INNER JOIN P0180DAT.CGANA01J cliente 
	ON cliente.CABSCA = testa_ordine_cliente.CDCAOO
	FETCH FIRST 10 ROWS ONLY
	
	
SELECT * FROM P0180DAT.CGANA01J FETCH FIRST 10 ROWS ONLY

SELECT * FROM P0180DAT.OCMOV00F WHERE CDCAOO FETCH FIRST 10 ROWS ONLY 
   
--0441800005
    
    
    
    
    
    