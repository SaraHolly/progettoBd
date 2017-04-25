package basidati;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Scanner;

import com.mysql.jdbc.ResultSetMetaData;

//TODO fare attenzione alla lunghezza dei campi, iban e cf in particolare in particolare

public class Operazioni {
	static Scanner in=new Scanner(System.in);	
	static Connection con;
	private static Statement st=null;
	private static ResultSet rs=null;

	public static void op0(){

		String query="SELECT nome,cognome FROM cliente";
		try{
			con=DBConnectionPool.getConnection();
			st=con.createStatement();
			rs = st.executeQuery(query);
			while(rs.next()){
				String nome=rs.getString("nome");
				String cognome=rs.getString("cognome");
				System.out.print(nome);
				System.out.println("\t"+cognome);

			}
			DBConnectionPool.releaseConnection(con);
		}catch(SQLException e){
			System.out.println(e.getMessage());
		}
	}

	public static void op1(){
		String cf=null;
		try{
			con=DBConnectionPool.getConnection();
			System.out.println("connsessione OK");				//da togliere 

			PreparedStatement ps=null;
			String query="INSERT INTO cliente (nome, cognome ,CF,datanascita, luogonascita) VALUES (?,?,?,?,?)";
			ps = con.prepareStatement(query);
			System.out.println("Inserisci il nome");
			//TODO dovresti gestire  la lunghezza della stringa?????
			ps.setString(1, in.nextLine());
			System.out.println("Inserisci il cognome");
			ps.setString(2, in.nextLine());
			System.out.println("Inserisci il CF");
			cf=in.nextLine();
			ps.setString(3, cf);
			ps.setDate(4,inserisciData("la data di nascita"));
			System.out.println("Inserisci il luogo di nascita");
			ps.setString(5, in.nextLine());
			/*System.out.println("Inserisci l'indirizzo");
			ps.setString(6, in.nextLine());
			 */
			ps.executeUpdate();
			//con.commit();

			while(true){
				query="INSERT INTO telefono(numero,cliente) VALUES (?,'"+cf+"')";
				ps=con.prepareStatement(query);
				System.out.println("inserisci un numero di telefono (0 per terminare");
				String telefono=in.nextLine();
				if(telefono.equals("0")){
					break;
				}
				if(telefono.length()!=10){
					System.out.println("Valore errato");
					continue;
				}
				int i;
				for(i=0;i<10;i++){
					if(!Character.isDigit(telefono.charAt(i))){
						System.out.println("Valore errato");
						break;
					}
				}
				if(i!=10){
					continue;
				}
				ps.setString(1, telefono);
				ps.executeUpdate();
				con.commit();		
			}
		}catch(SQLException e){
			System.out.println(e);
		}finally{
			DBConnectionPool.releaseConnection(con);
		}
	}

	public static void op2(){
		try{
			//TODO il saldo va inserito???(chiedere a mamma)
			con=DBConnectionPool.getConnection();
			PreparedStatement ps=null;
			String query="INSERT INTO conto(IBAN,saldo) VALUES(?,?)";
			ps=con.prepareStatement(query);
			System.out.println("inserisci l'IBAN");
			String iban=in.nextLine();
			ps.setString(1,iban);
			ps.setDouble(2, inserisciDouble("il saldo"));
			ps.executeUpdate();
			System.out.println("Inserisci il tipo di conto");
			String tipoConto=in.nextLine();
			//TODO gestisci il defalut
			String query1=null;
			switch(tipoConto){
				case "libretto": query1="INSERT INTO libretto(iban, tassoInt) VALUES(?,?)";
					ps=con.prepareStatement(query1);
					ps.setString(1, iban);
					ps.setDouble(2, inserisciDouble("il tasso"));
					break;
				case "bancoposta":  query1="INSERT INTO bancoposta(iban,tassoInt,costo,servInternet, carta) VALUES(?,?,?,?,?)";
					ps=con.prepareStatement(query1);
					ps.setString(1, iban);
					ps.setDouble(2, inserisciDouble("il tasso"));
					ps.setDouble(3, inserisciDouble("il costo"));
					System.out.println("Vuoi attivare i servizi internet? ('y','n')");
					String servInt=in.nextLine();
					if(!servInt.equals("y")){
						servInt="n";
					}
					ps.setString(4,servInt);
					System.out.println("Vuoi la carta? ('y','n')");
					String carta=in.nextLine();
					if(!carta.equals("y")){
						carta="n";
					}
					ps.setString(5, carta);
					break;
			//TODO vedi se puoi mettere la scadenza a tot da adesso
				case "postePay": query1="INSERT INTO postePay(iban, codSicur, scadenza) VALUES(?,?,?)";
					ps=con.prepareStatement(query1);
					ps.setString(1, iban);
					ps.setInt(2, inserisciIntero("il codice di sicurezza"));
					ps.setDate(3, inserisciData("la data di scadenza"));
					break;
			}
			ps.executeUpdate();
			con.commit();
		}catch(SQLException e){
			System.out.println(e);
		}finally{
			DBConnectionPool.releaseConnection(con);
		}
	}

	public static void op3(){
		try{
			con=DBConnectionPool.getConnection();
			PreparedStatement ps=null;
			String query="SELECT tipo from dipendenti WHERE matricola=?";
			int mat= inserisciIntero("la matricola dell'addetto allo sportello");
			ps=con.prepareStatement(query);
			ps.setInt(1, mat);
			rs=ps.executeQuery();
			rs.next();
			try{
				String tipo=rs.getString("tipo");
				if( tipo==null || !tipo.equals("addettoSportello")){
					System.out.println("un dipendente che non è un addetto allo sportello non può effettuare operazioni");
					DBConnectionPool.releaseConnection(con);
					return;
				}
			}catch(SQLException e){
				System.out.println("non esiste un dipendente con questa matricola");
				DBConnectionPool.releaseConnection(con);
				return;
			}
			System.out.println("Inserisci l'iban");
			String iban=in.nextLine();
			query="SELECT saldo FROM conto WHERE iban=?";
			ps=con.prepareStatement(query);
			ps.setString(1, iban);
			rs=ps.executeQuery();
			rs.next();
			double saldo=rs.getDouble("saldo");
			query="INSERT INTO operazione(importo)VALUES(?)";			
			ps=con.prepareStatement(query);
			double importo=inserisciDouble("l'importo");
			if(saldo - importo<0){
				System.out.println("impossibile prelevare una cifra superiore al saldo");
				DBConnectionPool.releaseConnection(con);
				return;
			}
			ps.setDouble(1, importo);
			ps.executeUpdate();
			query="UPDATE conto SET saldo=saldo+? WHERE iban=?";
			ps=con.prepareStatement(query);
			ps.setDouble(1,importo );
			ps.setString(2, iban);
			ps.executeUpdate();	
			query="SELECT MAX(codice) FROM operazione";
			st=con.createStatement();
			rs=st.executeQuery(query);
			rs.next();
			int n=rs.getInt("MAX(codice)");
			query="INSERT INTO interessatoDa (ibanConto, codOperazione) VALUES (\""+iban+"\","+n+")";
			ps=con.prepareStatement(query);
			ps.executeUpdate();
			query="INSERT INTO effettuatoDa (codice,dipendente) VALUES (?,?);";
			ps=con.prepareStatement(query);
			ps.setInt(1, n);
			ps.setInt(2, mat);
			ps.executeUpdate();
			con.commit();
			rs.close();
		}catch(SQLException e){
			System.out.println(e);
		}finally{
			DBConnectionPool.releaseConnection(con);
		}
	}

	public static void op4(){
		try{
			con=DBConnectionPool.getConnection();
			PreparedStatement ps=null;
			System.out.println("Inserisci l'iban");
			String iban=in.nextLine();
			String query="SELECT * FROM postePay WHERE iban='"+iban+"'";
			st=con.createStatement();
			rs=st.executeQuery(query);

			int count=0;
			while(rs.next()){
				count++;
			}
			System.out.println("count ="+count);
			
			if(count==0){
				query="SELECT * FROM bancoposta WHERE iban='"+iban+"'";
				st=con.createStatement();
				rs=st.executeQuery(query);
				rs.next();
				String carta;
				try{
				carta=rs.getString("servInternet");
				}catch(SQLException e){
					System.out.println("impossibile efettuare l'operazione su questo conto");
					return;
				}
				if(carta.equals("n")){
					System.out.println("non hai i servizi internet attivati, vuoi attivarli?(y-n)");
					String voglio=in.nextLine();
					if(voglio.equals("y")){
						query="UPDATE bancoposta SET servInternet = 'y' WHERE iban='"+iban+"'";
						con.commit();
					}else{
						return;
					}
				}
			}
			query="INSERT INTO operazione(importo,tipo)VALUES(?,'onLine')";			
			ps=con.prepareStatement(query);
			double importo=inserisciDouble("l'importo");
			ps.setDouble(1, importo);
			ps.executeUpdate();
			query="UPDATE conto SET saldo=saldo+? WHERE iban=?";
			ps=con.prepareStatement(query);
			ps.setDouble(1,importo );
			ps.setString(2, iban);
			ps.executeUpdate();	
			query="SELECT MAX(codice) FROM operazione";
			st=con.createStatement();
			rs=st.executeQuery(query);
			rs.next();
			int n=rs.getInt("MAX(codice)");
			query="INSERT INTO interessatoDa (ibanConto, codOperazione) VALUES (\""+iban+"\","+n+")";
			ps=con.prepareStatement(query);
			ps.executeUpdate();
			rs.close();
			con.commit();
			DBConnectionPool.releaseConnection(con);
			System.out.println("done");
		}catch(SQLException e){
			System.out.println(e);			
		}finally{
			DBConnectionPool.releaseConnection(con);
		}	
	}

	public static void op5(){
		try{
			con=DBConnectionPool.getConnection();
			PreparedStatement ps=null;
			String query="SELECT tipo from dipendenti WHERE matricola=?";
			int mat= inserisciIntero("la matricola del postino");
			ps=con.prepareStatement(query);
			ps.setInt(1, mat);
			rs=ps.executeQuery();
			rs.next();
			try{
				String tipo=rs.getString("tipo");
				if( tipo==null || !tipo.equals("postino")){
					System.out.println("un dipendente che non è un postino non può consegnare la posta");
					DBConnectionPool.releaseConnection(con);
					return;
				}
			}catch(SQLException e){
				System.out.println("non esiste un dipendente con questa matricola");
				DBConnectionPool.releaseConnection(con);
				return;
			}
			query="UPDATE posta SET dataCons=?, dipendente=? WHERE codice=?";
			ps=con.prepareStatement(query);
			ps.setDate(1, inserisciData("la data di consegna"));
			//TODO devo controllare che sia un postino??
			ps.setInt(2,mat);
			ps.setInt(3, inserisciIntero("il codice della posta"));
			ps.executeUpdate();
			con.commit();
			System.out.println("done");
		}catch(SQLException e){
			System.out.println(e);
		}finally{
			DBConnectionPool.releaseConnection(con);	
		}
	}

	public static void op6(){
		try{
			con=DBConnectionPool.getConnection();
			System.out.println("inserisci l'iban");
			String iban=in.nextLine();
			String query="SELECT * FROM conto WHERE iban='" +iban+"'";
			st=con.createStatement();
			rs=st.executeQuery(query);
			rs.next();
			double saldo=rs.getDouble("saldo");
			System.out.println("il saldo ddel conto è "+saldo);
			query="SELECT importo, dataOper, tipo FROM operazione, interessatoDa "+
					"WHERE operazione.codice=interessatoDa.codOperazione AND interessatoDa.ibanConto='"+iban+"'";
			st=con.createStatement();
			rs=st.executeQuery(query);
			printTable(rs);
			con.commit();
		}catch(SQLException e){
			System.out.println(e);			
		}finally{
			DBConnectionPool.releaseConnection(con);	
		}
		
	}

	public static void op7(){
		try{
			con=DBConnectionPool.getConnection();
			PreparedStatement ps=null;
			String query="SELECT nome, cognome,luogonascita, datanascita,indirizzo,count(*)AS numeroConti "+ 
					"FROM cliente, apre, conto WHERE apre.ibanConto=conto.IBAN AND apre.clienteCF=cliente.CF AND cliente.CF=?";
			ps=con.prepareStatement(query);
			System.out.println("inserisci il cf del cliente");
			String cf= in.nextLine();
			ps.setString(1, cf);
			ResultSet re = ps.executeQuery();
			printTable(re);
			query="SELECT numero FROM telefono WHERE telefono.cliente='"+cf+"'";
			ps=con.prepareStatement(query);
			re=ps.executeQuery();
			printTable(re);
			//con.commit();
		}catch(SQLException e){
			System.out.println(e);			
		}finally{
			DBConnectionPool.releaseConnection(con);	
		}		
	}

	public static void op9(){
		try{
			con=DBConnectionPool.getConnection();
			//qui non dovrebbe essere 3
			String query="SELECT  nome, cognome,count(*) AS numOperazioni FROM cliente,apre,bancoposta,interessatoDa"+
					" WHERE cliente.CF=apre.clienteCF AND apre.ibanConto=bancoposta.iban AND apre.ibanConto=interessatoDa.ibanConto"+
					" GROUP BY cliente.CF HAVING count(*)>3";
			st=con.createStatement();
			rs=st.executeQuery(query);
			printTable(rs);
		}catch(SQLException e){
			System.out.println(e);			
		}finally{
			DBConnectionPool.releaseConnection(con);	
		}
	}
		
	
	public static void op10(){
		try{
			con=DBConnectionPool.getConnection();
			java.util.Date dataUtil=new java.util.Date();
			Calendar dataOggi= Calendar.getInstance();
			dataOggi.setTime(dataUtil);
			dataOggi.add(Calendar.MONTH, -2);
			dataUtil=dataOggi.getTime();
			Date prima= new Date(dataUtil.getTime());
			dataOggi.add(Calendar.MONTH, -2);
			dataUtil=dataOggi.getTime();
			Date primaAncora= new Date(dataUtil.getTime());
			//System.out.println(primaAncora.toString());
			String query="SELECT nome,cognome FROM cliente as C, apre AS A "+
					"WHERE C.CF=A.clienteCF AND A.ibanConto IN ( SELECT IBAN FROM conto AS CO, interessatoDa AS I1, operazione as OP1 "+
					"WHERE CO.IBAN=I1.ibanConto AND I1.codOperazione=OP1.codice AND OP1.dataOper>'"+prima+"'"+ 
					"GROUP BY IBAN	HAVING count(*) <=0.5*( SELECT count(*) "+
					"FROM interessatoDa AS I2, operazione AS OP2 "+ 
					"WHERE I2.ibanConto=CO.IBAN AND OP2.codice=I2.codOperazione AND OP2.dataOper>= '"+primaAncora+"' AND OP2.dataOper<='"+prima+"'))";

			st=con.createStatement();
			rs=st.executeQuery(query);
			printTable(rs);
			con.commit();
		}catch(SQLException e){
			System.out.println(e);			
		}finally{
			DBConnectionPool.releaseConnection(con);	
		}
	}

	public static void op11(){
		try{
			con=DBConnectionPool.getConnection();
			int op= inserisciIntero("numero di operazioni");
			int p= inserisciIntero("quantità di posta");
			Date data=inserisciData("la data");
			System.out.println(data.toString());
			String query="(SELECT matricola FROM dipendenti,posta"+
					" WHERE dipendenti.matricola=posta.dipendente AND posta.dataCons>= '"+data.toString()+
					"' GROUP BY dipendenti.matricola HAVING count(*)>"+op+
					")UNION("+
					"SELECT matricola FROM dipendenti,effettuatoDa, operazione"+
					" WHERE dipendenti.matricola=effettuatoDa.dipendente AND effettuatoDa.codice=operazione.codice AND operazione.dataOper>'"+data.toString()+
					"' GROUP BY dipendenti.matricola HAVING count(*)>"+p+")";
			st=con.createStatement();
			rs=st.executeQuery(query);
			printTable(rs);
			con.commit();
		}catch(SQLException e){
			System.out.println(e);			
		}finally{
			DBConnectionPool.releaseConnection(con);	
		}		
	}

	public static int inserisciIntero(String s){
		System.out.println("Inserisci "+ s);

		int val;
		while(true){
			try{
				String valore=in.nextLine();
				if(valore.equals("\n")){									//cmq qua nn entra
					System.out.println("ho acchiappato il \\n");		//da togliere
					valore=in.nextLine();
				}
				val=Integer.parseInt(valore);
				break;
			}catch(NumberFormatException e){
				System.out.println("Inserisci "+ s+" (un intero)");
			}
		}
		return val;
	}

	//vedi di gestire il fatto dei mull 
	private static double inserisciDouble(String s){
		System.out.println("Inserisci "+ s);
		double val;
		while(true){
			try{
				val=Double.parseDouble(in.nextLine());
				break;
			}catch(NumberFormatException e){
				System.out.println("Inserisci "+ s+" (un double)");
			}
		}
		return val;
	}

	private static Date inserisciData(String s){
		System.out.println("inserisci "+ s + " dd-MM-yyyy");
		java.util.Date parsed;
		while(true){
			try{
				String str=in.nextLine();
				SimpleDateFormat format= new SimpleDateFormat("dd-MM-yyyy");
				parsed=format.parse(str);
				break;
			}catch(NumberFormatException e){
				System.out.println("Inserisci "+ s+" (dd-MM-yyyy)");
			}catch(ParseException e){
				System.out.println("Inserisci "+ s+" (dd-MM-yyyy)");
			}
		}
		Date sqldate= new Date(parsed.getTime());
		return sqldate;
	}

	private static void printTable(ResultSet rs){
		/*try{
			java.sql.ResultSetMetaData md=	rs.getMetaData();
			for(int	i = 1; i <=	md.getColumnCount(); i++ )
				System.out.print(md.getColumnLabel(i) + "     ") ;
			System.out.println();
			while( rs.next() ) {
				for(int	i = 1;i <= md.getColumnCount(); i++ )
					System.out.print( rs.getString(i) +" ") ;
				System.out.println() ;
			}
		}catch(SQLException e){
			System.out.println(e.getMessage());
		}*/
		java.sql.ResultSetMetaData md;
		int n=0;
		try{
			md = rs.getMetaData();
			for(int i=1; i<=md.getColumnCount(); i++){
				if(n<md.getColumnLabel(i).length())
					n = md.getColumnLabel(i).length();
			}
			while(rs.next()){
				for(int i=1; i<=md.getColumnCount(); i++){
					if(n<rs.getString(i).length())
						n = rs.getString(i).length();
				}
			}
			rs.beforeFirst();
			for(int i=0; i<md.getColumnCount()*n+md.getColumnCount(); i++)
				System.out.print("-");
			System.out.println();
			
			System.out.print("|");
			for(int i=1; i<=md.getColumnCount(); i++){
				System.out.print(md.getColumnLabel(i));
				for(int j=0; j<n-md.getColumnLabel(i).length(); j++)
					System.out.print(" ");
				System.out.print("|");
			}
			System.out.println();
			
			for(int i=0; i<md.getColumnCount()*n+md.getColumnCount(); i++)
				System.out.print("-");
			System.out.println();
			
			while(rs.next()){
				System.out.print("|");
				for(int i=1; i<=md.getColumnCount(); i++){
					System.out.print(rs.getString(i));
					for(int j=0; j<n-rs.getString(i).length(); j++)
						System.out.print(" ");
					System.out.print("|");
				}
				System.out.println();
				
				for(int i=0; i<md.getColumnCount()*n+md.getColumnCount(); i++)
					System.out.print("-");
				System.out.println();
			}
			rs.close();
		}catch(SQLException e){
			System.out.println(e);
		}
	}
}
