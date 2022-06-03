/* 
 *  NodeMCU GPIO - Pin Mapping
 * D0 - 16
 * D1 - 5
 * D2 - 4
 * D3 - 0
 * D4 - 2
 * D5 - 14
 * D6 - 12
 * D7 - 13
 * D8 - 15
*/

/**********************
  Wheel DC Motor PINS
***********************/
#define ENABLE_A 14 // Control Speed of Left Motors
#define INPUT_1  15 // Left motors
#define INPUT_2  13 // Left motors
#define INPUT_3  2 // Right motors
#define INPUT_4  0 // Right motors
#define ENABLE_B 12 // Control Speed of Right Motors

#include <ESP8266WiFi.h>
#include <WebSocketsServer.h>

// Start WebSocket server and listening to incoming WebSocket Clients on port 5045
WebSocketsServer webSocket = WebSocketsServer(5045);

/***********
  VARIABLES
************/
char ssid[] = "OL_DEMO";  // Dedicated WiFi local network for demo.
char pass[] = "was4ever";

// Serial Communication between Arduino and NodeMCU
const byte numChars = 32;
char receivedMsg[numChars];
boolean newData = false;

// Websocket variables
uint8_t ws_num = 0;

// Set the Rover speed
int roverSpeed = 230; //135,195,215,245

// Set the Rover speed
int rover_FW_BW_Speed = 80; //74,85

// Color detected
boolean isColorDetected = false;
String colorDetected = "NC";


/*********************************
    GAME CONFIGURATION
**********************************/
bool isGameStarted = false;

void setup()
{
    // Serial UART Communication between Arduino and NodeMCU module
   Serial.begin(115200);

   // DC motor pins assignment
   pinMode(ENABLE_A, OUTPUT);
   pinMode(ENABLE_B, OUTPUT);  
   pinMode(INPUT_1, OUTPUT);
   pinMode(INPUT_2, OUTPUT);
   pinMode(INPUT_3, OUTPUT);
   pinMode(INPUT_4, OUTPUT);
 
  // Connect to Wifi
  WiFi.begin(ssid,pass);

  // Create a static IP address for clients to connect to...
  IPAddress ip(192,168,0,115);   
  IPAddress gateway(192,168,0,1);   
  IPAddress subnet(192,168,0,255);   
  WiFi.config(ip, gateway, subnet);
 
  // Retry connection until timeout
  int count = 0; 
  while ( (WiFi.status() != WL_CONNECTED) && count < 17) 
  {
      delay(500);  
      count++;
  }
 
  if (WiFi.status() != WL_CONNECTED)
  { 
     while(1);
  }
 
  // Let Arduino know that Wifi connection has been established.
  Serial.println("<C>");
 
  // Start the websocket instance.
  webSocket.begin();
  webSocket.onEvent(webSocketEvent);
}

void loop() {
  webSocket.loop();
  // Check for incoming messages from Arduino
  receiveMsgsWithStartEndMarkers();
  processNewData();
  sendDetectedColor();
}

void webSocketEvent(uint8_t num, WStype_t type, uint8_t * payload, size_t length) {

    switch(type) {
        case WStype_DISCONNECTED: 
        {
          stopRover();
          isGameStarted = false;
          Serial.println("<GE>"); // Send to Arduino that the game is ended.
          break;
        }
        case WStype_CONNECTED:
        {
          ws_num=num;
          webSocket.sendTXT(num, "Rover Connected"); // Send to Client
          Serial.println("<WSC>"); // Send to Arduino that the Websocket connection has been established.
          break;
        }
        case WStype_TEXT:
        { 
          if (payload[0] == '1' || payload[0] == '4') { // 1 - Classic Mode, 2 - Planet Hop Mode, 3 - Guided Mode, 4 - Sudden Death
            isGameStarted = true;
            Serial.println("<GS>");
          }
         else if (payload[0] == '3') {
            isGameStarted = true;
            Serial.println("<GM>");
          }
          else if (payload[0] == '2') {
            isGameStarted = true;
            Serial.println("<PH>");
          }
          else if (payload[0] == 'F') {
            moveForward();
          }
          else if(payload[0] == 'B') {
            moveBackward();
          }
          else if(payload[0] == 'L') {
            moveLeft();
          }
          else if(payload[0] == 'R') {
            moveRight();
          }
          else if(payload[0] == 'S') {
            stopRover();
          }
          break;
        }    
    }
}

// Use analogWrite to control the forward/backward speed of the rover.
// Use digitalWrite to control the left/right speed of the rover, set to highest (255),
// to have smooth turning motion.
void moveForward(){ 
    if (isGameStarted) {
        analogWrite(ENABLE_A, roverSpeed);
        analogWrite(ENABLE_B, roverSpeed);
      
        analogWrite(INPUT_1, 0);
        analogWrite(INPUT_2, rover_FW_BW_Speed);

        analogWrite(INPUT_3, 0);
        analogWrite(INPUT_4, rover_FW_BW_Speed);
    }    
  }

void moveBackward(){ 
    if (isGameStarted) {
        analogWrite(ENABLE_A, roverSpeed);
        analogWrite(ENABLE_B, roverSpeed);

        analogWrite(INPUT_1, rover_FW_BW_Speed);
        analogWrite(INPUT_2, 0);

        analogWrite(INPUT_3, rover_FW_BW_Speed);
        analogWrite(INPUT_4, 0);
    }       
  }

void moveRight(){
    if (isGameStarted) { 
        analogWrite(ENABLE_A, roverSpeed);
        analogWrite(ENABLE_B, roverSpeed);
      
        digitalWrite(INPUT_1, LOW);
        digitalWrite(INPUT_2, HIGH);
      
        digitalWrite(INPUT_3, HIGH);
        digitalWrite(INPUT_4, LOW);
    }        
  }

void moveLeft(){
    if (isGameStarted) {
        analogWrite(ENABLE_A, roverSpeed);
        analogWrite(ENABLE_B, roverSpeed);

        digitalWrite(INPUT_1, HIGH);
        digitalWrite(INPUT_2, LOW);
      
        digitalWrite(INPUT_3, LOW);
        digitalWrite(INPUT_4, HIGH);
    }        
  }

void stopRover(){  
    if (isGameStarted) {
        analogWrite(ENABLE_A, roverSpeed);
        analogWrite(ENABLE_B, roverSpeed);
      
        digitalWrite(INPUT_1, LOW);
        digitalWrite(INPUT_2, LOW);
        
        digitalWrite(INPUT_3, LOW);
        digitalWrite(INPUT_4, LOW);
    }
 }

// Function used to receive messages from NodeMCU wih start (<) and end (>) markers
void receiveMsgsWithStartEndMarkers() {
    static boolean recvInProgress = false;
    static byte ndx = 0;
    char startMarker = '<';
    char endMarker = '>';
    char rc;
 
    while (Serial.available() > 0 && newData == false) {
        rc = Serial.read();

        if (recvInProgress == true) {
            if (rc != endMarker) {
                receivedMsg[ndx] = rc;
                ndx++;
                if (ndx >= numChars) {
                    ndx = numChars - 1;
                }
            }
            else {
                receivedMsg[ndx] = '\0'; // terminate the string
                recvInProgress = false;
                ndx = 0;
                newData = true;
            }
        }

        else if (rc == startMarker) {
            recvInProgress = true;
        }
    }
}

void processNewData() {
  if (newData == true && isGameStarted) {
      if (strlen(receivedMsg) == 24) { //strcmp(receivedMsg, "RED") == 0
         isColorDetected = true;
         colorDetected = receivedMsg;
      }
      else if (strcmp(receivedMsg, "BLU") == 0) {
         isColorDetected = true;
         colorDetected = receivedMsg;

      }
      else if (strcmp(receivedMsg, "GRN") == 0) {
         isColorDetected = true;
         colorDetected = receivedMsg;

      }
      else if (strcmp(receivedMsg, "YW") == 0) {
         isColorDetected = true;
         colorDetected = receivedMsg;

      }
      else if (strcmp(receivedMsg, "PUR") == 0) {
         isColorDetected = true;
         colorDetected = receivedMsg;
      }
      else {
        colorDetected = "NC";
      }
      newData = false;
  }
}

void sendDetectedColor() {
    if (isColorDetected && colorDetected != "NC" && isGameStarted) {
      webSocket.sendTXT(ws_num, colorDetected); // Send to Client, the detected color.
      isColorDetected = false;
      colorDetected = "NC";
  }
}
