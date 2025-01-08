DB_CONTAINER_NAME = fs_db
DB_CONTAINER_PORT = 33061

run: db_run sleep build migration start_java

sleep:
	./sleep.sh

start_java:
	java -jar target/AppService.jar

migration:
	bash /opt/maven/bin/mvn flyway:migrate

repair:
	bash /opt/maven/bin/mvn flyway:repair
	bash /opt/maven/bin/mvn flyway:migrate

build:
	bash /opt/maven/bin/mvn clean package

db_restart: db_down db_run

db_run:
	docker run -d \
      --name ${DB_CONTAINER_NAME} \
      -e MYSQL_DATABASE=mydatabase \
      -e MYSQL_USER=myuser \
      -e MYSQL_PASSWORD=secret \
      -e MYSQL_ROOT_PASSWORD=verysecret \
      -p ${DB_CONTAINER_PORT}:3306 \
      -v ./.db:/var/lib/mysql \
      mysql:8.0.33

db_down:
	docker stop ${DB_CONTAINER_NAME}
	docker rm ${DB_CONTAINER_NAME}

unlock:
	sudo chown -R ${USER}:${USER} ./.db
	chmod 775 ./.db

clean:
	bash /opt/maven/bin/mvn clean install