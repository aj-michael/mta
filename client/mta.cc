#include "led-matrix.h"
#include "graphics.h"

#include <unistd.h>
#include <math.h>
#include <stdio.h>
#include <signal.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <iostream>
#include <fstream>
#include <unistd.h>

using namespace rgb_matrix;

volatile bool interrupt_received = false;
static void InterruptHandler(int signo) {
  interrupt_received = true;
}

static void FillCircleOnCanvas(Canvas *canvas, Color *color, int x, int y, double r) {
  for (int x2 = 1 + x - r; x2 <= x + r; x2++) {
    int deltay = (int) sqrt(pow(r, 2) - pow(x - x2, 2));
    DrawLine(canvas, x2, y - deltay, x2, y + deltay, *color);
  }
}

static void WriteTemplate(Canvas *canvas, Font *font) {
  Color yellow(255, 255, 0);
  Color green(0, 255, 0);
  Color red(255, 0, 0);
  Color grey(169, 169, 169);
  std::string first_line ("NEXT MHTN");
  std::string train_line ("L");
  std::string min("Min");
  DrawText(canvas, *font, 1, font->baseline() - 1, green, first_line.c_str());
  DrawText(canvas, *font, 43, 2 * font->baseline() - 1, green, min.c_str());
  DrawText(canvas, *font, 43, 3 * font->baseline() - 1, green, min.c_str());
  FillCircleOnCanvas(canvas, &grey, 12, 21, 8.5);
  DrawText(canvas, *font, 10, 25, red, train_line.c_str());
}

static void UpdateTimes(Canvas *canvas, Font *font, Color color, std::string next, std::string nextnext) {

  if (next.length() == 1) {
    DrawText(canvas, *font, 36, 2 * font->baseline() - 1, color, next.c_str());
  } else {
    DrawText(canvas, *font, 29, 2 * font->baseline() - 1, color, next.c_str());
  }

  if (nextnext.length() == 1) {
    DrawText(canvas, *font, 36, 3 * font->baseline() - 1, color, nextnext.c_str());
  } else {
    DrawText(canvas, *font, 29, 3 * font->baseline() - 1, color, nextnext.c_str());
  }
}

int main(int argc, char *argv[]) {
  RGBMatrix::Options defaults;
  defaults.hardware_mapping = "adafruit-hat";
  defaults.rows = 32;
  defaults.chain_length = 2;
  defaults.parallel = 1;
  RGBMatrix *canvas = CreateMatrixFromFlags(&argc, &argv, &defaults);
  if (canvas == NULL)
    return 1;
  canvas->SetBrightness(50);

  signal(SIGTERM, InterruptHandler);
  signal(SIGINT, InterruptHandler);

  Font font;
  if (!font.LoadFont("../third_party/rpi-rgb-led-matrix/fonts/7x13.bdf")) {
    fprintf(stderr, "Couldn't load font '7x13.bdf'\n");
  }

  WriteTemplate(canvas, &font);

  std::string previousnext ("0");
  std::string previousfollowing ("0");
  Color red(255, 0, 0);
  Color off(0, 0, 0);
  while (1) {
    if (interrupt_received) {
      break;
    }
    std::ifstream fifo ("/tmp/mtafifo");
    std::string next;
    std::string following;
    std::getline(fifo, next, ',');
    std::getline(fifo, following, ',');
    std::cout << next << "," << following << std::endl;
    UpdateTimes(canvas, &font, off, previousnext, previousfollowing); 
    UpdateTimes(canvas, &font, red, next, following);
    previousnext = next;
    previousfollowing = following;
  }

  canvas->Clear();
  delete canvas;

  return 0;
}
