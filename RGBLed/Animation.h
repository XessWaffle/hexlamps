#include "Swatch.h"
#include "Arduino.h"

#define MAX_FRAMES 100

typedef struct Frame{
  Swatch* toDraw;
  int transitionMode;
  int del;
} frame_t;

class Animation{
  public:
    Animation();

    frame_t* getFrame(int ind);
    void setFrame(int ind, frame_t nFrame);
    void swapFrames(int from, int to);
    void addFrame(frame_t nFrame);
    void removeFrame(int frame);
    void clearFrames();
    void resetAnimation();
    int getNumFrames();
    int getCurrFrame();

    frame_t* next();
    frame_t* currFrame();
    frame_t* peekNextFrame();
    frame_t* peekPrevFrame();

  private:
    frame_t* _frames[MAX_FRAMES];
    int _currFrame, _numFrames;
    
};
