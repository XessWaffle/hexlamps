#include "ClientManager.h"

ClientManager::ClientManager(){
  _nodes = 0;
  Serial.println("Preparing client nodes");
  
  for(int i = 0; i < MAX_CLIENTS; i++){

    _clients[i] = (clientNode_t*) malloc(sizeof(clientNode_t));
    
    //_clients[i]->clientManage = NULL;
    _clients[i]->processed = false;
    _clients[i]->requestComplete = false;
    _clients[i]->reqDisconnect = false;
    _clients[i]->empty = true;
    _clients[i]->reqLength = 0;
    _clients[i]->lampNumber = -1;
    _clients[i]->disconnectTimer = 0;
    
    for(int j = 0; j < MAX_REQUEST; j++){
      _clients[i]->request[j] = ' ';
    }
  }

  Serial.println("Nodes prepped");
  
}
    
bool ClientManager::addClient(WiFiClient nextClient){

  if(_nodes >= MAX_CLIENTS){
    _openIndex = -1;
  } else {
    _clients[_openIndex]->clientManage = new WiFiClient(nextClient);
    _clients[_openIndex]->empty = false;
    
    _nodes++;
  }
  
  return true;
}

clientNode_t* ClientManager::process(){

  clientNode_t* ret = NULL;
  
  for(int i = 0; i < MAX_CLIENTS; i++){
    if(!_clients[i]->empty) {
   
      if(!_clients[i]->clientManage->connected() && !_clients[i]->clientManage->available()){
        _clients[i]->reqDisconnect = true;
      } else if(_clients[i]->disconnectTimer == DISCONNECT){
        _clients[i]->reqDisconnect = true;
      }

      _clients[i]->disconnectTimer++;
      
      
      if(_clients[i]->reqDisconnect){
        //Serial.println("REMOVE START");
        _clients[i]->clientManage->flush();
        _clients[i]->clientManage->stop();

        delete _clients[i]->clientManage;
        
        _clients[i]->clientManage = NULL;
        _clients[i]->processed = false;
        _clients[i]->empty = true;
        _clients[i]->requestComplete = false;
        _clients[i]->reqDisconnect = false;
        _clients[i]->reqLength = 0;
        _clients[i]->lampNumber = -1;
        _clients[i]->disconnectTimer = 0;

        for(int j = 0; j < MAX_REQUEST; j++){
          _clients[i]->request[j] = ' ';
        }
        
        _openIndex = i;
        
        //Serial.println("REMOVE END");
        _nodes--;
      } else if(_clients[i]->processed){
         
         for(int j = 0; j < MAX_REQUEST; j++){
            _clients[i]->request[j] = ' ';
         }
         
         _clients[i]->processed = false;
         _clients[i]->requestComplete = false;
         _clients[i]->reqLength = 0;
         _clients[i]->clientManage->flush();
        
      } else if(_clients[i]->requestComplete){

        String pre_req = String(_clients[i]->request);
        
        if(pre_req.substring(0, 4) == "lamp"){
          //Serial.println(pre_req.substring(5, _clients[i]->reqLength));
          int ln = pre_req.substring(5, _clients[i]->reqLength).toInt();
          
          this->purgeClients(ln);
          
          _clients[i]->lampNumber = ln;
          _clients[i]->processed = true;
      
          
        } else if(pre_req.substring(0, 4) == "ping"){
          _clients[i]->processed = true;
        } else {
          ret = _clients[i]; 
        }  
        
      } else if(_clients[i]->clientManage->available()){
        char next = _clients[i]->clientManage->read();
        
        _clients[i]->disconnectTimer = 0;
        
        if(next == '\r'){
          _clients[i]->requestComplete = true;
        } else {
          if(_clients[i]->reqLength < MAX_REQUEST){
            _clients[i]->request[_clients[i]->reqLength++] = next;
          }
        }
      }
    } else {      
      _openIndex = i;
    }
  }

  return ret;
  
}

bool ClientManager::globalInstruction(String inst){
  for(int i = 0; i < MAX_CLIENTS; i++){
      if(!_clients[i]->empty && _clients[i]->lampNumber > 0) {
        _clients[i]->clientManage->print(inst);
      }
  }

  return true;
}

bool ClientManager::localInstruction(String inst, int lampNumber){
   for(int i = 0; i < MAX_CLIENTS; i++){
      if(_clients[i]->lampNumber == lampNumber) {
        _clients[i]->clientManage->print(inst);

        return true;
      }
  }
}

bool ClientManager::appInstruction(String inst, int responseId){
  for(int i = 0; i < MAX_CLIENTS; i++){
      if(_clients[i]->lampNumber == responseId) {
        _clients[i]->clientManage->print(inst);

        return true;
      }
  }
}

bool ClientManager::clientConnected(int lampNumber){
  for(int i = 0; i < MAX_CLIENTS; i++){
    if(_clients[i]->lampNumber == lampNumber){
      return true;
    }
  }

  return false;
}

int ClientManager::nodes(){
  return _nodes;
}

void ClientManager::purgeClients(int lampNumber){
  for(int i = 0; i < MAX_CLIENTS; i++){
    if(_clients[i]->lampNumber == lampNumber){
      _clients[i]->reqDisconnect = true;
    }
  }
}
