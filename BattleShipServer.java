/**
* A BattleShipServer opens a server on the designated port and awaits connections from numerous clients.
* It also creates a graphical log for reporting server messages sent between server/client as well as error
* messages. Once two clients have connection the server starts a game thread for the two players to play,
* and then continues to do so unless an error occurs.
*
* @author G. Monagan modified by Alex Perrino
* @version November 29, 2017
*/
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
public class BattleShipServer extends JFrame implements BattleShippable {
   //Dimensions of frame
   public static final int FRAME_WIDTH = 1380;
   public static final int FRAME_HEIGHT = 720;

   //Font used for the server log messages
   public static final Font LOG_FONT = new Font("SansSerif", Font.BOLD, 16);
   
   //To format the date 12-Nov-2016 10:15 PM
   private static DateTimeFormatter FORMATTER = 
      DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);

   private JTextArea textAreaLog;
   
   /**
       * Starts the server which creates a log window, opens one socket per client
       * and starts games between two clients. The Server begins by giving a client a name.
       * @param args line arguments
       */
   public static void main(String[] args)   {
      new BattleShipServer();
   }
   
   /**
       * Appends a given message onto the textAreaLog and adds a carriage return.
       * Additionally it scrolls down so that the bottom of the textArea is always shown.
       * @param msg the message to display.
       */
   private void report(String msg)   {
      textAreaLog.append(msg + '\n');
      textAreaLog.setCaretPosition(textAreaLog.getDocument().getLength());
   }

   /**
       * Builds an extended frame that has a text area to display the server log messages 
       */
   public void buildLogFrame()   { 
      textAreaLog = new JTextArea();
      textAreaLog.setFont(LOG_FONT);
      //Always show last line
      DefaultCaret caret = (DefaultCaret)textAreaLog.getCaret();
      caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
      setSize(FRAME_WIDTH, FRAME_HEIGHT);
      setTitle("Server Activity Log");
      add(new JScrollPane(textAreaLog), BorderLayout.CENTER);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setVisible(true);
   }

   /**
       * Reports the server's IP address and name, and gives the current time.
       * @return gives the date and time nicely formatted
       * @throws UnknownHostException due to not an improper server
       */
   private String reportStatsOnServer() throws UnknownHostException   {
      report("This server's computer name is " + 
         InetAddress.getLocalHost().getHostName());
      report("This server's IP address is " + 
         InetAddress.getLocalHost().getHostAddress() + "\n");
      return LocalDateTime.now().format(FORMATTER);
   }

   /**
       * Reports the clients's domain name and IP address.
       * @param socket an open socket
       * @param n the client's number (starting with 1)
       */
   private void reportStatsOnClient(Socket socket, int n)   {
      InetAddress  addr = socket.getInetAddress();
      report("client " + n + "'s host name is " + addr.getHostName());
      report("client " + n + "'s IP Address is " + addr.getHostAddress());
   }

   /**
       * Builds a reporting window, as well as opens the server socket. It connects clients through
       * the server socket, and runs a thread for every two players, continuing unless an error occurs.
       */
   public BattleShipServer()    {
      buildLogFrame();
      try (ServerSocket serverSocket = new ServerSocket(PORT)) {
         String nowStr = reportStatsOnServer();        
         report("The server, port " + serverSocket.getLocalPort() 
            + ", started on " + nowStr);
         int gameNumber = 0;  
         while (true)   {
            gameNumber++;
            //listen for new connection requests
            Socket socket0 = serverSocket.accept();
            reportStatsOnClient(socket0, gameNumber);
            Socket socket1 = serverSocket.accept();
            reportStatsOnClient(socket1, gameNumber);
            //create game thread
            Runnable service = new BattleShipGame(socket0, socket1, textAreaLog, gameNumber);
            new Thread(service).start();
            report("starting thread for game " + gameNumber + " at " + 
               LocalDateTime.now().format(FORMATTER));
         }
      }
      catch(IOException e) {
         report("problems in server " + e.toString());
         e.printStackTrace(System.err);
      }
   }
}