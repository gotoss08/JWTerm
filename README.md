# JWTerm - Virtual Terminal Screen Emulator

## Description

**JWTerm** is a basic virtual terminal screen emulator written in **Java 21**. It is developed using **IntelliJ IDEA** and is primarily intended for use in **roguelike game development**. This emulator provides a minimalistic and efficient way to render text-based screens. The project is currently **very in development** and has just recently begun.

JWTerm does not rely on any external libraries, utilizing only Java's built-in **Swing** and **AWT** libraries. This makes it lightweight and easy to integrate into Java projects that require terminal-like interfaces.

Currently, the emulator supports rendering basic characters using the **Fixedsys font**, providing a retro aesthetic suitable for text-based games.

> **Note:** This project is still in early stages of development, and many features are yet to be implemented.

## Features

- **Terminal Screen Emulation**: Simulates a virtual terminal window, ideal for roguelike games or any text-based applications.
- **Fixedsys Font**: Uses the **Fixedsys** font for retro-style text rendering, enhancing the terminal-like experience.
- **Lightweight**: Written purely in Java with no external dependencies, leveraging Swing and AWT for graphical components.
- **Open Source**: JWTerm is free and open-source under the **MIT License**, allowing modification and redistribution.

## Installation

1. **Clone the repository**:
   First, clone the project to your local machine by running the following command in your terminal:
   ```bash
   git clone https://github.com/yourusername/JWTerm.git
   ```

2. **Open the project in IntelliJ IDEA**:
   - Launch **IntelliJ IDEA**.
   - Select **Open** from the welcome screen, then navigate to the folder where you cloned the repository and open the project.

3. **Set up the JDK**:
   - Once the project is opened, ensure that **Java 21 SDK** is selected for the project.
   - Go to **File** > **Project Structure** > **Project** and select **Java 21** under the **Project SDK** section. If you don't have Java 21 installed, you can download it directly from the [adoptium website](https://adoptium.net/).

4. **Build the project**:
   - In IntelliJ IDEA, the project should be automatically recognized as a Java project. However, if necessary, you can trigger a manual build by selecting **Build** > **Build Project** from the top menu.

5. **Run the application**:
   - To run the project, click the green **Run** button at the top-right corner of IntelliJ IDEA or use the shortcut `Shift + F10`.
   - This will execute the `Main.java` class located in `src/com/jwterm/Main.java`, opening the terminal emulator window.

> **Note:** Since this project is in the early stages of development, there may be additional setup steps or features that need further refinement as the project evolves.

## Usage

JWTerm currently renders basic characters on a window, simulating a terminal screen. This can be used for developing roguelike games or any text-based applications that require a terminal interface.

1. Once the application runs, a window will open displaying a virtual terminal screen.
2. The screen is ready to display text-based information such as game statuses, player messages, and more.

## License

JWTerm is released under the MIT License. See the [LICENSE](LICENSE) file for more details.

## Contributing

Feel free to fork the repository, submit issues, and open pull requests. All contributions are welcome!

> Important: Since this project is still in early development, contributions that help improve stability, features, or documentation are particularly appreciated.

## Acknowledgments

- Fixedsys font by [Kika](https://github.com/kika/fixedsys).
