#!/bin/bash

cd `dirname $0`

export JAVA_HOME=/usr/lib/jvm/jre-11
export PATH=$JAVA_HOME/bin:$PATH
JAVA=java
OPTS="-Xmx128M"

PORT=8082
APPNAME=${project.build.finalName}
JARFILE=${project.build.finalName}.jar

statusme() {
	pgrep -f $APPNAME
	case "$?" in
		0)	echo "active" ; exit $? ;;
		1)	echo "inactive" ; exit $? ;;
		*)	echo "error" ; exit $? ;;
	esac
}

isStopped() {
	pgrep -f $APPNAME
	case "$?" in
		0)	echo "active" ; exit 1 ;;
		1)	echo "inactive" ; exit 0 ;;
		*)	echo "error" ; exit $? ;;
	esac
}

startme() {
	echo "Starting $APPNAME"
	nohup $JAVA $OPTS -jar $JARFILE $PORT >/dev/null 2>&1 &
    sleep 1
	statusme
}

debugme() {
	echo "Starting DEBUG $APPNAME"
	$JAVA $OPTS -jar $JARFILE $PORT
}

stopme() {
	echo "Stopping $APPNAME..."
    pkill -f "java .* -jar $APPNAME.* $PORT"
    sleep 2
	isStopped
}

case "$1" in 
    start)   startme ;;
    stop)    stopme ;;
    restart) stopme
             startme ;;
    status)  statusme ;;
    debug)  debugme ;;
    *) echo "usage: $0 status|start|stop|restart|debug" >&2
       exit 1
       ;;
esac
