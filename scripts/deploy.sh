#REPOSITORY=/home/ubuntu/app
#cd $REPOSITORY
#
#APP_NAME=mobuza
#JAR_NAME=$(ls $REPOSITORY | grep 'SNAPSHOT.jar' | tail -n 1)
##JAR_NAME=$(ls $REPOSITORY/build/libs/ | grep 'SNAPSHOT.jar' | tail -n 1)
#JAR_PATH=$REPOSITORY/$JAR_NAME
#
#CURRENT_PID=$(pgrep -f $APP_NAME)
#
#if [ -z $CURRENT_PID ]
#then
#  echo "> 종료할것 없음."
#else
#  echo "> kill -9 $CURRENT_PID"
#  kill -15 $CURRENT_PID
#  sleep 5
#fi
#
#echo "> $JAR_PATH 배포"
#nohup java -jar $JAR_PATH > /dev/null 2> /dev/null < /dev/null &




#!/bin/bash

REPOSITORY=/home/ubuntu/app
PROJECT_NAME=mobuza

echo "> Build 파일 복사"

cp ./build/libs/*.jar $REPOSITORY/

echo "> 현재 구동중인 애플리케이션 pid 확인"

CURRENT_PID=$(pgrep -fl $PROJECT_NAME | grep java | awk '{print $1}')

echo "$CURRENT_PID"

if [ -z $CURRENT_PID ]; then
    echo "> 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다."
else
    echo "> kill -15 $CURRENT_PID"
    kill -15 $CURRENT_PID
    sleep 5
fi

echo "> 새 어플리케이션 배포"
JAR_NAME=$(ls -tr $REPOSITORY/*.jar | tail -n 1)

echo "> JAR Name: $JAR_NAME"

echo "> $JAR_NAME에 실행권한 추가"
chmod +x $JAR_NAME

echo "> $JAR_NAME 실행"
nohup java -jar $JAR_NAME > $REPOSITORY/nohup.out 2>&1 &