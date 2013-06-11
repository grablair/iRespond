CC=g++
CFLAGS=-Iinclude -c -Wall -std=c++11
LDFLAGS=-Iinclude -Llib -L/usr/lib64
SOURCES= \
	src/irespond.cpp \
	src/iRespondServer.cpp \
	src/PracticalSocket.cpp \
	src/ThreadPool.cpp \
	src/FingerprintDatabase.cpp \
	src/Utilities.cpp
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
OUTPUT_FOLDER=bin
EXECUTABLE=irespond

all: $(SOURCES) $(EXECUTABLE) test_send

$(EXECUTABLE): $(OBJECTS)
	$(CC) $(LDFLAGS) $(OBJECTS) $(LIBS) -o $(OUTPUT_FOLDER)/$@

test_send:
	$(CC) -std=c++11 src/test_send.cpp src/PracticalSocket.cpp -o $(OUTPUT_FOLDER)/test_send

.cpp.o:
	$(CC) $(CFLAGS) $< -o $@

clean:
	rm -f $(OBJECTS) $(EXECUTABLE)
