# Focus Mute
### v1.0.0

---
A plugin to mute the game client when it becomes unfocused.

Includes options to affect music, sound effects, and area sounds individually.

---
## FAQ
**How does area-sound muting work?**

While the game canvas is unfocused, the plugin suppresses positional sound events, including your own and other players' mining sounds. Sound effects and area sounds can be enabled independently. Volumes are saved when muting begins and restored when focus returns, the corresponding option is turned off, or the plugin is disabled. Changing options while focused does not mute the game.

**Music doesn't resume seamlessly if I click back to the game.**

Setting music volume to 0 causes the current song to stop, and unfortunately the game doesn't start playing it again when unmuted.
I would suggest either muting music entirely or unselecting the "Mute music" option.

**This plugin made me die on my PvP only HCIM because I tabbed out and didn't hear that I was getting attacked!**

Why did you think it was a good idea to install this plugin? If this happens to you, please email me at
*plugins+hcim* *(at)* *cdfisher.com*.

For any other support reasons, please make an issue here on GitHub or send an email to *plugins* *(at)* *cdfisher.com* and include
"Focus Mute" in the subject line.