import subprocess
import tkinter
import os

from tkinter import Tk, Label, Button, Entry, StringVar, DISABLED, NORMAL, END, W, E, LEFT, RIGHT, scrolledtext, WORD, \
    Text, Scrollbar, Y, filedialog, Frame, messagebox, filedialog, OptionMenu, CENTER, NE, NW
import tkinter
import os
import sys

# variable

class MyFirstGUI:
    def __init__(self, master):
        
        master.title("APRIORI APP - KLTN 2017")

        self.label = Label(master, text="FREQUENT ITEMSET MINING - APRIORI",font=("Times New Roman", 17),bg='#C6E2FF')
        self.label.grid(row=0,pady=20)

        # menu
        self.dropMenu = OptionMenu(master, tkvarmenu, *menu,command=self.changeMenu)
        self.dropMenu.grid(row=0, column=0,sticky=NW)

        self.frameOpption = Frame(master,bg='#C6E2FF')
        self.frameOpption.grid(row=1,column =0,sticky=W)
        self.frameOpption.propagate(0)
       
        self.l1 = Label(self.frameOpption, text="SUPPORT (0<x<1)", font=("Times New Roman", 10),bg='#C6E2FF')
        self.l1.grid(row=0, column=0,sticky =W,padx=10, pady= 5)

        self.l2 = Label(self.frameOpption, text="SUM OF TRANSACTION", font=("Times New Roman", 10),bg='#C6E2FF')
        self.l2.grid(row=1, column=0,sticky =W,padx=10, pady= 5)

        self.linkFile = Label(self.frameOpption, text="FILE", font=("Times New Roman", 10),bg='#C6E2FF')
        self.linkFile.grid(row=2, column=0,sticky =W,padx=10, pady= 5)

        # entry widgets, used to take entry fr m user
        self.e1 = Entry(self.frameOpption, width=50)
        self.e2 = Entry(self.frameOpption, width=50)
        self.e3 = Entry(self.frameOpption, width=50)

        # this will arrange entry widgets
        self.e1.grid(row=0,column=1,padx=5, pady= 5)
        self.e2.grid(row=1,column=1,padx=5, pady= 5)
        self.e3.grid(row=2,column=1,padx=5, pady= 5)

        # drop down
        self.popupMenu = OptionMenu(self.frameOpption, tkvar, *choices)
        Label(self.frameOpption, text="CHOOSE ALGORITHMS",bg='#C6E2FF').grid(row=0, column=2)
        self.popupMenu.grid(row=1, column=2)

        # add text
        self.open = Button(self.frameOpption, text="Open file", command=self.openfile)
        self.open.grid(row=2,column=2 ,padx=60, pady= 5)

        # FRAME TEXT
        # text area
        self.text_area = scrolledtext.ScrolledText(master, wrap=WORD,
                                              width=65, height=20,
                                              font=("Times New Roman", 15))
        self.text_area.grid(row=2,column=0,padx=20,pady=10)

        # placing cursor in text area
        self.text_area.focus()

        # time lable
        self.frameTime = Frame(master)
        self.frameTime.grid(row=3, column=0)

        # FRAME EXCUTE
        self.frameButton = Frame(master,bg='#C6E2FF')
        self.frameButton.grid(row=4, column=0, sticky=W)

        # add button
        self.compline = Button(self.frameButton, text="COMPILE",command=self.complie)
        self.compline.place(relx=0.5, rely=0.5, anchor=CENTER)
        self.compline.grid(row=4, column=1, padx=200, pady= 5)

        # xem kết quả
        self.result = Button(self.frameButton, text="RESULT", command=self.openResult)
        self.result.grid(row=4,column=2)

    def openfile(self):
        filetypes = (
            ('text files', '*.txt'),
            ('All files', '*.*')
        )
        filename = filedialog.askopenfilename(
            title='Open a file',
            initialdir='/',
            filetypes=filetypes)
        self.e3.delete(0,END)
        self.e3.insert(0 ,filename)

    def validateString(self):
        if (self.e1.get() == '' or self.e2.get()==''):
            messagebox.showwarning("Notification", "Please enter input transaction and support !")
            return;
        try:
            temp = float(self.e1.get())
            if(temp <= 0 or temp >=1):
                messagebox.showwarning("Notification", "Type of support is a float !")
                return;
            temp2 = int(self.e2.get())
        except:
            messagebox.showwarning("Notification", "Transaction and support is a number !")

    # read file
    def openResult(self,*args):
        try:
            if(tkvar.get()=='Apriori'):
                try:
                    file = open('/home/khanhduyuser/AprioriJava/outputApriori/output.txt').read()
                except:
                    messagebox.showwarning("Notification", "File not exit.Please run algorithms !")
            elif(tkvar.get()=='Apriori Improve'):
                try:
                    file = open('/home/khanhduyuser/AprioriJava/outputAprioriImprove/output.txt').read()
                except:
                    messagebox.showwarning("Notification", "File not exit.Please run algorithms !")
            else:
                try:
                    file = open('/home/khanhduyuser/AprioriJava/outputAprioriMap/part-r-00000').read()
                except:
                    messagebox.showwarning("Notification", "File not exit.Please run algorithms !")
            self.text_area.delete('1.0', END)
            self.text_area.insert(0.0, file)
        except:
            return

    # changemunu
    def changeMenu(self,*args):
        print(tkvarmenu.get())

    #compile
    def complie(self,*args):
        self.validateString()
       
        myPathApriori = "java"
        
        # Apriori
        if(tkvar.get()=='Apriori'):
                os.chdir("/home/khanhduyuser/AprioriJava")
                myPathApriori = myPathApriori+" "+ "Apriori"  + " "+self.e3.get()+" "+self.e1.get();
                subprocess.run([myPathApriori,""],shell=True)

        # AprioriImprove
        if(tkvar.get()=='Apriori Improve'):
                os.chdir("/home/khanhduyuser/AprioriJava")
                myPathApriori = myPathApriori + " "+"AprioriImproved"+" "+self.e3.get()+" "+self.e1.get();
                subprocess.run([myPathApriori,""],shell=True)

        # AprioriMapReduce
        if(tkvar.get()=='Apriori Map Reduce'):
                subprocess.run(["hdfs dfs -put"+" "+self.e3.get()+" "+"/home/khanhduyuser/",""],shell=True)
                if (os.path.exists('/home/khanhduyuser/AprioriJava/outputAprioriMap/part-r-00000')):
                    os.remove("/home/khanhduyuser/AprioriJava/outputAprioriMap/part-r-00000")
                os.chdir("/home/khanhduyuser/HadoopBasedApriori-master")
                subprocess.run(["hdfs dfs -rmr /home/khanhduyuser/output*",""],shell=True)
                linki = self.e3.get()
                tmp = linki.split("/")
                myPathApriori = "hadoop jar HadoopBasedApriori.jar apriori.AprioriDriver /home/khanhduyuser/" + tmp[len(tmp)-1] + " " + "/home/khanhduyuser/output 3" + " " + self.e1.get() + " " + self.e2.get()
                subprocess.run([myPathApriori,""],shell=True)
                subprocess.run(["hadoop fs -copyToLocal /home/khanhduyuser/output1/part-r-00000 /home/khanhduyuser/AprioriJava/outputAprioriMap",""],shell=True)
#main-
root = Tk()
tkvar = StringVar(root)
tkvarmenu = StringVar(root)
choices = {'Apriori','Apriori Improve','Apriori Map Reduce'}
menu = {'Statics','About'}
tkvar.set('Choose')
tkvarmenu.set('Menu')
root.geometry("780x780")
root.configure(bg='#C6E2FF')
root.resizable(0, 0)
my_gui = MyFirstGUI(root)
root.mainloop()
