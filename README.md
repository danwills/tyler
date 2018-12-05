# Tyler

It's name is a play on 'tiler', the idea being that you load a bunch of tilable images into it. And vaguely the chaos of Tyler Durden (from Fight Club).

This is a project that I started a rather long time ago, probably in 1999 or something.. it began from trying to implement a simple neural-net for a Cognitive Science assignment at Adelaide University. Didn't get very far past a very-prototype phase with the neural-net but learnt a lot about Java, and for me that unlocked some 2d-image-pixel flexibilities that eventually made Tyler happen.

I've always been a massive demoscene (and realtime-graphics generally) fan and some of the ideas in Tyler are intended to be realtime effects such that you might use with fairly tight synchronisation in a VJing or demo-engine scenario. I did a couple of VJing gigs using Tyler a fair while ago, but obviously that didn't turn into a profession.

So, what can we do with Tyler?

* Realtime pixel-feedback engine with various interesting ways to generate a warp or neighborhood for the feedback, including various fractals, noise, sines and an 11x11 boolean-matrix-based neighborhood generator.
* Complex (too complex? mostly useless) color/LUT-based transformation/color blending methods for advancing the pixel-automata iterations with.
* Idea of extracting a LUT from the image using a 'Tool' (along a line using a default extractor channel) and being able to then also edit and smooth the LUT values and use them in other parts of tyler (such as the pixel-neighbourhood-transfer-function and the particle color blend methods).
* 2D Particles emitted from the mouse pointer, with quite intricate ways to control their size/motion/draw-method and color evolution/alpha blending.
* Full capture of 'vector' particles and UI components into the pixel-automata realm (can be disabled for speed too).
* Tab or Right-click menu to access pretty much all actions, help and heaps of hotkeys, eg. hold Ctrl+Alt+Shift to move the menu around.

It's basically a particles and pixels playground, and I still think it's great fun!

Have had some flickering issues with builds on newer JDKs, I should provide a working (old) build to play with.

Anyone interested in adapting this code to run on Android and make it into a fast (glsl) fun pixel playground, or just to port it as-is in all its software-rendering-glory please contact me: gdanzo at gmail.com
