# Demo JavaCard Applet

This is a small demo applet, intended to show how tooling and programming for javaCard works at Grindfest 2025.

The applet generates and stores an AES128 key, and is then able to encrypt and decrypt a fixed sized buffer of supplied data using that key.

## Compilation

This applet used `ant-javacard`, another choice could have been e.g. `maven`.

If you use Nix, a flake and direnv configuration is provided. Otherwise, make sure your system has Java 8, `ant`, `maven` and Python 3 with `pyscard`.

## Emulation

You can use the `vsmartcard` virtual reader to create a virtual reader on your system, and then use the `jcardsim` emulator to attach an emulated instance of the applet to it.

## Disclaimer

This code serves for educational use only and does not hold up the the security requirements of real world usage.
