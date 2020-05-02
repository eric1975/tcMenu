/*
    The code in this file uses open source libraries provided by thecoderscorner

    DO NOT EDIT THIS FILE, IT WILL BE GENERATED EVERY TIME YOU USE THE UI DESIGNER
    INSTEAD EITHER PUT CODE IN YOUR SKETCH OR CREATE ANOTHER SOURCE FILE.

    All the variables you may need access to are marked extern in this file for easy
    use elsewhere.
 */

#ifndef MENU_GENERATED_CODE_H
#define MENU_GENERATED_CODE_H

#include <tcMenu.h>

#include "Scramble.h"
#include <JoystickSwitchInput.h>
#include <RuntimeMenuItem.h>

// all define statements needed
#define INT_PROP 10
#define INTERRUPT_SWITCHES false
#define SWITCH_IODEVICE io23017
#define JOYSTICK_PIN 2
#define TEST_CHOICE Choice1

// all variables that need exporting
extern ArduinoAnalogDevice analogDevice;
extern char[] expOnly;

// all menu item forward references.
extern IpAddressMenuItem menuSubIpItem;
extern TextMenuItem menuSubTextItem;
extern AnalogMenuItem menuSubTest2;
extern BackMenuItem menuBackSub;
extern SubMenuItem menuSub;
extern ListRuntimeMenuItem menuAbc;
extern AnalogMenuItem menuTest;
extern EnumMenuItem menuExtra;
extern const ConnectorLocalInfo applicationInfo;

// Callback functions must always include CALLBACK_FUNCTION after the return type
#define CALLBACK_FUNCTION

void CALLBACK_FUNCTION callback1(int id);
void CALLBACK_FUNCTION callback2(int id);
int fnAbcRtCall(RuntimeMenuItem* item, uint8_t row, RenderFnMode mode, char* buffer, int bufferSize);

void setupMenu();

#endif // MENU_GENERATED_CODE_H
