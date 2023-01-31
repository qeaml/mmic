# mmic

Small client-side mod. Below is a full list of additions and changes.

## Small Cosmetic Changes

* Block outline color can be changed.
* The little chat indicators on the left side of the chat can be disabled.
* Keys bound to the hotbar slots can be shown on top of the hotbar.
* Chunks can have randomly generated names.

## Sessions

You can now keep track of when you play and what you play.

Three types of sessions exist:
* **Game Sessions**, which last as long as the game is open.
* **World Sessions**, which last as long as you are in a singleplayer world.
* **Server Sessions**, which last as long as you are in a multiplayer server.

## MiniF3

Replaces the default bloated F3 debug HUD with a smaller, less information-packed
version. It is (just like all other mod features) optional, and can be toggled
on/off in the mod config.

![Minimised Debug HUD](f3mini.png) ![Default Debug HUD](f3default.png)

## Auto-Replant

Whenever harvesting a crop, it will be automatically replanted. This only
happens under the following conditions:

* Auto-Replant is enabled in the MMIC config.
* (If Sneaky Auto-Replant is enabled) The player is sneaking.
* The player has the corresponding seed in their hotbar.

## Static Hand

Disables the hand swing animation and the item equip/use animation. This does
not affect eating animations or bow/crossbow/trident draw animations.

## Brightness

Adds keybinds for increasing and decreasing the brightness options mid-game
(without having to view the options menu) as well as a fullbright toggle.

## Better Signs

### Centered Sign Text

This causes text on all signs to be always centered, regardless of how it is
laid out on the actual sign.

### Perfect Wall Signs

Moves the wall signs closer to the block they're on. This gets rid of that
tiny little gap between the sign and the wall. (Hitboxes are not affected by
this)

## Fake Lag

Adds a lag toggle with multiple different types of lag:

### Block

Prevents all outgoing packets from reaching the server until lag is toggled off.

### Clog

Stops all outgoing packets, sending them once lag is toggled off.

### Lossy Block

Prevents ~25% of all outgoing packets from reaching the server.

### Lossy Clog

Stops ~25% outgoing packets, sending them once lag is toggled off. Packets that
are not stopped reach the server as usual.

## Zoom

Adds a simple zoom system that modifies the in-game FOV. The zoom amount can be
changed permanently in the mod config, or temporarily by using the scroll wheel
while zoomed in.

## Grids

Adds a couple grids that one may find in a camera (e.g. rule of thirds, golden
ratio), all togglable via keybinds.
