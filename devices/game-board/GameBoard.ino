/* This sketch is uploaded in the ESP8266 WiFi module inside the game board.
 * 
 * The LEDs are controlled using the Makuna NeoPixelBus library.
 * 
 */
 
#include <NeoPixelBus.h>
#include <ESP8266WiFi.h>
#include <ESP8266WebServer.h>
#include <WebSocketsServer.h>
#include <SoftwareSerial.h>

const uint16_t PixelCount = 300; // 300 LEDS
const uint8_t PixelPin = 2;  // pixel pin is ignored for ESP8266 when using NeoPixelBus, it will automatically use GPIO3/RX

unsigned long previousMillis_SunBlink = 0; // millis for sun blinking animation
unsigned long previousMillis_SunBurst = 0; // millis for sun bursting animation
const long animation_interval = 650; // interval for blinking/bursting animations

boolean sunBlink_ON = false;
int sunBurstCounter = 0;
boolean isSunBursting = false;

//game mode options (hardcoded for now, future iterations may have multiple game modes)
boolean gameMode = 0;
int guidedModeCounter = 0;

//max colour saturation for base colours (R, G, B)
#define colorSaturation 255

//planet pixel indexes
#define PLANET_1_LED_START     0
#define PLANET_2_LED_START     113
#define PLANET_3_LED_START     172
#define PLANET_OL_LED_START    261
#define NUM_LEDS_PER_PLANET    25

//websocket server setup
ESP8266WebServer server(80);
WebSocketsServer webSocket = WebSocketsServer(5045);

//WS2812B LED strip setup
NeoPixelBus<NeoGrbFeature, NeoEsp8266Dma800KbpsMethod> strip(PixelCount, PixelPin);

//generic planet colour setup 
RgbColor red(colorSaturation, 0, 0);
RgbColor green(0, colorSaturation, 0);
RgbColor blue(0, 0, colorSaturation);
RgbColor purple(colorSaturation, 0, colorSaturation);
RgbColor yellow(colorSaturation, colorSaturation, 0);
RgbColor black(0);

//sun colours
RgbColor sun_OuterRing(255, 110, 0);
RgbColor sun_MiddleRing(255, 69, 0);
RgbColor sun_InnerRing(255, 30, 0);

//wifi credentials
char ssid[] = "OL_DEMO";  
char pass[] = "was4ever";

//game connection booleans
boolean isWifiConnected = false;
boolean isGameStarted = false;
boolean isWSConnected = false;

//planet retrieval booleans
boolean isPlanet1Retrieved = false;
boolean isPlanet2Retrieved = false;
boolean isPlanet3Retrieved = false;
boolean isPlanetOLRetrieved = false;

void setup()
{
    // begin Serial at 115200
    Serial.begin(115200);
    while (!Serial); // wait for serial attach

    Serial.println();
    Serial.println("Initializing...");
    Serial.flush();

    // resets all LEDs to an off state
    strip.Begin();
    strip.Show();
    
    Serial.println();
    Serial.println("Serial started at 115200");
    Serial.println();

    // Connect to a WiFi network
    Serial.print(F("Connecting to "));  Serial.println(ssid);
    WiFi.begin(ssid,pass);
    IPAddress ip(192,168,0,113); // IP for websocket connection  
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
    routerConnected();  // animate for connecting router
    isWifiConnected = true;

    //start websocket server
    webSocket.begin();
    webSocket.onEvent(webSocketEvent);
    
}


void loop()
{
    webSocket.loop();
    
    //WiFi is connected, websocket is NOT connected
    if (isWifiConnected && !isWSConnected) {  
       WSConnecting();
    }
    //websocket is connected, game is NOT started
    if (isWSConnected && !isGameStarted) {   
      sunON(blue);
    }
}

void webSocketEvent(uint8_t num, WStype_t type, uint8_t * payload, size_t length) {

    switch(type) {
        //websocket disconnected
        case WStype_DISCONNECTED: {
          Serial.print("WStype = ");   Serial.println(type);  
          Serial.print("WS payload = ");
          for(int i = 0; i < length; i++) { Serial.print((char) payload[i]); }
          Serial.println();
          // send message to client
          //webSocket.sendTXT(num, "Board Disconnected");
          isWSConnected = false;
          isSunBursting = false;
          guidedModeCounter = 0;

          if (isGameStarted) {
            endGame();
          }
          break;
          
        }
        //websocket connected
        case WStype_CONNECTED: {
          yield();
          Serial.print("WStype = ");   Serial.println(type); 
          IPAddress ip = webSocket.remoteIP(num);
          //Serial.println("[%u] Connected from %d.%d.%d.%d url: %s\n", num, ip[0], ip[1], ip[2], ip[3], payload);
          Serial.println();
          // send message to client
          webSocket.sendTXT(num, "Board Connected");
          isWSConnected = true;
          allLights_OFF();
          
          
          break;
        }
        //websocket message
        case WStype_TEXT: {
          char str[length];

          for(int i = 0; i < length; i++) { 
            str[i] = (char)payload[i]; 
          }

          Serial.print("GAME MODE: "); Serial.println(gameMode);
          
          //start game
          if (payload[0] == '1') {
            isGameStarted = true;
            startGame(gameMode);
            guidedModeCounter = 0;
            Serial.println("GAME STARTED");
          }

          //game mode 1, all planets on at the same time, option not currently available
          if (gameMode == 1) {
            //RED
            if (strncmp(str, "RED", strlen("RED")) == 0) {
              Serial.println("HIT OBSTACLE ");
            }
            //YW
            else if (strncmp(str, "YW", strlen("YW")) == 0) {
              planet3_OFF();
              isPlanet3Retrieved = true;
              Serial.println("PLANET 1 RESOURCE RETRIEVED");
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
              Serial.println("PLANET 2 RESOURCE RETRIEVED");
            }
            //GRN
            else if (strncmp(str, "GRN", strlen("GRN")) == 0) {
              //turn off planet OL
              planet2_OFF();
              isPlanet2Retrieved = true;
              Serial.println("PLANET 3 RESOURCE RETRIEVED ");
            }
          }

          //game mode 0, guided mode, only one planet on at a time => current play mode for v1
          if (gameMode == 0) {
            if (payload[0] != '1') {
              if (!(strncmp(str, "RED", strlen("RED")) == 0)) { //if colour detected is not red
                if (guidedModeCounter == 0) {
                  planet1_OFF();
                  isPlanet1Retrieved = true;
                  Serial.println("PLANET 1 RESOURCE RETRIEVED");
                  planet2_ON();
                  guidedModeCounter++;
                }
                else if (guidedModeCounter == 1) {
                  planet2_OFF();
                  isPlanet2Retrieved = true;
                  Serial.println("PLANET 2 RESOURCE RETRIEVED");
                  planet3_ON();
                  guidedModeCounter++;
                }
                else if (guidedModeCounter == 2) {
                  planet3_OFF();
                  isPlanet3Retrieved = true;
                  Serial.println("PLANET 3 RESOURCE RETRIEVED");
                  planetOL_ON();
                  guidedModeCounter++;
                }
                else if (guidedModeCounter == 3) {
                  planetOL_OFF();
                  isPlanetOLRetrieved = true;
                  Serial.println("OPEN LIBERTY RESOURCE RETRIEVED ");
                  guidedModeCounter = 0;
                }
              }
            }
          }

          Serial.print("WStype = ");   Serial.println(type);  
          Serial.print("WS payload = ");
          for(int i = 0; i < length; i++) { Serial.print((char) payload[i]); }
          Serial.println();
          break;
        }    
    }
}

//animation for when connecting to WiFi
void routerConnecting() {
  sunBlink(red);
}

//animation for when connected to WiFi
void routerConnected() {
  blinkOnce(0, PixelCount, green);
}

//turn on leds in a range
void fillSolidRange_ON(int startLed, int numLeds, RgbColor color) {
  for (int i = startLed; i < startLed+numLeds; i++) {
    strip.SetPixelColor(i, color);
  }
  strip.Show();
}

//turn off leds in range
void fillSolidRange_OFF(int startLed, int numLeds) {
  for (int i = startLed; i < startLed+numLeds; i++) {
    strip.SetPixelColor(i, black);
  }
  strip.Show();
}

//turn off planet 1 lights
void planet1_OFF() {
  fillSolidRange_OFF(PLANET_1_LED_START, NUM_LEDS_PER_PLANET);
}

//turn on planet 1 lights
void planet1_ON() { // BLUE
  fillSolidRange_ON(PLANET_1_LED_START, NUM_LEDS_PER_PLANET, blue);
}

//turn off planet 2 lights
void planet2_OFF() { 
  fillSolidRange_OFF(PLANET_2_LED_START, NUM_LEDS_PER_PLANET);
}

//turn on planet 2 lights
void planet2_ON() { // GREEN
  fillSolidRange_ON(PLANET_2_LED_START, NUM_LEDS_PER_PLANET, green);
}

//turn off planet 3 lights
void planet3_OFF() {
  fillSolidRange_OFF(PLANET_3_LED_START, NUM_LEDS_PER_PLANET);
}

//turn on planet 3 lights
void planet3_ON() { // YELLOW
  fillSolidRange_ON(PLANET_3_LED_START, NUM_LEDS_PER_PLANET, yellow);
}

//turn off planet OL lights
void planetOL_OFF() {
  fillSolidRange_OFF(PLANET_OL_LED_START, NUM_LEDS_PER_PLANET);
}

//turn on planet OL lights
void planetOL_ON() {  // PURPLE
  fillSolidRange_ON(PLANET_OL_LED_START, NUM_LEDS_PER_PLANET, purple);
}

//turn obstacles off
void obstacles_OFF() {
  fillSolidRange_OFF(25, 112);
  fillSolidRange_OFF(138, 171);
  fillSolidRange_OFF(197, 64);
  fillSolidRange_OFF(286, 12);
}

//turn obstacles on
void obstacles_ON() { // RED
  fillSolidRange_ON(25, 88, red);
  fillSolidRange_ON(138, 33, red);
  fillSolidRange_ON(197, 64, red);
  fillSolidRange_ON(286, 12, red);
}

//turn on all board lights
void allLights_ON() {
  planet1_ON();
  planet2_ON();
  planet3_ON();
  planetOL_ON();
  obstacles_ON();
}

//turn off all board lights
void allLights_OFF() {
  planet1_OFF();
  planet2_OFF();
  planet3_OFF();
  planetOL_OFF();
  obstacles_OFF();
}

//start space rover game
void startGame(int gameMode) {
  if (gameMode == 1) {
    allLights_ON();
  }
  if (gameMode == 0) {
    planet1_ON();
    obstacles_ON();
  }
}

//end space rover game
void endGame() {
  allLights_OFF();
  isGameStarted = false;
  isPlanet1Retrieved = false;
  isPlanet2Retrieved = false;
  isPlanet3Retrieved = false;
  isPlanetOLRetrieved = false;
}

//blink leds in a specific range once
void blinkOnce(int startLed, int numLeds, RgbColor color) {
  fillSolidRange_ON(startLed, numLeds, color);
  delay(500);
  fillSolidRange_OFF(startLed, numLeds);
  delay(500);
}

//turn sun leds on
void sunON(RgbColor color) {
  fillSolidRange_ON(69, 32, color);
  fillSolidRange_ON(217, 32, color);
}

//turn sun leds off
void sunOFF() {
  fillSolidRange_OFF(69, 32);
  fillSolidRange_OFF(217, 32);
}

//blink sun leds
void sunBlink(RgbColor color) {
  unsigned long currentMillis = millis();
  if (currentMillis - previousMillis_SunBlink >= animation_interval) {
    previousMillis_SunBlink = currentMillis;

    if (sunBlink_ON == false) {
      sunBlink_ON = true;
      fillSolidRange_ON(69, 32, color);
      fillSolidRange_ON(217, 32, color);
    } else {
      sunBlink_ON = false;
      fillSolidRange_OFF(69, 32);
      fillSolidRange_OFF(217, 32);
    }
  }
}

//set inner sun pixel ring colour
void sunInnerPixels() {
  for (int i = 70; i < 70 + 4*8; i+=4) {
    strip.SetPixelColor(i, sun_InnerRing);
  }
  for (int i = 218; i < 218 + 4*8; i+=4) {
    strip.SetPixelColor(i, sun_InnerRing);
  }
}

//set middle sun pixel ring colour
void sunMiddlePixels() {
  for (int i = 69; i < 69 + 2*16; i+=2) {
    strip.SetPixelColor(i, sun_MiddleRing);
  }
  for (int i = 217; i < 217 + 2*16; i+=2) {
    strip.SetPixelColor(i, sun_MiddleRing);
  }
}

//set outer sun pixel ring olour
void sunOuterPixels() {
  for (int i = 72; i < 72 + 4*8; i+=4) {
    strip.SetPixelColor(i, sun_OuterRing);
  }
  for (int i = 220; i < 220 + 4*8; i+=4) {
    strip.SetPixelColor(i, sun_OuterRing);
  }
}

//animation for when websocket is connecting
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
