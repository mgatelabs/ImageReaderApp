# ImageReaderApp
Android application for speeding up TMR farming with FFBExecute.

# What does it do?

This app sits in the background listening on port 8080 for the following commands:

## /setup
Push configuration details over to the app, since it is empty and requires data

## /check/{stateId}
Determine if the saved framebuffer matches any known image states

## /pixel/{offset}
Read a single pixel from the raw image
