#!/bin/env bash

# 启动一个JAR并处理重启逻辑
function start_jar {
    jar_name="$1"
    log_name="$2"
    arguments="${@:3}"  # 从第三个参数开始，作为传递给JAR的参数
    
    while true; do
        nohup java -jar -Dzookeeper.addr=10.0.0.201:2181 "$jar_name" $arguments > "$log_name" 2>&1 &
        pid=$!
        
        sleep 5  # 等待一段时间后检查进程是否运行
        if ps -p $pid > /dev/null; then
            echo "$jar_name started successfully. Exiting loop."
            break
        else
            echo "$jar_name failed to start. Retrying..."
        fi
    done
}

cd ../metaServer
mkdir log

# 启动metaServer并将日志保存到不同的文件
start_jar "metaServer-1.0.jar" "./log/metaserver1.log"
start_jar "metaServer-1.0.jar" "./log/metaserver2.log" "--spring.profiles.active=2"

cd ../dataServer
mkdir log

# 启动dataServer并将日志保存到不同的文件
start_jar "dataServer-1.0.jar" "./log/dataserver1.log"
start_jar "dataServer-1.0.jar" "./log/dataserver2.log" "--spring.profiles.active=2"
start_jar "dataServer-1.0.jar" "./log/dataserver3.log" "--spring.profiles.active=3"
start_jar "dataServer-1.0.jar" "./log/dataserver4.log" "--spring.profiles.active=4"
