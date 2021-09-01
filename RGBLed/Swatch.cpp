#include "Swatch.h"
#include <Arduino.h>

Swatch::Swatch(const char color[4]){
  _red = (int) color[0];
  _green = (int) color[1];
  _blue = (int) color[2];
}

Swatch::Swatch(int red, int green, int blue){
  _red = red;
  _green = green;
  _blue = blue;
}

Swatch::Swatch(int color){
  this->fromInt(color);
}

Swatch::Swatch(){
  
}

int Swatch::getRed(){
  return _red;
}

int Swatch::getPWMRed(){
  return (int)map(_red, 0, 256, 0, 1024);
}

void Swatch::setRed(int red){
  _red = red;
}

int Swatch::getBlue(){
  return _blue;
}

int Swatch::getPWMBlue(){
  return (int)map(_blue, 0, 256, 0, 1024);
}

void Swatch::setBlue(int blue){
  _blue = blue;
}

int Swatch::getGreen(){
  return _green;
}

int Swatch::getPWMGreen(){
  return (int)map(_green, 0, 256, 0, 1024);
}

void Swatch::setGreen(int green){
  _green = green;
}


bool Swatch::colorEquals(Swatch other){
  return other.getRed() == _red && other.getGreen() == _green && other.getBlue() == _blue;
}

void Swatch::fromInt(int color){
  _red = (color & 0x00FF0000) >> 16;
  _green = (color & 0x0000FF00) >> 8;
  _blue = color & 0x000000FF;
}

int Swatch::asInt(){
  return (0xFF << 24) | (_red << 16) | (_green << 8) | _blue;
}

void Swatch::black(){
  _red = 0;
  _green = 0;
  _blue = 0;
}

void Swatch::white(){
  _red = 255;
  _green = 255;
  _blue = 255;
}
