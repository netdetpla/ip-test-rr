FROM openjdk:11.0.5-jre-stretch

ADD ["build/libs/ip-test-rr-1-all.jar", "settings.properties", "/"]

CMD java -jar ip-test-rr-1-all.jar