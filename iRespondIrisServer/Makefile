CC=g++
CFLAGS=-Iinclude -c -Wall -std=c++11
LDFLAGS=-Iinclude -Llib -L/usr/lib64
SOURCES= \
	src/iris.cpp \
	src/iRespondServer.cpp \
	src/PracticalSocket.cpp \
	src/ThreadPool.cpp \
	src/FingerprintDatabase.cpp
LIBS= \
	-lbozorth3 \
	-lnfiq \
	-lmlp \
	-lmindtct \
	-lan2k \
	-limage \
	-lihead \
	-lwsq \
	-ljpegl \
	-lfet \
	-lcblas \
	-lioutil \
	-lutil \
	-lboost_filesystem \
	-lboost_system \
	-pthread
OBJECTS=$(SOURCES:.cpp=.o)
EXECUTABLE=bin/iris

all: $(SOURCES) $(EXECUTABLE)

$(EXECUTABLE): $(OBJECTS)
	$(CC) $(LDFLAGS) $(OBJECTS) $(LIBS) -o $@

.cpp.o:
	$(CC) $(CFLAGS) $< -o $@

clean:
	rm -f $(OBJECTS) $(EXECUTABLE)
