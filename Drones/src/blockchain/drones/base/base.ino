#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>

const int switchPin = 13; //to be edited by user
const int powerPin = 14; //to be edited by user
const int output = 2; //to be edited by user
const char* ssid = "BC-3688"; //to be edited by user
const char* password = "bowsterwifi"; //to be edited by user
const char* host = "http://10.142.147.184:8080";
const char* padNumber = "2"; //to be edited by user
char userID;
int power;

void setup() {
Serial.begin(115200);
  WiFi.begin(ssid, password);
 
  while (WiFi.status() != WL_CONNECTED) {
 
    delay(1000);
    Serial.print("Connecting..");
 
  }
  Serial.println();
  pinMode(switchPin, OUTPUT);
  pinMode(powerPin, OUTPUT);
  digitalWrite(powerPin, HIGH);
}

void loop() {
  while(true) {
    power = getPower();
    if (power != 0) {
      break;
    }
    delay(1000);
  }
  Serial.println(power);
  int time = 60 * 60 * power / output;
  Serial.println(time);
  digitalWrite(switchPin, HIGH);
  delay(time);
  Serial.println("completed");
  digitalWrite(switchPin, LOW);
  post();
}

int getPower() {
   if (WiFi.status() == WL_CONNECTED) { //Check WiFi connection status
   HTTPClient http;  //Declare an object of class HTTPClient
   String url = host;
   url+= "/device";
   url += "?pad=";
   url += padNumber;
   http.begin(url);  //Specify request destination
   int httpCode = http.GET();                                                                  //Send the request
    if (httpCode > 0) { //Check the returning code
      String payload = http.getString();
      Serial.println();//Get the request response payload
      Serial.println(payload);
      http.end(); 
      if(payload.length() == 25) {
        return 0;
      } else {
        userID = payload.charAt(61);
        Serial.println(userID);
        return payload.charAt(28) - 48;
      }
    }
  }
}

void post() {
  if (WiFi.status() == WL_CONNECTED) {
    HTTPClient http;
    String url = host;
    url += "/device";
    url += "?user=";
    url += userID;
    url += "&pad=";
    url += padNumber;
    url += "&expected=";
    url += power;
    http.begin(url);
    int httpCode = http.POST("Transaction completed");
    String payload = http.getString();                                        //Get the response payload
    Serial.println(httpCode);   //Print HTTP return code
    Serial.println(payload);    //Print request response payload
    http.end();
  }
}
