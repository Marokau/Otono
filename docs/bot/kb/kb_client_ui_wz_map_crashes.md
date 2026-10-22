# Client map crashes with the customized UI.wz

## Confirmed local workaround

On 2026-09-18, OuchMyToe could enter The Path of Glittering Crystal
(108010400), but the client crashed when taking its portal into the thief's
The Other Dimension boss room (108010401). The crash occurred with both
Kaentake and HeavenMS-localhost-WINDOW.exe using the same client data.
Server logs recorded connection resets without a corresponding script exception.

The owner backed up the customized Kaentake `UI.wz`, then replaced
`MapleStory/UI.wz` with a clean v83 `UI.wz` from a fresh install in their backup
folder. After that replacement, the owner successfully entered the room and
finished the Dark Lord clone fight.

The owner reports the current client folder is stable so far with **Kaentake
modded 1.2.1 and clean v83 UI.wz**. This records the working local combination;
it does not establish that every map has been tested.

To apply the workaround elsewhere:

1. Close the client and back up its current `UI.wz` outside the active filename.
2. Replace that client's `UI.wz` with a clean file from a compatible v83 install.
3. Relaunch and test the affected portal.

The customized F8 bot inventory interface stops working with the clean file.
The owner accepts this tradeoff because they do not use that interface. Keep
the customized file backup if that interface is needed later. This is a client
asset workaround; it does not require server code changes or a server restart.

## Verification limits

Only the thief's boss room is confirmed fixed on this local installation.
The owner's previous SoloMapling crashes have not been retested with clean
UI.wz, so it is unknown whether this workaround resolves those crashes.

The exact defective node or compatibility issue inside the customized UI.wz
has not been identified. Do not attribute this symptom to the Door of Dimension
NPC, server quest flags, or a Kaentake DLL defect based on these observations.
The binary UI.wz files and their backups are not included in this documentation
commit.
