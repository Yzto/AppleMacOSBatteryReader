# AppleMacOSBatteryReader
Java-MacOS-Terminal-Tool showing current and dynamic battery state.

<br>A Single Class Application - All applied Classes are internal classes.

<br>As data source it uses the command line:
<br>ioreg -l
<br>Call "man ioreg" to learn more about the tool ioreg.

<br>Current version is 0.1. It works, but I hope to improve some nasty consistence problems. 
<br>Possibly ioreg might be the problem. 
<br>Actualization rate is at present internally defined as 60 seconds.
<br>Especially the calculated current power (Watt W) is not consistent:  
<br>We calculate it in different ways - so it give rise to to quite diffrent power values.

<br>We will get better ;-)

<br> yours, the real Zyto
