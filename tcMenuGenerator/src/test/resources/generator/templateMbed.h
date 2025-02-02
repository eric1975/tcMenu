/*
    The code in this file uses open source libraries provided by thecoderscorner

    DO NOT EDIT THIS FILE, IT WILL BE GENERATED EVERY TIME YOU USE THE UI DESIGNER
    INSTEAD EITHER PUT CODE IN YOUR SKETCH OR CREATE ANOTHER SOURCE FILE.

    All the variables you may need access to are marked extern in this file for easy
    use elsewhere.
 */

#ifndef MENU_GENERATED_CODE_H
#define MENU_GENERATED_CODE_H

#include <Arduino.h>
#include <tcMenu.h>
#include "Scramble.h"
#include <JoystickSwitchInput.h>
#include <superTheme123.h>
#include "MySpecialTransport.h"
#include <RemoteMenuItem.h>
#include <ScrollChoiceMenuItem.h>
#include <RuntimeMenuItem.h>
#include <EditableLargeNumberMenuItem.h>
#include <IoAbstraction.h>
#include <mbed/HalStm32EepromAbstraction.h>
#include <RemoteAuthentication.h>

// variables we declare that you may need to access
extern const PROGMEM ConnectorLocalInfo applicationInfo;
extern TcMenuRemoteServer remoteServer;
extern ArduinoAnalogDevice analogDevice;
extern char[] expOnly;
extern const GFXfont sans24p7b;

// Any externals needed by IO expanders, EEPROMs etc


// Global Menu Item exports
extern RemoteMenuItem menuIoTMonitor;
extern EepromAuthenticationInfoMenuItem menuAuthenticator;
extern ScrollChoiceMenuItem menuMySubSub1CustomChoice;
extern ScrollChoiceMenuItem menuMySubSub1RamChoice;
extern ScrollChoiceMenuItem menuMySubSub1EepromChoice;
extern Rgb32MenuItem menuMySubSub1RGB;
extern DateFormattedMenuItem menuMySubSub1DateField;
extern TimeFormattedMenuItem menuMySubSub1TimeField;
extern IpAddressMenuItem menuMySubSub1IPAddress;
extern TextMenuItem menuMySubSub1TextItem;
extern EditableLargeNumberMenuItem menuMySubSub1IntLarge;
extern EditableLargeNumberMenuItem menuMySubSub1DecLarge;
extern BackMenuItem menuBackMySubSub1;
extern SubMenuItem menuMySubSub1;
extern ListRuntimeMenuItem menuMySubMyList;
extern ActionMenuItem menuMySubMyAction;
extern FloatMenuItem menuMySubMyFloat;
extern BooleanMenuItem menuMySubMyBoolean;
extern EnumMenuItem menuMySubMyEnum;
extern AnalogMenuItem menuMySubMyAnalog;

// Provide a wrapper to get hold of the root menu item and export setupMenu
inline MenuItem& rootMenuItem() { return menuMySubMyAnalog; }
void setupMenu();

// Callback functions must always include CALLBACK_FUNCTION after the return type
#define CALLBACK_FUNCTION

int fnMySubMyListRtCall(RuntimeMenuItem* item, uint8_t row, RenderFnMode mode, char* buffer, int bufferSize);
int fnMySubSub1CustomChoiceRtCall(RuntimeMenuItem* item, uint8_t row, RenderFnMode mode, char* buffer, int bufferSize);
void CALLBACK_FUNCTION onActionItem(int id);
void CALLBACK_FUNCTION onAnalogItem(int id);
void CALLBACK_FUNCTION onBoolChange(int id);
void CALLBACK_FUNCTION onDecLarge(int id);
void CALLBACK_FUNCTION onEnumChange(int id);
void CALLBACK_FUNCTION onIpChange(int id);
void CALLBACK_FUNCTION onRamChoice(int id);
void CALLBACK_FUNCTION onRgb(int id);
void CALLBACK_FUNCTION onRomChoice(int id);

#endif // MENU_GENERATED_CODE_H
