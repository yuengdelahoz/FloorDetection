import numpy as np
import os
import sys
from algorithm import FloorDetection

def processBatch():
    for folder in os.scandir('Images'):
        if folder.is_dir():
            for subfolder in os.scandir(folder.path):
                if subfolder.is_dir() and subfolder.name=='originals':
                    for img in os.scandir(subfolder.path):
                        if img.name.endswith('.png') or img.name.endswith('jpg'):
                            print(img.path)
                            fp = FloorDetection()
                            fp.edgeDetection(img)
                            fp.lineDetection()
                            fp.floorBoundary()
                            fp.floorDetection()
                            fp.drawFloor()

def processImage(img):
    for file in os.scandir():
        if file.name == img:
            fp = FloorDetection()
            fp.edgeDetection(file)
            fp.lineDetection()
            fp.floorBoundary()
            fp.floorDetection()
            fp.drawFloor()

# processImage('hallway_320x240_016.jpg')
# processImage('corridor.png')
# processImage('frame-00001.png')
# processBatch()
fp = FloorDetection()
fp.evaluate()
