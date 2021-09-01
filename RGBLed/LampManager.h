#include "SwatchList.h"
#include "Animation.h"

#define TRANSITION_MODE_SMOOTH 1
#define TRANSITION_MODE_FLASH 2
#define TRANSITION_MODE_DEFAULT 3

#define TRANSITION_RESOLUTION 100

#define SWATCH_MODE_RANDOM 4
#define SWATCH_MODE_FLIPFLOP 5
#define SWATCH_MODE_LOOP 6

#define DRAW_MODE_ANIMATION 7
#define DRAW_MODE_DIRECT 8
#define DRAW_MODE_SYNC 9
#define DRAW_MODE_CONTROLLED_ANIMATION 10

#define DRAW_DIRECT_DELAY 3 
#define DEFAULT_DELAY 1000

#define RED_PIN 16
#define GREEN_PIN 4
#define BLUE_PIN 5

class LampManager{
  public:
    LampManager();
    LampManager(SwatchList list);

    int getDelay();
    void setDelay(int delayTime);

    int getTransitionMode();
    void setTransitionMode(int transitionMode);

    int getSwatchMode();
    void setSwatchMode(int swatchMode);

    int getDrawMode();
    void setDrawMode(int drawMode);

    Swatch* getDrawSwatch();
    void setDrawSwatch(int color);

    void generateTransition();
    void determineNextSwatch();

    SwatchList getSwatchList();
    void setSwatchList(SwatchList list);

    Animation getAnimation();
    void setAnimation(Animation anim);

    void updateLamp();
    void updateManager();

    void initiate();

    void resetSwatch();

    void unhold();
    bool isHold();

  private:
    Swatch *_transition[TRANSITION_RESOLUTION];
    Swatch _first, _next;
    Swatch *_draw, *_curr;

    SwatchList _list;
    Animation _anim;

    bool flash = false;
    int _transitionMode = TRANSITION_MODE_DEFAULT, _transitionIndex = 0, _prevTransitionMode = TRANSITION_MODE_DEFAULT;

    bool flipflop = false;
    int _swatchMode = SWATCH_MODE_LOOP, _swatchIndex = 0;
    int _counter = 0, _startTime = 0, _currTime = 0, _userDelay = DEFAULT_DELAY, _actionDelay = DEFAULT_DELAY;
    
    int _drawMode = DRAW_MODE_ANIMATION;

    bool holdCurr = false;
    
    
};
