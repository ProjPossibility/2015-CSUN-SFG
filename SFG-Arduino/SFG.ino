//Team: Marina Slijepcevic, Noah Anderson, Matthew Hemersky, Matthew Newbill 
//Author: Noah J Anderson
//February 15th 2015
//SS12 2015 Code for a Cause Hackathon
//Purpose: Arduino ino for Usage of Sonar and Pixy
//Sensors to pass information via Bluetooth or some
//wireless connection to an Android Application
//which should allow Blind to walk/run on a track
//with color coded lines.

#include <SoftwareSerial.h>
#include <bluetooth.h>
#include <SPI.h>  
#include <Pixy.h>

//
// begin license header
//
// This file is part of Pixy CMUcam5 or "Pixy" for short
//
// All Pixy source code is provided under the terms of the
// GNU General Public License v2 (http://www.gnu.org/licenses/gpl-2.0.html).
// Those wishing to use Pixy source code, software and/or
// technologies under different licensing terms should contact us at
// cmucam@cs.cmu.edu. Such licensing terms are available for
// all portions of the Pixy codebase presented here.
//
// end license header

Bluetooth *blue = new Bluetooth("ExampleRobot");
Pixy pixy;
const int pwPin1 = 3;
const int anPin = 0;

void setup()
{

  Serial.begin(9600);
  Serial.print("Starting...\n");
  blue->setupBluetooth();//bluetooth setup	 
  pixy.init();           //pixy setup
  pinMode(pwPin1, INPUT);//sonar setup
}

void loop()
{
  sendBluetooth(getJSONResult(getPixyInfo(), getSonarInfoAnalog()));
}

String getPixyInfo()
{
  static int i = 0;
  int j;
  uint16_t blocks;
  char buf[32]; 
  blocks = pixy.getBlocks();
  if (blocks)
  {
    i++;
    if (i % 50 == 0)
    {
      if(pixy.blocks[0].x < 60 )
      {
        return "Direction:Right";
      }
      else if(pixy.blocks[0].x > 300)
      {
        return "Direction:Left";
      }
    }
  }
  return "Direction:Unchanged";
}

String getSonarInfoAnalog()
{
  long anVolt, mm, inches, feet;
  anVolt = analogRead(anPin);
  mm = anVolt * 5;
  inches = mm / 25.4;
  feet = inches / 12;
  return getDistanceThreshold(feet);
}

String getSonarInfoPW()
{
  const int pwPin1 = 3;
  long sensor, mm, inches, feet;
  sensor = pulseIn(pwPin1, HIGH);
  mm = sensor;      //Takes the pulse width and tells Arduino it is equal to millimeters
  inches = mm / 25.4;   //Takes mm and converts it to inches
  feet  = inches / 12;
  return getDistanceThreshold(feet);
}

String getDistanceThreshold(long feet)
{
  String result = "Distance:";
  if (feet > 15)
    result += "-1";
  else if(feet < 15 && feet > 10)
    result += "15";
  else if(feet < 10 && feet > 5)
    result += "10";
  else if(feet < 5)
    result += "5";
}

void sendBluetoothMessage(String msg)
{
  if(Serial.available())
  {
    blue->Send(msg);
  }
}
