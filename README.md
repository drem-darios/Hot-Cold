Hot-Cold
========

ABOUT HOT COLD
Hot Cold is a scavenger hunt game using iBeacon BLE technology. The game is played by selecting a difficulty. After selecting a difficulty, you can start looking for items. We have included three items, which are the three BlueCat beacons we had for testing. By selecting one of the beacon names on the list, the app will start looking for that beacon. The beacon meter will slowly move from the maximum cold state to the maximum hot state. Once the device is in the maximum hot state, the user can re-select the item on the list to acknowledge they are close to the beacon. When the user has acknowledged they have seen all beacons, a message is displayed to let the user know they have won and the game is over. The user can play as many times as they’d like.
iBEACON LIMITATIONS
The beacons could be unpredictable at times. Android is limited to one callback per beacon per scan, so if you scan an area for a longer period of time, the user could be out of the range by the time the scan starts again.  Similarly, if your scan is too short, you could miss the beacon’s advertisement entirely. This inaccuracy made it a challenge to smoothly transition from one state to the next.
//TODO
One thing we wanted to do, but didn’t get a chance to complete is to implement managing of the beacons in the game. This would give the user the ability to create their own scavenger hunt game, provided they had their own set of beacons to play with. The app would have to download a set of games on startup and store it locally. Also there is currently only one difficulty, which is Easy. We were thinking the other difficulties may incorporate some GPS as well. If the user is not in the area of the beacons, the GPS would point them in the general direction of the beacons unit they are in a close enough zone to start the game, or not at all if you want to make it really tough.

