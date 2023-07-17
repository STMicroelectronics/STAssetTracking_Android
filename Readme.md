# ST Asset Tacking

This repository contains the ST Asset Tracking app source code.

With the ST Asset Tracking app, you can configure and set-up the threshold of Asset Tracking Bluetooth Low Energy device and NFC Sensor.
The app communicates with the [FP-ATR-BLE1](https://www.st.com/en/embedded-software/fp-atr-ble1.html), [FP-SNS-SMARTAG1](https://www.st.com/en/embedded-software/fp-sns-smartag1.html), [FP-SNS-SMARTAG2](https://www.st.com/en/embedded-software/fp-sns-smartag2.html), [FP-ATR-ASTRA1](https://www.st.com/en/embedded-software/fp-atr-astra1.html) functional packs firmware running on an STM32 microcontroller, which manages the specific environmental and motion sensor data logging activity. The app also supports [Astra](https://www.st.com/en/evaluation-tools/steval-astra1b.html) board that has NFC and short / long range connectivity (BLE, LoRa, and 2.4 GHz and sub 1-GHz proprietary protocols).

The app lets you change sampling intervals, choose which sensor data is logged, and the conditions that trigger data logging.
Once configured, the app is starting to track the position, environmental and motion sensors and allows the user to send recorded data to the ST AWS dashboard.
The data collected on dashboard can be consulted through charts and filtered through time intervals up to 7 days before.

## Download the source

To clone the repository:
```Shell
git clone https://github.com/STMicroelectronics/STAssetTracking_Android
```

## License

Copyright (c) 2023  STMicroelectronics – All rights reserved
The STMicroelectronics corporate logo is a trademark of STMicroelectronics

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

- Redistributions of source code must retain the above copyright notice, this list of conditions
and the following disclaimer.

- Redistributions in binary form must reproduce the above copyright notice, this list of
conditions and the following disclaimer in the documentation and/or other materials provided
with the distribution.

- Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
STMicroelectronics company nor the names of its contributors may be used to endorse or
promote products derived from this software without specific prior written permission.

- All of the icons, pictures, logos and other images that are provided with the source code
in a directory whose title begins with st_images may only be used for internal purposes and
shall not be redistributed to any third party or modified in any way.

- Any redistributions in binary form shall not include the capability to display any of the
icons, pictures, logos and other images that are provided with the source code in a directory
whose title begins with st_images.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
OF SUCH DAMAGE.
