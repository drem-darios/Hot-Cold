Hot-Cold
========

ABOUT HOT COLD:

Hot Cold is a scavenger hunt game using iBeacon BLE technology. The game is played by selecting a difficulty. After selecting a difficulty, you can start looking for items. We have included three items, which are the three BlueCat beacons we had for testing. 

GAME RULES:

By selecting one of the beacon names on the list, the app will start looking for that beacon. The beacon meter will slowly move from the maximum cold state to the maximum hot state. Once the device is in the maximum hot state, the user can re-select the item on the list to acknowledge they are close to the beacon. When the user has acknowledged they have seen all beacons, a message is displayed to let the user know they have won and the game is over. The user can play as many times as they’d like.

DISTANCES:

SOLID ICE - 13 meters away
Freezing - 8 meters away
Cold - 5 meters away
Hot - 3 meters away
Burning Up - 2 meters away
ON FIRE - 1 meter away. This is the goal distance. 

iBEACON LIMITATIONS:

The beacons could be unpredictable at times. Android is limited to one callback per beacon per scan, so if you scan an area for a longer period of time, the user could be out of the range by the time the scan starts again.  Similarly, if your scan is too short, you could miss the beacon’s advertisement entirely. This inaccuracy made it a challenge to smoothly transition from one state to the next.

SCAN METER:

SCAN METER is drawn on the canvas using simple android canvas draw functions, Scan meter is structured as though the colors indicate the temperature (Range to be specific to this project). Each time a new trigger to moveHand is issued, the drawHand moves on the meter depciting the change in temperature/range along with the background color transition. The closer the device on which the app is installed to the selected Beacon the temperature/color changes between different states based on how close the android device is to the beacon. The layout is divided between the draw canvas which holds the meter and a basic ListView which displays different beacons available for scanning.

//TODO

There is currently only one difficulty, which is Easy. We were thinking the other difficulties may incorporate some GPS as well. If the user is not in the area of the beacons, the GPS would point them in the general direction of the beacons unit they are in a close enough zone to start the game, or not at all if you want to make it really tough. Also, we would like to add a feature where the user adds a picture of the item they found to confirm rather than reselecting the item from the list. This would make sure the user actually found the item, and is not just saying they did. 