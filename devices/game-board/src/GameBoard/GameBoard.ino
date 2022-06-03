// NeoPixelTest
// This example will cycle between showing four pixels as Red, Green, Blue, White
// and then showing those pixels as Black.
//
// Included but commented out are examples of configuring a NeoPixelBus for
// different color order including an extra white channel, different data speeds, and
// for Esp8266 different methods to send the data.
// NOTE: You will need to make sure to pick the one for your platform 
//
//
// There is serial output of the current state so you can confirm and follow along
//


#define _GNU_SOURCE
#include <NeoPixelBus.h>
#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <WebSocketsServer.h>
#include <SoftwareSerial.h>

const uint16_t PixelCount = 400;
const uint8_t PixelPin = 2;  // make sure to set this to the correct pin, ignored for Esp8266

unsigned long previousMillis_SunBlink = 0;
unsigned long previousMillis_SunBurst = 0;
unsigned long previousMillis_PlanetBlink = 0;
const long animation_interval = 650;

boolean sunBlink_ON = false;
boolean planetBlink_ON = false;
int sunBurstCounter = 0;
boolean isSunBursting = false;
boolean isPlanet1Blinking = false;
boolean isPlanet2Blinking = false;
boolean isPlanet3Blinking = false;
boolean isPlanetOLBlinking = false;

#define colorSaturation 255

#define PLANET_1_LED_START     18
#define PLANET_2_LED_START     160
#define PLANET_3_LED_START     218
#define PLANET_OL_LED_START    331
#define NUM_LEDS_PLANET_1    26
#define NUM_LEDS_PLANET_2    30
#define NUM_LEDS_PLANET_3    26
#define NUM_LEDS_PLANET_OL    32

ESP8266WebServer server(80);
WebSocketsServer webSocket = WebSocketsServer(5045);

NeoPixelBus<NeoGrbFeature, NeoEsp8266Dma800KbpsMethod> strip(PixelCount, PixelPin);

RgbColor red(0, colorSaturation, 0);
RgbColor green(colorSaturation, 0, 0);
RgbColor blue(0, 0, colorSaturation);
RgbColor purple(0, 255, colorSaturation);
RgbColor yellow(colorSaturation, colorSaturation, 0);
RgbColor black(0);

//sun colours
RgbColor sun_OuterRing(110, 255, 0);
RgbColor sun_MiddleRing(69, 255, 0);
RgbColor sun_InnerRing(30, 255, 0);

char ssid[] = "OL_DEMO";  // use your own network ssid and password
char pass[] = "was4ever";

//connection booleans
boolean isWifiConnected = false;
boolean isGameStarted = false;
boolean isWSConnected = false;

//planet retrieval booleans
boolean isPlanet1Retrieved = false;
boolean isPlanet2Retrieved = false;
boolean isPlanet3Retrieved = false;
boolean isPlanetOLRetrieved = false;

int gameMode = 0;

void setup() {

    Serial.begin(115200);
    while (!Serial); // wait for serial attach

    Serial.println();
    Serial.println("Initializing...");
    Serial.flush();

    // this resets all the neopixels to an off state
    strip.Begin();
    strip.Show();
    

    Serial.println();
    Serial.println("Serial started at 115200");
    Serial.println();

    // Connect to a WiFi network
    Serial.print(F("Connecting to "));  Serial.println(ssid);
    WiFi.begin(ssid,pass);
    IPAddress ip(192,168,0,117);   
    IPAddress gateway(192,168,0,1);   
    IPAddress subnet(192,168,0,255);  
    WiFi.config(ip, gateway, subnet);
    Serial.println("");

    // connection with timeout
    int count = 0; 
    while ( (WiFi.status() != WL_CONNECTED) && count < 17)  {
        routerConnecting();
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
    routerConnected();
    isWifiConnected = true;
 
    webSocket.begin();
    webSocket.onEvent(webSocketEvent);
}


void loop() {
    webSocket.loop();
    if (isWifiConnected && !isWSConnected) {
       WSConnecting();
    }
    if (isWSConnected && !isGameStarted) {
      sunON(blue);
    }

    if (isGameStarted) {
      if (isPlanet1Blinking) {
        planetBlink(1);
      }
      else if (isPlanet2Blinking) {
        planetBlink(2);
      }
      else if (isPlanet3Blinking) {
        planetBlink(3);
      }
      else if (isPlanetOLBlinking) {
        planetBlink(4);
      }
    }
}

void webSocketEvent(uint8_t num, WStype_t type, uint8_t * payload, size_t length) {

    switch(type) {
        case WStype_DISCONNECTED: {
          Serial.print("WStype = ");   Serial.println(type);  
          Serial.print("WS payload = ");
          for(int i = 0; i < length; i++) { Serial.print((char) payload[i]); }
          Serial.println();
          // send message to clvient
          //webSocket.sendTXT(num, "Board Disconnected");
          isWSConnected = false;
          isSunBursting = false;

          if (isGameStarted) {
            endGame();
            gameMode = 0;
          }
          break;
          
        }
        case WStype_CONNECTED: {
          yield();
          Serial.print("WStype = ");   Serial.println(type); 
          IPAddress ip = webSocket.remoteIP(num);
          //Serial.println("[%u] Connected from %d.%d.%d.%d url: %s\n", num, ip[0], ip[1], ip[2], ip[3], payload);
          Serial.println();
          // send message to client
          webSocket.sendTXT(num, "Board Connected");
          isWSConnected = true;
          //cycle
          allLights_OFF();
          
          
          
          break;
        }
        case WStype_TEXT: {
          //Serial.println("payload = ");
          //Serial.println(payload[0]);
          //Serial.println((char)payload[0]);
          Serial.print("WS payload = ");
          for(int i = 0; i < length; i++) { Serial.print((char) payload[i]); }
          Serial.println();
          
          char str[length];

          for(int i = 0; i < length; i++) { 
            str[i] = (char)payload[i]; 
          }
          
          //classic
          if (payload[0] == '1') {
            isGameStarted = true;
            gameMode = 1;
            startGame();
            Serial.println("GAME STARTED: Classic");
          }

          //planet hop
          if (payload[0] == '2') {
            isGameStarted = true;
            gameMode = 2;
            startGame();
            Serial.println("GAME STARTED: Planet Hop");
          } 

          //guided
          if (payload[0] == '3') {
            isGameStarted = true;
            gameMode = 3;
            startGame();
            Serial.println("GAME STARTED: Guided");
          } 

          //sudden death
          if (payload[0] == '4') {
            isGameStarted = true;
            gameMode = 4;
            startGame();
            Serial.println("GAME STARTED: Sudden Death");
          } 
          
          //captures
       
          //RED
          if (strncmp(str, "RED", strlen("RED")) == 0) {
            //flicker lights
            Serial.println("HIT OBSTACLE!!");
          }

          //YW
          else if (strncmp(str, "YW", strlen("YW")) == 0) {
            //turn off planet 1
            planet3_OFF();
            isPlanet3Retrieved = true;
            Serial.println("PLANET 3 RESOURCE RETRIEVED");

            if (gameMode == 3) {
              if (isPlanet1Retrieved && isPlanet2Retrieved) {
                planetOL_ON();               
              }
              else {
                isPlanet3Retrieved = false;
              }
            }
          }

          //PUR
          else if (strncmp(str, "PUR", strlen("PUR")) == 0) {
            //turn off planet 2
            planetOL_OFF();
            isPlanetOLRetrieved = true;
            Serial.println("OPEN LIBERTY RESOURCE RETRIEVED ");
          }

          //BLU
          else if (strncmp(str, "BLU", strlen("BLU")) == 0) {
            //turn off planet 3
            planet1_OFF();
            isPlanet1Retrieved = true;
            Serial.println("PLANET 1 RESOURCE RETRIEVED");

            if (gameMode == 3) {
              planet2_ON();
            }
          }

          //GRN
          else if (strncmp(str, "GRN", strlen("GRN")) == 0) {
            //turn off planet OL
            planet2_OFF();
            isPlanet2Retrieved = true;
            Serial.println("PLANET 2 RESOURCE RETRIEVED ");

            if (gameMode == 3) {
              if (isPlanet1Retrieved) {
                 planet3_ON();
              }
              else {
                isPlanet2Retrieved = false;
              }
            }
          }

          //planet hop specific

          char* pipeChar = strcasestr(str, "|");

          if (pipeChar != NULL) {
            String s = String(str);
            int index = s.indexOf("|");
            String func = s.substring(0, index);
            String col = s.substring(index+1, length);
  
            if (func.equals("blinkColour")) {
              Serial.print("BLINK ");
              Serial.println(col);

              //BLU
              if (col.equals("YW")) {
                planetBlink(3);
                Serial.println("PLANET 3 BLINK");
                isPlanet3Blinking = true;
              }
    
              //PUR
              else if (col.equals("PUR")) {
                planetBlink(4);
                Serial.println("PLANET OL BLINK");
                isPlanetOLBlinking = true;
              }
    
              //YW
              else if (col.equals("BLU")) {
                planetBlink(1);
                Serial.println("PLANET 1 BLINK");
                isPlanet1Blinking = true;
              }
    
              //GRN
              else if (col.equals("GRN")) {
                planetBlink(2);
                Serial.println("PLANET 2 BLINK");
                isPlanet2Blinking = true;
              }
            }

            else if (func.equals("setColour")) {
              Serial.print("SET ");
              Serial.println(col);
                            
              //BLU
              if (col.equals("YW")) {
                planet3_ON();
                Serial.println("PLANET 3 ON");
                stopPlanetBlinking();
                planet1_OFF();
                planet2_OFF();
                planetOL_OFF();
              }
    
              //PUR
              else if (col.equals("PUR")) {
                planetOL_ON();
                Serial.println("PLANET OL ON");
                stopPlanetBlinking();
                planet1_OFF();
                planet2_OFF();
                planet3_OFF();
              }
    
              //YW
              else if (col.equals("BLU")) {
                planet1_ON();
                Serial.println("PLANET 1 ON");
                stopPlanetBlinking();
                planet2_OFF();
                planet3_OFF();
                planetOL_OFF();
              }
    
              //GRN
              else if (col.equals("GRN")) {
                planet2_ON();
                Serial.println("PLANET 2 ON");
                stopPlanetBlinking();
                planet1_OFF();
                planet3_OFF();
                planetOL_OFF();
              }
            }
          }

          //tester

          if(strncmp(str, "ALL_LIGHTS_ON", strlen("ALL_LIGHTS_ON")) == 0) {
            allLights_ON();
            Serial.println("ALL_LIGHTS_ON");
          }
          else if(strncmp(str, "ALL_LIGHTS_OFF", strlen("ALL_LIGHTS_OFF")) == 0) {
            allLights_OFF();
            Serial.println("ALL_LIGHTS_OFF");
          }

          Serial.println();
          Serial.println("------");
          Serial.println();
          break;
        }    
    }
}

void stopPlanetBlinking() {
  isPlanet1Blinking = false;
  isPlanet2Blinking = false;
  isPlanet3Blinking = false;
  isPlanetOLBlinking = false;
}

void routerConnecting() {
  sunBlink(red);
}

void routerConnected() {
  blinkOnce(0, PixelCount, green);
}

//turn on x number of leds in range
void fillSolidRange_ON(int startLed, int numLeds, RgbColor color) {
  for (int i = startLed; i < startLed+numLeds; i++) {
    //Serial.println(i);
    strip.SetPixelColor(i, color);
  }
  strip.Show();
}

//turn off x number of leds in range
void fillSolidRange_OFF(int startLed, int numLeds) {
  for (int i = startLed; i < startLed+numLeds; i++) {
    strip.SetPixelColor(i, black);
  }
  strip.Show();
}


//turn off planet 1 lights
void planet1_OFF() {
  fillSolidRange_OFF(PLANET_1_LED_START, NUM_LEDS_PLANET_1);
}

//turn on planet 1 lights
void planet1_ON() {
  //planet 1 colour is green
  fillSolidRange_ON(PLANET_1_LED_START, NUM_LEDS_PLANET_1, blue);
}

//turn off planet 2 lights
void planet2_OFF() {
  fillSolidRange_OFF(PLANET_2_LED_START, NUM_LEDS_PLANET_2);
}

//turn on planet 2 lights
void planet2_ON() {
  //planet 2 colour is indigo
  fillSolidRange_ON(PLANET_2_LED_START, NUM_LEDS_PLANET_2, green);
}

//turn off planet 3 lights
void planet3_OFF() {
  fillSolidRange_OFF(PLANET_3_LED_START, NUM_LEDS_PLANET_3);
}

//turn on planet 3 lights
void planet3_ON() {
  fillSolidRange_ON(PLANET_3_LED_START, NUM_LEDS_PLANET_3, yellow);
}

//turn off planet OL lights
void planetOL_OFF() {
  fillSolidRange_OFF(PLANET_OL_LED_START, NUM_LEDS_PLANET_OL);
}

//turn on planet OL lights
void planetOL_ON() {
  fillSolidRange_ON(PLANET_OL_LED_START, NUM_LEDS_PLANET_OL, purple);
}

//turn obstacles off
void obstacles_OFF() {
    fillSolidRange_OFF(0, 17);
    fillSolidRange_OFF(44, 116);
    fillSolidRange_OFF(190, 28);
    fillSolidRange_OFF(244, 87);
    fillSolidRange_OFF(363, 11);
}

void obstacles_ON() {
    fillSolidRange_ON(0, 17, red);
    fillSolidRange_ON(44, 116, red);
    fillSolidRange_ON(190, 28, red);
    fillSolidRange_ON(244, 87, red);
    fillSolidRange_ON(363, 11, red);
}

void allLights_ON() {
  planet1_ON();
  planet2_ON();
  planet3_ON();
  planetOL_ON();
  obstacles_ON();
}

void allLights_OFF() {
  planet1_OFF();
  planet2_OFF();
  planet3_OFF();
  planetOL_OFF();
  obstacles_OFF();
}

void startGame() {
  if (gameMode == 1 || gameMode == 4) {
    allLights_ON();
  }
  if (gameMode == 2) {
    obstacles_ON();
  }
  if (gameMode == 3) {
    planet1_ON();
    obstacles_ON();
  }
}

void endGame() {
  allLights_OFF();
  isGameStarted = false;
  isPlanet1Retrieved = false;
  isPlanet2Retrieved = false;
  isPlanet3Retrieved = false;
  isPlanetOLRetrieved = false;
  stopPlanetBlinking();
}

void blinkOnce(int startLed, int numLeds, RgbColor color) {
  fillSolidRange_ON(startLed, numLeds, color);
  delay(500);
  fillSolidRange_OFF(startLed, numLeds);
  delay(500);
}

void planetBlink(int planet) {
  unsigned long currentMillis = millis();
  if (currentMillis - previousMillis_PlanetBlink >= animation_interval) {
    previousMillis_PlanetBlink = currentMillis;

    if (planetBlink_ON == false) {
      planetBlink_ON = true;
      if (planet == 1) {
        planet1_ON();
      }
      else if (planet == 2) {
        planet2_ON(); 
      }
      else if (planet == 3) {
        planet3_ON();        
      }
      else if (planet == 4) {
        planetOL_ON();        
      }
    } else {
      planetBlink_ON = false;
      if (planet == 1) {
        planet1_OFF();
      }
      else if (planet == 2) {
        planet2_OFF();
      }
      else if (planet == 3) {
        planet3_OFF();
      }
      else if (planet == 4) {
        planetOL_OFF();
      }
    }
  }
}

void sunON(RgbColor color) {
  fillSolidRange_ON(79, 45, color);
  fillSolidRange_ON(263, 48, color);    
}

void sunOFF() {
  fillSolidRange_OFF(79, 45);
  fillSolidRange_OFF(263, 48);    
}

void sunBlink(RgbColor color) {
  unsigned long currentMillis = millis();
  if (currentMillis - previousMillis_SunBlink >= animation_interval) {
    previousMillis_SunBlink = currentMillis;

    if (sunBlink_ON == false) {
      sunBlink_ON = true;
      fillSolidRange_ON(79, 45, color);
      fillSolidRange_ON(263, 48, color);
    } else {
      sunBlink_ON = false;
      fillSolidRange_OFF(79, 45);
      fillSolidRange_OFF(263, 48);
    }
  }
}

void sunInnerPixels() {
   fillSolidRange_ON(79, 7, sun_InnerRing);
   fillSolidRange_ON(116, 8, sun_InnerRing);
   fillSolidRange_ON(263, 8, sun_InnerRing);
   fillSolidRange_ON(303, 8, sun_InnerRing);
}

void sunMiddlePixels() {
   fillSolidRange_ON(86, 7, sun_MiddleRing);
   fillSolidRange_ON(108, 8, sun_MiddleRing);
   fillSolidRange_ON(271, 8, sun_MiddleRing);
   fillSolidRange_ON(295, 8, sun_MiddleRing);
}

void sunOuterPixels() {
     fillSolidRange_ON(93, 7, sun_OuterRing);
     fillSolidRange_ON(100, 8, sun_OuterRing);
     fillSolidRange_ON(279, 8, sun_OuterRing);
     fillSolidRange_ON(287, 8, sun_OuterRing);
}

void WSConnecting() {
   unsigned long currentMillis = millis();
   if (currentMillis - previousMillis_SunBurst >= animation_interval) {
      previousMillis_SunBurst = currentMillis;

      if (!isSunBursting) {
        isSunBursting = true;
        planet1_ON();
        planet2_ON();
        planet3_ON();
        planetOL_ON();
      }

      sunOFF();
      
      if (sunBurstCounter == 0) {
        sunBurstCounter = 1;
        sunInnerPixels();
      }
      else if (sunBurstCounter == 1) {
        sunBurstCounter = 2;
        sunMiddlePixels();
      }
      else if (sunBurstCounter == 2) {
        sunBurstCounter = 0;
        sunOuterPixels();
      }

   }
   strip.Show();
}
