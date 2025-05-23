#!/usr/bin/env python3

import argparse
from smartcard.System import readers

# Parse arguments
parser = argparse.ArgumentParser(description = 'Read javacard memory')
parser.add_argument('-l', '--list-readers', action='store_true', dest='listreaders', 
    help='list available PC/SC readers')
parser.add_argument('-r', '--reader', nargs='?', dest='reader', type=int, 
    const=0, default=0, 
    required=False, help='index of the PC/SC reader to use (default: 0)')
args = parser.parse_args()

if(args.listreaders):
    # List readers
    redlist = readers()
    if(len(redlist) == 0):
        print('warning: No PC/SC readers found')
        exit(1)
    redlist.sort(key=str)
    print('info: Available PC/SC readers (' + str(len(redlist)) + '):')
    for i, reader in enumerate(redlist):
        print(str(i) + ': ' + str(reader))
else:
    # Read usage
    redlist = readers()
    if(len(redlist) == 0):
        print('error: No PC/SC readers found')
        exit(1)
    if(args.reader < 0 or args.reader >= len(redlist)):
        print('error: Specified reader index is out of range')
        exit(1)
    redlist.sort(key=str)
    red = redlist[args.reader]
    print('info: Using reader ' + str(args.reader) + ': ' + str(red))

    connection = red.createConnection()
    connection.connect()
    # Select the applet
    print('info: Sending applet selection')
    data, sw1, sw2 = connection.transmit(
        [0x00, 0xA4, 0x04, 0x00, 0x0B, 0xA0, 0x00, 0x00, 0x08, 0x46, 0x6D, 0x64, 0x65, 0x6D, 0x6F, 0x01])
    if(sw1 == 0x90 and sw2 == 0x00):
        print('success: Applet selected, card response is ok')
        
        # Encryption test
        payload = "Hello world".encode("ascii")
        payload = payload.ljust(16, b'\0')
        print('info: Sending encryption command')
        data, sw1, sw2 = connection.transmit(
            [0x00, 0x01, 0x00, 0x00, len(payload)] + list(payload) + [0x00])
        if(sw1 == 0x90 and sw2 == 0x00):
            print('success: Card response is ok')
            response = bytes(data)
            print(f'info: Encrypted data: {response.hex()}')
        else:
            print('error: status response: ' + f'{sw1:02x}' + ' ' + f'{sw2:02x}')

        # Decryption test
        print('info: Sending decryption command')
        data, sw1, sw2 = connection.transmit(
            [0x00, 0x02, 0x00, 0x00, len(response)] + list(response) + [0x00])
        if(sw1 == 0x90 and sw2 == 0x00):
            print('success: Card response is ok')
            response = bytes(data)
            print(f'info: Decrypted data: {response.hex()} = {response.decode("ascii")}')
        else:
            print('error: status response: ' + f'{sw1:02x}' + ' ' + f'{sw2:02x}')

    else:
        print('error: Card response: ' + f'{sw1:02x}' + ' ' + f'{sw2:02x}')
    connection.disconnect()

