# JWTerm - Terminal Emulator Framework

## Description

**JWTerm** is a Java-based terminal emulator framework designed for text-based/ASCII graphical applications, with a focus on roguelike game development. Built with **Java 21**, the project provides a professional game loop system, hardware-accelerated rendering, and a grid-based character display system.

JWTerm is built on standard Java libraries (**Swing** and **AWT**) without external dependencies, making it lightweight and easy to integrate into any Java project requiring terminal-like interfaces.

> **Note:** While JWTerm is functional, it's still under active development with more features planned, and current functionality is limited to basic character rendering.
> 
> The project is in its early stages, and contributions are welcome!

One of the inspirations for JWTerm comes from the **AsciiPanel** project, which provides a simple, efficient way to render text-based panels for roguelike games. You can find more about AsciiPanel [here](https://github.com/trystan/AsciiPanel).

## Implemented Features

- **Terminal Screen Emulation**: Grid-based character display with configurable foreground and background colors
- **Professional Game Loop System**: Fixed-timing game loop targeting 60 FPS with proper delta time calculation
- **Hardware-accelerated Rendering**: Double-buffered rendering using Java2D with OpenGL support
- **Customizable Font System**: Support for loading and caching custom monospace fonts
- **Input Handling**: Keyboard input processing with modifier key support (Ctrl, Shift combinations)
- **Debug Overlay**: Togglable performance metrics display (FPS, frame time, update/render timing)
- **Window Management**: Proper window creation with resize handling and DPI scaling awareness
- **Logging System**: Comprehensive Java logging with both console and file output
- **Grid Navigation**: API for positioning and manipulating characters on the screen
- **Performance Optimization**: Sleep timing to maintain target framerate while minimizing CPU usage
- **Fixedsys Font**: Uses the **Fixedsys** font for retro-style text rendering, enhancing the terminal-like experience.
- **Open Source**: JWTerm is free and open-source under the **MIT License**, allowing modification and redistribution.

## Utility Classes

- **Size**: Width and height management
- **Dimension**: Column and row management for grid systems
- **Padding**: Horizontal and vertical padding management
- **Timer**: High-precision timing utilities for performance measurement
- **LoggingUtility**: Configurable logging with custom formatting

## Planned Features

- Mouse input support
- Camera system for larger virtual spaces
- Enhanced API for text and screen manipulation
- Animation system
- Simplified integration API for any project
- More predefined glyphs and color schemes

## Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/gotoss08/JWTerm.git
   ```

2. **Open the project in IntelliJ IDEA**:
   - Launch **IntelliJ IDEA**
   - Select **Open** and navigate to the cloned repository

3. **Set up the JDK**:
   - Ensure **Java 21 SDK** is selected for the project
   - Go to **File** > **Project Structure** > **Project** and select **Java 21**

4. **Build and run**:
   - Build the project using **Build** > **Build Project**
   - Run the application using the green **Run** button or `Shift + F10`

## Usage Example

Refer to the [TerminalAppDemo](./src/com/jwterm/demo/TerminalAppDemo.java) class in the `com.jwterm.demo` package for a simple example of how to create a terminal application using JWTerm.

### Example Code

JWTerm provides an abstract base class that you can extend to create your own terminal-based applications:

```java
package com.jwterm.demo;

import com.jwterm.JWTerm;
import com.jwterm.TermScreen;
import com.jwterm.utils.LoggingUtility;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

public class TerminalAppDemo extends JWTerm {

    private static final Logger LOGGER = LoggingUtility.getLogger(TerminalAppDemo.class.getName());

    public TerminalAppDemo() {
        super("JWTerm Demo", 1280, 720);
    }

    public void start() {
        // Initialize your application here
        LOGGER.info("Starting terminal application");
        runGameLoop();
    }

    @Override
    protected void update(double deltaTime) {
        // Update your application logic here
    }

    @Override
    protected void render(Graphics2D g, double deltaTime) {
        // Add custom rendering here if needed
        // But for now there is no API for custom rendering
        // It will be added in the future
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used in this implementation
        // But you can handle character input here if needed
    }

    @Override
    public void keyPressed(KeyEvent e) {

        // Handle general key events here

        int modifiers = e.getModifiersEx();
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_ESCAPE) {
            LOGGER.info("ESC pressed, exiting application");
            this.running = false;
        }

        // Example: check for modifier keys and specific key combinations

        if (isCtrlDown(modifiers) && isShiftDown(modifiers) && keyCode == KeyEvent.VK_A) {
            LOGGER.info("Ctrl+Shift+A: Clearing screen");
            termScreen.fill(null);
        }

        if (isCtrlDown(modifiers) && isShiftDown(modifiers) && keyCode == KeyEvent.VK_B) {
            LOGGER.info("Ctrl+Shift+B: Filling screen with walls");
            termScreen.fill(TermScreen.Glyph.WALL);
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Not used in this implementation
        // But you can handle key release events here if needed
    }

    public static void main(String[] args) {
        TerminalAppDemo app = new TerminalAppDemo();
        app.start();
    }

}
```

## Default Controls

- **F1**: Toggle debug overlay
- **ESC**: Exit application (in demo)
- **Ctrl+Shift+A**: Clear screen (in demo)
- **Ctrl+Shift+B**: Fill screen with wall characters (in demo)

## License

JWTerm is released under the MIT License. See the [LICENSE](LICENSE) file for more details.

## Contributing

Contributions are welcome! Feel free to fork the repository, submit issues, and open pull requests.

> Important: Since this project is still in early development, contributions that help improve stability, features, or documentation are particularly appreciated.

## Acknowledgments

- Fixedsys font by [Kika](https://github.com/kika/fixedsys).
- Inspiration from the **AsciiPanel** project by [Trystan](https://github.com/trystan/AsciiPanel).
