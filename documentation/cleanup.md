# Cleanup 

This document covers the cleanup process for the OpenLiberty Space Rover mission demo. To see all the separate components to keep track of during cleanup, see [here](./cleanup.md/#full-item-list).

## Local Network

1. Unplug the WiFi router.

## Space Rover

1. Turn OFF the Space Rover using the switch in its power cord.
2. Unplug the power cord from the Space Rover.

## Game Board

1. Turn OFF the Game Board using the switch in its power cord.
2. Unplug the power cord from the Game Board.
3. Remove the four plexiglass sheets from the top of the board.
4. Untwist the twist-tie on the bottom-left corner of the barrier (leave the twist-tie looped loosely so that it can be used again for future setups).
5. Remove the barrier from around the Game Board and fold it flat, per the following image:

![Flattened barrier](../images/game-board/gb-flat-barrier.png)

6. Remove the four clamps from underneath the board intersections. **REMEMBER TO PUT THE FOUR CLAMPS BACK INTO THE ZIPLOCK BAG THEY CAME IN.**
7. Pull the connected sections of the connectors out of the board holes.
8. Disconnect the connectors.
9. All the components should now be separated.

## Mini PC

1. Stop the docker containers, using:
    ```
    docker-compose down
    ```
2. Return to the homepage of the game.
3. Shut down the Mini PC.

## Gesture Control

1. Shut down the laptop hosting the gesture control.

## Full Item List

To keep track of all the components and parts being shipped/packed, the following section lists all of the separate components to keep track of.

### WiFi Router + Power

- [ ] Wifi Router + Router Cord
- [ ] Power Bar

### Pelican Box (contains Space Rover)

- [ ] Space Rover
- [ ] Power Source Unit + Switch
- [ ] Mini PC

### Game Board

- [ ] Four Boards
- [ ] Four Black Connectors
- [ ] Four Plexiglass Sheets
- [ ] Eight Plexiglass Barriers
- [ ] Power Source Unit
- [ ] Screws and Washers (set of 16, bring extra)

### Mini PC

- [ ] Mini PC Cable
- [ ] Keyboard

### Gesture Control

- [ ] Space Rover Laptop

### Extra Tools

- [ ] Arduino
- [ ] NodeMCU
- [ ] Solder/Soldering Iron
- [ ] RFID Sensor





