#include "Animation.h"
Animation::Animation(){
  _currFrame = 0;
  _numFrames = 0;
  
  for(int i = 0; i < MAX_FRAMES; i++){ 
    _frames[i] = (frame_t*) malloc(sizeof(frame_t));
    
    _frames[i]->toDraw = new Swatch();
    _frames[i]->del = -1;
    _frames[i]->transitionMode = -1;
    
  }
}

frame_t* Animation::getFrame(int ind){
  if(ind >= 0 && ind < _numFrames){
    return _frames[ind];
  }

  return _frames[MAX_FRAMES - 1];
}

void Animation::setFrame(int ind, frame_t nFrame){
  if(ind >= 0 && ind < _numFrames){
    _frames[ind]->toDraw->fromInt(nFrame.toDraw->asInt());
    _frames[ind]->del = nFrame.del;
    _frames[ind]->transitionMode = nFrame.transitionMode;
      
  }
}

void Animation::swapFrames(int from, int to){
  
  if(from < _numFrames && from >= 0 && to < _numFrames && to >= 0){
      int del = _frames[from]->del, col = _frames[from]->toDraw->asInt(), trM = _frames[from]->transitionMode;

      _frames[from]->del = _frames[to]->del;
      _frames[from]->transitionMode = _frames[to]->transitionMode;
      _frames[from]->toDraw->fromInt(_frames[to]->toDraw->asInt());

      _frames[to]->del = del;
      _frames[to]->transitionMode = trM;
      _frames[to]->toDraw->fromInt(col);
      
  }
}

void Animation::removeFrame(int frame){
  if(frame < _numFrames){
    for(int i = frame; i < _numFrames - 1; i++){
      _frames[i]->toDraw->fromInt(_frames[i + 1]->toDraw->asInt());
      _frames[i]->del = _frames[i + 1]->del;
      _frames[i]->transitionMode = _frames[i + 1]->transitionMode;
    }
  
    _frames[_numFrames - 1]->toDraw->black();
    _frames[_numFrames - 1]->del = -1;
    _frames[_numFrames - 1]->transitionMode = -1;
  
    _numFrames--;
  }
}

void Animation::addFrame(frame_t nFrame){
  if(_numFrames < MAX_FRAMES){
    _frames[_numFrames]->toDraw->fromInt(nFrame.toDraw->asInt());
    _frames[_numFrames]->del = nFrame.del;
    _frames[_numFrames]->transitionMode = nFrame.transitionMode;
    
    _numFrames++;
  }
}

void Animation::clearFrames(){
  for(int i = 0; i < _numFrames; i++){
    _frames[i]->toDraw->black();
    _frames[i]->del = -1;
    _frames[i]->transitionMode = -1;
  }

  _numFrames = 0;
  _currFrame = 0;
}

Frame* Animation::next(){

  _currFrame++;
  
  if(_currFrame >= _numFrames){
    _currFrame = 0;
  }

  return _frames[_currFrame];
}

Frame* Animation::currFrame(){
  return this->getFrame(_currFrame);
}

Frame* Animation::peekNextFrame(){

  int check = _currFrame + 1;
  
  if(check >= _numFrames){
    check = 0;
  }

  return _frames[check];
}

Frame* Animation::peekPrevFrame(){
  int check = _currFrame - 1;

  if(check < 0){
    check = _numFrames - 1;
  }

  return _frames[check];
}

void Animation::resetAnimation(){
  _currFrame = 0;
}

int Animation::getNumFrames(){
  return _numFrames;
}

int Animation::getCurrFrame(){
  return _currFrame;
}
