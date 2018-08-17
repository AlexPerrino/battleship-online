public interface BattleShippable {
   /**
       * Names of each ship.
       */
   String[] ships = { "Aircraft Carrier - 5", "Battleship - 4", 
      "Submarine - 3", "Cruiser - 3", "Destroyer - 2"};
   /**
       * Number of ships each player has.
       */
   int NUM_SHIPS = 5;
   
   /**
       * Size of the Grid.
       */
   int GRID_DIMENSION = 10;
   
   /**
       * The default port.
       */
   int PORT = 4444;

   //Default hostname for testing purposes
   String HOST = "localhost";

   //Protocol Constant Values
   
   /**
       * Message sent by the server to the client <br>
       * NAME takes one integer argument  <br>
       * <p>
       * NAME <em>n</em>
       * </p>
       * where <em>n</em> is the name (number) of the client. 
       */
   int NAME = 100;      
   
   /**
       * Message sent by the client to the server <br>
       * CONFIRM_SHIPS takes one boolean argument <br>
       * <p>
       * CONFIRM_SHIPS <em>bool</em>
       * </p>
       * where <em>bool</em> is the boolean confirmation from the client. Sent when
       * both of the players have placed their ships and are ready.
       */
   int CONFIRM_SHIPS = 101;
   
   /**
       * Message sent by either the client or the server <br>
       * FIRE takes two integer arguments <br>
       * <p>
       * FIRE <em>player</em> <em>space</em>
       * </p>
       * where <em>player</em> is the player who is being fired at, and space is the
       * single digit index of the GridSpace being fired at. Sent by a player during their
       * turn in an attempt to try and sink their opponent's battleships.
       */
   int FIRE = 102;
   
   /**
       * Message sent by the client to the server <br>
       * SUCCESS takes two integer arguments followed by two boolean arguments<br>
       * <p>
       * SUCCESS <em>player</em> <em>index</em> <em>success</em> <em>sunk</em>
       * </p>
       * where <em>player</em> is the player who was fired at, and index is the
       * single digit index of the GridSpace being fired at. Additionally, success is
       * whether or not the shot was a hit or not, and sunk is whether or not the Ship was sunk.
       * Used to indicate to the server if the shot succeeded or not so it can relay back to the 
       * player who fired the shot originally.
       */
   int SUCCESS = 103;
      
   /**
       * Message sent by the server to the client <br>
       * MARK takes one integer argument followed by two boolean arguments<br>
       * <p>
       * MARK <em>index</em> <em>success</em> <em>sunk</em>
       * </p>
       * where <em>index</em> is the space that was fired at, success is
       * whether or not the shot was a hit or not, and sunk is whether or not the Ship was sunk.
       * Used to update the display of a player who has fired a shot.
       */
   int MARK = 104;
   
   /**
       * Message sent by the client to the server or vice-versa <br>
       * TURN takes one boolean argument if sent by the server OR <br>
       * TURN takes one integer argument if sent by the client  <br>
       * <p>
       * TURN <em>bool</em> OR <br>
       * TURN <em>player</em>
       * </p>
       * where <em>bool</em> is the boolean value from the server telling the player
       * if it is their turn or not, and player is the player whose turn has just finished. 
       * Turns are set by the server at the start of the game, but after that the server only
       * sets the current turn after receiving a TURN command from a player.
       */
   int TURN = 105;
   
   /**
       * Message sent by the client to the server <br>
       * WIN takes one integer argument <br>
       * <p>
       * WIN <em>player</em>
       * </p>
       * where <em>player</em> is the player who has won the game. Used to notify
       * the server that the game has been won.
       */
   int WIN = 106;
   
   /**
       * Message sent by the server to the client <br>
       * GAME_OVER takes one integer argument <br>
       * <p>
       * GAME_OVER <em>player</em>
       * </p>
       * where <em>player</em> is the player who has won the game. Used to notify
       * the players that the game has ended and been won.
       */
   int GAME_OVER = 107;
   
   /**
       * Message sent by the server to the client <br>
       * DONE does not have arguments <br>
       * <p>
       *    DONE 
       * </p>
       * DONE ends the game for the player it is sent to. DONE is used to notify a client
       * that the game has ended early.
       */
   int DONE = 108;
   
   /**
       * Message sent by the client to the server <br>
       * QUIT_SHIP takes one integer argument <br>
       * <p>
       * QUIT_SHIP <em>player</em>
       * </p>
       * where <em>player</em> is the player who is quitting the game. Used to notify the
       * server that a client has quit the game at the ship placement stage.
       */
   int QUIT_SHIP = 109;
   
   /**
       * Message sent by the client to the server <br>
       * QUIT takes one integer argument <br>
       * <p>
       * QUIT <em>player</em>
       * </p>
       * where <em>player</em> is the player who is quitting the game. Used to notify the
       * server that a client has quit the game during the main phase.
       */
   int QUIT = 110;
   
   /**
      * Converts an integer command cmd to its string representation. 
      * <p>Supported commands are <br>
      *    NAME   server  &rarr; client, one int <br>
      *    CONFIRM_SHIPS     client  &rarr; server, one boolean <br>
      *    FIRE      server  &larr;&rarr; client, two ints <br>
      *    SUCCESS      client  &rarr; server, two ints followed by two booleans <br>
      *    MARK   server  &rarr; client, one int followed by two booleans <br>
      *    TURN   client  &larr;&rarr; server, one int or one boolean <br>
      *    WIN   client  &rarr; server, one int <br>
      *    GAME_OVER   server  &rarr; client, one int <br>
      *    DONE   server  &rarr; client, no arguments <br>
      *    QUIT_SHIP      client  &larr;&rarr; server, one int <br>
      *    QUIT      client  &rarr; server, one int <br>
      * </p>
      * A command that is not supported returns the string
      * "UNRECOGNIZABLE COMMAND".
      * @param cmd an integer corresponding to a command
      * @return String the textual representation of the command cmd
      */ 
   default String cmdToString(int cmd)   {
      String cmdString;
      switch (cmd)      {
         case NAME: 
            cmdString = "NAME";
            break;
         case CONFIRM_SHIPS:
            cmdString = "CONFIRM_SHIPS";
            break;
         case FIRE:
            cmdString = "FIRE";
            break;
         case SUCCESS:
            cmdString = "SUCCESS";
            break;
         case MARK: 
            cmdString = "MARK";
            break;
         case TURN:
            cmdString = "TURN";
            break;
         case WIN: 
            cmdString = "WIN";
            break;
         case GAME_OVER:
            cmdString = "GAME_OVER";
            break;
         case DONE:
            cmdString = "DONE";
            break;
         case QUIT_SHIP:
            cmdString = "QUIT_SHIP";
            break;
         case QUIT:
            cmdString = "QUIT";
            break;
         default:
            cmdString = "UNRECOGNIZABLE COMMAND";
      }
      return cmdString;
   }
   
   /**
      * Converts an integer index shipNo to its Ship string representation. 
      * A command that is not supported returns the string "Unknown Ship".
      * @param shipNo an integer corresponding to a Ship index.
      * @return the textual representation of the Ship.
      */ 
   default String getShipString(int shipNo)  {
      String shipName;
      switch (shipNo) {
         case 0:
            shipName = "Aircraft Carrier";
            break;
         case 1:
            shipName = "Battleship";
            break;
         case 2:
            shipName = "Submarine";
            break;
         case 3:
            shipName = "Cruiser";
            break;
         case 4:
            shipName = "Destroyer";
            break;
         default:
            shipName = "Unknown Ship";
      }
      return shipName;
   }
}