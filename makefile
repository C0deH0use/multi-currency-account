.PHONY: build run run-dependencies run-db destroy

build:
	docker compose build api

run: build
	docker compose up

run-db:
	docker compose up db --force-recreate --renew-anon-volumes

destroy:
	docker compose down -v
	docker container prune --force
