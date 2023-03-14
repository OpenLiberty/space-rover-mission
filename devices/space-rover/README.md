# Open Liberty Space Rover

![PXL_20220601_195408183 PORTRAIT](https://user-images.githubusercontent.com/29461649/171947809-2c4b5d0b-51a4-471a-8e71-2164c8137d4f.jpg)

The Space Rover has 6 addressable (WS2811) LEDs underneath the spaceship model, one white head light LED, and two red tail light LEDs. The "front" of the Space Rover is designated by the white head light, and all directions for gesture control/keyboard control movement of the Rover follow that convention; for example, FORWARD will drive in the direction the white head light LED is facing.

The Space Rover contains two microcontrollers: Teensy 4.1 and NodeMCU.

The NodeMCU handles the WiFi connection to the services and controls the 4 DC motors for the Rover's movement.

The Teensy 4.1 controls the RFID sensor for planet detection, along with the LEDs that are connected to the Rover, including the head and tail lights.

Both the Teensy and NodeMCU are serially connected, via the hardware TX and RX pins on both microcontrollers.

The NodeMCU will receive Game Start, Game End, and Directional (Forward - F, Backward - B, Left - L, Right - R, Stop - S) messages from the game service via a websocket connection. The Game Start and Game End messages will be forwarded serially to Teensy, to notify the LEDs to react accordingly. Once the game play is in session, the RFID sensor will send the detected planets (Blue - "BLU", Green - "GRN", Yellow - "YW", Purple - "PUR", Red - "RED") to the NodeMCU, which will then be forwarded to the game service.

## Circuit Schematic Diagram

In the circuit diagram below, you can find the wire connections between the Teensy, NodeMCU, H-Bridge Motor Driver, 4 x DC motors, Color sensor, and the Rover LEDs.

### L298N H-Bridge Motor Driver Pin Mapping to NodeMCU:
- ENA -> D5
- IN1 -> D8
- IN2 -> D7
- IN3 -> D4
- IN4 -> D3
- ENB -> D6

### Battery voltage measurement to Analog Pin on NodeMCU:
- 7.2V to 3.3V Voltage Divider -> A0

### RFID Sensor Pin Mapping to Teensy:
- SDA -> 10
- SCK -> 13
- MOSI -> 11
- MISO -> 12
- IRQ -> NOT CONNECTED
- GND -> GND
- RST -> 9
- 3.3V -> 3.3V

### LED Pin Mapping to Teensy:
- Addressable LEDs -> 6
- Headlight -> 5 & 7

![Space Rover Circuit Schematic Diagram](../../images/SpaceRover_Circuit_Schematic.png)

