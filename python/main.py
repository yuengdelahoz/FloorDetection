import numpy as np
import os
import sys
from algorithm import *

for folder in os.scandir('Images'):
    if folder.is_dir():
        for subfolder in os.scandir(folder.path):
            if subfolder.is_dir() and subfolder.name=='originals':
                for img in os.scandir(subfolder.path):
                    fp = FloorDetection(img)
