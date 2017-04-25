DROP database IF EXISTS provaProgetto;
CREATE database provaProgetto;
USE provaProgetto;

DROP TABLE IF EXISTS cliente;

CREATE TABLE cliente(
	nome char(20) not null,
    cognome char(20) not null,
    CF char(16) not null primary key,
    luogonascita char(30),
    datanascita date
/*indirizzo char(70)*/
);

DROP TABLE IF EXISTS telefono;

CREATE TABLE telefono(
	numero char(10) not null primary key,
    cliente char(16),
    FOREIGN KEY (cliente) REFERENCES cliente(CF) ON DELETE CASCADE
);

DROP TABLE IF EXISTS conto;

CREATE TABLE conto(
	IBAN char(27) not null primary key,
    saldo double default 0
);

DROP TABLE IF EXISTS libretto;

CREATE TABLE libretto(
	tassoInt double,
    iban char(27),
    FOREIGN KEY (iban) REFERENCES conto(IBAN) ON DELETE CASCADE
);

DROP TABLE IF EXISTS bancoposta;

CREATE TABLE bancoposta(
	tassoInt double,
    servInternet enum('y','n') default 'n',
    costo double default 0,
    carta enum('y','n') default 'n',
	iban char(27),
    FOREIGN KEY (iban) REFERENCES conto(IBAN) ON DELETE CASCADE
);

DROP TABLE IF EXISTS postePay;

CREATE TABLE postePay(
	numCarta int not null UNIQUE AUTO_INCREMENT,
    scadenza date,
    codSicur int not null ,
	iban char(27),
    FOREIGN KEY (iban) REFERENCES conto(IBAN) ON DELETE CASCADE
)AUTO_INCREMENT=100;

DROP TABLE IF EXISTS apre;

CREATE TABLE apre(
	ibanConto char(27),
	clienteCF char(16),
	FOREIGN KEY (clienteCF) REFERENCES cliente(CF) ON DELETE CASCADE, 
    FOREIGN KEY (ibanConto) REFERENCES conto(IBAN)	ON DELETE CASCADE
	
);

DROP TABLE IF EXISTS operazione;

CREATE TABLE operazione(
	codice int  not null AUTO_INCREMENT, 
    importo double not null,
    dataOper datetime DEFAULT now(), 		/* ci vorrebbe current_date() ma mi da errore*/
    tipo enum('sportello', 'onLine') DEFAULT 'sportello',
	PRIMARY KEY (codice)	
)AUTO_INCREMENT=5;

DROP TABLE IF EXISTS dipendenti;

CREATE TABLE dipendenti(
	nome char(20) not null,
    cognome char(20) not null,
	indirizzo char(50) ,
    luogonascita char(30),
    datanascita date,
    tipo enum('postino','addettoSportello'),
	matricola int not null primary key
);

DROP TABLE IF EXISTS effettuatoDa;

CREATE TABLE effettuatoDa(
	codice int,
	dipendente int,
	FOREIGN KEY (dipendente) REFERENCES dipendenti(matricola),
	FOREIGN KEY (codice) REFERENCES operazione(codice)
);

DROP TABLE IF EXISTS posta;

CREATE TABLE posta(
	codice int  not null AUTO_INCREMENT,
	dataSped date not null,
	tipo enum('pacchi', 'lettere', 'raccomandate'),
    dipendente int,
    indirizzo char(30),
    mittente char(20),
    dataCons date,
    PRIMARY KEY (codice),
	FOREIGN KEY (dipendente) REFERENCES dipendenti(matricola)
)AUTO_INCREMENT=10;

DROP TABLE IF EXISTS pacchi;

CREATE TABLE pacchi(
	peso double,
	volume double,
	codice int,
	FOREIGN KEY (codice) REFERENCES posta(codice) ON DELETE CASCADE
);
/*
DROP TABLE IF EXISTS spedisce;

CREATE TABLE spedisce(
	codPosta int,	
	clienteCF char(16),
	FOREIGN KEY (clienteCF) REFERENCES cliente(CF) ON DELETE CASCADE, 
	FOREIGN KEY (codPosta) REFERENCES posta(codice) ON DELETE CASCADE
);
*/
DROP TABLE IF EXISTS interessatoDa;

CREATE TABLE interessatoDa(
	ibanConto char(27),
	codOperazione int,
	FOREIGN KEY (ibanConto) REFERENCES conto(IBAN)	ON DELETE CASCADE,
	FOREIGN KEY (codOperazione) REFERENCES operazione(codice) ON DELETE CASCADE

);
/*****************popoliamo la base di dati******************/
LOAD DATA LOCAL INFILE 'cliente.txt' INTO TABLE cliente FIELDS TERMINATED BY','(nome, cognome, CF, luogonascita,datanascita);
/*manca l'indirizzo*/
LOAD DATA LOCAL INFILE 'dipendenti.txt' INTO TABLE dipendenti FIELDS TERMINATED BY','(nome, cognome, indirizzo, luogonascita, datanascita,matricola,tipo);
LOAD DATA LOCAL INFILE 'conto.txt' INTO TABLE conto FIELDS TERMINATED BY','(IBAN);
LOAD DATA LOCAL INFILE 'bancoposta.txt' INTO TABLE bancoposta FIELDS TERMINATED BY','(iban,tassoInt, servInternet,
costo, carta);
LOAD DATA LOCAL INFILE 'postepay.txt' INTO TABLE postePay FIELDS TERMINATED BY','(iban,scadenza,codSicur);
/*mancano le scritture in cose tipo bancoposta & co*/
LOAD DATA LOCAL INFILE 'apre.txt' INTO TABLE apre FIELDS TERMINATED BY','(ibanConto, clienteCF);
LOAD DATA LOCAL INFILE 'posta.txt' INTO TABLE posta FIELDS TERMINATED BY','(dataSped, dataCons, tipo, dipendente, indirizzo, mittente);
LOAD DATA LOCAL INFILE 'operazioni.txt' INTO TABLE operazione FIELDS TERMINATED BY','(importo, dataOper, tipo);
LOAD DATA LOCAL INFILE 'interessatoDa.txt' INTO TABLE interessatoDa FIELDS TERMINATED BY','(ibanConto, codOperazione);
LOAD DATA LOCAL INFILE 'effettuatoDa.txt' INTO TABLE effettuatoDa FIELDS TERMINATED BY','(codice, dipendente);

/************* operazioni********************/

/*operazione 1*/
/*mi sono dimenticata il telefono*/
INSERT INTO cliente (nome, cognome ,CF, luogonascita, datanascita) VALUES ('Mario', 'Rossi','RSSMRA82B14G813s','Pompei', '1982-02-14');
INSERT INTO telefono(numero,cliente) VALUES ('0819548723','RSSMRA82B14G813s');
/*operazione 2*/
INSERT INTO conto(IBAN , saldo) VALUES ('IT65C256387451245963258749','1000.00');
/*NOTA: verso operazione teniamo 0,N*/
/*NOTA: devo scrivere anche in una tra libretto, bancoposta e postepay perchè e ||*/
INSERT INTO apre(ibanConto , clienteCF) VALUES ('IT65C256387451245963258749','RSSMRA82B14G813s');
INSERT INTO libretto VALUES ('3', 'IT65C256387451245963258749');

/*operazione 3*/
/*NOTA:questo va fatto qua??*/
/*NOTA: qui e dopo devo fare il fatto della ricerca dell'ultima operazione*/
INSERT INTO dipendenti(nome, cognome, indirizzo, luogonascita, datanascita,matricola) VALUES ('Gennaro', 'Bianchi', 'via Spinelli 55, Pompei', 'Scafati', '1970-03-15', '422923');
INSERT INTO operazione (importo) VALUES (-50.00);
UPDATE conto SET saldo=saldo -50 WHERE iban='IT65C256387451245963258749';
INSERT INTO interessatoDa (ibanConto) VALUES ('IT65C256387451245963258749');
INSERT INTO effettuatoDa (codice,dipendente) VALUES ('36','422923');
  
/*operazione 4*/ 
INSERT INTO operazione (importo,tipo) VALUES (+100.00,'onLine');
/*INSERT INTO effettuatoDa (codice,dipendente) VALUES ('6','422923');*/
UPDATE conto SET saldo=saldo +100 WHERE iban='IT65C256387451245963258749';
INSERT INTO interessatoDa (ibanConto, codOperazione) VALUES ('IT65C256387451245963258749', '37');

/*operazione 5*/
UPDATE posta SET dataCons='2016-12-24', dipendente=426854 WHERE codice=12; 

/*operazione 6*/
SELECT importo, dataOper, tipo FROM operazione, interessatoDa WHERE operazione.codice=interessatoDa.codOperazione AND interessatoDa.ibanConto='IT65C256387451245963258749';
SELECT saldo FROM conto WHERE iban='IT65C256387451245963258749';

/*operazione 7*/
SELECT nome, cognome,luogonascita, datanascita,count(*)AS numeroConti 
FROM cliente,apre, conto WHERE apre.ibanConto=conto.IBAN AND apre.clienteCF=cliente.CF AND cliente.CF='RSSMRA82B14G813s';
SELECT numero FROM telefono WHERE telefono.cliente='RSSMRA82B14G813s';

/*operazione 9*/
SELECT  nome, cognome,count(*) ASnumOperazioni FROM cliente,apre,bancoposta,interessatoDa WHERE cliente.CF=apre.clienteCF AND
apre.ibanConto=bancoposta.iban AND apre.ibanConto=interessatoDa.ibanConto
GROUP BY cliente.CF
HAVING count(*)>3; /*50*/

/*operazione 10*/
SELECT nome,cognome
FROM cliente as C, apre AS A
WHERE C.CF=A.clienteCF AND A.ibanConto IN(SELECT IBAN 
	FROM conto AS CO, interessatoDa AS I1,operazione as OP1 
	WHERE CO.IBAN=I1.ibanConto AND I1.codOperazione=OP1.codice AND OP1.dataOper>'2016-10-22' 
	GROUP BY IBAN 
	HAVING count(*) <=0.5*( SELECT count(*) 
		FROM interessatoDa AS I2, operazione AS OP2 
		WHERE I2.ibanConto=CO.IBAN AND OP2.codice=I2.codOperazione AND OP2.dataOper>= '2016-08-022' AND OP2.dataOper<='2016-10-22'));

/*operazione 11*/
/*qui non dovrebbe essere 3*/
(SELECT matricola FROM dipendenti,posta WHERE dipendenti.matricola=posta.dipendente AND posta.dataCons>= '2016-12-10' GROUP BY dipendenti.matricola HAVING count(*)>3
)UNION(
SELECT matricola FROM dipendenti,effettuatoDa, operazione WHERE dipendenti.matricola=effettuatoDa.dipendente AND effettuatoDa.codice=operazione.codice AND operazione.dataOper>'2016-12-10' GROUP BY dipendenti.matricola HAVING count(*)>3);
