# ST Asset Tacking

This repository contains the ST Asset Tracking app source code.

With the ST Asset Tracking app, you can configure and set-up the threshold of Asset Tracking Sigfox node running the [FP-ATR-SIGFOX1](https://www.st.com/content/st_com/en/products/embedded-software/mcu-mpu-embedded-software/stm32-embedded-software/stm32-ode-function-pack-sw/fp-atr-sigfox1.html) firmware package. This package is based on NUCLEO-L053R8 or NUCLEO-L476RG boards, and it supports X-NUCLEO-S2868A1 (built around S2-LP high performance ultra-low power Sub-1GHz RF transceiver) and X-NUCLEO-IDB05A1 (built around BlueNRG-MS very low power BLE4.2 network processor module) for Connectivity, and X-NUCLEO-IKS01A2 for Sensing.

FP-ATR-SIGFOX1 is a firmware package running on STM32 which lets you read data from environmental and motion sensors and send collected data via Sigfox connectivity, moreover Low-energy device geolocation is provided by the Sigfox infrastructure
The app lets you change sampling intervals, choose which sensor data is logged, and the conditions that trigger data logging.
Once configured, using BLE connectivity, the system is starting to track the position, environmental and motion sensors and send it by LPWAN Sigfox service.

## Download the source

Since the project uses git submodules, <code>--recursive</code> option must be used to clone the repository:

```Shell
git clone --recurse-submodules https://github.com/STMicroelectronics/STAssetTracking_Android
```

or run
```Shell
git clone https://github.com/STMicroelectronics/STAssetTracking_Android
git submodule update --init --recursive
```

## License

Copyright (c) 2019  STMicroelectronics – All rights reserved
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
