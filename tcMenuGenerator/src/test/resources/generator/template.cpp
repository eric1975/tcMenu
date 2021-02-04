/*
    The code in this file uses open source libraries provided by thecoderscorner

    DO NOT EDIT THIS FILE, IT WILL BE GENERATED EVERY TIME YOU USE THE UI DESIGNER
    INSTEAD EITHER PUT CODE IN YOUR SKETCH OR CREATE ANOTHER SOURCE FILE.

    All the variables you may need access to are marked extern in this file for easy
    use elsewhere.
 */

#include <tcMenu.h>
#include "project_menu.h"

// Global variable declarations

ArduinoAnalogDevice analogDevice(42);
const int PROGMEM anotherVar;
const int PROGMEM allowedPluginVar;

// Global Menu Item declarations

RENDERING_CALLBACK_NAME_INVOKE(fnIpItemRtCall, ipAddressRenderFn, "Ip Item", -1, NO_CALLBACK)
IpAddressMenuItem menuIpItem(fnIpItemRtCall, 79, NULL);
RENDERING_CALLBACK_NAME_INVOKE(fnTextItemRtCall, textItemRenderFn, "Text Item", -1, callback2)
TextMenuItem menuTextItem(fnTextItemRtCall, 99, 10, &menuIpItem);
const PROGMEM AnalogMenuInfo minfoOverrideAnalog2Name = { "test2", 2, 4, 100, callback1, 0, 1, "dB" };
AnalogMenuItem menuOverrideAnalog2Name(&minfoOverrideAnalog2Name, 0, &menuTextItem);
RENDERING_CALLBACK_NAME_INVOKE(fnSubRtCall, backSubItemRenderFn, "sub", -1, NO_CALLBACK)
const PROGMEM SubMenuInfo minfoOverrideSubName = { "sub", 100, 0xffff, 0, NO_CALLBACK };
BackMenuItem menuBackOverrideSubName(fnOverrideSubNameRtCall, &menuOverrideAnalog2Name);
SubMenuItem menuOverrideSubName(&minfoOverrideSubName, &menuBackOverrideSubName, NULL);
ListRuntimeMenuItem menuAbc(1043, 2, fnAbcRtCall, &menuOverrideSubName);
const PROGMEM AnalogMenuInfo minfoTest = { "test", 1, 2, 100, NO_CALLBACK, 0, 1, "dB" };
AnalogMenuItem menuTest(&minfoTest, 0, &menuAbc);
const char enumStrExtra_0[] PROGMEM = "test";
const char* const enumStrExtra[] PROGMEM  = { enumStrExtra_0 };
const PROGMEM EnumMenuInfo minfoExtra = { "Extra", 20, 5, 0, NO_CALLBACK, enumStrExtra };
EnumMenuItem menuExtra(&minfoExtra, 0, &menuTest);
const PROGMEM ConnectorLocalInfo applicationInfo = { "tester", "uuid1" };

// Set up code

void setupMenu() {
    switches.initialise(io23017, true);
    switches.addSwitch(BUTTON_PIN, &null);
    switches.onRelease(BUTTON_PIN, [](uint8_t /*key*/, bool held) {
            anotherFn(20);
        });

    // Read only and local only function calls
    menuOverrideAnalog2Name.setReadOnly(true);
    menuTest.setReadOnly(true);
    menuOverrideSubName.setLocalOnly(true);
    menuOverrideAnalog2Name.setLocalOnly(true);
}

