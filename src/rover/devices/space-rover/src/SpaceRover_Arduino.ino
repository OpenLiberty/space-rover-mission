#include <SPI.h>
#include <MFRC522.h>
#include <FastLED.h>

bool CALIBRATE = false; //Set to true if RFID sensor calibration is required

/***********
    PINS
************/
// RFID SENSOR PINS
#define SS_SPI 10
#define RST 9

// WS2811 LEDS (Rover Lights)
#define NUM_LEDS 7
#define DATA_PIN 6
#define BRIGHTNESS 255

// Create RFID Instance
MFRC522 rfid(SS_SPI, RST);

// Rover Headlights
const int ROVER_HEADLIGHTS = 7;

/***********
  VARIABLES
************/
// Serial Communication between Arduino and NodeMCU
const byte numChars = 32;
char receivedMsg[numChars];
boolean newData = false;

// Color RFID HEX
//String BLUE_COLOR = "0C 58 AA 5B"; //TEST
String BLUE_COLOR = "DC D0 D4 5B";
String GREEN_COLOR = "CC B0 90 5B";
String YELLOW_COLOR = "DC 82 32 5B";
String PURPLE_COLOR = "CC 12 B3 5B";

// Rover Lights
CRGB leds[NUM_LEDS];
bool led_on = false;
uint8_t gHue=0;
unsigned long previousMillis = 0; // Will be used to track how long since last event occurred
unsigned long previousMillis_blink = 0; // Will be used to track how long since last event occurred
unsigned long previousMillis_cycle = 0; // Will be used to track how long since last event occurred
unsigned long blink_interval = 500; // Delay to determine when we see the next LED, increase value to slow down speed of LEDs
unsigned long breath_interval = 20; // Delay to determine when we see the next LED, decrease value to slow down speed of LEDs

// Rover headlights/taillights
unsigned long previousMillis_Headlights=0; // Will be used to track how long since last event occurred
int headLightState = LOW; // ledState used to set the LED
const long headlight_interval = 500; // interval at which to blink (milliseconds)

// Planet Detection
bool blueCaptured = false;
bool greenCaptured = false;
bool yellowCaptured = false;
bool purpleCaptured = false;

/*********************************
    GAME CONFIGURATION
**********************************/
bool isWifiConnected = false;
bool isWSConnected = false; // WebSocket connection
bool isGameStarted = false;
bool isGameModePlanetHop = false;
bool isGameModeGuided = false;

void setup()
{
  // Hardware Serial UART Communication between Arduino and NodeMCU module
  Serial.begin(115200);

  // Initiate SPI Bus instance
  SPI.begin();

  // Initiate the RFID sensor instance
  rfid.PCD_Init();

  // Initialize the Rover LEDs
  FastLED.addLeds<WS2811, DATA_PIN, RGB>(leds, NUM_LEDS);

  FastLED.setBrightness(BRIGHTNESS);
  
  // Rover Headlights
  pinMode(ROVER_HEADLIGHTS, OUTPUT);

  // Turn off all lights
  turnOffAllLights();
}

void loop()
{
  // Check for incoming messages from NodeMCU
  receiveMsgsWithStartEndMarkers();
  processNewData();
  
  if (!isWifiConnected && !isGameStarted) { // no wifi connection
    blinkRoverLeds(CRGB::Red, true);
  }  
  
  if (isWifiConnected && !isWSConnected && !isGameStarted) { // no websocket connection
    breathColorLeds();
  } 
  
  if (isWifiConnected && isWSConnected && !isGameStarted) { // standby mode
    blinkRoverHeadLights();
    turnOnRoverLeds(CRGB::Turquoise, true);
  }
  
  if (isWifiConnected && isWSConnected && isGameStarted) { // game started
    turnOnRoverHeadLights();
    turnOnRoverLeds(CRGB::Turquoise, true);
    detectRFIDtag();
  }
  
  if (isWifiConnected && !isWSConnected && isGameStarted) { // Error state
    turnOnRoverLeds(CRGB::Red, false);
  }

  if (CALIBRATE)
    detectRFIDtag();
}

// Function used to receive messages from NodeMCU with start (<) and end (>) markers
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
    if (newData == true) {
        if (strcmp(receivedMsg, "C") == 0) {
           isWifiConnected=true;
        }
        else if (strcmp(receivedMsg, "WSC") == 0) {
           isWSConnected=true;
        }
        else if (strcmp(receivedMsg, "GS") == 0) {
            isGameStarted=true;
        }
        else if (strcmp(receivedMsg, "GM") == 0) { // Guided Mode
            isGameStarted=true;
            isGameModeGuided=true;
        }
        else if (strcmp(receivedMsg, "PH") == 0) { //Planet Hop Mode
            isGameStarted=true;
            isGameModePlanetHop=true;
        }
        else if (strcmp(receivedMsg, "GE") == 0) {
            resetGameStartState();
        }
        newData = false;
    }
}

void detectRFIDtag() {    
  if (rfid.PICC_IsNewCardPresent()) // Check if a new card or sticker is present
  {
    if (rfid.PICC_ReadCardSerial()) // If present, read the RFID/NFC to get the UID
    {
      String UID_str= "";
      for (byte i = 0; i < rfid.uid.size; i++) 
      {
         UID_str.concat(String(rfid.uid.uidByte[i] < 0x10 ? " 0" : " "));
         UID_str.concat(String(rfid.uid.uidByte[i], HEX));
      }
      UID_str.toUpperCase();
      determineColorFromRFID(UID_str); // Determine the color from the detected UID
    }
  }
}

void determineColorFromRFID(String UID_str) {
  String detectedID = UID_str.substring(1);
  unsigned int UID_len = detectedID.length();
  
  if (UID_len == 20) {
   // Send color to NodeMCU
   Serial.println("<RED|"+detectedID+">");
   blinkRoverDamageLights(); 
  }    
  else if (detectedID == YELLOW_COLOR && !yellowCaptured) 
  {   
   // Send color to NodeMCU
   Serial.println("<YW>");
   activateRoverPlanetCollectionLight(CRGB::Orange); //CRGB(255,255,0)
   if (!isGameModePlanetHop && !isGameModeGuided) {
      yellowCaptured = true;
   }
  } 
  else if (detectedID == PURPLE_COLOR && !purpleCaptured)    
  {  
   // Send color to NodeMCU
   Serial.println("<PUR>");
   activateRoverPlanetCollectionLight(CRGB(255,0,255));
   if (!isGameModePlanetHop && !isGameModeGuided) {
      purpleCaptured = true;
   }
  }  
  else if (detectedID == BLUE_COLOR && !blueCaptured)
  {
   // Send color to NodeMCU
   Serial.println("<BLU>");
   activateRoverPlanetCollectionLight(CRGB(0,0,255));
   if (!isGameModePlanetHop && !isGameModeGuided) {
      blueCaptured = true;
   }
  }  
  else if (detectedID == GREEN_COLOR && !greenCaptured)
  {
   // Send color to NodeMCU
   Serial.println("<GRN>");
   activateRoverPlanetCollectionLight(CRGB(0,255,0));
   if (!isGameModePlanetHop && !isGameModeGuided) {
      greenCaptured = true;
   }
  }
}

void fillBreathColorLeds() {
     fill_solid(leds, NUM_LEDS-1, CHSV( gHue, 255, 255));
     FastLED.show();
}

void breathColorLeds() {
  // Get current time and determine how long since last check
  unsigned long currentMillis = millis();
  if ((unsigned long)(currentMillis - previousMillis) >= breath_interval) { 
     previousMillis = currentMillis;
     fillBreathColorLeds();
     gHue++;
  }
}

void blinkRoverLeds(CRGB color, bool skipCanopyLight) {
  // Get current time and determine how long since last check
  unsigned long currentMillis = millis();
  if ((unsigned long)(currentMillis - previousMillis_blink) >= blink_interval) { 
     previousMillis_blink = currentMillis;
     if (!led_on) {
        turnOnRoverLeds(color, skipCanopyLight);
        led_on=true;
     }
     else {
        turnOffRoverLeds();
        led_on=false;
     } 
  }
}

void turnOnCanopyLight(CRGB color) {
  leds[6] = color;
  FastLED.show();
}

void turnOffCanopyLight() {
  leds[6] = CRGB::Black;
  FastLED.show();
}

void turnOnRoverLeds(CRGB color, bool skipCanopyLight) {
  int numOfLedsMinusCanopy = NUM_LEDS;
  if (skipCanopyLight) {
    numOfLedsMinusCanopy = NUM_LEDS-1;
    turnOffCanopyLight();
  }
  // Set LEDs to specified color and turn the LEDs on
  for (int i = 0; i < numOfLedsMinusCanopy; i++) {
    leds[i] = color;
  }
  FastLED.show();
}

void turnOffRoverLeds() {
  // Set LEDs to Black and turn the LEDs off
  for (int i = 0; i < NUM_LEDS; i++) {
    leds[i] = CRGB::Black;
  }
  FastLED.show();
}

void blinkRoverHeadLights() {
  // Get current time and determine how long since last check
  unsigned long currentMillis = millis();
  if (currentMillis - previousMillis_Headlights >= headlight_interval) {
    previousMillis_Headlights = currentMillis;

    if (headLightState == LOW) {
      headLightState = HIGH;
    } else {
      headLightState = LOW;
    }

    // Turn rover headlights on/off depending on LED state
    digitalWrite(ROVER_HEADLIGHTS, headLightState);
  }
}

void activateRoverPlanetCollectionLight(CRGB planetColor) {
  unsigned long interval = 1500; //secs to repeat
  FastLED.clear();
  turnOnCanopyLight(planetColor);
  unsigned long startMillis = millis(); 
  unsigned long endMillis = startMillis;
  while (endMillis - startMillis <= interval) { 
    cyclePlanetCaptureLed(planetColor);
    endMillis = millis();
  }
  turnOnRoverLeds(CRGB::Turquoise, true);
}

void cyclePlanetCaptureLed(CRGB planetColor) {
  for (int i = 0; i < NUM_LEDS-1; i++) {
    leds[i] = planetColor;
    FastLED.show();
    delay(150);
    leds[i] = CRGB::Black;
  }
}

void blinkRoverDamageLights() {
  turnOnRoverLeds(CRGB::DarkRed, false);
  delay(1000);
  turnOnRoverLeds(CRGB::Turquoise, true);
}

void turnOnRoverHeadLights() {
  digitalWrite(ROVER_HEADLIGHTS, HIGH);     // turn on Rover Headlight
}

void turnOffRoverHeadLights() {
  digitalWrite(ROVER_HEADLIGHTS, LOW);     // turn off Rover Headlight
}

void turnOffAllLights() {
  turnOffCanopyLight();
  FastLED.clear();
  FastLED.show();
  turnOffRoverHeadLights();
}

void resetColorDetection() {
  blueCaptured = false;
  greenCaptured = false;
  yellowCaptured = false;
  purpleCaptured = false;
}

void resetGameStartState() {
  isWSConnected=false;
  isGameStarted=false;
  isGameModePlanetHop=false;
  isGameModeGuided=false;
  turnOffAllLights();
  resetColorDetection();
}
