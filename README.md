# ImageReaderApp
Android application for speeding up TMR farming with FFBExecute.

# What does it do?

This app sits in the background listening on port 8080 for the following commands:

## Is it needed?

Technically, the helper is not needed, but without the helper, it will take typically 2 seconds to grab the screen.  With the helper, it can grab the screen in under 0.25 seconds.

## /setup
Push configuration details over to the app
* State & Screen details
* Map reading details

## /check/{stateId}
Determine if the saved framebuffer matches any known image states

## /pixel/{offset}
Read a single pixel from the raw image

## /map
This will read the screen and create a map definition of what it sees.

```
BBBBB############BBB#######
BBBBB############BB########
BBBBBB###########BB########
BBBBBB###########BB########
BBBBB############BBB#######
#BBB#############BBB#######
BBBB#############BBB#######
BBB##############BBB#######
#################BBB#######
####BBB###########BBBBB####
####BBB##############BB####
##BBBBBBBB##BBB############
BBBBBBBBBBBB???GB##########
BBBBBBBBBBBB???GG##########
BBBBBBB#####???############
BBBBB######################
#BBB#BB####################
BBBBBBB####################
BBBBBBB####################
BBBBBB#####################
BBBBB######################
#BBB#######################
#BBB#######################
#BBB#######################
#GGG#######################
#GGG#######################
#GGG#######################
```
