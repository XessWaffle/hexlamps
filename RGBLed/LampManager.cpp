#include "LampManager.h"
#include <Arduino.h>


LampManager::LampManager(SwatchList list){
  _list = list;
  for(int i = 0; i < TRANSITION_RESOLUTION; i++){
    _transition[i] = new Swatch();
  }

  _draw = new Swatch();
  _curr = new Swatch();
  
}

LampManager::LampManager(){
  for(int i = 0; i < TRANSITION_RESOLUTION; i++){
    _transition[i] = new Swatch();
  }

  _draw = new Swatch();
  _curr = new Swatch();
}

int LampManager::getDelay(){
  return _userDelay;
}
void LampManager::setDelay(int delayTime){
  _userDelay = delayTime;
  
  if(_transitionMode == TRANSITION_MODE_SMOOTH){
    _actionDelay = _userDelay/TRANSITION_RESOLUTION;
  } else if(_transitionMode == TRANSITION_MODE_FLASH){
    _actionDelay = _userDelay/2;
  } else if(_transitionMode == TRANSITION_MODE_DEFAULT){
    _actionDelay = _userDelay;
  }
}

int LampManager::getTransitionMode(){
  return _transitionMode;
}

void LampManager::setTransitionMode(int transitionMode){

  _prevTransitionMode = _transitionMode;
  
  if(transitionMode == TRANSITION_MODE_SMOOTH){
    _actionDelay = _userDelay/TRANSITION_RESOLUTION;
  } else if(transitionMode == TRANSITION_MODE_FLASH){
    _actionDelay = _userDelay/2;
  } else if(transitionMode == TRANSITION_MODE_DEFAULT){
    _actionDelay = _userDelay;
  }
  
  _transitionMode = transitionMode;
}

int LampManager::getSwatchMode(){
  return _swatchMode;
}

int LampManager::getDrawMode(){
  return _drawMode;
}

void LampManager::setDrawMode(int drawMode){
  _drawMode = drawMode;
}

SwatchList LampManager::getSwatchList(){
  return _list;
}

void LampManager::setSwatchList(SwatchList list){
  _list = list;
}

void LampManager::setAnimation(Animation anim){
  _anim = anim;
}

void LampManager::setSwatchMode(int swatchMode){
  _swatchMode = swatchMode;
}

Swatch* LampManager::getDrawSwatch(){
  return _draw;
}

void LampManager::setDrawSwatch(int color){
  _draw->fromInt(color);
}

void LampManager::generateTransition(){
  float dr = (float)(_next.getRed() - _first.getRed())/(TRANSITION_RESOLUTION - 1);
  float dg = (float)(_next.getGreen() - _first.getGreen())/(TRANSITION_RESOLUTION - 1);
  float db = (float)(_next.getBlue() - _first.getBlue())/(TRANSITION_RESOLUTION - 1);

  float r = (float) _first.getRed();
  float g = (float) _first.getGreen();
  float b = (float) _first.getBlue();

  for(int i = 0; i < TRANSITION_RESOLUTION; i++){
    _transition[i]->setRed(r + i * dr);
    _transition[i]->setGreen(g + i * dg);
    _transition[i]->setBlue(b + i * db);
  }

}

void LampManager::determineNextSwatch(){
  if(_drawMode == DRAW_MODE_CONTROLLED_ANIMATION){
    
    _first = *(_anim.peekPrevFrame()->toDraw);
    _next = *(_anim.currFrame()->toDraw);
   
  } else {
  
    _first = _list.getSwatch(_swatchIndex);
  
    if(_swatchMode == SWATCH_MODE_RANDOM){
      
      int nextIndex = random(_list.numSwatches());
      
      do{
        nextIndex = random(_list.numSwatches());
      }while(nextIndex == _swatchIndex);
    
    
      _next = _list.getSwatch(nextIndex);
    
      _swatchIndex = nextIndex;
      
    } else if(_swatchMode == SWATCH_MODE_LOOP){
      int nextIndex = _swatchIndex + 1;
  
      if(nextIndex >= _list.numSwatches()) nextIndex = 0;
  
      _next = _list.getSwatch(nextIndex);
  
      _swatchIndex = nextIndex;
      
    } else if(_swatchMode == SWATCH_MODE_FLIPFLOP){
  
      if(flipflop){
        int nextIndex = _swatchIndex + 1;
  
        if(nextIndex >= _list.numSwatches()){
          nextIndex = _swatchIndex - 1;
          flipflop = false;
        }
  
        _next = _list.getSwatch(nextIndex);
  
        _swatchIndex = nextIndex;
      
      } else {
         int nextIndex = _swatchIndex - 1;
  
        if(nextIndex < 0){
          nextIndex = _swatchIndex + 1;
          flipflop = true;
        }
  
        _next = _list.getSwatch(nextIndex);
  
        _swatchIndex = nextIndex;
      }
      
    }
  }

}

void LampManager::updateLamp(){
  if(!holdCurr){
    if(_transitionMode == TRANSITION_MODE_SMOOTH){
      
      if(_transitionIndex == TRANSITION_RESOLUTION){
        if(_drawMode != DRAW_MODE_CONTROLLED_ANIMATION){
          this->determineNextSwatch();
        }
        
        holdCurr = true;
        
        this->generateTransition();
        _transitionIndex = 0;
      } else {
  
        analogWrite(RED_PIN, _transition[_transitionIndex]->getPWMRed());
        analogWrite(GREEN_PIN, _transition[_transitionIndex]->getPWMGreen());
        analogWrite(BLUE_PIN, _transition[_transitionIndex++]->getPWMBlue());

        _curr->setRed(_transition[_transitionIndex - 1]->getRed());
        _curr->setGreen(_transition[_transitionIndex - 1]->getGreen());
        _curr->setBlue(_transition[_transitionIndex - 1]->getBlue());
        
      }
      
    } else if(_transitionMode == TRANSITION_MODE_DEFAULT){
  
      if(_drawMode != DRAW_MODE_CONTROLLED_ANIMATION){
        this->determineNextSwatch();
      }
      
      analogWrite(RED_PIN, _next.getPWMRed());
      analogWrite(GREEN_PIN, _next.getPWMGreen());
      analogWrite(BLUE_PIN, _next.getPWMBlue());
  
      _curr->setRed(_next.getRed());
      _curr->setGreen(_next.getGreen());
      _curr->setBlue(_next.getBlue());
  
      holdCurr = true;
      
    } else if(_transitionMode == TRANSITION_MODE_FLASH){
      if(flash){
        analogWrite(RED_PIN, _next.getPWMRed());
        analogWrite(GREEN_PIN, _next.getPWMGreen());
        analogWrite(BLUE_PIN, _next.getPWMBlue());
  
        _curr->setRed(_next.getRed());
        _curr->setGreen(_next.getGreen());
        _curr->setBlue(_next.getBlue());
        
        flash = !flash;
      } else {
  
        if(_drawMode != DRAW_MODE_CONTROLLED_ANIMATION){
          this->determineNextSwatch();
        }
        
        analogWrite(RED_PIN, 0);
        analogWrite(GREEN_PIN, 0);
        analogWrite(BLUE_PIN, 0);
  
        _curr->setRed(0);
        _curr->setGreen(0);
        _curr->setBlue(0);
  
        flash = !flash;
  
        holdCurr = true;
      }
    }
  }
}

void LampManager::updateManager(){
  if(_drawMode == DRAW_MODE_ANIMATION){
    _currTime = millis();
  
    if(_currTime - _startTime > _actionDelay){
      this->updateLamp(); 
      holdCurr = false; 
      _startTime = millis();
    }
  } else if(_drawMode == DRAW_MODE_DIRECT){
    _currTime = millis();
  
    if(_currTime - _startTime > DRAW_DIRECT_DELAY){

      if(_curr->getRed() > _draw->getRed()){ _curr->setRed(_curr->getRed() - 1); } else if(_curr->getRed() < _draw->getRed()){ _curr->setRed(_curr->getRed() + 1); }
      if(_curr->getGreen() > _draw->getGreen()){ _curr->setGreen(_curr->getGreen() - 1); } else if(_curr->getGreen() < _draw->getGreen()){ _curr->setGreen(_curr->getGreen() + 1); }
      if(_curr->getBlue() > _draw->getBlue()){ _curr->setBlue(_curr->getBlue() - 1); } else if(_curr->getBlue() < _draw->getBlue()){ _curr->setBlue(_curr->getBlue() + 1); }
      
      analogWrite(RED_PIN, _curr->getPWMRed());
      analogWrite(GREEN_PIN, _curr->getPWMGreen());
      analogWrite(BLUE_PIN, _curr->getPWMBlue());
      
      _startTime = millis();
    }
  } else if(_drawMode == DRAW_MODE_CONTROLLED_ANIMATION){
    _currTime = millis();

    if(_currTime - _startTime > _actionDelay){
      this->updateLamp();
      _startTime = millis();
    }
  }
}

void LampManager::initiate(){
  _startTime = millis();
}

void LampManager::resetSwatch(){
  _swatchIndex = 0;
}

void LampManager::unhold(){
  holdCurr = false;
  this->determineNextSwatch();
}

bool LampManager::isHold(){
  return holdCurr;
}
