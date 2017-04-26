VERTX_HOME="/home/handy/app/notificator"
VERTXLOG_CONF_DIR="$VERTX_HOME/conf/log/logback.xml"
LOG_DIR="$VERTX_HOME/logs"
VERTX_NOHUP_LOG="$LOG_DIR/sys.log"

VM_ARG="-server"

export JAVA_OPTS="$JAVA_OPTS -Xms1024m"
export JAVA_OPTS="$JAVA_OPTS -Xmx1024m"
export JAVA_OPTS="$JAVA_OPTS -XX:MaxTenuringThreshold=15"
export JAVA_OPTS="$JAVA_OPTS -XX:+UseParallelGC -XX:ParallelGCThreads=2"
export JAVA_OPTS="$JAVA_OPTS -XX:+DisableExplicitGC"
export JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:$LOG_DIR/gc.log"

VM_ARG="$VM_ARG -Dlogback.configurationFile=file:$VERTXLOG_CONF_DIR"
VM_ARG="$VM_ARG -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory"

VM_ARG="$VM_ARG -XX:+HeapDumpOnOutOfMemoryError"
VM_ARG="$VM_ARG -XX:HeapDumpPath=$LOG_DIR/oome.hprof"


RUN_CMD="java $VM_ARG -jar $VERTX_HOME/hs_gms_srv_nti-fat.jar"

vertx_pid(){
	echo `ps -ef | grep vertx | grep -v grep | awk '{print $2}'`
}

start(){
	PID=$(vertx_pid)
 
        if [ -n "$PID" ]; then
		echo "Notificator Server is Already Running!!"
	else
		echo "Notificator Server initializing..."
		nohup sh -c "exec $RUN_CMD >>$VERTX_NOHUP_LOG 2>&1" >/dev/null &
		echo "start done!!"
	fi
	
	return 0
}

stop(){
	PID=$(vertx_pid)

	if [ "$PID" == "" ]; then
        	echo "Notificator Server is Not Running!!"
        	exit 1
	else
        	echo "Shutting down Notificator Server: $PID"
        	kill $PID 2>/dev/null
        	sleep 2
        	kill -9 $PID 2>/dev/null
        	echo "STOPPED `date`" >>$VERTX_NOHUP_LOG
	fi
	
	return 0
}

case $1 in
	start)
		start
		;;
	stop)
		stop
		;;
	restart)
		stop
		start
		;;
	status)
		PID=${vertx_pid}
		if [ -n "$PID" ]; then
           		echo "notificator server is running with pid: $PID"
 	        else
         	        echo "notificator server is not running"
	        fi
        	;;
	*)
		echo $"Usage : $0 {start|stop|restart}"
		exit 1
esac
exit 0
