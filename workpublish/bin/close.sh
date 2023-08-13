#!/bin/bash

# 要关闭的端口列表
ports=(8000 8001 9001 9002 9003 9004)

for port in "${ports[@]}"
do
    # 获取进程PID并关闭进程
    pid=$(netstat -tunlp | grep ":$port " | awk '{print $7}' | awk -F\/ '{print $1}')
    
    if [ -n "$pid" ]; then
        echo "Closing process on port $port (PID: $pid)"
        # 终止进程
        kill -9 "$pid"
    else
        echo "No process found on port $port"
    fi
done