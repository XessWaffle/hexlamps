#include "ESP8266WiFi.h"
#include "heltec.h"

#define MAX_REQUEST 500
#define MAX_CLIENTS 25
#define APP_CLIENT -9
#define RESP_CLIENT_MULTIPLIER -10
#define DISCONNECT 10000

typedef struct ClientNode{
  WiFiClient *clientManage;
  
  char request[MAX_REQUEST] = "";
  
  bool requestComplete = false, 
        processed = false, 
        empty = true, 
        reqDisconnect = false;
  
  int lampNumber = -1;
  int reqLength = 0;

  int disconnectTimer = 0;
  
} clientNode_t;


class ClientManager{
    public:
      ClientManager();
      
      bool addClient(WiFiClient nextClient);
      clientNode_t* process();
      bool globalInstruction(String inst);
      bool localInstruction(String inst, int lampNumber);
      bool appInstruction(String inst, int responseId);
      bool clientConnected(int lampNumber);
      
      int nodes();
      void purgeClients(int lampNumber);

    private:
      clientNode_t *_clients[MAX_CLIENTS];
      int _openIndex = 0;
      int _nodes;
};
