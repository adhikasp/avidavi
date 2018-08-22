# avidavi

avidavi is an application to send Android sensors data to a PLC device
using Modbus-TCP protocol. Currently support gyroscope, acceleration,
and proximity sensors.

![architecture diagram](https://i.imgur.com/MAhmZAW.jpg)

## Background

The Fourth Industrial Revolution is the era when breakthrough in technology
blurring the lines between physical, biological, and digital world. This
innovation cause a fundamental shift in the way human live, think, and socialize.
If we guide ourself with the new paradigm of Fourth Industrial Revolution, the
control systems in IoT system could be integrated more humanly with the use of
more immersive or intuitive input method. One of the possible solution is to use
smartphone. This research try to explore the usage of smartphone as an input
device in IoT system. The application will send real-time data update of its
accelerometer, gyroscope, and proximity sensor to a server (a PLC in our case)
using Modbus TCP protocol. The result is Modbus TCP protocol could be used to
send the sensor data in real-time with the periodic sensor data update of
50 milliseconds. The network latency between sending data from smartphone
to PLC is 0.094 millisenconds. This suggest that Modbus TCP, a robust and
proven protocol in industrial world, could be adopted in modern era as a
protocol of communication in IoT system and that smartphone could be used
as a control device.

![devices](https://i.imgur.com/LpM0KXk.jpg)

## Architecture

![android diagram](https://i.imgur.com/LpM0KXk.jpg)

## Screenshot

![homepage screenshot](https://i.imgur.com/x05n3IA.jpg)
![memory read screenshot](https://i.imgur.com/Vmequur.jpg)
![setting screenshot](https://i.imgur.com/MAhmZAW.jpg)
