# Troubleshooting

This document covers some common troubleshooting items for the OpenLiberty Space Rover mission demo.

## Local Network

**Ensure that the TP-Link WiFi Router is connected and on, and that the Mini PC is connected to the OL_DEMO internet.** 

If the local WiFi network is not up, or if the Mini PC is not connected to the OL_DEMO internet, nothing will work. The following list includes some signs of the WiFi network not being up:
* Both the Space Rover and the Game Board will continue blinking their respective light patterns for attempting to connect to WiFi. Those patterns can be seen for the Space Rover [here](./setup.md#space-rover-state-light-patterns) and for the Game Board [here](./setup.md#game-board-state-light-patterns).
* When attempting to play the game, the UI will return a message that says there was a failure to connect to the game service. This is because the game service must run on the OL_DEMO internet in order to successfully send the websocket connection.

## Devices

**Ensure that both the Space Rover and Game Board are receiving power.**

Check:

- Is the power cord plugged into the device?
- Is the power cord plugged into the power bar?
- Are the devices turned on using the switches on each of their respective power cords?

If the devices are receiving power, then they should both immediately display individual light patterns that represent an attempt to connect to the local WiFi network.

The Space Rover and Game Board should both require no additional setup after completing the setup steps [here](./setup.md#space-rover) and [here](./setup.md#game-board) respectively. They will both automatically attempt to connect to WiFi, and after a successful WiFi connection, will indefinitely wait for a websocket connection from the game service to start/play the game. Both devices will immediately display light patterns after being turned on to represent different setup, standby, and gameplay states, that can be used for troubleshooting. See the light patterns for the Space Rover [here](./setup.md#space-rover-state-light-patterns) and for the Game Board [here](./setup.md#game-board-state-light-patterns) to troubleshoot what state the devices are currently in (ie. connecting to WiFi, waiting for a websocket connection, etc.). _**A game can only be started when both devices are connected to a websocket.**_

In the case where no LEDs are turned on after powering up either the Space Rover or the Game Board, and it is verified that both devices are receiving power, this may designate an issue with the circuitry inside the devices. See [here](../devices/space-rover/README.md) for the circuit schematics of the Space Rover, and [here](../devices/game-board/README.md) for the circuit schematics of the Game Board. To access the circuitry/breadboard of the Space Rover, unscrew the top half of the spaceship model. The circuitry/breadboard of the Game Board can be accessed underneath Board 1 (all the four boards are labelled with a number underneath).

## Space Rover

Occasionally when moving FORWARDS, the Space Rover may stall (you will be able to hear the sound of the motors, but the car will not move). To resolve this, just move the Rover in any of the other three directions (LEFT, RIGHT, BACKWARDS).

## Game Board

The addressable LEDs in the Game Board are connected in series in one long line, so if there is any connection failure between the LEDs, you will be able to find the specific connection to address the issue. For example, if the first one hundred LEDs are lighting up, but the rest are off, then there may be a bad connection between the hundreth LED and the hundred-and-first LED. After a successful WiFi connection, all LEDs flash green once, so you can use this to verify that all the LEDs are working and well-connected. Turn off and on the Game Board to verify this.

If there is any unsual flickering of lights, verify in the circuitry that the dataline of the LED strip is properly connected to the logic level shifter and ESP8266 WiFi Module. See the circuitry diagram [here](../devices/game-board/README.md).

## Mini PC

The Mini PC should automatically set everything up; the expected behaviour after setup can be seen [here](../documentation/setup.md#mini-pc). If there are any issues when starting a new game with connecting to the game service, the Space Rover, or the Game Board, verify the following:

- Is the Mini PC connected to the OL_DEMO internet?
- Is the Space Rover ON and waiting for a websocket connection via its corresponding light pattern?
- Is the Game Board ON and waiting for a websocket connection via its corresponding light pattern?

You can also verify that the containers are up and running with a:

```
docker ps
```

Any other unusual issues will most likely be resolved with a restart of the containers. In the `/services` directory, use:
```
docker-compose down

docker-compose up
```

## Laptop/Gesture Control

If there are any issues when connecting the Gestures to the Game Service, verify the following:

  - Is the Mini PC connected to the OL_DEMO internet?
  - Are the Mini PC services up and running?
  - Is the Laptop Connected to the OL_DEMO internet?

    If you still have a problem after verifying this, please restart the Python Script. 

    If this problem still persists, follow the [cleanup](https://github.com/OpenLiberty/space-rover-mission/blob/main/documentation/cleanup.md#laptopgesture-control) and [startup](https://github.com/OpenLiberty/space-rover-mission/blob/main/documentation/setup.md#laptop-with-a-camera) process for the Laptop again.


If the gesture control is connected to the game service, but the Space Rover is not moving, verify the following:

  - Is the Laptop Connected to the OL_DEMO internet?
  - Are the hardware components (Space Rover and Game Board) setup correctly?
  - Has the game started on the Front-End GUI by clicking `Begin Mission`?
  - Does the console logs on VS Code recieve messages about the hnad gestures dectected signs?

    If you still have a problem after verifying this, please restart the Python Script to re-establish connection to the game service.

    If this problem still persists, follow the [cleanup](https://github.com/OpenLiberty/space-rover-mission/blob/main/documentation/cleanup.md#laptopgesture-control) and [startup](https://github.com/OpenLiberty/space-rover-mission/blob/main/documentation/setup.md#laptop-with-a-camera) process for the Laptop again.
