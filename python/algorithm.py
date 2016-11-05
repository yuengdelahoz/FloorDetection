import cv2
import numpy as np
import os
import sys

class FloorDetection:
    def __init__(self,img):
        self.img = cv2.imread(img.path)
        self.height, self.width = self.img.shape[0:2]
        folder,_ = img.path.split('originals')
        self.outputimg = folder + 'output/' + img.name
        print(self.outputimg)

    def edgeDetection(self):
        self.edges = cv2.canny(self.img,30,90)

    def lineDetection(self):
        lines = cv2.HoughLinesP(edges, 1, np.pi / 180, 50, 50, 10)
        self.lines = []
        for line in lines:
            x1,y1,x2,y2 = line[0]
            if y1 > self.height/2 and y2 > height/2:
                self.lines(line[0])
