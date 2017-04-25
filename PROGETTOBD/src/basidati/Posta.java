package basidati;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Posta {

	public static void main(String[] args) {

		JFrame frame=new JFrame();
		frame.setTitle("Operazioni");
		JPanel panel=new JPanel();
		panel.setLayout(new GridLayout(14, 1));
		panel.add(new JLabel("Op1: Inserisci un Cliente."));
		panel.add(new JLabel("Op2: Apri un conto (di qualsiasi tipo) ad un Cliente."));
		panel.add(new JLabel("Op3: Effettuare un'operazione allo sportello."));
		panel.add(new JLabel("Op4: Effettuare un'operazione tramite internet."));
		panel.add(new JLabel("Op5: Consegna della posta da parte di un postino."));
		panel.add(new JLabel("Op6: Stampa dell'estratto conto di un cliente, comprese tutte le  operazioni effettuate su quel conto."));
		panel.add(new JLabel("Op7: Stampa di tutte le informazioni che riguardano un cliente."));
		panel.add(new JLabel("Op8: Spedizione della posta da parte di un Cliente."));
		panel.add(new JLabel("Op9: Trovare nome, cognome e numero di telefono dei clienti che hanno intestato un conto "));
		panel.add(new JLabel("		bancoposta, che non hanno una carta e che hanno effettuato più di 50 operazioni."));
		panel.add(new JLabel("Op10: Trovare nome e cognome dei clienti che negli ultimi due mesi hanno effettuato la metà "));
		panel.add(new JLabel("		delle operazione effettuate nei due mesi precedenti."));
		panel.add(new JLabel("Op11: Trovare tutti i dipendenti che sono addetti agli sportelli e che hanno effettuato almeno "));
		panel.add(new JLabel("		n operazioni da una certa oppure i postini che dalla stessa  data hanno effettuato più di n consegne."));
		frame.setSize(750, 450);
		frame.add(new JPanel(), BorderLayout.WEST);
		frame.add(panel);
		frame.setVisible(true);
		
		int i=Operazioni.inserisciIntero("il numero dell'operazione(0 per terminare");
		
		while(i>=0 && i<12){

			switch(i){
			case 0: return;
			case 1: Operazioni.op1(); break;
			case 2: Operazioni.op2(); break;
			case 3: Operazioni.op3(); break;
			case 4: Operazioni.op4(); break;
			case 5: Operazioni.op5(); break;
			case 6: Operazioni.op6(); break;
			case 7: Operazioni.op7(); break;
			//
			case 9: Operazioni.op9(); break;
			case 10: Operazioni.op10(); break;
			case 11:Operazioni.op11(); break;
			default : System.out.println("Valore non valido");
			}
			i=Operazioni.inserisciIntero("il numero dell'operazione");
		}

	}

}
