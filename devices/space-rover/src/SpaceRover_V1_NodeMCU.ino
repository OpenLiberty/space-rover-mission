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

#include<SoftwareSerial.h>
#include <ESP8266WiFi.h>
#include <WebSocketsServer.h>

SoftwareSerial SUART(4, 5); //RX=D2; TX=D1
bool DEBUG = false;   //show more logs in Serial monitor

// Start WebSocket server and listening to incoming WebSocket Clients on port 5045
WebSocketsServer webSocket = WebSocketsServer(5045);

/***********
  VARIABLES
************/
char ssid[] = "OL_DEMO";  // Dedicated WiFi local network for demo.
char pass[] = "#####"; // WIFI PASSWORD

// Serial Communication between Arduino and NodeMCU
const byte numChars = 32;
char receivedMsg[numChars];
boolean newData = false;

// Websocket variables
uint8_t ws_num = 0;

// Set the Rover speed
int roverSpeed = 135; 

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
   Serial.begin(9600);
   SUART.begin(9600); 

   // DC motor pins assignment
   pinMode(ENABLE_A, OUTPUT);
   pinMode(ENABLE_B, OUTPUT);  
   pinMode(INPUT_1, OUTPUT);
   pinMode(INPUT_2, OUTPUT);
   pinMode(INPUT_3, OUTPUT);
   pinMode(INPUT_4, OUTPUT);
 
  // Connect to Wifi
  Serial.print(F("Connecting to "));  Serial.println(ssid);
  WiFi.begin(ssid,pass);

  // Create a static IP address for clients to connect to...
  IPAddress ip(192,168,0,111);   
  IPAddress gateway(192,168,0,1);   
  IPAddress subnet(192,168,0,255);   
  WiFi.config(ip, gateway, subnet);
  Serial.println("");
 
  // Retry connection until timeout
  int count = 0; 
  while ( (WiFi.status() != WL_CONNECTED) && count < 17) 
  {
      Serial.print(".");  
      delay(500);  
      count++;
  }
 
  if (WiFi.status() != WL_CONNECTED)
  { 
     Serial.println("");  Serial.print("Failed to connect to ");  Serial.println(ssid);
     while(1);
  }
 
  Serial.println("");
  Serial.println(F("[CONNECTED]"));   Serial.print("[IP ");  Serial.print(WiFi.localIP()); 
  Serial.println("]");

  // Let Arduino know that Wifi connection has been established.
  SUART.println("<C>");
 
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
          if (DEBUG) {
            Serial.print("WStype = ");   Serial.println(type);  
            Serial.print("WS payload = ");
            for(int i = 0; i < length; i++) { Serial.print((char) payload[i]); }
            Serial.println();
          }
          stopRover();
          isGameStarted = false;
          SUART.println("<GE>"); // Send to Arduino that the game is ended.

          if (DEBUG)
            Serial.println("Game Ended...");
        
          break;
        }
        case WStype_CONNECTED:
        {
          if (DEBUG) {
            Serial.print("WStype = ");   Serial.println(type); 
            IPAddress ip = webSocket.remoteIP(num);
            Serial.println();
          }
          if (DEBUG) 
              Serial.println("Client ID : " + num);
          ws_num=num;
          webSocket.sendTXT(num, "Rover Connected"); // Send to Client
          SUART.println("<WSC>"); // Send to Arduino that the Websocket connection has been established.
          if (DEBUG) 
              Serial.println("Websocket connected");
          break;
        }
        case WStype_TEXT:
        {
          if (DEBUG) 
              Serial.println("Client ID : " + num);
          
          if (payload[0] == '1') {
            isGameStarted = true;
            SUART.println("<GS>");

            if (DEBUG)
              Serial.println("Game Started...");
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

          if (DEBUG) {
            Serial.print("WStype = ");   Serial.println(type);  
            Serial.print("WS payload = ");
            for(int i = 0; i < length; i++) { Serial.print((char) payload[i]); }
            Serial.println();
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

        analogWrite(INPUT_1, 74);
        analogWrite(INPUT_2, 0);

        analogWrite(INPUT_3, 74);
        analogWrite(INPUT_4, 0);
    
        if (DEBUG)
            Serial.println("FORWARD");
    }    
  }

void moveBackward(){ 
    if (isGameStarted) {
        analogWrite(ENABLE_A, roverSpeed);
        analogWrite(ENABLE_B, roverSpeed);
      
        analogWrite(INPUT_1, 0);
        analogWrite(INPUT_2, 74);

        analogWrite(INPUT_3, 0);
        analogWrite(INPUT_4, 74);
              
        if (DEBUG)
            Serial.println("BACKWARD");
    }       
  }

void moveRight(){
    if (isGameStarted) { 
        analogWrite(ENABLE_A, roverSpeed);
        analogWrite(ENABLE_B, roverSpeed);

        digitalWrite(INPUT_1, HIGH);
        digitalWrite(INPUT_2, LOW);
      
        digitalWrite(INPUT_3, LOW);
        digitalWrite(INPUT_4, HIGH);
        
        if (DEBUG)
            Serial.println("RIGHT");
    }        
  }

void moveLeft(){
    if (isGameStarted) {
        analogWrite(ENABLE_A, roverSpeed);
        analogWrite(ENABLE_B, roverSpeed);
      
        digitalWrite(INPUT_1, LOW);
        digitalWrite(INPUT_2, HIGH);
      
        digitalWrite(INPUT_3, HIGH);
        digitalWrite(INPUT_4, LOW);
      
        if (DEBUG)
            Serial.println("LEFT");
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
        
        if (DEBUG)
            Serial.println("STOP");
    }
 }

// Function used to receive messages from NodeMCU wih start (<) and end (>) markers
void receiveMsgsWithStartEndMarkers() {
    static boolean recvInProgress = false;
    static byte ndx = 0;
    char startMarker = '<';
    char endMarker = '>';
    char rc;
 
    while (SUART.available() > 0 && newData == false) {
        rc = SUART.read();

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
      if (DEBUG) {
        Serial.print("Received msg : ");
        Serial.println(receivedMsg);
      }

      if (strcmp(receivedMsg, "RED") == 0) {
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
      if (DEBUG)
          Serial.println("Sending Color: " + colorDetected);
      webSocket.sendTXT(ws_num, colorDetected); // Send to Client, the detected color.
      isColorDetected = false;
      colorDetected = "NC";
  }
}
