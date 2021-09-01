#include "Swatch.h"

#define MAX_SWATCHES 50

class SwatchList{
  public:
    SwatchList();

    void addSwatch(Swatch s);
    void removeSwatch(Swatch s);
    void swapSwatches(int from, int to);
    Swatch getSwatch(int index);
    
    int numSwatches();

  private:
    Swatch _swatches[MAX_SWATCHES];
    int _num = 0;
};
