# FP-ATR-BLE1 protocol

The communication is based on the BlueSTSDK Debug console.

It is a command/response protocol the command is a string ended by `\n`  
and the response has the format:

`[Done|Warning|Error] - data\n`

the first part contains the command status:
- Done: the command completes successfully
- Warning: the command completes but with a warning
- Error: the command wasn't able to complete

after the - there are the optionals response data.

## Commands

### setTime
set the board time. the format must be: HH:mm:ss

this command answer: "Time format Correct" if the time is correctly set

### setDate
set the current date. the format must be: weekDay/dd/MM/yy

this command answer: "Date format Correct" if the time is correctly set

### statusLog
return the current log status, the status can be:
- **Run**: the board is recoding data to the SD
- **Stop**: the log is completed and the file into the SD are completed
- **Pause**: the board is not recording any data into the SD but the log
could start again without creating a new log file

### startLog

start a new acquisition

the answer can be:
- **Done - DataLog Started** if the log correctly starts
- **Warning - DataLog is already running** if the log doesn't start because an acquisition is ongoing
- **Error - XXX** if the error XXX happen

### stopLog

stop an acquisition and dump the data to the SD

the answer can be:
- **Done - DataLog Stopped** if the log correctly stopped
- **Warning - DataLog is not running** if the there are no logs to stop
- **Error - XXX** if the error XXX happen

### readLog

return the content o the last acquisition

the answer is:
```
Done - DataLog Open

*log content*
```

log content is a string of *n* bytes with the log contents.
to have the string length you can use the command sizeLog.


## Log format
the log are stored as a CSV file.
the first line contains the file version, the format is:

`Version,#`

currently the only accepted version is 1.

the second line contains only the string `Data` and tell that the other lines contains the acquired data

the third line contains the column header, since the user can decide witch data to log the header is dynamically generated.
the possible values are:
- `Time [HH:MM:SS.mmm]`: **Mandatory**. This column will contains the
  time where the event happen.
- `Date [DD/MM/YY]`: **Mandatory**. This column will contains the
  date where the event happen
- `Temperature ['C]`: This column will contains the temperature in
  Celsius degree (float number)
- `Pressure [mb]`:This column will contains the pressure in milli bar
  (float number)
- `Humidity [%]`:This column will contains the humidity as percentage
  (float number)
- `HwEvent [Type]`: This column will contains the event detected by the
  accelerometer.

All the other lines will contains the detected value.

Note: if a value is not available the column could be empty.

### HW events mapping
the possible value for the hardware event are:
- TL = Orientation event, Top Left orientation
- TR = Orientation event, Top Right orientation
- BL = Orientation event, Bottom Left orientation
- BR = Orientation event, Bottom Right orientation
- U = Orientation event, Up orientation
- D = Orientation event, Down orientation
- T = Tilt event, Unknown orientation
- WU = Wake up event, Unknown orientation
