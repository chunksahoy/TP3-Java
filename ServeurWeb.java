/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

//package tp3.serveurweb;

/**
 *
 * @author Charles
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
/*
* ServeurWeb.java
* par Charles Hunter-Roy et Francis Clément, 2014
* petit serveur web qui attend des connections, on peut le fermer à tout moment grâce à la classe Terminateur
*/
public class ServeurWeb {
    private int port;
    static final int DELAI = 500;
    static final int PORT_MIN = 0;
    static final int PORT_MAX = 65537;
    private static final String CONFIG_PATH = "config.txt";
    static int nbClients = 0;
    private boolean pasFini = true;
    private String filePath = "c:\\www\\";
    private String index = "index.html";
    private ArrayList<String> settings = new ArrayList<String>();
    ServerSocket serveur = null;
    Thread threadTerminateur;
    boolean listing = false;
    
    private static void lireConfigurations(ServeurWeb server) throws Exception{
        File config = new File(CONFIG_PATH);
        if(config.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(config));
            String ligne = "";
            while(ligne != null) {
               ligne = reader.readLine();
               if(ligne != null)
                  server.settings.add(ligne);
            }
         }
    }
    private static void configurer(ServeurWeb server) {
        String param[];
        for(int i = 0; i < server.settings.size(); ++i) {
            param = server.settings.get(i).split("=");
            
            switch (param[0]) {
                case "port":
                    server.port = Integer.parseInt(param[1]);
                    break;
                case "racine":
                    server.filePath = param[1];
                    break;
                case "index":
                    server.index = param[1];
                    break;
                case "listage":
                    if(param[1].equalsIgnoreCase("oui")) {
                        server.listing = true;
                    }
                    break;
            }
        }
    }
    public static void main(String args[]) {
        try {
            int port = 80;
            String filePath = "c:\\www\\";
            final String CONFIG_PATH = "config.txt";

            switch( args.length )
            {
                case 0:
                    try
                    {                        
                        ServeurWeb serveur = new ServeurWeb();
                        lireConfigurations(serveur);
						
                        if(serveur.settings != null) {
                           configurer(serveur);
                        }
                        serveur.lancerServeur();                        
                    }
                    catch( NumberFormatException nfe )
                    {
                        System.err.println( "Le numero de port doit etre un nombre entier" );
                    }
                    break;
                case 1:
                    try
                    {
                        port = Integer.parseInt( args[ 0 ] );
                        
                        if( ( port >= PORT_MIN ) && ( port <= PORT_MAX ) )
                        {
<<<<<<< HEAD
                           ServeurWeb serveur = new ServeurWeb(port);
                           lireConfigurations(serveur);
						
                           if(serveur.settings != null) {
                              configurer(serveur);
                           }
                            serveur.port = port;
                            serveur.lancerServeur();
=======
                            ServeurWeb serveur = new ServeurWeb(port);
							lireConfigurations(serveur);
						
							if(serveur.settings != null) {
								configurer(serveur);
								serveur.port = port;
							}
							serveur.lancerServeur(); 
>>>>>>> d1b071f3d95090e7915dbaa083bdc9bcf9ae7840
                        }
                        else
                        {
                            System.err.println( "Le numero de port est hors intervale" );
                        }
                    }
                    catch( NumberFormatException nfe )
                    {
                        System.err.println( "Le numero de port doit etre un nombre entier" );
                    }
                    break;
                case 2:
                    try
                    {
                        port = Integer.parseInt( args[ 0 ] );
                        filePath = args[1].toString();
                        
                        if(( port >= PORT_MIN ) && ( port <= PORT_MAX )  && new File(filePath).exists())
                        {
                            ServeurWeb serveur = new ServeurWeb(port, filePath);
							lireConfigurations(serveur);
						
							if(serveur.settings != null) {
								configurer(serveur);
								serveur.port = port;
								serveur.filePath = filePath;
							}
							serveur.lancerServeur(); 
                        }
                        else
                        {
                            System.err.println( "Le numero de port est hors intervale" );
                        }
                    }
                    catch( NumberFormatException nfe )
                    {
                        System.err.println( "Le numero de port doit etre un nombre entier" );
                    }
                    break;
                default:
                    System.err.println( "Il y a trop de parametres" );
                    break;
            }
            
        } catch (Exception ex) {
            System.err.print(ex);
            System.exit(1);
        }
    }
    ServeurWeb() {
        this.port = 80;
        this.filePath = "C:\\www";        
        Terminateur test = new Terminateur();
        threadTerminateur = new Thread(test);
        threadTerminateur.start();
    }
    
    ServeurWeb(int nb) {
        this.port = nb;
        Terminateur test = new Terminateur();
        threadTerminateur = new Thread(test);
        threadTerminateur.start();
    }
    ServeurWeb(int nb, String path) {
        this.port = nb;
        this.filePath = path;
        Terminateur test = new Terminateur();
        threadTerminateur = new Thread(test);
        threadTerminateur.start();
    }
    
    public int getPort() {
        return this.port;
    }
    public String getFilePath() {
        return this.filePath;
    }
    public void bouclerServeur() {
        Thread threadClient;
        while (pasFini) {
            Socket socket;
            try {
                serveur.setSoTimeout(DELAI);
                socket = serveur.accept();
                System.out.println("Ouverture d'une connexion");
                
                Client client = new Client(socket, this.getFilePath(), listing, index, port);
                threadClient = new Thread(client);
                threadClient.start();
                
                nbClients++;
                System.out.println("nb Clients: " + nbClients);
                
            } catch (SocketTimeoutException e) {
                pasFini = threadTerminateur.isAlive();
            } catch (SocketException e){
                System.out.println("Connection Interrompue");
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
    
    public void lancerServeur() {
        try {
            serveur = new ServerSocket(port);
            System.out.println("Serveur en ligne (port=" + getPort() + "," + "racine=" + new File(filePath).getPath() + ")");
            bouclerServeur();
            
        } catch (SocketTimeoutException ex) {
            System.err.println("Délai expiré!");
        } catch (BindException ex) {
            System.err.println("Port(" + getPort() +  ") déjà utilisé!");
        } catch(SocketException e){
            System.out.println("Connection Interrompue");
        } catch (IOException ioe) {
            System.err.println("Erreur de traitement!");
        } catch (Exception ex) {
            System.err.println(ex);
        } finally {
            if (serveur != null) {
                System.out.println("Fermeture du serveur");
                try {
                    serveur.close();
                } catch(SocketException e){
                    System.out.println("Connection Interrompue");
                } catch (Exception ex) {
                    System.err.println(ex);
                }
            }
            System.exit(1);
        }
    }
}
