/**
* A BattleShipClient creates a GUI for the user to input an IP address in order to connect to a
* BattleShipServer. If a successful connection made to the server by two clients then a game is started,
* and a GUI is created for the player to place their Ships. Once both players have placed their Ships the
* GUI for the main phase of the game is created and players take turns firing at each others Ships.
*
* @author Alex Perrino
* @version November 29, 2017
*/
import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
public class BattleShipClient extends JFrame implements Runnable, BattleShippable  {
   //Server Variables
   private Socket socket;
   private DataOutputStream toServer;
   private DataInputStream fromServer;
   Thread gameClient;
   //Game Specific Variables
   private int player;
   private boolean isTurn;
   private JComboBox<String> shipList;   
   private Grid playerGrid;
   private Grid opponentGrid;
   private Ship[] playerShips;
   //Game Lobby Specific Varaibles
   private JTextField ipField;
   private JButton searchButton;
   private JLabel searchStatus;
   //GUI specific Variables
   private JFrame shipGUI;
   private JFrame playerGUI;
   private JButton returnButton;
   private JButton quitButton;
   private JButton confirmButton;
   private JButton clearButton;
   private JLabel shipStatus;
   private JLabel gameStatus;
   private JLabel numPlayerShips;
   private JLabel numOpponentShips;
   
   /**
       * Sets up the GUI and connects to the server..
       * @param args the line commands
       */
   public static void main(String[] args)   {
      new BattleShipClient();
   }

   /**
       * Sets up the GUI as well as the I/O streams from the socket.
       */   
   public BattleShipClient()   {
      shipList = new JComboBox<String>(ships);
      shipList.addActionListener(e -> updateActiveShip());
      shipStatus = new JLabel("Select a Ship from the list and then select two spaces.",
         JLabel.CENTER);
      initializeGameData();
      createLobbyGUI();
   }
   
   /**
       * Initializes the game's data, such as the Grids and Ships as well as the other instance variables.
       */ 
   public void initializeGameData()   {
      playerGrid = new Grid(shipStatus);
      opponentGrid = new Grid();
      playerShips = new Ship[NUM_SHIPS];
      createShips(playerShips);
      shipStatus.setText("Select a Ship from the list and then select two spaces.");
      gameStatus = new JLabel("WAITING FOR OTHER PLAYER", JLabel.CENTER);
      numPlayerShips = new JLabel("" + NUM_SHIPS);
      numOpponentShips = new JLabel("" + NUM_SHIPS);
      isTurn = false;
      gameClient = new Thread(this);
      updateActiveShip();
      //~ addDefaultShips();
   }
   
   /**
       * Creates Ships for a given player, each with the appropriate length.
       * @param playerShips the specificed player's array
       */ 
   public void createShips(Ship[] playerShips)  {
      playerShips[0] = new Ship(5);
      playerShips[1] = new Ship(4);
      playerShips[2] = new Ship(3);
      playerShips[3] = new Ship(3);
      playerShips[4] = new Ship(2);
   }
   
   /**
       * Tester function used to reduce the hassle of testing by placing Ships in a default pattern.
       */ 
   private void addDefaultShips() {
      playerGrid.setCurrentShip(playerShips[0]);
      playerGrid.setSpace(playerGrid.getSpace(0));
      playerGrid.setSpace(playerGrid.getSpace(4));
      playerGrid.setCurrentShip(playerShips[1]);
      playerGrid.setSpace(playerGrid.getSpace(10));
      playerGrid.setSpace(playerGrid.getSpace(13));
      playerGrid.setCurrentShip(playerShips[2]);
      playerGrid.setSpace(playerGrid.getSpace(20));
      playerGrid.setSpace(playerGrid.getSpace(22));
      playerGrid.setCurrentShip(playerShips[3]);
      playerGrid.setSpace(playerGrid.getSpace(30));
      playerGrid.setSpace(playerGrid.getSpace(32));
      playerGrid.setCurrentShip(playerShips[4]);
      playerGrid.setSpace(playerGrid.getSpace(40));
      playerGrid.setSpace(playerGrid.getSpace(41));
   }
   
   /** 
       * Continues to send and receive data from the server as long as the server does not send
       * a DONE command.
       */  
   @Override
   public void run()   {
      System.out.println("WAITING TO RECEIVE COMMANDS FROM SERVER");
      try   {
         while (true)  {
            int cmd = fromServer.readInt();
            System.out.println("THE SERVER'S CURRENT COMMAND IS: " + cmdToString(cmd));
            if (cmd == GAME_OVER)   {
               endOfGame();
               return;
            }
            else if (cmd == QUIT_SHIP)   {
               disableShipGUI();
               shipStatus.setText("Game Over - Opponent quit!");
               return;
            }
            else if (cmd == DONE)  {
               endGame();
               return;
            }
            else
               executeCommand(cmd);
         }
      }
      catch (IOException e)   {
         System.out.println("THREAD CLOSING");
      }
      finally  {
         System.out.println("DONE RECEIVING COMMANDS FROM SERVER");
         try   {
            closeStreams();
         }
         catch (IOException e)   {
            System.out.println("FAILED TO CLOSE SOCKET/STREAMS");
            System.out.println(e.getMessage());
         }
      }
   }
   
   /**
       * Given a protocol constant value which has been sent by the server, execute a specific command.
       * The command may or may not send additional protocols back to the server.
       * @param cmd a protocol constant value sent by the server
       * @throws IOException if there is a communication error between server/client
       */ 
   public void executeCommand(int cmd) throws IOException  {
      if (cmd == NAME) {
         player = fromServer.readInt();
         System.out.println("I AM " + player);
         setVisible(false);
         searchStatus.setText(" ");
         setShipGUI();
      }
      else if (cmd == FIRE)  {
         int index = fromServer.readInt();
         GridSpace space = playerGrid.getSpace(index);
         space.setOutOfPlay();
         boolean hit = space.isOccupied();
         boolean sunk = checkShipStatus(space);
         toServer.writeInt(SUCCESS);
         toServer.writeInt(player);
         toServer.writeInt(index);
         toServer.writeBoolean(hit);
         toServer.writeBoolean(sunk);
         toServer.flush();
         playerGrid.updateSpace(space, hit);
         if (sunk)
            updateShipCount(numPlayerShips);
      }
      else if (cmd == MARK)  {
         int index = fromServer.readInt();
         GridSpace space = opponentGrid.getSpace(index);
         boolean success = fromServer.readBoolean();
         boolean sunk = fromServer.readBoolean();
         opponentGrid.updateSpace(space, success);
         if (sunk)
            updateShipCount(numOpponentShips);
         if (Integer.parseInt(numOpponentShips.getText()) == 0) {
            toServer.writeInt(WIN);
            toServer.writeInt(player);
            toServer.flush();
         }
         else  {
            toServer.writeInt(TURN);
            toServer.writeInt(player);
            toServer.flush();
         }
      }
      else if (cmd == TURN)   {
         isTurn = fromServer.readBoolean();
         setTurn();
      }
   }
   
   /**
       * Called when the GAME_OVER command is sent by the server, preventing the player from
       * making any more moves and displays who has won the game.
       * @throws IOException if there is a communication error between server/client
       */ 
   public void endOfGame() throws IOException   {
      isTurn = false;
      int winner = fromServer.readInt();
      addReturnToLobby();
      if (player == winner)   {
         gameStatus.setText("GAME OVER - You won!");
      }
      else  {
         gameStatus.setText("GAME OVER - You lost!");
      }
   }
   
   /**
       * Called when the DONE command is sent by the server, preventing the player from
       * making any more moves and displays that the opponent left the game.
       * @throws IOException if there is a communication error between server/client
       */ 
   public void endGame() throws IOException  {
      isTurn = false;
      gameStatus.setText("GAME HAS ENDED - Opponent quit!");
      addReturnToLobby();
   }
   
   /**
       * Sets up the initial game lobby GUI, and creates the connection/socket.
       */ 
   public void createLobbyGUI()  {
      final int FRAME_WIDTH = 900;
      final int FRAME_HEIGHT = 250;
      final int IP_WIDTH = 10;
      JPanel infoPanel = createInfoPanel();
      JPanel gamePanel = new JPanel(new BorderLayout());
      JPanel searchPanel = new JPanel();
      JLabel gameLabel = new JLabel("The game will begin after another player has been found.", JLabel.CENTER);
      searchButton = new JButton("Search for Game");
      JLabel ipLabel = new JLabel("IP Address: ");
      ipField = new JTextField(IP_WIDTH);
      ipField.addActionListener(e -> searchForGame());
      searchStatus = new JLabel(" ", JLabel.CENTER);
      searchButton.addActionListener(e -> searchForGame());
      gamePanel.add(gameLabel, BorderLayout.NORTH);
      searchPanel.add(ipLabel);
      searchPanel.add(ipField);
      searchPanel.add(searchButton);
      gamePanel.add(searchPanel, BorderLayout.CENTER);
      gamePanel.add(searchStatus, BorderLayout.SOUTH);
      add(infoPanel, BorderLayout.NORTH);
      add(gamePanel, BorderLayout.SOUTH);
      setTitle("BattleShip - Lobby");
      setSize(FRAME_WIDTH, FRAME_HEIGHT);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setVisible(true);
   }
   
   /**
       * Creates the informative panels for the game lobby's GUI.
       * @return the completed panel for the game lobby's GUI.
       */ 
   public JPanel createInfoPanel()  {
      JPanel infoPanel = new JPanel(new BorderLayout());
      JPanel instructionPanel = new JPanel(new BorderLayout());
      JLabel header = new JLabel("Instructions:", JLabel.CENTER);
      JLabel upperInfo = new JLabel("Place your battleships within the playing area by selecting " +
         "one from the dropdown menu and then selecting two spaces to be its end points.", JLabel.CENTER);
      JLabel middleInfo = new JLabel("Once all five ships have been placed confirm your placement and wait " +
         "for the other player to place all of their ships.", JLabel.CENTER);
      JLabel middle2Info = new JLabel("Once both players have placed their ships " +
         "the game will begin. Players then take turns firing at the opponent's ships " +
         "and try to sink them.", JLabel.CENTER);
      JLabel lowerInfo = new JLabel("A O represents where your ships are, a black X represents a missed shot, " +
         " and a red X represents a successful shot.", JLabel.CENTER);
      instructionPanel.add(middleInfo, BorderLayout.NORTH);
      instructionPanel.add(middle2Info, BorderLayout.CENTER);
      instructionPanel.add(lowerInfo, BorderLayout.SOUTH);
      infoPanel.add(header, BorderLayout.NORTH);
      infoPanel.add(upperInfo, BorderLayout.CENTER);
      infoPanel.add(instructionPanel, BorderLayout.SOUTH);
      return infoPanel;
   }
   
   /**
       * Creates a GUI for the Ship placement phase of the game.
       */ 
   public void setShipGUI()   {
      final int FRAME_WIDTH = 700;
      final int FRAME_HEIGHT = 900;
      isTurn = true;
      shipGUI = new JFrame();
      JPanel upperPanel = new JPanel();
      JPanel infoPanel = new JPanel(new BorderLayout());
      upperPanel.add(new JLabel("Selected Ship: "));
      upperPanel.add(shipList);
      confirmButton = new JButton("Confirm Ships");
      confirmButton.addActionListener(e -> validateShips());
      quitButton = new JButton("Quit Game");
      quitButton.addActionListener(e -> quitShipScreen());
      clearButton = new JButton("Clear Ship");
      clearButton.addActionListener(e -> resetShip());
      upperPanel.add(clearButton);
      upperPanel.add(confirmButton);
      infoPanel.add(upperPanel, BorderLayout.NORTH);
      infoPanel.add(shipStatus, BorderLayout.SOUTH);
      shipGUI.add(infoPanel, BorderLayout.NORTH);
      shipGUI.add(playerGrid);
      shipGUI.add(quitButton, BorderLayout.SOUTH);
      shipGUI.setTitle("BattleShip - Set Ships");
      shipGUI.setSize(FRAME_WIDTH, FRAME_HEIGHT);
      shipGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      shipGUI.setVisible(true);
   }
   
   /**
       * Creates a GUI for a specified player with a specified opponent for the main phase of the game.
       * @param playerGrid the player
       * @param opponentGrid the opponent of player
       */ 
   public void createGUI(Grid playerGrid, Grid opponentGrid) {
      final int FRAME_WIDTH = 1050;
      final int FRAME_HEIGHT = 500;
      isTurn = false;
      playerGUI = new JFrame();
      JPanel p1 = new JPanel(new BorderLayout());
      JPanel p2 = new JPanel(new BorderLayout());
      JPanel p1info = new JPanel();
      JPanel p2info = new JPanel();
      p1info.add(new JLabel("Your Remaining Ships: "));
      p1info.add(numPlayerShips);
      p1.add(p1info, BorderLayout.NORTH);
      p1.add(playerGrid);
      p2info.add(new JLabel("Opponent's Remaining Ships: "));
      p2info.add(numOpponentShips);
      p2.add(p2info, BorderLayout.NORTH);
      p2.add(opponentGrid);
      opponentGrid.shipsAreSet();
      addActionListeners();
      quitButton = new JButton("Quit Game");
      quitButton.addActionListener(e -> quitGame()); 
      playerGUI.add(gameStatus, BorderLayout.NORTH);
      playerGUI.add(p1, BorderLayout.WEST);
      playerGUI.add(p2, BorderLayout.EAST);
      playerGUI.add(quitButton, BorderLayout.SOUTH);   
      playerGUI.setFocusable(true);
      playerGUI.setSize(FRAME_WIDTH, FRAME_HEIGHT);
      playerGUI.setTitle("BattleShip - Game");
      playerGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      playerGUI.setVisible(true);
   }
   
   /**
       * Adds a return to lobby button to the game in order to play additional games.
       * To be used if a player quits or the game has been won.
       */ 
   public void addReturnToLobby()   {
      returnButton = new JButton("Return to Lobby");
      returnButton.addActionListener(e -> returnToLobby());
      playerGUI.add(returnButton, BorderLayout.SOUTH);
      quitButton.setVisible(false);
      returnButton.setVisible(true);
   }
   
   /**
       * Adds a return to lobby button to the game in the ship placement GUI in order to play additional games.
       * To be used if a player quits during the ship placement phase.
       */ 
   public void addReturnToLobbyFromShip()   {
      returnButton = new JButton("Return to Lobby");
      returnButton.addActionListener(e -> returnToLobby());
      shipGUI.add(returnButton, BorderLayout.SOUTH);
      quitButton.setVisible(false);
      returnButton.setVisible(true);
   }
   
   /**
       * Disposes of the current game's GUIs and reinitializes the game's data in order to play again.
       */ 
   public void returnToLobby()   {
      shipGUI.dispose();
      playerGUI.dispose();
      setVisible(true);
      initializeGameData();
      searchButton.addActionListener(e -> searchForGame());
      ipField.addActionListener(e -> searchForGame());
   }
   
   /**
       * Attempts to create a connection to the IP Address given in the JTextField in the GUI.
       * If it succeeds it then starts a thread, and disables the searchButton from searching again.
       */ 
   public void searchForGame()  {
      String hostName = ipField.getText();
      if (openConnection(hostName)) {
         searchStatus.setText("Connection successful: waiting for other player.");
         removeButtonListeners(searchButton);
         removeTextFieldListeners(ipField);
         gameClient.start();
      }
      else
         searchStatus.setText("Connection failed: try again.");
   }
   
   /**
       * Updates the currentShip and shipIndex of the specified Grid using the JComboBox.
       */
   public void updateActiveShip()   {
      int shipIndex = shipList.getSelectedIndex();
      playerGrid.setCurrentShip(playerShips[shipIndex]);
      playerGrid.setShipIndex(shipIndex);
   }
   
   /**
       * Resets the currently selected Ship in the JComboBox.
       */
   public void resetShip() {
      int shipIndex = shipList.getSelectedIndex();
      Ship currentShip = playerShips[shipIndex];
      if (currentShip.isSet())   {
         int shipLength = currentShip.getLength();      
         currentShip.clearSpaces();
         playerShips[shipIndex] = new Ship(shipLength);
         playerGrid.updateGridDisplay('O');
         shipStatus.setText("Removed " + getShipString(shipIndex) + "!");
      }
      else  {
         shipStatus.setText(getShipString(shipIndex) + " has not been " +
            "set yet!");
      }
      updateActiveShip();
   }
   
   /**
       * Checks the status of a Ship at a given GridSpace. If it finds a Ship at the given GridSpace
       * and the Ship has been sunk it returns true. If it finds a Ship which has not been sunk, or
       * does not find a Ship at all it will return false.
       * @param space the GridSpace which was fired at
       * @return true if a Ship at the space has been sunk, false otherwise
       */    
   public boolean checkShipStatus(GridSpace space) {
      for (int i = 0; i < playerShips.length; i++) {
         if (playerShips[i].atSpace(space))
            return playerShips[i].isSunk();
      }
      return false;
   }
   
   /**
       * Sends a CONFIRM_SHIPS message to the server.
       */
   public void sendConfirmMessage() {
         try   {
            toServer.writeInt(CONFIRM_SHIPS);
            toServer.writeBoolean(true);
            toServer.flush();
         }
         catch (IOException e)   {
            System.out.println("ERROR SENDING CONFIRM MESSAGE");
            e.printStackTrace();
         }
   }
   
   /**
       * Sends a QUIT message to the server and enables the JButton to return to lobby.
       */
   public void quitGame()  {
      if (isTurn) {
         try   {
            toServer.writeInt(QUIT);
            toServer.writeInt(player);
            toServer.flush();
            isTurn = false;
            gameStatus.setText("Game Over - You quit!");
            addReturnToLobby();
         }
         catch (IOException e)   {
            System.out.println("ERROR SENDING QUIT MESSAGE");
            e.printStackTrace();
         }
      }
   }
   
   /**
       * Sends a QUIT_SHIP message to the server from the shipGUI frame and enables the 
       * JButton to return to lobby.
       */
   public void quitShipScreen()  {
      try   {
         toServer.writeInt(QUIT_SHIP);
         toServer.writeInt(player);
         toServer.flush();
         isTurn = false;
         shipStatus.setText("Game Over - You quit!");
         disableShipGUI();
         addReturnToLobbyFromShip();
      }
      catch (IOException e)   {
         System.out.println("ERROR SENDING QUIT_SHIP MESSAGE");
         e.printStackTrace();
      }
   }
   
   /**
       * Removes the ActionListeners from the Ship placement GUI.
       */
   public void disableShipGUI()  {
      removeButtonListeners(confirmButton);
      removeButtonListeners(clearButton);
   }
   
   /**
       * Closes the Sockets and DataInput/OutPutStreams.
       * @throws IOException if there is an error closing the sockets/streams.
       */
   public void closeStreams() throws IOException   {
      fromServer.close();
      toServer.close();
      socket.close();
   }
   
   /**
       * Validates whether all of a players Ships have been placed properly or not.
       */ 
   public void validateShips()   {
      if (areShipsPlaced())   {
         playerGrid.shipsAreSet();
         createGUI(playerGrid, opponentGrid);
         shipGUI.setVisible(false);
         sendConfirmMessage();
      }
      else
         shipStatus.setText("Ships have not been placed yet!");
   }
   
   /**
       * @return true if all of the Ships have been placed, false otherwise
       */ 
   public boolean areShipsPlaced()  {
      for (int i = 0; i < NUM_SHIPS; i++) {
         if (!playerShips[i].isSet())
            return false;
      }
      return true;
   }
   
   /**
       * Adds an ActionListener to every GridSpace in the opponent's Grid. The ActionListener
       * is for the FIRE command which communicates with the server.
       */   
   public void addActionListeners() {
      for (int i = 0; i < GRID_DIMENSION; i++)  {
         for (int j = 0; j < GRID_DIMENSION; j++)  {
            GridSpace tmp = opponentGrid.getSpace(i, j);
            tmp.addActionListener(e -> sendFireCommand(tmp));
         }
      }
   }
   
   /**
       * Removes the ActionListeners for a given JButton.
       * @param button the JButton which should have its listeners removed
       */
   public void removeButtonListeners(JButton button) {
      ActionListener[] listeners = button.getActionListeners();
      for (int i = 0; i < listeners.length; i++)
         button.removeActionListener(listeners[i]);
   }
   
   /**
       * Removes the ActionListeners for a given JTextField.
       * @param textField the JTextField which should have its listeners removed
       */
   public void removeTextFieldListeners(JTextField textField)  {
      ActionListener[] listeners = textField.getActionListeners();
      for (int i = 0; i < listeners.length; i++)
         textField.removeActionListener(listeners[i]);
   }
   
   /**
       * Removes the ActionListeners for a given space and replaces them with a new listener.
       * The new listener warns that this space has already been fired at.
       * @param space the GridSpace which should have its listeners removed
       */
   public void removeActionListeners(GridSpace space) {
      ActionListener[] listeners = space.getActionListeners();
      for (int i = 0; i < listeners.length; i++)
         space.removeActionListener(listeners[i]);
      space.addActionListener(e -> invalidSpaceMessage());
   }
   
   /**
       * Fires a shot at the opponent's grid if it is the player's turn.
       * @param space the GridSpace being fired at
       */
   public void sendFireCommand(GridSpace space) {
      if (isTurn) {
         try   {
            toServer.writeInt(FIRE);
            if (player == 0)
               toServer.writeInt(1);
            if (player == 1)
               toServer.writeInt(0);
            int location = space.getSingleIndex(GRID_DIMENSION);
            toServer.writeInt(location);
            System.out.println("Client" + player + " FIRING AT " + location + ".");
            toServer.flush();
            removeActionListeners(space);
         }
         catch (IOException e)   {
            System.out.println("ERROR FIRING");
         }
      }
   }

   /**
       * Sets the message of a player's GUI to warn of an invalid move.
       */
   public void invalidSpaceMessage() {
      if (isTurn)
         gameStatus.setText("Your Turn - Invalid Move!");
   }
   
   /**
       * Sets the turn of the player based on the current value of isTurn, updating the GUI label value.
       */ 
   public void setTurn()  {
      if (isTurn)
         gameStatus.setText("Your Turn");
      else
         gameStatus.setText("Opponent's Turn");
   }
   
   /**
       * Decrements the given label's text value by one.
       * @param label the label to be decremented
       */ 
   public void updateShipCount(JLabel label) {
      int newCount = Integer.parseInt(label.getText()) - 1;
      label.setText("" + newCount);
   }
   
   /**
       * Creates a socket with the PORT and opens its input 
       * and output streams, fromServer and toServer. 
       */
   private boolean openConnection(String serverHost)    {
      try   {
         this.socket = new Socket(serverHost, PORT);
         this.fromServer = new DataInputStream(socket.getInputStream());
         this.toServer = new DataOutputStream(socket.getOutputStream());
         return true;
      }
      catch (SecurityException e)      {
         System.err.print("a security manager exists: ");
         System.err.println("its checkConnect doesn't allow the connection");
         return false;
      }
      catch (UnknownHostException e)       {
         System.err.println("The IP address of the host could not be found");
         return false;
      }
      catch (IOException e)       {
         System.err.println("Cannot connect to the server \"" + serverHost + "\"");
         return false;
      }
   }
}