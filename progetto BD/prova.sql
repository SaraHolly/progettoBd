DROP database IF EXISTS provaProgetto;
CREATE database provaProgetto;
USE provaProgetto;

DROP TABLE IF EXISTS cliente;

CREATE TABLE cliente(
	nome char(20) not null,
    cognome char(20) not null,
    CF char(16) primary key,
    luogonascita char(30),
    datanascita date,
	indirizzo char(70)
);

DROP TABLE IF EXISTS telefono;

CREATE TABLE telefono(
	numero char(10) primary key,
    cliente char(16),
    FOREIGN KEY (cliente) REFERENCES cliente(CF) ON DELETE CASCADE
);

DROP TABLE IF EXISTS conto;

CREATE TABLE conto(
	IBAN char(27) primary key,
    saldo double not null
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
    dataScad date not null,/*potremmo definirla in termini di adesso??*/
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
	matricola int primary key
);

DROP TABLE IF EXISTS allaPosta;

CREATE TABLE allaPosta(
	codice int,
	dipendente int,
	FOREIGN KEY (dipendente) REFERENCES dipendenti(matricola),
	FOREIGN KEY (codice) REFERENCES operazione(codice)
);

DROP TABLE IF EXISTS posta;

CREATE TABLE posta(
	codice int  primary key,
	dataSped date not null,
	tipo enum('pacchi', 'lettere', 'raccomandate'),
    dipendente int,
    destinatario char(16),
	FOREIGN KEY (dipendente) REFERENCES dipendenti(matricola),
	FOREIGN KEY (destinatario) REFERENCES cliente(CF) ON DELETE CASCADE 

);

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
LOAD DATA LOCAL INFILE 'dipendenti.txt' INTO TABLE dipendenti FIELDS TERMINATED BY','(nome, cognome, indirizzo, luogonascita, datanascita,matricola);

/************* operazioni********************/

/*operazione 1*/
INSERT INTO cliente (nome, cognome ,CF, luogonascita,datanascita, indirizzo) VALUES ('Mario', 'Rossi','RSSMRA82B14G813s','Pompei', '1982-02-14', 'via Spinelli 23 , Pompei');

/*operazione 2*/
INSERT INTO conto(IBAN , saldo) VALUES ('IT65C256387451245963258749','1000.00');
/*NOTA: verso operazione teniamo 0,N*/
/*NOTA: devo scrivere anche in una tra libretto, bancoposta e postepay perchè e ||*/
INSERT INTO apre(ibanConto , clienteCF) VALUES ('IT65C256387451245963258749','RSSMRA82B14G813s');
INSERT INTO libretto VALUES ('3', 'IT65C256387451245963258749');

/*operazione 3*/
/*NOTA:questo va fatto qua??*/
INSERT INTO dipendenti(nome, cognome, indirizzo, luogonascita, datanascita,matricola) VALUES ('Gennaro', 'Bianchi', 'via Spinelli 55, Pompei', 'Scafati', '1970-03-15', '422923');
INSERT INTO operazione (importo) VALUES (-50.00);
UPDATE conto SET saldo=saldo -50 WHERE iban='IT65C256387451245963258749';
INSERT INTO interessatoDa (ibanConto, codOperazione) VALUES ('IT65C256387451245963258749', '5');
INSERT INTO allaPosta (codice,dipendente) VALUES ('5','422923');
  
/*operazione 4*/ 
INSERT INTO operazione (importo) VALUES (+100.00);
INSERT INTO allaPosta (codice,dipendente) VALUES ('6','422923');
UPDATE conto SET saldo=saldo +100 WHERE iban='IT65C256387451245963258749';

/*operazione 5*/

/*operazione 6*/
SELECT importo, dataOper, tipo FROM operazione, interessatoDa WHERE operazione.codice=interessatoDa.codOperazione AND interessatoDa.ibanConto='IT65C256387451245963258749';
SELECT saldo FROM conto WHERE iban='IT65C256387451245963258749';

/*operazione 7*/
SELECT * FROM cliente WHERE CF='RSSMRA82B14G813s';

