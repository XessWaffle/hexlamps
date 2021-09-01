#ifndef SWATCH_H

#define SWATCH_H

class Swatch{
  public:
    Swatch(const char color[4]);
    Swatch();
    Swatch(int red, int green, int blue);
    Swatch(int color);

    int getRed();
    int getPWMRed();
    void setRed(int red);
    
    int getGreen();
    int getPWMGreen();
    void setGreen(int green);
    
    int getBlue();
    int getPWMBlue();
    void setBlue(int blue);
    
    bool colorEquals(Swatch other);

    void fromInt(int color);
    int asInt();

    void black();
    void white();
    
  private:
    int _red = 0, _green = 0, _blue = 0;
  
};

#endif
