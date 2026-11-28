GRADLE := ./gradlew
ARGS   ?=

.PHONY: build test run benchmark clean rebuild help

build:
	$(GRADLE) build

test:
	$(GRADLE) test

run:
	$(GRADLE) run --args="$(ARGS)"

benchmark:
	$(GRADLE) benchmark --args="$(ARGS)"

clean:
	$(GRADLE) clean

rebuild: clean build

help:
	@echo "make build                               Сборка и тесты"
	@echo "make test                                Тесты"
	@echo "make run ARGS=\"--mode dynamic ...\"       Генерация PNG"
	@echo "make benchmark ARGS=\"--sizes=1000 ...\"   Замеры, results/benchmarks.csv"
	@echo "make clean                               Очистка build/"
	@echo "make rebuild                             clean + build"