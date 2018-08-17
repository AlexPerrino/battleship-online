# battleship-online
Server based battleship game which can host multiple games between pairs of players (written in Java).

The server awaits connections from two players before starting a game between them, and then waits for the next pair of players.
Players are able to join by inputting the server's IP address into their client from the "game lobby". Instructions as to how to 
play are provided to the players in this lobby as well. Whenever a game ends, whether a player quits, wins, or loses, the player
is able to return back to this lobby and connect to the server once more to play again.

Two batch files are included for creating both a client and a server jar file for conveinence.

Additionally, three batch files have been included for basic testing: one to start a client, another to start a server,
and a final one for full game testing with one server and two clients.
