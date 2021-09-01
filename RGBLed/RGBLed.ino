#include <ESP8266WiFi.h>        // Include the Wi-Fi library
#include "LampManager.h"

#define RED_PIN 16
#define GREEN_PIN 4
#define BLUE_PIN 5

#define LAMP_NUMBER 1

#define MAX_BRIGHTNESS 900
#define PING_TIME 10000


IPAddress server(42,42,42,42);     // IP address of the AP
WiFiClient lampClient;

LampManager lm;
SwatchList temp;
Animation build;

String lastInst;
int ping = PING_TIME;
bool isOn = false;
bool frameFinished = false;

void setup() {

  Serial.begin(9600);
  
  pinMode(RED_PIN, OUTPUT);
  pinMode(GREEN_PIN, OUTPUT);
  pinMode(BLUE_PIN, OUTPUT);
  pinMode(LED_BUILTIN, OUTPUT);

  WiFi.begin("LampInterface", "yeeticus1268");

  while(WiFi.status() != WL_CONNECTED){
    digitalWrite(LED_BUILTIN, HIGH);
    delay(500);
    digitalWrite(LED_BUILTIN, LOW);
    delay(500);
  }

  for(int i = 0; i < 5; i++){
    digitalWrite(LED_BUILTIN, HIGH);
    delay(100);
    digitalWrite(LED_BUILTIN, LOW);
    delay(100);
  }

  lampClient.connect(server, 80);
  sendInstruction(String("lamp " + String(LAMP_NUMBER) + "\r"));

  temp = SwatchList();
  temp.addSwatch(Swatch(255, 0, 0)); //red
  temp.addSwatch(Swatch(0, 255, 0)); //green
  temp.addSwatch(Swatch(0, 0, 255)); //blue

 /*temp.addSwatch(Swatch(255, 0, 0)); //red
 temp.addSwatch(Swatch(255, 17, 0));  //orange
 temp.addSwatch(Swatch(230, 70, 0));  //yellow*/

  /*temp.addSwatch(Swatch(255, 0, 0));
  temp.addSwatch(Swatch(0,0, 255)); //POLICE*/

  /*temp.addSwatch(Swatch(255, 0, 255));
  temp.addSwatch(Swatch(0, 0, 255));*/

  //red, orange, yellow,
  //green, blue, purple,
  //purple, pink, red
  
  lm = LampManager(temp);
  lm.setTransitionMode(TRANSITION_MODE_SMOOTH);
  lm.setSwatchMode(SWATCH_MODE_RANDOM);
  lm.setDelay(2000);
  lm.initiate();

  build = Animation();

  lm.setAnimation(build);

  delay(200);
  digitalWrite(LED_BUILTIN, HIGH);
}

void loop() {

  if(lampClient.connected()){
    if(lampClient.available()){
      String inst = lampClient.readStringUntil('\r');
      process(inst);
    }

  } else {
    lampClient.flush();
    lampClient.stop();
    
    lampClient.connect(server, 80);
    sendInstruction(String("lamp " + String(LAMP_NUMBER) + "\r"));
  }
  
  if(lm.getDrawMode() == DRAW_MODE_CONTROLLED_ANIMATION && lm.isHold() && !frameFinished){
    String inst = "f ";
    inst += LAMP_NUMBER;
    inst += "\r";
    sendInstruction(inst);

    /*for(int i = 1; i < 7; i++){
      String inst = "f ";
      inst += i;
      inst += "\r";
      sendInstruction(inst);
    }*/
    
    frameFinished = true;
  }
  
  if(isOn){
    lm.updateManager();
  } else {
    analogWrite(RED_PIN, 0);
    analogWrite(GREEN_PIN, 0);
    analogWrite(BLUE_PIN, 0);
  }

 
  
  ping--;

  if(ping == 0){
    sendInstruction("ping\r");
    ping = PING_TIME;
  }
  
}

void sendInstruction(String instruction){
  lastInst = instruction;
  lampClient.print(instruction);
  ping = PING_TIME;
}

void process(String instruction){

  Serial.println(instruction);
  
  
  if(instruction.substring(0, 2) == "on"){
    isOn = true;
    
  } else if(instruction.substring(0, 3) == "off"){
    isOn = false;
    lm.resetSwatch();
  
  } else if(instruction.substring(0, 3) == "dir"){
    int color = instruction.substring(6).toInt();
    lm.setDrawMode(DRAW_MODE_DIRECT);
    lm.setDrawSwatch(color);

    Serial.println(lm.getDrawSwatch()->asInt());
    
  } else if(instruction.substring(0, 3) == "trm"){
    int transitionMode = instruction.substring(6).toInt();
    lm.setDrawMode(DRAW_MODE_ANIMATION);
    lm.setTransitionMode(transitionMode);

    Serial.println(lm.getTransitionMode());
    
  } else if(instruction.substring(0, 3) == "swm"){
    int swatchMode = instruction.substring(6).toInt();
    lm.setDrawMode(DRAW_MODE_ANIMATION);
    lm.setSwatchMode(swatchMode);

    Serial.println(lm.getSwatchMode());
    
  } else if(instruction.substring(0,3) == "del"){
    lm.setDelay(instruction.substring(6).toInt());

    Serial.println(lm.getDelay());
    
  } else if(instruction.substring(0, 4) == "addp"){
    temp.addSwatch(Swatch(instruction.substring(7).toInt()));
    lm.setSwatchList(temp);

    for(int i = 0; i < temp.numSwatches(); i++){
      Serial.println(temp.getSwatch(i).asInt());
    }
    
  } else if(instruction.substring(0, 4) == "remp"){
    temp.removeSwatch(Swatch(instruction.substring(7).toInt()));
    lm.setSwatchList(temp);
    
    for(int i = 0; i < temp.numSwatches(); i++){
      Serial.println(temp.getSwatch(i).asInt());
    }
    
  } else if(instruction.substring(0, 4) == "swap"){
    int to = instruction.substring(instruction.indexOf(":")).toInt();
    int from = instruction.substring(7, instruction.indexOf(":")).toInt();
    
    temp.swapSwatches(from, to);
    lm.setSwatchList(temp);

    for(int i = 0; i < temp.numSwatches(); i++){
      Serial.println(temp.getSwatch(i).asInt());
    }
    
  } else if(instruction.substring(0, 4) == "areq"){

    int ln = LAMP_NUMBER;
    
    String resp = "lrep :l ";
    resp += ln;
    resp += " ";

    resp = String(resp + ":d " + lm.getDrawMode() + " ");
    resp = String(resp + ":t " + lm.getTransitionMode() + " ");
    resp = String(resp + ":s " + lm.getSwatchMode() + " ");
    resp = String(resp + ":w " + lm.getDelay() + " ");
    
    resp = String(resp + ":p ");

    for(int i = 0; i < temp.numSwatches(); i++){
      resp = String(resp + temp.getSwatch(i).asInt() + ",");
    }

    resp = String(resp + " ");

    resp = String(resp + ":c " + lm.getDrawSwatch()->asInt() + "\r");

    sendInstruction(resp);
    Serial.println(resp);
    
  } else if(instruction.substring(0, 4) == "anim"){
    if(instruction.substring(5,8) == "end"){
      lm.setDrawMode(DRAW_MODE_DIRECT);
      lm.unhold();
      build.resetAnimation();
      lm.setAnimation(build);
      Serial.println("STOPPING");
    } else {
      lm.setDrawMode(DRAW_MODE_CONTROLLED_ANIMATION);
      Serial.println("STARTING");
    }
  } else if(instruction.substring(0, 4) == "addf"){
    String check = instruction.substring(4);
    check.trim();
    
    frame_t* next = parseFrame(check);

    build.addFrame(*next);

    free(next->toDraw);
    free(next);

    printFrames();

    build.resetAnimation();
    lm.setAnimation(build);
    lm.determineNextSwatch();
     
  } else if(instruction.substring(0, 4) == "setf"){

    instruction = instruction.substring(4);
    instruction.trim();
    
    int loc = instruction.substring(0, instruction.indexOf(" ")).toInt();

    String check = instruction.substring(instruction.indexOf(" "));
    check.trim();

    frame_t* setfr = parseFrame(check);

    build.setFrame(loc, *setfr);

    free(setfr->toDraw);
    free(setfr);

    printFrames();
    build.resetAnimation();
    lm.setAnimation(build);
    lm.determineNextSwatch();
   
  } else if(instruction.substring(0, 4) == "swaf"){
    int to = instruction.substring(instruction.indexOf(":") + 1).toInt();
    int from = instruction.substring(5, instruction.indexOf(":")).toInt();
    
    build.swapFrames(from, to);
    printFrames();
    build.resetAnimation();
    lm.setAnimation(build);
    lm.determineNextSwatch();

   
  } else if(instruction.substring(0, 4) == "remf"){
    build.removeFrame(instruction.substring(5).toInt());
    printFrames();
    build.resetAnimation();
    lm.setAnimation(build);
    lm.determineNextSwatch();
   
  } else if(instruction.substring(0, 5) == "start"){
    printFrame(build.getCurrFrame());
    lm.setTransitionMode(build.currFrame()->transitionMode);
    lm.setDelay(build.currFrame()->del);
    lm.unhold();
    build.next();
    lm.setAnimation(build);
    frameFinished = false;
  } else if(instruction.substring(0, 6) == "clearf"){
    build.clearFrames();
    build.resetAnimation();
    lm.setAnimation(build);
    lm.determineNextSwatch();
    printFrames();
  }
  
}

Swatch parseRGB(String instruction){
  int r, g, b;
  r = instruction.substring(0, instruction.indexOf(" ")).toInt();
  instruction = instruction.substring(instruction.indexOf(" ") + 1);
  g = instruction.substring(0, instruction.indexOf(" ")).toInt();
  instruction  = instruction.substring(instruction.indexOf(" ") + 1);
  b = instruction.toInt();

  
  return Swatch(r, g, b);
}

frame_t* parseFrame(String check){
  frame_t* nextFrame = new frame_t();
  
  while(check.indexOf(":") >= 0){
    check = check.substring(check.indexOf(":") + 1);

    String alias = check.substring(0, 1);
    String cmd = check.substring(2);
    cmd.trim();
    
    
    if(alias == "l"){
      int ln = cmd.substring(0, cmd.indexOf(" ")).toInt();

      if(ln == LAMP_NUMBER){
        String infoStr = cmd.substring(cmd.indexOf(" "));
        infoStr.trim();
        int color = infoStr.substring(0, infoStr.indexOf(" ")).toInt();
        
        nextFrame->toDraw = new Swatch(color);
        
      }
    } else if(alias == "d"){
      nextFrame->del = cmd.substring(0, cmd.indexOf(" ")).toInt();
    } else if(alias == "t"){
      nextFrame->transitionMode = cmd.substring(0, cmd.indexOf(" ")).toInt();
    }
    
  }

  return nextFrame;

}

void printFrames(){
   
    for(int i = 0; i < build.getNumFrames(); i++){
      printFrame(i);
    }
    
}

void printFrame(int i){
  Serial.print("Frame ");
  Serial.print(i);
  Serial.print(" ");
  Serial.print(build.getFrame(i)->toDraw->asInt());
  Serial.print(" ");
  Serial.print(build.getFrame(i)->del);
  Serial.print(" ");
  Serial.println(build.getFrame(i)->transitionMode);
}

void parseDrawMode(String instruction){
 
  if(instruction == "direct"){
    lm.setDrawMode(DRAW_MODE_DIRECT);
  } else if(instruction == "animation"){
    lm.setDrawMode(DRAW_MODE_ANIMATION);
  } else if(instruction == "sync"){
    lm.setDrawMode(DRAW_MODE_SYNC);
  }
}

void parseTransitionMode(String instruction){

  if(instruction == "smooth"){
    lm.setTransitionMode(TRANSITION_MODE_SMOOTH);
  } else if(instruction == "flash"){
    lm.setTransitionMode(TRANSITION_MODE_FLASH);
  } else if(instruction == "default"){
    lm.setTransitionMode(TRANSITION_MODE_DEFAULT);
  }
}

void parseSwatchMode(String instruction){
  if(instruction == "loop"){
    lm.setSwatchMode(SWATCH_MODE_LOOP);
  } else if(instruction == "random"){
    lm.setSwatchMode(SWATCH_MODE_RANDOM);
  } else if(instruction == "flipflop"){
    lm.setSwatchMode(SWATCH_MODE_FLIPFLOP);
  }
}
