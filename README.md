# ImageReaderApp
Android application for speeding up processing with FFBExecute.

# Is it needed?

Yes, FFBExecute has been re-written to explicitly need this in order to execute correctly over a Wi-Fi connection.

# What does it do?

This app sits in the background listening on port 8080 for the following commands:

## /setup
Push configuration details over to the app
* State & Screen details
* Map reading details

## /check/{stateId}
Determine if the saved framebuffer matches any known views for the given state

## /pixel/{offset}
Read a single pixel from the raw image

## /download
Return the raw image for further processing

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
