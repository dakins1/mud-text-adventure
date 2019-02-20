# mud-text-adventure
My MUD, a text adventure game. 

INSTRUCTIONS:

To play this game, you need a bash terminal. This will not work on Windows. 
(Telnet on Windows transfers information one char at a time, whereas bash telnet
  will communicate line by line. This program is made for bash telnet.)
  
  Compile and run the program to start the server. 
  Then, from the same machine, you can connect to the game from a bash terminal. The command "telnet localhost 4040" should get
  you connected. From there, you can start typing and pressing return to submit your commands to the server. 
  And that's how you play. 
  
  More than one client can play at once. One way to do this would be opening another local terminal,
  and entering "telnet localhost 4040" again. This does create a multiplayer game, but since there is only one keyboard, only one terminal can interact with the server at once. That's no fun. So, from another machine with a bash terminal, you can "telnet <sshName> 4040". The sshName should be the name of the machine hosting the server. Then both machines can simultaneously play and interact with one another. 
  
  
  
