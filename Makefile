PROJECT = meetusvr
COMPOSE_FILE = ./docker-compose-dev.yml


setup_dev:
	asdf install


## DOCKER #####################################################################

compose_up:
	docker-compose -f $(COMPOSE_FILE) -p $(PROJECT) up

compose_down:
	docker-compose -f $(COMPOSE_FILE) -p $(PROJECT) down

docker_rmi_dangling:
	docker rmi -f `docker images -qa --filter "dangling=true"`

db_restore:
	# pv ../backup_db.sql.gz | gunzip | docker exec -i $(PROJECT)_db_1 sh -c 'mysql -uroot -pvendor360 vendor360_dev'
	pv "../nasnav.bak.sql.gz" | gunzip | docker-compose -f $(COMPOSE_FILE) -p $(PROJECT) exec -T postgres sh -c 'psql'
