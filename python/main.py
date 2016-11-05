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
                        if img.name.endswidth('.png') or img.name.endswidth('jpg'):
                            fp = FloorDetection(img)
                            fp.edgeDetection()
                            fp.lineDetection()
                            fp.floorBoundary()

def processImage(img):
    for file in os.scandir():
        if file.name == img:
            fp = FloorDetection(file)
            fp.edgeDetection()
            fp.lineDetection()
            fp.floorBoundary()
            fp.floorDetection()

processImage('hallway_320x240_016.jpg')
