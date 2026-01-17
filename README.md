# NEUNIX STEGENGINE v1.0

NEUNIX STEGENGINE is a production-ready Java CLI steganography engine that allows you to hide ANY type of file inside a PNG image using LSB (Least Significant Bit) steganography, with optional AES-256 encryption and compression.

This tool is NOT limited to text messages.

You can hide:
• Text files  
• PNG images  
• WAV audio  
• ZIP archives  
• APK files  
• Any binary file (depends only on PNG size)

If it fits, it hides.

------------------------------------------------------------

IMPORTANT DISCLAIMER (READ THIS)

This software is provided for EDUCATIONAL, RESEARCH, and LEGITIMATE USE ONLY.

The developer (NeunixStudios) is NOT responsible for:
• Any illegal usage
• Any data loss
• Any damage to devices
• Any violation of laws or platform policies
• Any misuse of steganography or encryption

By using this tool, YOU take FULL responsibility for how it is used.

Use this engine ONLY where steganography and encryption are legal and permitted.
If you do not agree, DO NOT use this software.

------------------------------------------------------------

WHY NEUNIX STEGENGINE?

Most steganography apps only hide simple text messages like:
"hello this is a secret"

NEUNIX STEGENGINE hides REAL FILES:
PNG → PNG  
WAV → PNG  
ZIP → PNG  
APK → PNG  
ANY BINARY → PNG  

Very few apps in the entire Play Store support this level of functionality.

------------------------------------------------------------

FEATURES

• PNG LSB steganography (lossless)
• Embed ANY file type
• AES-256 password encryption (optional)
• Explicit wrong-password detection
• Automatic file-type restoration
• Original filename recovery
• Compression before embedding
• Interactive terminal UI
• CLI argument mode
• Single-file Java engine
• No external libraries required
• Works on Linux, Windows, macOS, Android (Termux)

------------------------------------------------------------

HOW IT WORKS (SIMPLE EXPLANATION)

1. The selected file is optionally compressed
2. If a password is provided:
   • AES-256 encryption is applied
3. Metadata is added:
   • Magic signature
   • Original filename
   • File size
4. Data is hidden bit-by-bit into the PNG pixel RGB values
5. The output PNG looks completely normal

During extraction, everything is reversed safely.

------------------------------------------------------------

REQUIREMENTS

• Java JDK 11 or newer
• Any operating system

------------------------------------------------------------

INSTALLATION

1. Clone the repository:
   git clone https://github.com/19919rohit/Neunix-StegEngine.git
   cd Neunix-StegEngine

2. Compile:
   javac StegEngine.java

3. Run:
   java StegEngine

------------------------------------------------------------

RUN AS A JAR (RECOMMENDED)

Create runnable JAR:
jar cfe StegEngine.jar StegEngine StegEngine.class

Run:
java -jar StegEngine.jar

------------------------------------------------------------

INTERACTIVE MODE (BEGINNER FRIENDLY)

Run:
java StegEngine

Main menu:
NEUNIX STEGENGINE v1.0
---------------------------------
1. Embed file into PNG
2. Extract file from PNG
3. Exit

------------------------------------------------------------

EMBED FILE INTO PNG

You will be asked:

Carrier PNG path:
File to embed:
Output PNG path:
Password (optional):

Example (ZIP → PNG):

Carrier PNG path: carrier.png
File to embed: secret.zip
Output PNG path: output.png
Password (optional): mypassword

Result:
ZIP file is now hidden inside output.png

------------------------------------------------------------

EXTRACT FILE FROM PNG

You will be asked:

Stego PNG:
Output folder:
Password (optional):

Example:

Stego PNG: output.png
Output folder: ./extracted/
Password (optional): mypassword

Result:
Original file is restored with correct filename and file type

------------------------------------------------------------

CLI MODE (ADVANCED USERS)

Embed:
java StegEngine -i carrier.png -e secret.apk -o output.png -p password123

Extract:
java StegEngine -i output.png -x -o recovered.apk -p password123

------------------------------------------------------------

SUPPORTED FILE TYPES

Text files (.txt, .py, .java, .md)
PNG images
WAV audio
ZIP archives
APK files
ANY binary file

The engine does NOT care about file type.
Only size matters.

------------------------------------------------------------

FILE SIZE RULES

Payload must fit inside the carrier PNG.

Rule:
(payload size × 8 bits) ≤ (PNG width × height × 3 bytes)

Example:
• 1920×1080 PNG ≈ ~6MB payload capacity
• Use larger PNGs for larger files
• ZIP compression recommended for big data

------------------------------------------------------------

PASSWORD & ENCRYPTION

• AES-256-CBC encryption
• PBKDF2 key derivation
• Wrong password is detected explicitly
• No silent corruption

WARNING:
If the password is lost, data is unrecoverable.

------------------------------------------------------------

COMMON ERRORS & FIXES

Payload too large:
→ Use a bigger PNG or compress the file

Not a stego PNG:
→ File was not created using this engine

Wrong password:
→ Password must match exactly

------------------------------------------------------------

BEST PRACTICES

• Always test with small files first
• ZIP files before embedding large data
• Use only lossless PNG (never JPG)
• Keep backups of original files

------------------------------------------------------------

SECURITY NOTICE

This tool provides strong encryption but is NOT a replacement for full disk encryption.
Use responsibly.

------------------------------------------------------------

LICENSE

MIT License

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT.

------------------------------------------------------------

AUTHOR

NeunixStudios

------------------------------------------------------------

FINAL NOTE

This project proves that steganography is not limited to text messages.

You can hide:
• Images
• Audio
• ZIP archives
• APK files
• Entire projects

Inside a single PNG image.

You are fully responsible for how you use this software.
