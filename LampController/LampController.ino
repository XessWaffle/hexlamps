#include "heltec.h"
#include "ESP8266WiFi.h"
#include "ClientManager.h"

#define READY_STATE 0x3F
#define SERIAL_BUFFER 3

WiFiServer server(80);

IPAddress IP(42,42,42,42);
IPAddress mask = (255, 255, 255, 0);

int currState;
String _dispStack[SERIAL_BUFFER];

ClientManager cm;

void setup()
{

  Serial.begin(115200);
  
  Heltec.begin(true /*DisplayEnable Enable*/, true /*Serial Enable*/);
  
  Heltec.display->clear();

  Heltec.display->drawString(0, 0, "Initializing Soft AP...");
  Heltec.display->display();

  delay(1000);

  WiFi.mode(WIFI_AP);
  
  if(WiFi.softAP("LampInterface", "yeeticus1268", 1, false, 8))
  {
     Heltec.display->clear();
     Heltec.display->drawString(0, 0, "Ready");
     Heltec.display->display();
  }
  else
  {
     Heltec.display->clear();
     Heltec.display->drawString(0, 0, "Failed");
     Heltec.display->display();
  }
    
  WiFi.softAPConfig(IP, IP, mask);
  server.begin();

  cm = ClientManager();
  
  currState = 0;
  
  delay(1000);
}

void loop()
{

  WiFiClient nClient = server.available();

  if(nClient){
    cm.addClient(nClient);
  }

  clientNode_t* toProcess = cm.process();

  if(toProcess != NULL){
    process(toProcess);
  }

  if(currState == READY_STATE){
    cm.globalInstruction("start\r");
    currState = 0;
  }
  
  String stations = String("Stations Connected: ");
  String statprt = String(stations + String(WiFi.softAPgetStationNum()));

  String nodes = "Clients: ";
  String cliprt = String(nodes + String(cm.nodes()));

  Heltec.display->clear();
  Heltec.display->drawString(0, 0, statprt);
  Heltec.display->drawString(0, 10, cliprt);
  Heltec.display->display();
  /*Heltec.display->clear();
  
  for(int i = 0; i < SERIAL_BUFFER; i++){
    Heltec.display->drawString(0, 10 * i, _dispStack[i]);
  }
  
  Heltec.display->display();*/
  
}


void process(clientNode_t* node){
  String req = String(node->request).substring(0, node->reqLength);
  
  Serial.println(req);

  if(req.substring(0, 1) == "f"){

    String check = req.substring(req.indexOf(" ") + 1);
    int ln = check.substring(0, check.indexOf(" ")).toInt();
    
    currState = currState | (0x1 << (ln - 1));

    Serial.println(currState, HEX);
  } else if(req.substring(0, 4) == "areq"){

    int ln = req.substring(5, node->reqLength).toInt();

    node->lampNumber = ln * RESP_CLIENT_MULTIPLIER;
    
    if(cm.clientConnected(ln)){
      cm.localInstruction(String(req + "\r"), ln);
    } else {
      cm.appInstruction("nc\r", node->lampNumber);
      cm.appInstruction("done\r", node->lampNumber);
    }
  } else if(req.substring(0, 4) == "lrep"){

    int respId = req.substring(8, 9).toInt() * RESP_CLIENT_MULTIPLIER;
    
    cm.appInstruction(req + "\r", respId);
    cm.appInstruction("done\r", respId);
    
  } else if(req.substring(0, 4) == "anim" || req.substring(0, 4) == "remf" || req.substring(0, 4) == "swaf"){
    cm.globalInstruction(String(req + "\r"));
    
  } else if(req.substring(0, 4) == "addf" || req.substring(0, 4) == "setf") {

    String check = req.substring(req.indexOf(":l"));
    
    check = check.substring(check.indexOf(" ") + 1);
    
    int ln = check.substring(0, check.indexOf(" ")).toInt();

    cm.localInstruction(String(req + "\r"), ln);
  } else {
    String check = req.substring(req.indexOf(" ") + 1);
    int ln = check.substring(0, check.indexOf(" ")).toInt();

    cm.localInstruction(String(req + "\r"), ln);
    
  }

  node->processed = true;
}

void push(String push){
  for(int i = 0; i < SERIAL_BUFFER; i++){
    _dispStack[i] = _dispStack[i+1];
  }

  _dispStack[SERIAL_BUFFER - 1] = push;
}
