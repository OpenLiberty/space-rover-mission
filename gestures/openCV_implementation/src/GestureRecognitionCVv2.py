#Import all the important libraries
import asyncio
from asyncio import futures
import cv2
from cv2 import imshow
import cvzone
import mediapipe as mp
import websockets
from cvzone.HandTrackingModule import HandDetector
import time
 
print("Starting to connect")
uri = "ws://192.168.0.101:9070/roversocket"

# Asynchronously try to connect to the server
@asyncio.coroutine
def main():
    connecting = True

    while connecting:
        try:
            print("Here")
            done, pending = yield from asyncio.wait([websockets.connect(uri)])
            # assert not pending
            future, = done
            print(future.result())
        except:
            print("Here 2")
            print("Unble to connect to the machine")
            time.sleep(5)
            res = None
        else:
            print("Here 3")
            connecting = False

# Run the main loop until we are able to connect to the server
asyncio.get_event_loop().run_until_complete(main())

async def repl():
    async with websockets.connect(uri) as websocket:

        # Send successful connection message to the server
        print("Connected")
        await websocket.send("connectGesture")

        # Speed Control parameters for the Rover
        slowParamForwardReverse = 20
        slowParamLeftRight = 15
        previous = "S" * slowParamForwardReverse

        # Font
        font = cv2.FONT_HERSHEY_SIMPLEX

        # Set up for fps counter
        fpsCounter = cvzone.FPS()

        # Start video stream from your computer camera
        capture = cv2.VideoCapture(0) 

        # Window name
        windowName = "Hand Gesture Recognition Live Capture"
        cv2.namedWindow(windowName, cv2.WND_PROP_AUTOSIZE)

        # Show the screen capture as full screen
        cv2.setWindowProperty(windowName, cv2.WND_PROP_FULLSCREEN,
                            cv2.WINDOW_FULLSCREEN)

        # Using cvzone library to detect and track the hand
        detector = HandDetector(detectionCon=0.6, minTrackCon=0.6, maxHands=1)

        # Capture continuous video
        i = 1 
        while True:

            # Get image frame
            ret, image = capture.read() # read from the video

            # Get interruption key from keyboard
            k = cv2.waitKey(1) # get input from keyboard

            # Flip image frame
            cv2image1 = cv2.flip(image,1)
            
            # Show FPS on screen
            fps, img = fpsCounter.update(cv2image1,pos=(50,80),color=(0,255,0),scale=3,thickness=3)

            # Find the hand and its landmarks
            hands, img = detector.findHands(img, flipType=False)

            if hands:
                # Hand 1
                hand1 = hands[0]
                lmList1 = hand1["lmList"]  # List of 21 Landmark points
                bbox1 = hand1["bbox"]  # Bounding box info x,y,w,h
                centerPoint1 = hand1['center']  # center of the hand cx,cy
                handType1 = hand1["type"]  # Handtype Left or Right
                handXmin = hand1["xmin"]
                handXmax = hand1["xmax"]
                handYmin = hand1["ymin"]
                handYmax = hand1["ymax"]
                handBoxW = hand1["boxW"]
                handBoxH = hand1["boxW"]
                fingers1 = detector.fingersUp(hand1)
                
                # Prevent the user from standing too far
                if handBoxW * handBoxH < 4000:
                    cv2.putText(img,'Bring your hand closer to the camera',(400,140),font,0.9,(255,100,0),2,cv2.LINE_AA)

                # Prevent the user from moving their hand outside the middle of the screen
                elif handXmin < 50 or handXmax > 1250 or handYmax > 675 or handYmin < 50:
                    cv2.putText(img,'Make sure your hand is in the middle of the screen',(400,140),font,0.9,(255,100,0),2,cv2.LINE_AA)

                # We can detect gesture now that the hand is properly positioned on Camera
                else:
                    # Find Distance between two Landmarks. 

                    #print(lmList1)

                    # Index finger
                    indexLength, indexInfo = detector.findDistance(lmList1[8][0:2], lmList1[5][0:2])  # with draw

                    # Second finger
                    secondLength, secondInfo = detector.findDistance(lmList1[12][0:2], lmList1[9][0:2])  # with draw

                    #print(fingers1)

                    # Go Forward:
                    if ((indexInfo[3] - indexInfo[1] > 75) and (secondInfo[3] - secondInfo[1] > 75) and (abs(indexInfo[2] - indexInfo[0]) < 75) and (abs(secondInfo[2] - secondInfo[0]) < 75)):
                        cv2.putText(img,'Signal Detected: Go Forward',(465,140),font,1.2,(255,100,0),2,cv2.LINE_AA)
                        print('Go Forward')
                        if "F" in previous:
                            print('Software skipping')
                            previous = previous[1:] + "S"
                        else:
                            previous = previous[1:] + "F"
                            print(previous)
                            await websocket.send("F")
                    
                    # Reverse:
                    elif ((indexInfo[1] - indexInfo[3] > 75) and (secondInfo[1] - secondInfo[3] > 75) and (abs(indexInfo[2] - indexInfo[0]) < 50) and (abs(secondInfo[2] - secondInfo[0]) < 50)):
                        cv2.putText(img,'Signal Detected: Reverse',(465,140),font,1.2,(255,100,0),2,cv2.LINE_AA)
                        print('Reverse')
                        if "B" in previous:
                            print('Software skipping')
                            previous = previous[1:] + "S"
                        else:
                            previous = previous[1:] + "B"
                            print(previous)
                            await websocket.send("B")

                    # Turn Left:
                    elif ((indexInfo[2] - indexInfo[0] > 50) and (secondInfo[2] - secondInfo[0] > 50)):
                        cv2.putText(img,'Signal Detected: Turn Left',(465,140),font,1.2,(255,100,0),2,cv2.LINE_AA)
                        print('Turn Left')
                        if "L" in previous[-slowParamLeftRight:]:
                            print('Software skipping')
                            previous = previous[1:] + "S"
                        else:
                            previous = previous[1:] + "L"
                            print(previous)
                            await websocket.send("L")

                    # Turn Right:
                    elif ((indexInfo[0] - indexInfo[2] > 50) and (secondInfo[0] - secondInfo[2] > 50)):
                        cv2.putText(img,'Signal Detected: Turn Right',(465,140),font,1.2,(255,100,0),2,cv2.LINE_AA)
                        print('Turn Right')
                        if "R" in previous[-slowParamLeftRight:]:
                            print('Software skipping')
                            previous = previous[1:] + "S"
                        else:
                            previous = previous[1:] + "R"
                            print(previous)
                            await websocket.send("R")

                    # Stop Moving
                    elif ((fingers1 == [0,0,0,0,0]) or (fingers1 == [1,0,0,0,0]) or (fingers1 == [1,1,1,1,1]) or (fingers1 == [0,1,1,1,1])):
                        cv2.putText(img,'Signal Detected: Stop Moving',(465,140),font,1.2,(255,100,0),2,cv2.LINE_AA)
                        print('Stop Moving')
                        previous = previous[1:] + "S"
                        print(previous)  
                        await websocket.send("S")

                    # Unsure
                    else: 
                        cv2.putText(img,'Unrecognized gesture. Stoping Car.',(465,140),font,1.2,(255,100,0),2,cv2.LINE_AA)
                        print('Car stopped Moving')
                        previous = previous[1:] + "S"
                        print(previous)  
                        await websocket.send("S")

            else:
                # If the user's hand leaves the camera, send the stop signal
                cv2.putText(img,'Display your hand in the camera',(400,140),font,0.9,(255,0,0),3,cv2.LINE_AA)

                if previous[-1] != "S":
                    print('Stop Moving')
                    previous = previous[1:] + "S"
                    print(previous)  
                    await websocket.send("S")

            cv2.imshow(windowName,img)
                
            if k == 27: # Press 'Esc' key to exit 
                #await websocket.send("Hand Gesture Control connection closed.")
                break # Exit while loop
                
        #========================================

        # Close down window
        cv2.destroyAllWindows() 
        # Disable your camera 
        capture.release()

# Run the Hand Gesture Recognition ascynchronously after the connection works        
asyncio.get_event_loop().run_until_complete(repl())
