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

RUN apt-get install zip unzip

#Install npm
RUN apt-get install npm -y

#Install vim
RUN apt-get install vim -y

#React port
#EXPOSE 9000
EXPOSE 8080

RUN useradd -ms /bin/bash ppitu
RUN adduser ppitu sudo

USER ppitu

RUN mkdir -p /home/ppitu/project/backend

WORKDIR /home/ppitu/project/backend
RUN cd /home/ppitu/project/backend

COPY . .

RUN ls -l

#RUN sbt package
#CMD sbt run

RUN sbt dist
RUN cd target/universal/
CMD unzip backend-1.0.zip && cd backend-1.0/bin/ && ./backend -Dplay.evolutions.db.default.autoApply=true

#RUN sbt playGenerateSecret

#CMD sbt run
#ENTRYPOINT sbt run
#CMD sbt "start -Dplay.evolutions.db.default.autoApply=true -Dhttp.port=8080"

#RUN mkdir /home/ppitu/project/backend

#VOLUME /home/ppitu/project/backend

