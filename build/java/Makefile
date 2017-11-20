# Based on https://www.cs.swarthmore.edu/~newhall/unixhelp/javamakefiles.html
# Modfied by Stephan Kostreski for UMD CMSC414 Fall2017 use 

# You should only need to edit the block below .
# 	JFLAGS, LIBSCLASSPATH need to know about libraries you intend to use
# 	It will make the libraries available to both logappend and logread.
###############################################################################
# Library setup
JFLAGS = -g -classpath .:/java/[MyLibraryNameGoesHere1].jar:/java/[MyLibraryNameGoesHere2].jar
LIBSCLASSPATH = /java/[MyLibraryNameGoesHere1].jar /java/[MyLibraryNameGoesHere2].jar

# Dependencies of the Logappend program.
LOGAPPENDCLASSES = \
	src/LogAppend/Main.java

# Dependencies of the Logread program.
LOGREADCLASSES = \
	src/LogRead/Main.java 
###############################################################################

##############################################
# You should not need to edit anything below..
##############################################
JC = javac
JAR=jar cvfm
LOGREADBINDIR=logreadbin
LOGAPPENDBINDIR=logappendbin
MANIFESTDIR=META-INF
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

# WILL CONSTRUCT:
# BOTH jar programs
# setup the logappend and logread execution scripts in the root folder.
all: logreadprogram logappendprogram setfolder

# Construct the jar files
logreadprogram: $(LOGREADCLASSES)
	mkdir -p $(LOGREADBINDIR)
	mkdir -p $(LOGREADBINDIR)/$(MANIFESTDIR)
	$(JC) $(JFLAGS) $(LOGREADCLASSES) -d $(LOGREADBINDIR)
	find $(LOGREADBINDIR) -name "*.class" | sed -e "s,^$(LOGREADBINDIR)/,," > $(LOGREADBINDIR)/atmsources.txt
	echo "Class-Path: logread $(LIBSCLASSPATH)" > $(LOGREADBINDIR)/$(MANIFESTDIR)/ATMMANIFEST.MF
	echo "Main-Class: src.LogRead.Main" >> $(LOGREADBINDIR)/$(MANIFESTDIR)/ATMMANIFEST.MF
	cd $(LOGREADBINDIR); $(JAR) logread.jar $(MANIFESTDIR)/ATMMANIFEST.MF @atmsources.txt
	cd $(LOGREADBINDIR); chmod +x logread.jar

logappendprogram: $(LOGAPPENDCLASSES )
	mkdir -p $(LOGAPPENDBINDIR)
	mkdir -p $(LOGAPPENDBINDIR)/$(MANIFESTDIR)
	$(JC) $(JFLAGS) $(LOGAPPENDCLASSES) -d $(LOGAPPENDBINDIR)
	find $(LOGAPPENDBINDIR) -name "*.class" | sed -e "s,^$(LOGAPPENDBINDIR)/,," > $(LOGAPPENDBINDIR)/banksources.txt
	echo "Class-Path: logappend $(LIBSCLASSPATH)" > $(LOGAPPENDBINDIR)/$(MANIFESTDIR)/BANKMANIFEST.MF
	echo "Main-Class: src.LogAppend.Main" >> $(LOGAPPENDBINDIR)/$(MANIFESTDIR)/BANKMANIFEST.MF
	cd $(LOGAPPENDBINDIR); $(JAR) logappend.jar $(MANIFESTDIR)/BANKMANIFEST.MF @banksources.txt
	cd $(LOGAPPENDBINDIR); chmod +x logappend.jar

# Sets up shell script files with current DIR
setfolder:
	echo "#!/bin/bash" > logappend
	echo "#!/bin/bash" > logread
	echo "DIR=\"$(shell pwd)\"" >> logappend
	echo "DIR=\"$(shell pwd)\"" >> logread
	cat logappend.pre >> logappend
	cat logread.pre >> logread
	chmod +x logappend
	chmod +x logread

clean:
	rm -r $(LOGREADBINDIR)/ $(LOGAPPENDBINDIR)/
