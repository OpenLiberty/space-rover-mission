#include<SoftwareSerial.h>

SoftwareSerial SUART(2, 3); //SRX=2; STX=3
bool DEBUG = false;   //show more logs in Serial monitor
bool CALIBRATE = false; //Set to true if color sensor calibration is required

/***********
    PINS
************/
// COLOR SENSOR
const int s0 = 13;
const int s1 = 9;
const int s2 = 10;
const int s3 = 11;
const int out = 12;

// LEDs
const int LEDPIN1 = 8;
const int LEDPIN2 = 7;
const int LEDPIN3 = 6;
const int LEDPIN4 = A0;
const int LEDPIN5 = 5;
const int LEDPIN6 = 4;

// Array with Arduino pins containing LEDs in sequence
byte LEDpins[] = {8,7,6,A0,5,4.};

// Rover Headlights
const int ROVER_HEADLIGHTS = A1;

// Rover Damage Lights
const int DAMAGE_LIGHTS = A2;

/***********
  VARIABLES
************/
// Serial Communication between Arduino and NodeMCU
const byte numChars = 32;
char receivedMsg[numChars];
boolean newData = false;

// Color sensor reading values
int redColor = 0;
int greenColor = 0;
int blueColor = 0;

// Rover Lights
unsigned long previousMillis=0; // Will be used to track how long since last event occurred
unsigned long interval = 170; // Delay to determine when we see the next LED, increase value to slow down speed of LED cycling
int LEDstate=0x01; // Variable to track which LED to turn on, start at 0001

// Rover headlights
unsigned long previousMillis_Headlights=0; // Will be used to track how long since last event occurred
int headLightState = LOW; // ledState used to set the LED
const long headlight_interval = 650; // interval at which to blink (milliseconds)

/*********************************
    GAME CONFIGURATION
**********************************/
bool isWifiConnected = false;
bool isWSConnected = false; // WebSocket connection
bool isGameStarted = false;

void setup()
{
  // Serial UART Communication between Arduino and NodeMCU module
  Serial.begin(9600);
  SUART.begin(9600); 

  // Led pins assignment
  pinMode(LEDPIN1, OUTPUT);
  pinMode(LEDPIN2, OUTPUT);
  pinMode(LEDPIN3, OUTPUT);
  pinMode(LEDPIN4, OUTPUT);
  pinMode(LEDPIN5, OUTPUT);
  pinMode(LEDPIN6, OUTPUT);

  // Rover Headlights
  pinMode(ROVER_HEADLIGHTS, OUTPUT);

  // Rover Damage Lights
  pinMode(DAMAGE_LIGHTS, OUTPUT);

  // Color Sensor pins
  pinMode(s0, OUTPUT);  
  pinMode(s1, OUTPUT);  
  pinMode(s2, OUTPUT);  
  pinMode(s3, OUTPUT);  
  pinMode(out, INPUT); 

  // Setting color filter frequency scaling to 20% for Arduino
  digitalWrite(s0,HIGH);
  digitalWrite(s1,LOW); 

  Serial.println("<Arduino setup is complete>");
}

void loop()
{
  // Check for incoming messages from NodeMCU
  receiveMsgsWithStartEndMarkers();
  processNewData();
  
  if (!isWifiConnected && !isGameStarted) { // no wifi connection
    blinkRoverLeds();
  } 
  
  if (isWifiConnected && !isWSConnected && !isGameStarted) { // no websocket connection
    cycleRoverLeds();
    blinkRoverHeadLights();
  } 
 
  if (isWifiConnected && isWSConnected && !isGameStarted) { // standby mode
   blinkRoverHeadLights();
   turnOnRoverLeds();
  }

  if (isWifiConnected && isWSConnected && isGameStarted) { // game started
   turnOnRoverHeadLights();
   detectColor();
  }

  if (isWifiConnected && !isWSConnected && isGameStarted) { // Error state
     turnOnRoverDamageLights();
  }

  if (CALIBRATE)
    detectColor();
}

// Function used to receive messages from NodeMCU with start (<) and end (>) markers
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
    if (newData == true) {
        if (DEBUG) {
          Serial.print("Received msg : ");
          Serial.println(receivedMsg);
        }

        if (strcmp(receivedMsg, "C") == 0) {
           isWifiConnected=true;
        }
        else if (strcmp(receivedMsg, "WSC") == 0) {
           isWSConnected=true;
        }
        else if (strcmp(receivedMsg, "GS") == 0) {
            isGameStarted=true;
        }
        else if (strcmp(receivedMsg, "GE") == 0) {
            isWSConnected=false;
            isGameStarted=false;
        }
        newData = false;
    }
}

void detectColor() {
  getReadingsFromSensor();
  if (DEBUG) { 
    Serial.print("R:");  
    Serial.print(redColor, DEC);  
    Serial.print(" G: ");  
    Serial.print(greenColor, DEC);  
    Serial.print(" B: ");  
    Serial.print(blueColor, DEC);  
    Serial.println();
  }
  determineColor();
}

void getReadingsFromSensor()  
{    
  // RED
  digitalWrite(s2, LOW);  
  digitalWrite(s3, LOW);  
  redColor = pulseIn(out, LOW);
  delay(200);

  if (redColor > 1000 || redColor < 0) {
    redColor = 0;
  }

  // BLUE
  digitalWrite(s3, HIGH);  
  blueColor = pulseIn(out, LOW);
  delay(200); 

  if (blueColor > 1000 || blueColor < 0) {
    blueColor = 0;
  }

  // GREEN
  digitalWrite(s2, HIGH);  
  greenColor = pulseIn(out, LOW);
  delay(200); 

  if (greenColor > 1000 || greenColor < 0) {
    greenColor = 0;
  }
  
}

void determineColor() {
  if ((redColor >= 7 && redColor <= 57) && (greenColor >= 15 && greenColor <= 156) && (blueColor >= 24 && blueColor <= 238)) 
  {
   if (DEBUG)   
      Serial.println("YELLOW");
   
   // Send color to NodeMCU
   SUART.println("<YW>");
   delay(500); 
  } 
  else if ((redColor >= 8 && redColor <= 42) && (greenColor >= 29 && greenColor <= 115) && (blueColor >= 10 && blueColor <= 52))    
  {  
   if (DEBUG)
      Serial.println("PURPLE");
   
   // Send color to NodeMCU
   SUART.println("<PUR>"); 
   delay(500); 
  }  
  else if ((redColor >= 0 && redColor <= 780) && (greenColor >= 32 && greenColor <= 71) && (blueColor >= 8 && blueColor <= 21))  
  {
   if (DEBUG)  
      Serial.println("BLUE");
   
   // Send color to NodeMCU
   SUART.println("<BLU>"); 
   delay(500); 
  }  
  else if ((redColor >= 153 && redColor <= 613) && (greenColor >= 24 && greenColor <= 116) && (blueColor >= 48 && blueColor <= 230)) 
  {
   if (DEBUG)  
      Serial.println("GREEN");

   // Send color to NodeMCU
   SUART.println("<GRN>"); 
   delay(500);
  } 
  else if (redColor < blueColor && redColor < greenColor && redColor >= 3 && redColor <= 58) {
   if (DEBUG)
      Serial.println("RED");
   
   // Send color to NodeMCU
   SUART.println("<RED>");
   blinkRoverDamageLights(); 
   delay(500);
  }
}

void blinkRoverLeds() {
  turnOnRoverLeds();
  delay(1000);
  turnOffRoverLeds();
  delay(1000);
}

void cycleRoverLeds() {
  for (int x=0; x < 6; x++)
    digitalWrite(LEDpins[x], bitRead(LEDstate,x));

  // Get current time and determine how long since last check
  unsigned long currentMillis = millis();
  if ((unsigned long)(currentMillis - previousMillis) >= interval) { 
    previousMillis = currentMillis;
 
      // Use "<<" to "bit-shift" everything to the left once
      LEDstate = LEDstate << 1;
      // 0x20 is the "last" LED, another shift makes the value 0x40
      if (LEDstate == 0x40) {
        // turn on the one before "0x01"
        LEDstate = 0x01;
      }
  }
}

void turnOnRoverLeds() {
  digitalWrite(LEDPIN1, HIGH);    // turn on LED1 
  digitalWrite(LEDPIN2, HIGH);    // turn on LED2
  digitalWrite(LEDPIN3, HIGH);    // turn on LED3 
  digitalWrite(LEDPIN4, HIGH);    // turn on LED4
  digitalWrite(LEDPIN5, HIGH);    // turn on LED5
  digitalWrite(LEDPIN6, HIGH);    // turn on LED6    
}

void turnOffRoverLeds() {
  digitalWrite(LEDPIN1, LOW);     // turn off LED1
  digitalWrite(LEDPIN2, LOW);     // turn off LED2
  digitalWrite(LEDPIN3, LOW);     // turn off LED3
  digitalWrite(LEDPIN4, LOW);     // turn off LED4
  digitalWrite(LEDPIN5, LOW);     // turn off LED5
  digitalWrite(LEDPIN6, LOW);     // turn off LED6
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

void turnOnRoverHeadLights() {
  digitalWrite(ROVER_HEADLIGHTS, HIGH);     // turn on Rover Headlight
}

void turnOffRoverHeadLights() {
  digitalWrite(ROVER_HEADLIGHTS, LOW);     // turn off Rover Headlight
}

void blinkRoverDamageLights() {
  turnOnRoverDamageLights();
  delay(1000);
  turnOffRoverDamageLights();
}

void turnOffRoverDamageLights() {
  digitalWrite(DAMAGE_LIGHTS, LOW);     // turn off Rover Damage light
}

void turnOnRoverDamageLights() {
  digitalWrite(DAMAGE_LIGHTS, HIGH);     // turn on Rover Damage light
}
