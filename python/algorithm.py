import cv2
import numpy as np
import os
import sys
from operator import itemgetter

def calculateMetrics(GT,OUTPUT):
    ''' This method calculates Accuracy, Precision, and Recall
        Relevant items = Superpixels that represent Objects on the floor
        TP = True Positive - Superpixels that were correctly classified as part of the object
        TP = True Positive - Superpixels that were correctly classified as part of the object
        TN = True Negative - Superpixels that were correctly classified as NOT part of the object
        FP = False Positive - Superpixels that were INcorrectly classified as part of the object
        FN = False Negative - Superpixels that were INcorrectly classified as NOT part of the object.

        Accuracy = (TP + TN)/(TP + TN + FP +FN)
        Precision = TP/(TP+FP)
        Recall = TP/(TP + FN)
    '''
    acc = 0
    prec = 0
    rec = 0
    h,w = GT.shape
    # print(h,w)
    TP,TN,FP,FN = 0,0,0,0
    for y in range(h):
        for x in range(w):
            v = 2*GT[y][x]/255 - OUTPUT[y][x]/255
            # print(v)
            if v == 0:
                TP += 1
            elif v == 1:
                TN += 1
            elif v == -1:
                FN += 1
            elif v == 2:
                FP +=1
    # print ('TP',TP,'TN',TN,'FP',FP,'FN',FN)
    acc = (TP + TN)/(TP + TN + FP +FN)
    if TP + FP !=0:
        prec = TP/(TP + FP)
    if TP + FN !=0:
        rec = TP/(TP + FN)

    print('Accuracy:',acc,',Precision:',prec,',Recall:',rec)
    return (acc,prec,rec)


    def paintImages(self,lines,name,img,Wimg):
        B,G,R =[int(c) for c in np.random.randint(0,256,size=(3,))]
        for line in lines:
            x1,y1,x2,y2 = line
            cv2.line(img,(x1,y1),(x2,y2),(B,G,R),2)
            cv2.line(Wimg,(x1,y1),(x2,y2),(B,G,R),2)
            cv2.imwrite(name,img)
            # cv2.imwrite(name+self.outputimg,img)
            # cv2.imwrite(name+self.outputimg,Wimg)

class FloorDetection:
    def __init__(self):
        self.outputimg =  ""

    def edgeDetection(self,img):
        self.img = cv2.imread(img.path)
        self.Wimg = np.ones(self.img.shape[0:2])*255
        self.height, self.width = self.img.shape[0:2]
        folder,_ = img.path.split('originals')
        self.outputimg1 = folder + 'output/' + img.name
        self.outputimg2 = folder + 'lines_on_originals/' + img.name
        self.edges = cv2.Canny(self.img,30,90)
        return self.edges

    def lineDetection(self):
        lines = cv2.HoughLinesP(self.edges, 1, np.pi / 180, 50, 50, 10)
        self.lines = []
        if (lines == None):
            return
        for line in lines:
            x1,y1,x2,y2 = line[0]
            if y1 > self.height/2 and y2 > self.height/2:
                self.lines.append(line[0])
        # self.paintImages(self.lines,"lines_",self.img.copy(),self.Wimg.copy())
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
        self.Lvlines,self.Rvlines = self.getVcandidates(90,10)
        self.Lolines,self.Rolines = self.getOcandidates(45,20)

        # img = self.img.copy()
        # Wimg = self.Wimg.copy()
        # self.paintImages([line[0] for line in self.Lvlines],"Vlines_",img,Wimg)
        # self.paintImages([line[0] for line in self.Rvlines],"Vlines_",img,Wimg)
        #
        # img = self.img.copy()
        # Wimg = self.Wimg.copy()
        # self.paintImages([line[0] for line in self.Lolines],"Olines_",img,Wimg)
        # self.paintImages([line[0] for line in self.Rolines],"Olines_",img,Wimg)

    def floorDetection(self):
        Ocont = []
        ptsL = 0,self.height//2,0,self.height-1
        ptsR = self.width-1,self.height//2, self.width-1,self.height-1
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
                    elif (Ocont[0]=='OR'):
                        ptsL = self.getVpoints(self.Lvlines,-1)

        self.floorLines = [ptsL,ptsR,[ptsL[0],ptsL[1],ptsR[0],ptsR[1]]]
        # self.paintImages(self.floorLines,self.outputimg2,self.img.copy(),self.Wimg.copy())

    def drawFloor(self):
        img = self.img.copy()
        xl1,yl1,xl2,yl2 =  self.floorLines[0]
        Y = yl2 - yl1
        X = xl2 - xl1
        ml = Y/X if X !=0 else float('inf')
        if (ml == float('inf')):
            xref1 = xl1
        else:
            bl = yl1 - ml * xl1

        xr1,yr1,xr2,yr2 =  self.floorLines[1]
        Y = yr2 - yr1
        X = xr2 - xr1
        mr = Y/X if X !=0 else float('inf')
        if (mr == float('inf')):
            xref2 = xr1
        else:
            br = yr1 - mr * xr1

        for y in range(self.height):
            for x in range(self.width):
                if (y >= self.height/2):
                    if ml != float('inf'):
                        xref1 = (y - bl)/ml
                    if mr !=float('inf'):
                        xref2 = (y - br)/mr
                    if x >=xref1 and x<=xref2:
                        self.Wimg[y,x]= 0
                        img[y,x] = img[y,x]*0.6 + np.array([255,0,0])*0.4
        cv2.imwrite(self.outputimg1,self.Wimg)
        cv2.imwrite(self.outputimg2,img)

    def evaluate(self):
        Accuracy = []
        Precision = []
        Recall = []
        for folder in os.scandir('Images'):
            if folder.is_dir():
                for subfolder in os.scandir(folder.path):
                    if subfolder.is_dir() and subfolder.name =='labels':
                        print(subfolder.name)
                        for file in os.scandir(subfolder.path):
                            if file.name.endswith('.png') or file.name.endswith('.jpg'):
                                print(file.path)
                                lbl = cv2.imread(file.path,0)
                                folder,_ = file.path.split('labels')
                                output = cv2.imread(folder+'output/'+file.name,0)
                                if lbl == None or output == None:
                                    continue
                                acc,prec,rec, = calculateMetrics(lbl,output)
                                Accuracy.append(acc)
                                Precision.append(prec)
                                Recall.append(rec)
        Acc = np.mean(Accuracy)
        Prec = np.mean(Precision)
        Rec = np.mean(Recall)
        print('System Evaluation Results')
        print('Accuracy',Acc)
        print('Precision',Prec)
        print('Recall',Rec)
        return Acc,Prec,Rec
