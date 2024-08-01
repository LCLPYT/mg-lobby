# mg-lobby
A server-sided Fabric mod that serves as a mini-game launcher.
It introduces a game lifecycle that features a lobby- and a mini-game-phase.
In the lobby phase, players can attend various activities while waiting for the next mini-game to start.

Other Fabric mods serve as mini-game providers, which will be located by this mod.
After the players voted for a mini-game they would like to play *(coming soon™*)*, 
this mod will take care of launching that mini-game.
Upon starting a mini-game, this mod will go inactive and pass control over to the mini-game.
When a mini-game is over, it is expected to pass control back to mg-lobby, which will restart the lifecycle.

**At the moment, an admin has to set the next game via the /setgame command.*

## Integrating Mini-Games with mg-lobby
Documentation of how to write mini-games and integrate them into mg-lobby is coming soon.
For the time being, one can take a look at the setup of [Arcade Party 2](https://github.com/LCLPYT/arcade-party-2), which uses mg-lobby.