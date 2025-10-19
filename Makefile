AUTH_JWT_SECRET?=dev-secret-0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCD
    SPRING_PROFILES_ACTIVE?=dev-test

    check:
	mvn -T1C -DskipITs=false -DskipTests=false -Dstyle.skip=false verify

    test:
	mvn -DskipITs=false test

    run-eureka:
	mvn -pl :discovery-eureka spring-boot:run

    run-hms:
	mvn -pl :hotel-service spring-boot:run

    run-bs:
	mvn -pl :booking-service spring-boot:run

    run-gw:
	mvn -pl :gateway spring-boot:run

    up: run-eureka run-hms run-bs run-gw

    clean:
	mvn clean
