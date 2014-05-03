/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

//package tp3.serveurweb;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


/*
* Client.java
* par Charles Hunter-Roy et Francis Clément, 2014
* petit client web qui se connecte à un serveur pour y lire des fichiers
*
*/
public class Client implements Runnable {
    
    private final int DELAI = 500;
    private Socket socket;
	private int port = 80;
    private String filePath;
    private ArrayList<String> commandes = new ArrayList<String>();
    private ArrayList<String> contents = new ArrayList<String>();
    private File fichier;
    private File[] listeFichiers;
    private int nbFichiers = 0;
    private String prompt = "=>";
    private boolean listing = false;
    private String index = "index.html";

    // constructeur paramétrique
    public Client(Socket socket) {
        this.socket = socket;
        initialiserCommandes();
    }
    
    public Client(Socket socket, String path) {
        this.socket = socket;
        this.filePath = path;
        initialiserCommandes();
        initialiserListeFichiers();
    }
	
	public Client(Socket socket, String path, boolean liste, String index) {
        this.socket = socket;
        this.filePath = path;
		this.listing = liste;
		this.index = index;
        initialiserCommandes();
        initialiserListeFichiers();
    }
    public Client(Socket socket, String path, boolean liste, String index, int port) {
        this.socket = socket;
        this.filePath = path;
		this.listing = liste;
		this.index = index;
		this.port = port;
        initialiserCommandes();
        initialiserListeFichiers();
    }
    private void initialiserListeFichiers() {
        fichier = new File(filePath);
        listeFichiers = fichier.listFiles();
    }
    
    // initialisation des commandes valides
    public void initialiserCommandes() {
        commandes.add("GET");
        commandes.add("HEAD");
    }
    public void initialiserContents() {
        contents.add(".html");
        contents.add(".txt");
        contents.add(".gif");
        contents.add(".jpeg");
        contents.add(".jpg");
        contents.add(".png");
    }
    
    // vérifie si la ligne passé en paramètre est dans la liste de commandes
    // valides
    private boolean verifierCommande(String ligne) {
        boolean valide = false;
        for (int i = 0; i < commandes.size() && !valide; ++i) {
            if (ligne.toUpperCase().startsWith(commandes.get(i))
                    && ligne != null) {
                valide = true;
            }
        }
        return valide;
    }
    
    private boolean entreeValide(String commande) {
        boolean valide = true;
        if (commande.split("\\s+").length <= 0
                || commande.split("\\s+").length > 3) {
            valide = false;
        }
        return valide;
    }
    
    private String lire(BufferedReader reader) throws IOException {
        String temp = "derp";
        String result = reader.readLine();
        while(!temp.equals("") && temp != null) {
            temp = reader.readLine();
        }
        return result;
    }
    private void listerContenu(PrintWriter writer, File[] liste, File file) throws Exception {
		writer.println("<pre>");
		writer.println("Contenu du dossier " + file.getName() + ": ");
        for (int i = 0; i < liste.length; i++) {
            if (liste[i].isFile()) {
                nbFichiers++;
				String[] temp = liste[i].getAbsolutePath().replace("\\", "/").split(filePath.replace("\\", "/"));
				writer.printf("%-40s %-5s %-15s %-5s %tD %n", "<a href='localhost:" + port + temp[1] +
				"'>" + liste[i].getName() + "</a>", "Taille:", liste[i].length(), "Dernieres modif.:", liste[i].lastModified());
            } else if (liste[i].isDirectory()) {
                nbFichiers++;
				String[] temp = liste[i].getAbsolutePath().replace("\\", "/").split(filePath.replace("\\", "/"));
				writer.printf("%-60s %-5s %tD %n", "<a href='localhost:"+ port + temp[1] +
				"'>[]"+ liste[i].getName() + "</a>" ,"Dernieres modif.:", liste[i].lastModified());
            }
        }
		writer.println("</pre");
    }
    private void ecrireLigne(PrintWriter writer, String msg) {
        writer.println(msg);
    }
    private void ecrire(PrintWriter writer, String msg) {
        writer.print(msg);
    }
    private void ecrireFormater(PrintWriter writer, String format, String[] msg) {
        writer.printf(format, msg.toString());
    }
    private String getCommandes(String ligne) {
        boolean trouve = false;
        String commande = "";
        for (int i = 0; i < commandes.size() && !trouve; ++i) {
            if (ligne.toUpperCase().startsWith(commandes.get(i))
                    && ligne != null) {
                trouve = true;
                commande = commandes.get(i);
            }
        }
        return commande;
    }
    public String getDateRfc822(Date date)
    {
        SimpleDateFormat formatRfc822
                = new SimpleDateFormat( "EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z",
                        Locale.US );
        
        return formatRfc822.format(date);
    }
    private String getContentType(String filePath) {
        String contentType = "";
        switch(filePath) {
            case "html":
                contentType = "text/html";
                break;
            case "txt":
                contentType = "text/plain";
                break;
            case "gif":
                contentType = "image/gif";
                break;
            case "jpeg":
                contentType = "images/jpeg";
                break;
            case "jpg":
                contentType = "images/jpeg";
                break;
            case "png":
                contentType = "images/png";
                break;
        }
        return contentType;
    }
    
    private void reponsesServeur(String filePath) throws IOException {
        File fichier = new File(filePath);
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
		writer.println("HTTP/1.0 200 OK");
		writer.println("Server: Serveur Web v0.2 par Charles Hunter-Roy et Francis Clement");
		writer.println("Date: " + getDateRfc822(new Date()).toString());
		writer.println("Content-type: " + getContentType(filePath.substring(filePath.lastIndexOf('.')+1)));
		writer.println("Last-modified: " + String.valueOf(getDateRfc822(new Date(fichier.lastModified()))));
		if(fichier.isFile())
			writer.println("Content-length: " + String.valueOf(fichier.length()));
		writer.println();
    }
    
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                    socket.getOutputStream()), true);
					
            //listerContenu((new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)), listeFichiers);            
            //ecrireLigne(new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true), nbFichiers + " fichier(s) disponible(s)");
            
            boolean pasFini = true;
            
            while (pasFini) {
                //writer.print(prompt);
                //writer.flush();
                String ligne = reader.readLine();
                String commande = "200 Ok";
				if(ligne.equals("")) {
					ligne = ;
				}
                if (verifierCommande(ligne)) {
                    String fichier = ligne.trim().substring(getCommandes(ligne.trim()).length()+2, ligne.lastIndexOf(" ")).trim();
                    File file = new File(filePath + "\\" + fichier);
                    if(!entreeValide(ligne.trim()) && !file.exists()) {
                        commande = "HTTP/1.0 400 Mauvaise Requete";
                        writer.println(commande);
                        pasFini = false;
                        Thread.sleep(DELAI);
                    }else if (!file.exists() && pasFini) {
                    
                     commande = "HTTP/1.0 404 Fichier Inexistant";
                     writer.println(commande);
                     pasFini = traiterCommande(ligne.split("\\s")[0].toUpperCase(),"404.html" , new PrintWriter(
																													new OutputStreamWriter(
																															socket.getOutputStream())));						
						Thread.sleep(DELAI);
					}
                    if(pasFini)
						pasFini = traiterCommande(ligne.split("\\s")[0].toUpperCase(), filePath + "\\" + fichier, new PrintWriter(new OutputStreamWriter(socket.getOutputStream())));
                    
                }
                System.out.println("fermeture d'une connexion "
                        + ServeurWeb.nbClients);
                reader.close();
                writer.close();
                socket.close();
            }
		} catch(EOFException e) {
                
        } catch(SocketException e){
            //System.err.println("Connection Interrompue");
        } catch (InterruptedException e) {
            System.err.println("Connection interrompue!");
        } catch (IOException e) {
            System.err.println(e);
        } catch (NullPointerException e) {
            System.err.println("Client interrompu" );
			e.printStackTrace(System.err);
        } catch (Exception e) {
            System.err.println("Erreur inattendue!: " + e);
        } 
		finally {
			ServeurWeb.nbClients--;
			try{
				socket.close();
			}
			catch(Exception e) {
				
			}
		}
    } 

    private void traiterFichier (File file) throws Exception {
        if (file.exists()) {
            if (file.isDirectory()) {
                File page = new File(index);
				if(page.exists()) {
					traiterFichier(page);
				}
				else if(!page.exists() && listing){
					File[] liste = file.listFiles();					
					listerContenu(new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true), liste, file);
				}
				else {
					File erreur = new File("403.html");
					traiterFichier(erreur);
				}
         } 
			else if (file.isFile()) {                
                FileInputStream fis = new FileInputStream(file);
                OutputStream os = new BufferedOutputStream(socket.getOutputStream());
                
				byte[] buff = new byte[1024];
				int i = 0;
				do {
					i = fis.read(buff);
					if(i != -1)
						os.write(buff, 0, i);
						os.flush();
				} while(i != -1);
				if(fis != null){
					try{
						fis.close();
					}catch(Exception e){
					/* Don't care */
					}
				}
				if(os != null){
					try{
						os.close();
					}catch(Exception e){
					/* Don't care */
					}
				}
			}
			Thread.sleep(DELAI);
		}
    }   
    
    private boolean traiterCommande (String commande, String fileName, PrintWriter writer) throws Exception{
        try {
            File fichier = new File(fileName);
            switch(commande) {
                case "GET":
                    reponsesServeur(fileName);
                    traiterFichier(fichier);
                    break;
                case "HEAD":
                    reponsesServeur(fileName);
                    break;
                default: writer.println("HTTP/1.0 500 Commande non supportee");
                break;
            }
            return false;
        }catch(Exception ex) {
            throw(ex);
        }
		finally {
			//writer.close();
		}
    }
    
    public void start() {
        
    }
}
