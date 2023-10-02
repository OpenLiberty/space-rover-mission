#################################################################################
# Copyright (c) 2022 IBM Corporation and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License 2.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-2.0/
#
# SPDX-License-Identifier: EPL-2.0
#################################################################################

# Import all the important libraries
import asyncio
from asyncio import futures
import cv2
from cv2 import imshow
import cvzone
import mediapipe as mp
import websockets
from cvzone.HandTrackingModule import HandDetector
import time
import sys

print("Starting to connect")
#uri = "ws://192.168.0.101:9070/roversocket"
URI = "ws://localhost:9070/roversocket"


# Asynchronously try to connect to the server


@asyncio.coroutine
def main():
    connecting = True

    while connecting:
        try:

            done, pending = yield from asyncio.wait([websockets.connect(URI)])

            # assert not pending
            future, = done
            print(future.result())
        except:
            print("Unable to connect to the machine")
            time.sleep(5)
        else:
            connecting = False


# Run the main loop until we are able to connect to the server
asyncio.get_event_loop().run_until_complete(main())


async def repl():
    async with websockets.connect(URI) as websocket:

        # Send successful connection message to the server
        print("Connected")

        # Speed Control parameters for the Rover
        previous = "S"

        # Font
        font = cv2.FONT_HERSHEY_SIMPLEX

        # Set up for fps counter
        fps_counter = cvzone.FPS()
        window_name = "Hand Gesture Recognition Live Capture"

        # Use default capture device with default rendering
        capture = cv2.VideoCapture(0)
        # Window name
        cv2.namedWindow(window_name, cv2.WND_PROP_AUTOSIZE)

        # set to full screen on all OS
        cv2.setWindowProperty(window_name, cv2.WND_PROP_FULLSCREEN,
                              cv2.WINDOW_FULLSCREEN)


        # Use 720 manual setting for the webcam. Static resolution values are used below so we must keep the
        # video feed constant.
        capture.set(cv2.CAP_PROP_FRAME_WIDTH, 1280)
        capture.set(cv2.CAP_PROP_FRAME_HEIGHT, 720)
        # Using cvzone library to detect and track the hand
        detector = HandDetector(detectionCon=0.6, minTrackCon=0.6, maxHands=1)

        # Capture continuous video
        while True:

            # Get image frame
            _, image = capture.read()  # read from the video

            # Get interruption key from keyboard
            k = cv2.waitKey(1)  # get input from keyboard

            # Flip image frame
            cv2image1 = cv2.flip(image, 1)

            # Show FPS on screen
            _, img = fps_counter.update(cv2image1, pos=(
                50, 80), color=(0, 255, 0), scale=3, thickness=3)

            # Find the hand and its landmarks
            hands, img = detector.findHands(img, flipType=False)

            if hands:
                # Hand 1
                hand1 = hands[0]
                lmList1 = hand1["lmList"]  # List of 21 Landmark points
                bbox1 = hand1["bbox"]  # Bounding box info x,y,w,h

                handXmin = bbox1[0]
                handXmax = bbox1[0] + bbox1[2]
                handYmin = bbox1[1]
                handYmax = bbox1[1] + bbox1[3]
                handBoxW = bbox1[2]
                handBoxH = bbox1[3]

                # Prevent the user from standing too far
                if handBoxW * handBoxH < 4000:
                    cv2.putText(img, 'Bring your hand closer to the camera',
                                (400, 140), font, 0.9, (255, 100, 0), 2, cv2.LINE_AA)

                # Prevent the user from moving their hand outside the middle of the screen
                elif handXmin < 50 or handXmax > 1250 or handYmax > 675 or handYmin < 50:
                    cv2.putText(img, 'Make sure your hand is in the middle of the screen',
                                (400, 140), font, 0.9, (255, 100, 0), 2, cv2.LINE_AA)

                # We can detect gesture now that the hand is properly positioned on Camera
                else:
                    # Find Distance between two Landmarks.

                    # print(lmList1)

                    # Index finger
                    _, index_info = detector.findDistance(
                        lmList1[8][0:2], lmList1[5][0:2])  # with draw

                    # Second finger
                    _, second_info = detector.findDistance(
                        lmList1[12][0:2], lmList1[9][0:2])  # with draw
                    network_msg, human_msg = get_direction_msg(
                        index_info, second_info)
                    cv2.putText(img, f'Gesture Detected: {human_msg}',
                                (465, 140), font, 1.2, (255, 100, 0), 2, cv2.LINE_AA)
                    previous = await send_msg_if_not_previous(websocket, previous, network_msg)

            else:
                # If the user's hand leaves the camera, send the stop signal
                cv2.putText(img, 'Display your hand in the camera',
                            (400, 140), font, 0.9, (255, 0, 0), 3, cv2.LINE_AA)
                previous = await send_msg_if_not_previous(websocket, previous, "S")

            cv2.imshow(window_name, img)

            if k == 27:  # Press 'Esc' key to exit
                # await websocket.send("Hand Gesture Control connection closed.")
                break  # Exit while loop

        # ========================================

        # Close down window
        cv2.destroyAllWindows()
        # Disable your camera
        capture.release()


async def send_msg_if_not_previous(websocket, previous_msg, msg):
    '''Sends msg to the websocket so long as it is not the same string as 'previous_msg' '''
    if msg != previous_msg:
        if msg != "S":
            await websocket.send("S")
            print("Sent message", "S")
        await websocket.send(msg)
        print("Sent message", msg)
        previous_msg = msg
    return previous_msg


def get_direction_msg(first_finger_index, second_finger_index):
    '''Returns the network message and human readable direction given the first finger and second finger index information based on 720p resolution'''
    if (first_finger_index[3] - first_finger_index[1] > 75) and (second_finger_index[3] - second_finger_index[1] > 75) and (abs(first_finger_index[2] - first_finger_index[0]) < 75) and (abs(second_finger_index[2] - second_finger_index[0]) < 75):
        msg = "F", "Forward"
    elif ((first_finger_index[1] - first_finger_index[3] > 75) and (second_finger_index[1] - second_finger_index[3] > 75) and (abs(first_finger_index[2] - first_finger_index[0]) < 50) and (abs(second_finger_index[2] - second_finger_index[0]) < 50)):
        msg = "B", "Reverse"
    elif (first_finger_index[2] - first_finger_index[0] > 50) and (second_finger_index[2] - second_finger_index[0] > 50):
        msg = "L", "Left"
    elif (first_finger_index[0] - first_finger_index[2] > 50) and (second_finger_index[0] - second_finger_index[2] > 50):
        msg = "R", "Right"
    else:
        msg = "S", "Stop"
    return msg


# Run the Hand Gesture Recognition ascynchronously after the connection works
asyncio.get_event_loop().run_until_complete(repl())
