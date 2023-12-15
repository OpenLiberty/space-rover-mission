# Game Board

The Game Board contains an ESP8266 NodeMCU - it handles the WiFi connection to the services and controls a WS2811 addressable LED strip with 374 LEDs that represent the planets and obstacles on the board.

The NodeMCU will receive Game Start, Game End, and colour messages (Blue - "BLU", Green - "GRN", Yellow - "YW", Purple - "PUR", Red - "RED") from the game service via a web socket connection. The colour messages are used during game play to determine which LEDs should be turned off/kept on.

## Circuit Schematic Diagram

In the circuit diagram below, you can find the wire connections between the NodeMCU, WS2811 addressable LED strip, and external power source.

### LED Pin Mapping to NodeMCU:
- WS2812B LED strip -> GPIO3

![Game Board Circuit Schematic Diagram](../../../../assets/GameBoard_Circuit_Schematic.png)
