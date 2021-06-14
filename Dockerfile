FROM ubuntu:20.04
LABEL maintainer="ppitu"

#Install Java 1.8
RUN apt-get update
RUN apt-get install openjdk-8-jdk -y
RUN apt-get install scala -y

#Install wget
RUN apt-get install wget -y

#Install crul
RUN apt-get install curl -y

#Install gnupg2
RUN apt-get install gnupg2 -y

#Install scala-2.12.3
RUN apt-get remove scala-library scala -y
RUN wget https://downloads.lightbend.com/scala/2.12.3/scala-2.12.3.deb
RUN dpkg -i scala-2.12.3.deb
RUN rm -r scala-2.12.3.deb

#Install sbt
RUN echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list
RUN curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add
RUN apt-get update -y 
RUN apt-get install sbt -y

#Install npm
RUN apt-get install npm -y

#Install vim
RUN apt-get install vim -y

#React port
EXPOSE 3000

#Play port
EXPOSE 3001

RUN useradd -ms /bin/bash ppitu
RUN adduser ppitu sudo

USER ppitu
WORKDIR /home/ppitu/project/backend

RUN mkdir /home/ppitu/project/backend

VOLUME /home/ppitu/project/backend
