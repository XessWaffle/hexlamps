#include "SwatchList.h"

SwatchList::SwatchList(){
  
}


void SwatchList::addSwatch(Swatch s){
  if(_num < MAX_SWATCHES){
    _swatches[_num++] = s;
  }
}


void SwatchList::removeSwatch(Swatch s){

  int shiftIndex = -1;
  
  for(int i = 0; i < _num; i++){
    if(s.colorEquals(_swatches[i])){
      shiftIndex = i;
      break;
    }
  }

  if(shiftIndex >= 0){
    for(int i = shiftIndex; i < _num - 1; i++){
      _swatches[i] = _swatches[i + 1];
    }

    _num--;
    
  }

    
}

void SwatchList::swapSwatches(int from, int to){
  if(from >= 0 && to >= 0 && from < _num && to < _num){
    Swatch ref = _swatches[to];
    _swatches[to] = _swatches[from];
    _swatches[from] = ref;
  }
}


Swatch SwatchList::getSwatch(int index){

  if(index >= 0 && index < _num){
    return this->_swatches[index];
  }

  return Swatch();
}

int SwatchList::numSwatches(){
  return _num;
}
