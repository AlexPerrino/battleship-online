/** 
* Defines the thread class for running a new game. It processes commands sent by both
* of the clients, and appropriately sends commands in response to them. It starts off by
* by sending a NAME to each client.
* 
* @author Alex Perrino
* @version November 29, 2017
*/
import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
public class BattleShipGame implements Runnable, BattleShippable  {
   //Already opened sockets
   private Socket socket0; 
   private Socket socket1; 
   //Place where the messages of the log are reported
   private JTextArea textAreaLog;
   //The server numbers each game: report this number when outputting
   private int gameNumber;
   //Client DataStreams
   private DataInputStream fromClient0;
   private DataOutputStream toClient0; 
   private DataInputStream fromClient1;
   private DataOutputStream toClient1;
   
   /**
       * Receives the open socket so that input/output streams can be attached
       * and it receives the JTextArea where the messages will be logged.
       * It keeps track of which client it's interacting with.
       * @param s0 a socket that is opened already
       * @param s1 a socket that is opened already
       * @param tA a JTextArea that receives messages
       * @param gN the game number (used for reporting purposes)
       */
   public BattleShipGame(Socket s0, Socket s1, JTextArea tA, int gN)   {
      socket0 = s0;
      socket1 = s1;
      textAreaLog = tA;
      gameNumber = gN;
   }
   
   /**
       * Helper function to report messages to the textAreaLog which is an instance variable. 
       * It keeps the last appended line showing at the bottom.
       */
   private void report(String msg)   {
      textAreaLog.append("GAME NUMBER: " + gameNumber + " " + msg + "\n");
      textAreaLog.setCaretPosition(textAreaLog.getDocument().getLength());
   }
   
   /**
       * Runs a thread:
       * Sets up the DataInputStreams and DataOutputStreams and waits to receive commands from
       * both of the clients in turn.
       */
   public void run()   {
      try   {
         try   {
            fromClient0 = new DataInputStream(socket0.getInputStream());
            toClient0 = new DataOutputStream(socket0.getOutputStream());
            fromClient1 = new DataInputStream(socket1.getInputStream());
            toClient1 = new DataOutputStream(socket1.getOutputStream());
            executeCmds();
         }
         finally  {
            socket0.close();
            socket1.close();
            fromClient0.close();
            fromClient1.close();
            toClient0.close();
            toClient1.close();
         }
      }
      catch (Exception e)  {
         report("GAME THREAD CLOSING");
      }
   }
   
   /**
       * Execute all commands until the QUIT or QUIT_SHIP command is received from 
       * either client. If there is an unknown command it then stops, and does not continue.
       */
   private void executeCmds() throws IOException   {
      report("SENDING NAME COMMAND TO PLAYER 0");
      report("SENDING NAME COMMAND TO PLAYER 1");
      //Name each client
      toClient0.writeInt(NAME);
      toClient0.writeInt(0);
      toClient0.flush();      
      toClient1.writeInt(NAME);
      toClient1.writeInt(1);
      toClient1.flush();
      //Wait for both players to send the CONFIRM_SHIPS or QUIT_SHIP command 
      boolean done = false;
      boolean shipsReady = false;
      while (!shipsReady)       {
         int cmd0 = fromClient0.readInt();
         int cmd1 = fromClient1.readInt();
         if (cmd0 == CONFIRM_SHIPS && cmd1 == CONFIRM_SHIPS)   {
            boolean c0Ready = fromClient0.readBoolean();
            boolean c1Ready = fromClient1.readBoolean();
            shipsReady = true;
         }
         if (cmd0 == QUIT_SHIP)  {
            report("EARLY QUIT FROM PLAYER 0");
            sendDoneMessage(0);
         }
         if (cmd1 == QUIT_SHIP)  {
            report("EARLY QUIT FROM PLAYER 1");
            sendDoneMessage(1);
         }
      }
      //Start main phase of the game
      updateTurn(0);
      while (!done)  {
         if (runCommand(fromClient0, 0))
            done = true;
         if (!done && runCommand(fromClient1, 1))
            done = true;
      }
      report("GAME FINISHED");
   }
   
   /**
       * Execute all commands until the QUIT command is received from either client, 
       * If there is an unknown command it then stops, and does not continue.
       * @param client the DataInputStream for a given client
       * @param playerNumber the player who is sending the command, for reporting purposes
       * @return true if the client has given a QUIT, WIN, or invalid command, false otherwise
       * @throws IOException if there is a communication error between server/client
       */
   public boolean runCommand(DataInputStream client, int playerNumber) throws IOException   {
      int cmd = client.readInt();
      report("RECEIVED " + cmdToString(cmd) + " COMMAND FROM " +
         "PLAYER " + playerNumber);
      switch (cmd)   {
         case QUIT:
            int player = client.readInt();
            sendDoneMessage(player);
            return true;
         case FIRE:
            player = client.readInt();
            int space = client.readInt();
            if (player == 0)  {
               toClient0.writeInt(FIRE);
               toClient0.writeInt(space);
               toClient0.flush();
               report("SENDING FIRE COMMAND TO PLAYER 0");
            }
            if (player == 1)  {
               toClient1.writeInt(FIRE);
               toClient1.writeInt(space);
               toClient1.flush();
               report("SENDING FIRE COMMAND TO PLAYER 1");
            }
            return false;
         case SUCCESS:
            player = client.readInt();
            int index = client.readInt();
            boolean success = client.readBoolean();
            boolean sunk = client.readBoolean();
            if (player == 0)  {
               toClient1.writeInt(MARK);
               toClient1.writeInt(index);
               toClient1.writeBoolean(success);
               toClient1.writeBoolean(sunk);
               toClient1.flush();
               report("SENDING MARK COMMAND TO PLAYER 1");
            }
            if (player == 1)  {
               toClient0.writeInt(MARK);
               toClient0.writeInt(index);
               toClient0.writeBoolean(success);
               toClient0.writeBoolean(sunk);
               toClient0.flush();
               report("SENDING MARK COMMAND TO PLAYER 0");
            }
            return false;
         case CONFIRM_SHIPS:
            boolean dummyValue = client.readBoolean();
            return false;
         case TURN:
            player = client.readInt();
            if (player == 0)
               updateTurn(1);
            else if (player == 1)
               updateTurn(0);
            report("SENDING TURN COMMAND TO BOTH PLAYERS");
            return false;
         case WIN:
            player = client.readInt();
            toClient0.writeInt(GAME_OVER);
            toClient0.writeInt(player);
            toClient0.flush();
            toClient1.writeInt(GAME_OVER);
            toClient1.writeInt(player);
            toClient1.flush();
            report("SENDING GAME_OVER COMMAND TO BOTH PLAYERS");
            return true;
         default:
            return true;
      }
   }
   
   /**
       * Sends a DONE command to the player who did not send the QUIT command.
       * @param player the player who sent the QUIT command
       * @throws IOException if there is a communication error between server/client
       */
   public void sendDoneMessage(int player) throws IOException   {
      if (player == 0)  {
         toClient1.writeInt(DONE);
         toClient1.flush();
         report("SENDING DONE COMMAND TO PLAYER 1");
      }
      else if (player == 1)   {
         toClient0.writeInt(DONE);
         toClient0.flush();
         report("SENDING DONE COMMAND TO PLAYER 0");
      }
   }
   
   /**
       * Updates the turns of both players, setting the given player to have their turn,
       * and the other to be waiting.
       * @param player the player whose turn it now is
       * @throws IOException if there is a communication error between server/client
       */
   public void updateTurn(int player) throws IOException   {
      if (player == 0)  {
         toClient0.writeInt(TURN);
         toClient0.writeBoolean(true);
         toClient0.flush();
         toClient1.writeInt(TURN);
         toClient1.writeBoolean(false);
         toClient1.flush();
      }
      else if (player == 1)   {
         toClient0.writeInt(TURN);
         toClient0.writeBoolean(false);
         toClient0.flush();
         toClient1.writeInt(TURN);
         toClient1.writeBoolean(true);
         toClient1.flush();
      }
   }
}