import cv2
import numpy as np
import os
import sys
from operator import itemgetter

class FloorDetection:
    def __init__(self,img):
        self.img = cv2.imread(img.path)
        self.Wimg = np.ones(self.img.shape[0:2])*255
        self.height, self.width = self.img.shape[0:2]
        # folder,_ = img.path.split('originals')
        # self.outputimg = folder + 'output/' + img.name
        self.outputimg =  ""+img.name

    def edgeDetection(self):
        self.edges = cv2.Canny(self.img,30,90)
        return self.edges

    def lineDetection(self):
        lines = cv2.HoughLinesP(self.edges, 1, np.pi / 180, 50, 50, 10)
        self.lines = []
        for line in lines:
            x1,y1,x2,y2 = line[0]
            if y1 > self.height/2 and y2 > self.height/2:
                self.lines.append(line[0])
        self.paintImages(self.lines,"lines_",self.img.copy(),self.Wimg.copy())
        return self.lines

    def getVcandidates(self,theta,error):
        Lcand = []
        Rcand = []
        for line in self.lines:
            x1,y1,x2,y2 = line
            Y = y2 - y1
            X = x2 - x1
            m = Y/X if X !=0 else float('inf')
            b = y1 - m*x1
            dist = np.sqrt(Y**2 + X**2)
            angle = np.arctan2(Y,X)
            ang = np.abs(np.degrees(angle))
            if ang >= theta - error and ang <= theta + error:
                if x1 < self.width/2 and x2 < self.width/2:
                    Lcand.append([line,m,b,dist])
                elif x1 > self.width/2 and x2 > self.width/2:
                    Rcand.append([line,m,b,dist])
        return Lcand,Rcand

    def getOcandidates(self,theta,error):
        Lcand = []
        Rcand = []
        for line in self.lines:
            x1,y1,x2,y2 = line
            Y = y2 - y1
            X = x2 - x1
            m = Y/X if X !=0 else float('inf')
            b = y1 - m*x1
            dist = np.sqrt(Y**2 + X**2)
            angle = np.degrees(np.arctan2(Y,X))
            ang = np.abs(angle)
            if ang >= theta - error and ang <= theta + error:
                if angle < -10:
                    Lcand.append([line,m,b,dist])
                elif angle > 10:
                    Rcand.append([line,m,b,dist])
        return Lcand,Rcand


    def getVpoints(self,lines,s):
        # Sort lines based on length in decreasing order
        slines = sorted(lines, key=itemgetter(3),reverse=True)
        # choose the longest line
        theline = slines[0][:]
        # choose point closes to the floor
        x1,y1,x2,y2 = theline[0]
        if y1 > self.height/2:
            py = y1
            px = x1
        elif y2 > self.height/2:
            py = y2
            px = x2
        # create line that goes through point (px,py) with slope equal to +o-1.4
        m = s*1.4
        b = py - m * px

        # find point that goes through the line and it is in the middle of the image
        my = int(np.ceil(self.height/2))
        mx = int(np.ceil((my-b)/m))

        # find point that goes through the line and it is in the bottom of the image
        my = int(np.ceil(self.height/2))
        by = self.height-1
        bx = int(np.ceil((by-b)/m))
        return mx,my,bx,by

    def getOpoints(self,lines):
        # Sort lines based on length in decreasing order
        slines = sorted(lines, key=itemgetter(3),reverse=True)
        # choose the longest line
        theline,m,b,dist = slines[0][:]
        x1,y1,x2,y2 = theline

        # find point that goes through the line and it is in the middle of the image
        my = int(np.ceil(self.height/2))
        mx = int(np.ceil((my-b)/m))

        # find point that goes through the line and it is in the bottom of the image
        by = self.height-1
        bx = int(np.ceil((by-b)/m))
        return mx,my,bx,by


    def floorBoundary(self):
        self.Lvlines,self.Rvlines = self.getOcandidates(90,10)
        self.Lolines,self.Rolines = self.getVcandidates(45,20)

        img = self.img.copy()
        Wimg = self.Wimg.copy()
        self.paintImages([line[0] for line in self.Lvlines],"Vlines_",img,Wimg)
        self.paintImages([line[0] for line in self.Rvlines],"Vlines_",img,Wimg)

        img = self.img.copy()
        Wimg = self.Wimg.copy()
        self.paintImages([line[0] for line in self.Lolines],"Olines_",img,Wimg)
        self.paintImages([line[0] for line in self.Rolines],"Olines_",img,Wimg)

    def floorDetection(self):
        Ocont = []
        if(len(self.Lolines)>0):
            ptsL = self.getOpoints(self.Lolines)
            Ocont.append('OL')

        if(len(self.Rolines)>0):
            ptsR = self.getOpoints(self.Rolines)
            Ocont.append('OR')

        op = len(Ocont)
        if (op!=2):
            if (op==0):
                # if vlines lists are not empty find left and right points
                if(len(self.Lvlines)>0):
                    ptsL = self.getVpoints(self.Lvlines,-1)
                if(len(self.Rvlines)>0):
                    ptsR = self.getVpoints(self.Rvlines,1)
            else:

                if (Ocont[0]=='OL'): # if vlines lists are empty
                    if (len(self.Rvlines)>0):
                        ptsR = self.getVpoints(self.Rvlines,1)
                    else:
                        ptsL = self.getVpoints(self.Lvlines,-1)

        self.flines = [ptsL,ptsR,[ptsL[0],ptsL[1],ptsR[0],ptsR[1]]]
        self.paintImages(flines,'floor_',self.img.copy(),self.Wimg.copy())

    def draFloor(self):


    def paintImages(self,lines,name,img,Wimg):
        B,G,R =[int(c) for c in np.random.randint(0,256,size=(3,))]
        for line in lines:
            x1,y1,x2,y2 = line
            cv2.line(img,(x1,y1),(x2,y2),(B,G,R),2)
            cv2.line(Wimg,(x1,y1),(x2,y2),(B,G,R),2)
        cv2.imwrite(name+self.outputimg,img)
        cv2.imwrite('W'+name+self.outputimg,Wimg)
